package com.boot.pay.payment.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付订单查询结果
 *
 * @author quannnn
 */
@Data
@Builder
public class PayOrderVO {

    private String paymentNo;

    private String orderNo;

    private BigDecimal amount;

    private BigDecimal feeAmount;

    private BigDecimal settleAmount;

    private Integer status;

    private String statusDesc;

    private String subject;

    private String description;

    private String channelCode;

    private String channelName;

    private String clientIp;

    private String notifyUrl;

    private String attach;

    private Date expireTime;

    private Date payTime;

    private Date createTime;
}
