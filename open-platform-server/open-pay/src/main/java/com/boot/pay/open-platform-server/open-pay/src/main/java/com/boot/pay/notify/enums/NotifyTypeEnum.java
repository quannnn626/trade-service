package com.boot.pay.notify.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
* @author quannnn
* @description 支付回调通知类型枚举
* @createDate 2026-08-20
*/
@Getter
@AllArgsConstructor
public enum NotifyTypeEnum {

    PAY_SUCCESS(1, "支付成功"),
    REFUND_SUCCESS(2, "退款成功"),
    REFUND_FAILED(3, "退款失败");

    private final Integer code;
    private final String desc;
}
