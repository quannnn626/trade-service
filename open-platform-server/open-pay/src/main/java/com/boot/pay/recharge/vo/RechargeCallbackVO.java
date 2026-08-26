package com.boot.pay.recharge.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值到账返回
 *
 * @author quannnn
 */
@Data
@Builder
public class RechargeCallbackVO {

    /** 充值单号 */
    private String rechargeNo;

    /** 实际到账金额 */
    private BigDecimal arrivalAmount;
}
