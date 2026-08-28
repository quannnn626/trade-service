package com.boot.pay.refund.vo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * 退款订单详情 VO（运营后台用）
 * <p>
 * 含退款单全量信息 + 商户/用户/审核人回填 + 内嵌关联支付订单关键信息；
 * 关联流水由前端复用 flow 查询接口按账户/支付单号获取，不在此返回。
 *
 * @author quannnn
 */
@Data
@Builder
public class RefundDetailVO {

    /** 退款单号 */
    private String refundNo;

    /** 支付单号 */
    private String paymentNo;

    /** 商户退款单号 */
    private String merchantRefundNo;

    /** 商户编号（关联回填） */
    private String merchantNo;

    /** 商户名称（关联回填） */
    private String merchantName;

    /** 用户ID */
    private Long userId;

    /** 用户名称（关联回填） */
    private String userName;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 退款类型 code（1-全额 2-部分） */
    private Integer refundType;

    /** 退款类型名称 */
    private String refundTypeName;

    /** 申请退款金额 */
    private BigDecimal applyAmount;

    /** 实际退款金额 */
    private BigDecimal actualAmount;

    /** 退还手续费 */
    private BigDecimal feeRefund;

    /** 退款渠道（1-原路退回） */
    private Integer refundChannel;

    /** 退款状态 code */
    private Integer status;

    /** 退款状态名称 */
    private String statusName;

    /** 审核状态 code（0-待审核 1-通过 2-驳回） */
    private Integer auditStatus;

    /** 审核状态名称 */
    private String auditStatusName;

    /** 审核人ID */
    private Long auditorId;

    /** 审核人名称（关联回填） */
    private String auditorName;

    /** 审核时间 */
    private Date auditTime;

    /** 退款失败原因 */
    private String failReason;

    /** 退款回调地址 */
    private String notifyUrl;

    /** 申请时间 */
    private Date applyTime;

    /** 完成时间 */
    private Date finishTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 关联支付订单信息 */
    private PayOrderRef order;

    /**
     * 关联支付订单关键信息
     */
    @Data
    @Builder
    public static class PayOrderRef {
        /** 商户订单号 */
        private String orderNo;

        /** 商品标题 */
        private String subject;

        /** 订单金额 */
        private BigDecimal amount;

        /** 订单状态 code */
        private Integer status;

        /** 订单状态名称 */
        private String statusName;
    }
}
