package com.boot.pay.payment.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 执行支付返回结果
 *
 * @author quannnn
 */
@Data
@Builder
public class ExecutePayVO {

    /** 平台支付流水号 */
    private String paymentNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 订单状态 */
    private Integer status;

    /** 订单状态描述 */
    private String statusDesc;

    /** 支付完成时间 */
    private Date payTime;
}
