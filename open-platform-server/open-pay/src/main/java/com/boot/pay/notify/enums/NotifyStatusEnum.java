package com.boot.pay.notify.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
* @author quannnn
* @description 支付回调通知状态枚举
* @createDate 2026-08-20
*/
@Getter
@AllArgsConstructor
public enum NotifyStatusEnum {

    WAIT(0, "待通知"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败（达上限）");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static NotifyStatusEnum of(Integer code) {
        for (NotifyStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
