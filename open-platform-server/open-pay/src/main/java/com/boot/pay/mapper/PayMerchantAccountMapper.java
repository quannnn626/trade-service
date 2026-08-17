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
}




