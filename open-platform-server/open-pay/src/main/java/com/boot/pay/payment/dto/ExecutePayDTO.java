package com.boot.pay.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 执行支付请求参数
 *
 * @author quannnn
 */
@Data
public class ExecutePayDTO {

    /** 平台支付流水号 */
    @NotBlank(message = "支付流水号不能为空")
    private String paymentNo;

    /** 付款用户 ID（平台用户） */
    @NotNull(message = "付款用户不能为空")
    private Long userId;

    /** 支付密码（余额支付时需要） */
    @NotBlank(message = "支付密码不能为空")
    private String payPassword;
}
