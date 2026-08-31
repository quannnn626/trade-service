package com.boot.pay.mapper;

import com.boot.pay.domain.PayMerchantAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
* @author quannnn
* @description 针对表【pay_merchant_account(商户资金账户表)】的数据库操作Mapper
* @createDate 2026-08-03 12:26:43
* @Entity com.boot.pay.domain.PayMerchantAccount
*/
public interface PayMerchantAccountMapper extends BaseMapper<PayMerchantAccount> {

    /**
     * 乐观锁增加余额（支付入账，按结算金额到账）
     * <p>
     * 同时累计 total_income。
     *
     * @param merchantId 商户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     入账金额（结算金额）
     * @return 受影响行数，0 表示版本冲突
     */
    int addBalance(@Param("merchantId") Long merchantId,
                   @Param("oldVersion") Integer oldVersion,
                   @Param("amount") BigDecimal amount);

    /**
     * 乐观锁扣减余额（退款扣款，按退款净额出账）
     * <p>
     * version 条件保证并发安全，balance >= amount 二次兜底防止余额变负。
     * 同时累计 total_expense。
     *
     * @param merchantId 商户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     扣减金额（退款金额 - 退还手续费）
     * @return 受影响行数，0 表示余额不足或版本冲突
     */
    int deductBalance(@Param("merchantId") Long merchantId,
                      @Param("oldVersion") Integer oldVersion,
                      @Param("amount") BigDecimal amount);

    /**
     * 乐观锁调整余额（超管人工调账）
     * <p>
     * amount 正数为加款、负数为扣款；balance + amount >= 0 兜底防止余额变负。
     * 不累计 total_income/total_expense（调账为人工修正，不影响业务统计口径）。
     *
     * @param merchantId 商户 ID
     * @param oldVersion 读取时的版本号
     * @param amount     调账金额（可正可负）
     * @return 受影响行数，0 表示余额不足或版本冲突
     */
    int adjustBalance(@Param("merchantId") Long merchantId,
                      @Param("oldVersion") Integer oldVersion,
                      @Param("amount") BigDecimal amount);
}




