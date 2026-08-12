package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 交易平台商户表
 * @TableName pay_merchant
 */
@TableName(value ="pay_merchant")
@Data
public class PayMerchant {
    /**
     * 商户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商户编号
     */
    private String merchantNo;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 商户类型
     */
    private Integer merchantType;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 默认回调通知地址
     */
    private String notifyUrl;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 企业全称
     */
    private String companyName;

    /**
     * 营业执照号
     */
    private String businessLicense;

    /**
     * 结算方式 1-T+1 2-T+0 3-周结 4-月结
     */
    private Integer settleType;

    /**
     * 结算费率（如0.0060=0.6%）
     */
    private BigDecimal settleFeeRate;

    /**
     * 授权过期时间
     */
    private Date expireTime;

    /**
     * 密钥版本号
     */
    private Integer secretVersion;

    /**
     * IP白名单（JSON数组）
     */
    private String whiteIpList;

    /**
     * 单日交易限额
     */
    private BigDecimal dailyLimit;

    /**
     * 单笔交易限额
     */
    private BigDecimal singleLimit;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审核状态 0-待审核 1-已通过 2-已驳回
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;
}
