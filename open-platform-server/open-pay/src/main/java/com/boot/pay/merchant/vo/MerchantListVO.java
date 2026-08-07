package com.boot.pay.merchant.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantListVO {

    /** 商户编号 */
    private String merchantNo;

    /** 商户名称 */
    private String merchantName;

    /** 企业全称 */
    private String companyName;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 商户类型 */
    private Integer merchantType;

    /** 结算费率 */
    private BigDecimal settleFeeRate;

    /** 状态 0禁用 1启用 */
    private Integer status;

    /** 审核状态 0待审 1通过 2驳回 */
    private Integer auditStatus;

    /** 创建时间 */
    private String createTime;
}
