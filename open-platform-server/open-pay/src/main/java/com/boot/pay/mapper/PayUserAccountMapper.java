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
}




