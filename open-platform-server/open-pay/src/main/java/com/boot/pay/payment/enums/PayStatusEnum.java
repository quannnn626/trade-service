package com.boot.pay.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum PayStatusEnum {

    WAIT_PAY(0, "待支付"),
    PAYING(1, "支付中"),
    SUCCESS(2, "支付成功"),
    FAILED(3, "支付失败"),
    CLOSED(4, "已关闭"),
    REFUNDING(5, "退款中"),
    REFUNDED(6, "已退款");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static PayStatusEnum of(Integer code) {
        for (PayStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
