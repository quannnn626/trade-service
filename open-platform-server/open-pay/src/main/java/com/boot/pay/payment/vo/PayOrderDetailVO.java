package com.boot.pay.payment.vo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * 支付订单详情 VO（运营后台用）
 *
 * @author quannnn
 */
@Data
@Builder
public class PayOrderDetailVO {

    /** 支付单号 */
    private String paymentNo;

    /** 商户订单号 */
    private String orderNo;

    /** 商城支付单号 */
    private String merchantPaymentNo;

    /** 商户编号（关联回填） */
    private String merchantNo;

    /** 商户名称（关联回填） */
    private String merchantName;

    /** 付款用户ID（未支付前为空） */
    private Long userId;

    /** 用户编号（关联回填） */
    private String userNo;

    /** 用户名（关联回填） */
    private String userName;

    /** 用户手机号（关联回填） */
    private String userPhone;

    /** 支付渠道编码 */
    private String channelCode;

    /** 支付渠道名称 */
    private String channelName;

    /** 商品标题 */
    private String subject;

    /** 订单描述 */
    private String description;

    /** 支付金额 */
    private BigDecimal amount;

    /** 手续费金额 */
    private BigDecimal feeAmount;

    /** 结算金额（amount - feeAmount） */
    private BigDecimal settleAmount;

    /** 支付状态 code */
    private Integer status;

    /** 支付状态名称 */
    private String statusName;

    /** 客户端/服务器IP */
    private String clientIp;

    /** 订单级回调地址 */
    private String notifyUrl;

    /** 支付完成跳转地址 */
    private String returnUrl;

    /** 附加数据（透传） */
    private String attach;

    /** 支付过期时间 */
    private Date expireTime;

    /** 订单超时自动关闭时间 */
    private Date timeoutExpire;

    /** 支付完成时间 */
    private Date payTime;

    /** 关单时间 */
    private Date closeTime;

    /** 关单原因 */
    private String closeReason;

    /** 结算状态 0-未结算 1-已结算 */
    private Integer settleStatus;

    /** 结算时间 */
    private Date settleTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
