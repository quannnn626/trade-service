package com.boot.pay.refund.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * 退款订单列表项 VO（运营后台用）
 *
 * @author quannnn
 */
@Data
@Builder
public class RefundListVO {

    /** 退款单号 */
    private String refundNo;

    /** 支付单号 */
    private String paymentNo;

    /** 商户退款单号 */
    private String merchantRefundNo;

    /** 商户编号（关联 pay_merchant 回填） */
    private String merchantNo;

    /** 商户名称（关联 pay_merchant 回填） */
    private String merchantName;

    /** 申请退款金额 */
    private BigDecimal applyAmount;

    /** 实际退款金额 */
    private BigDecimal actualAmount;

    /** 退还手续费 */
    private BigDecimal feeRefund;

    /** 退款类型 code（1-全额 2-部分） */
    private Integer refundType;

    /** 退款类型名称 */
    private String refundTypeName;

    /** 退款状态 code */
    private Integer status;

    /** 退款状态名称 */
    private String statusName;

    /** 审核状态 code（0-待审核 1-通过 2-驳回） */
    private Integer auditStatus;

    /** 审核状态名称 */
    private String auditStatusName;

    /** 退款原因 */
    private String refundReason;

    /** 完成时间 */
    private String finishTime;

    /** 创建时间 */
    private String createTime;
}
