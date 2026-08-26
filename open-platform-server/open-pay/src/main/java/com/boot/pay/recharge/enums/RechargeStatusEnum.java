package com.boot.pay.recharge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 充值单状态枚举
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum RechargeStatusEnum {

    /** 0-待支付（创建充值单后，银行到账前） */
    WAIT_PAY(0, "待支付"),

    /** 2-充值成功（银行到账，余额已入账） */
    SUCCESS(2, "充值成功");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static RechargeStatusEnum of(Integer code) {
        for (RechargeStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
