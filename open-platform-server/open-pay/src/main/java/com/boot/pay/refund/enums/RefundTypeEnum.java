package com.boot.pay.refund.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款类型枚举
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum RefundTypeEnum {

    /** 1-全额退款 */
    FULL(1, "全额"),

    /** 2-部分退款 */
    PARTIAL(2, "部分");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static RefundTypeEnum of(Integer code) {
        for (RefundTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
