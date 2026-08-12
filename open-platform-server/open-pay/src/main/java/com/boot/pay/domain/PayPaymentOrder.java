package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 支付订单表
 * @TableName pay_payment_order
 */
@TableName(value ="pay_payment_order")
@Data
public class PayPaymentOrder {
    /**
     * 支付订单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 付款用户
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态
     */
    private Integer status;

    /**
     * 支付过期时间
     */
    private Date expireTime;

    /**
     * 支付完成时间
     */
    private Date payTime;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 商城支付单号
     */
    private String merchantPaymentNo;

    /**
     * 支付渠道ID
     */
    private Long channelId;

    /**
     * 客户端/服务器IP
     */
    private String clientIp;

    /**
     * 商品标题（展示用）
     */
    private String subject;

    /**
     * 订单描述
     */
    private String description;

    /**
     * 订单级别回调地址（覆盖商户默认回调地址）
     */
    private String notifyUrl;

    /**
     * 支付完成跳转地址
     */
    private String returnUrl;

    /**
     * 附加数据（透传，原样返回给商户）
     */
    private String attach;

    /**
     * 订单超时自动关闭时间
     */
    private Date timeoutExpire;

    /**
     * 关单时间
     */
    private Date closeTime;

    /**
     * 关单原因
     */
    private String closeReason;

    /**
     * 手续费金额
     */
    private BigDecimal feeAmount;

    /**
     * 结算金额（amount - fee_amount）
     */
    private BigDecimal settleAmount;

    /**
     * 结算状态 0-未结算 1-已结算
     */
    private Integer settleStatus;

    /**
     * 结算时间
     */
    private Date settleTime;

    /**
     * 订单签名（防篡改）
     */
    private String sign;

}