package com.boot.pay.refund.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款单状态枚举
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum {

    /** 0-处理中（创建退款单后，资金操作完成前） */
    PROCESSING(0, "处理中"),

    /** 1-成功（退款完成） */
    SUCCESS(1, "成功"),

    /** 2-失败（审核驳回等，记录 fail_reason） */
    FAILED(2, "失败");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static RefundStatusEnum of(Integer code) {
        for (RefundStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
