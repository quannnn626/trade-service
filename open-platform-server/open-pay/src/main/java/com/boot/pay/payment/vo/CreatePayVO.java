package com.boot.pay.payment.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 创建支付订单返回结果
 *
 * @author quannnn
 */
@Data
@Builder
public class CreatePayVO {

    /** 平台支付流水号 */
    private String paymentNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 订单状态 */
    private Integer status;

    /** 订单状态描述 */
    private String statusDesc;

    /** 订单过期时间 */
    private Date expireTime;
}
