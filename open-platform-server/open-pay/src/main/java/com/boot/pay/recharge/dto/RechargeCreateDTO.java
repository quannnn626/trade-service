package com.boot.pay.recharge.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建充值单请求参数
 *
 * @author quannnn
 */
@Data
public class RechargeCreateDTO {

    /** 充值金额 */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    /** 银行名称（银行卡充值时） */
    private String bankName;

    /** 银行卡尾号 */
    private String cardNoTail;
}
