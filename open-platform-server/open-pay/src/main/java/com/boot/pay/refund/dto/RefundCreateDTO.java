package com.boot.pay.refund.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发起退款请求参数
 *
 * @author quannnn
 */
@Data
public class RefundCreateDTO {

    /** 支付单号 */
    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;

    /** 商户退款单号（幂等去重用） */
    @NotBlank(message = "商户退款单号不能为空")
    private String merchantRefundNo;

    /** 退款金额 */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 退款类型 1-全额 2-部分 */
    @NotNull(message = "退款类型不能为空")
    private Integer refundType;
}
