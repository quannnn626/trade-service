package com.boot.pay.refund.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款结果返回
 *
 * @author quannnn
 */
@Data
@Builder
public class RefundVO {

    /** 平台退款单号 */
    private String refundNo;

    /** 支付单号 */
    private String paymentNo;

    /** 实际退款金额 */
    private BigDecimal refundAmount;

    /** 退还手续费 */
    private BigDecimal feeRefund;

    /** 退款单状态 */
    private Integer status;

    /** 退款单状态描述 */
    private String statusDesc;

    /** 审核状态 0-待审核 1-通过 2-驳回 */
    private Integer auditStatus;

    /** 完成时间 */
    private Date finishTime;
}
