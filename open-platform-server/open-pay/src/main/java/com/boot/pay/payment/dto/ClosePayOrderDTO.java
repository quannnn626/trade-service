package com.boot.pay.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手动关单请求参数（平台内部，运营人员操作）
 *
 * @author quannnn
 */
@Data
public class ClosePayOrderDTO {

    /** 支付单号 */
    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;

    /** 关单原因（可选，为空默认"手动关闭"） */
    @Size(max = 255, message = "关单原因不能超过255个字符")
    private String closeReason;
}
