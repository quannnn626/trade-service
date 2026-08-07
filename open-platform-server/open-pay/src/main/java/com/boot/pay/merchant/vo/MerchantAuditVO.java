package com.boot.pay.merchant.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户审核返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAuditVO {

    /** 商户编号 */
    private String merchantNo;

    /** 审核结果 */
    private Integer auditStatus;

    /** 商户启用状态 */
    private Integer status;

    /** 资金账户编号（审核通过时创建） */
    private String accountNo;

    /** 审核备注 */
    private String auditRemark;
}
