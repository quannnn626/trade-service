package com.boot.pay.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退款审核请求参数（平台内部，管理员操作）
 *
 * @author quannnn
 */
@Data
public class AuditRefundDTO {

    /** 退款单号 */
    @NotBlank(message = "退款单号不能为空")
    private String refundNo;

    /** 审核结果 1-通过 2-驳回 */
    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;

    /** 审核备注（驳回时必填，作为退款失败原因） */
    private String auditRemark;
}
