package com.boot.pay.payment.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * 支付订单列表项 VO（运营后台用）
 *
 * @author quannnn
 */
@Data
@Builder
public class PayOrderListVO {

    /** 支付单号 */
    private String paymentNo;

    /** 商户订单号 */
    private String orderNo;

    /** 商户编号（关联 pay_merchant 回填） */
    private String merchantNo;

    /** 商户名称（关联 pay_merchant 回填） */
    private String merchantName;

    /** 商品标题 */
    private String subject;

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

    /** 支付完成时间 */
    private String payTime;

    /** 创建时间 */
    private String createTime;
}
