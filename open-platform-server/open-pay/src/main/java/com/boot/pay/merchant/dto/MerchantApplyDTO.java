package com.boot.pay.merchant.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 商户入驻申请请求
 */
@Data
public class MerchantApplyDTO {

    /** 商户名称 */
    @NotBlank(message = "商户名称不能为空")
    private String merchantName;

    /** 企业全称 */
    @NotBlank(message = "企业全称不能为空")
    private String companyName;

    /** 营业执照号 */
    @NotBlank(message = "营业执照号不能为空")
    private String businessLicense;

    /** 联系人姓名 */
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    /** 联系人电话 */
    @NotBlank(message = "联系人电话不能为空")
    private String contactPhone;

    /** 联系人邮箱 */
    @NotBlank(message = "联系人邮箱不能为空")
    private String contactEmail;

    /** 结算方式 1-T+1 2-T+0 3-周结 4-月结 */
    private Integer settleType;

    /** 结算费率（如0.0060=0.6%），范围 0.001~0.05 */
    @NotNull(message = "结算费率不能为空")
    @DecimalMin(value = "0.001", message = "费率最低0.1%")
    @DecimalMax(value = "0.05", message = "费率最高5%")
    private BigDecimal settleFeeRate;

    /** 单日交易限额 */
    @NotNull(message = "单日交易限额不能为空")
    @DecimalMin(value = "0.01", message = "单日限额必须大于0")
    private BigDecimal dailyLimit;

    /** 单笔交易限额 */
    @NotNull(message = "单笔交易限额不能为空")
    @DecimalMin(value = "0.01", message = "单笔限额必须大于0")
    private BigDecimal singleLimit;

    /** IP白名单（JSON数组字符串，如 ["192.168.1.1"]） */
    private String whiteIpList;

    /** 默认回调通知地址 */
    private String notifyUrl;

    /** 商户类型 */
    private Integer merchantType;

    /** 备注 */
    private String remark;
}
