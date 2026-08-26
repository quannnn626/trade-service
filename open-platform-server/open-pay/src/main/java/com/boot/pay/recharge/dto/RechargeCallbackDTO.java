package com.boot.pay.recharge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 充值到账通知参数（模拟银行回调）
 *
 * @author quannnn
 */
@Data
public class RechargeCallbackDTO {

    /** 充值单号 */
    @NotBlank(message = "充值单号不能为空")
    private String rechargeNo;

    /** 充值状态（模拟阶段仅支持 SUCCESS） */
    @NotBlank(message = "充值状态不能为空")
    private String payStatus;
}
