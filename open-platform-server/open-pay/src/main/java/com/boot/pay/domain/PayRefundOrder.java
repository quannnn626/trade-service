package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 退款订单表
 * @TableName pay_refund_order
 */
@TableName(value ="pay_refund_order")
@Data
public class PayRefundOrder {
    /**
     * 退款ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 商户退款单号（幂等用）
     */
    private String merchantRefundNo;

    /**
     * 退款类型 1-全额 2-部分
     */
    private Integer refundType;

    /**
     * 申请退款金额
     */
    private BigDecimal applyAmount;

    /**
     * 实际退款金额
     */
    private BigDecimal actualAmount;

    /**
     * 退还手续费
     */
    private BigDecimal feeRefund;

    /**
     * 退款渠道 1-原路退回
     */
    private Integer refundChannel;

    /**
     * 申请时间
     */
    private Date applyTime;

    /**
     * 审核状态 0-待审核 1-通过 2-驳回
     */
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 退款失败原因
     */
    private String failReason;

    /**
     * 退款回调地址
     */
    private String notifyUrl;

}
