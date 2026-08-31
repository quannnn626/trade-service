package com.boot.pay.mapper;

import com.boot.pay.domain.PayUserAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
* @author quannnn
* @description 针对表【pay_user_account(用户钱包账户表)】的数据库操作Mapper
* @createDate 2026-08-03 12:26:43
* @Entity com.boot.pay.domain.PayUserAccount
*/
public interface PayUserAccountMapper extends BaseMapper<PayUserAccount> {

    /**
     * 乐观锁扣减余额（支付扣款）
     * <p>
     * version 条件保证并发安全，balance >= amount 二次兜底防止余额变负。
     * 同时累计 total_expense 与当日支付金额 daily_used。
     *
     * @param userId     用户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     扣减金额
     * @return 受影响行数，0 表示余额不足或版本冲突
     */
    int deductBalance(@Param("userId") Long userId,
                      @Param("oldVersion") Integer oldVersion,
                      @Param("amount") BigDecimal amount);

    /**
     * 乐观锁增加余额（退款入账）
     * <p>
     * 同时累计 total_income。
     *
     * @param userId     用户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     入账金额（退款金额）
     * @return 受影响行数，0 表示版本冲突
     */
    int addBalance(@Param("userId") Long userId,
                   @Param("oldVersion") Integer oldVersion,
                   @Param("amount") BigDecimal amount);

    /**
     * 乐观锁增加冻结金额（账户资金冻结）
     * <p>
     * version 条件保证并发安全，frozen_amount 只增不减，可用余额不变。
     *
     * @param userId     用户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     冻结金额
     * @return 受影响行数，0 表示版本冲突
     */
    int addFrozenAmount(@Param("userId") Long userId,
                        @Param("oldVersion") Integer oldVersion,
                        @Param("amount") BigDecimal amount);

    /**
     * 乐观锁减少冻结金额（账户资金解冻）
     * <p>
     * version 条件保证并发安全，冻结金额大于等于解冻金额的校验在 Service 层完成。
     *
     * @param userId     用户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     解冻金额
     * @return 受影响行数，0 表示版本冲突
     */
    int subtractFrozenAmount(@Param("userId") Long userId,
                             @Param("oldVersion") Integer oldVersion,
                             @Param("amount") BigDecimal amount);

    /**
     * 乐观锁调整余额（超管人工调账）
     * <p>
     * amount 正数为加款、负数为扣款；balance + amount >= 0 兜底防止余额变负。
     * 不累计 total_income/total_expense（调账为人工修正，不影响业务统计口径）。
     *
     * @param userId     用户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     调账金额（可正可负）
     * @return 受影响行数，0 表示余额不足或版本冲突
     */
    int adjustBalance(@Param("userId") Long userId,
                      @Param("oldVersion") Integer oldVersion,
                      @Param("amount") BigDecimal amount);
}




