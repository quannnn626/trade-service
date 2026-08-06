package com.boot.pay.merchant.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户入驻申请返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantApplyVO {

    /** 商户编号 */
    private String merchantNo;

    /** 商户名称 */
    private String merchantName;

    /** 应用Key */
    private String appKey;

    /** 应用密钥（仅首次返回明文，请妥善保管） */
    private String appSecret;

    /** 审核状态 */
    private Integer auditStatus;

    /** 结算费率 */
    private BigDecimal settleFeeRate;

    /** 提示信息 */
    private String tip;
}
