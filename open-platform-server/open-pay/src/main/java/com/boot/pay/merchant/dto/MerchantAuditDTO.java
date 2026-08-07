package com.boot.pay.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商户审核请求
 */
@Data
public class MerchantAuditDTO {

    /** 商户编号 */
    @NotBlank(message = "商户编号不能为空")
    private String merchantNo;

    /** 审核结果 1-通过 2-驳回 */
    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    /** 审核备注（驳回时必填原因） */
    private String auditRemark;
}
