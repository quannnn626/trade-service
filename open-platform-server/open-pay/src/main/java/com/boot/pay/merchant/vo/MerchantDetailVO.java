package com.boot.pay.merchant.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户详情（含资金账户信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDetailVO {

    /** === 基本信息 === */

    private String merchantNo;
    private String merchantName;
    private String companyName;
    private String businessLicense;
    private Integer merchantType;
    private String appKey;
    private Integer status;
    private Integer auditStatus;
    private String auditRemark;

    /** === 联系人 === */

    private String contactName;
    private String contactPhone;
    private String contactEmail;

    /** === 结算配置 === */

    private Integer settleType;
    private BigDecimal settleFeeRate;

    /** === 风控配置 === */

    private BigDecimal dailyLimit;
    private BigDecimal singleLimit;
    private String whiteIpList;
    private String notifyUrl;

    /** === 账户信息 === */

    private String accountNo;
    private BigDecimal balance;
    private BigDecimal frozenAmount;

    /** === 时间 === */

    private String createTime;
    private String updateTime;

    /** === 其他 === */

    private String remark;
    private Integer secretVersion;
}
