package com.boot.pay.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付订单请求参数
 *
 * @author quannnn
 */
@Data
public class CreatePayDTO {

    /** 商户订单号（幂等去重用） */
    @NotBlank(message = "商户订单号不能为空")
    private String orderNo;

    /** 支付金额 */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    /** 商品标题 */
    @NotBlank(message = "商品标题不能为空")
    private String subject;

    /** 商品描述 */
    private String description;

    /** 支付渠道编码（BALANCE / ALIPAY / WECHAT） */
    @NotBlank(message = "支付渠道不能为空")
    private String channelCode;

    /** 订单级别回调地址（不填则用商户默认回调地址） */
    private String notifyUrl;

    /** 支付完成跳转地址 */
    private String returnUrl;

    /** 附加数据，原样透传 */
    private String attach;

    /** 订单过期分钟数，默认 30 分钟 */
    private Integer expireMinutes;
}
