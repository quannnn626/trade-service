package com.boot.pay.refund.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款审核状态枚举
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum RefundAuditStatusEnum {

    /** 0-待审核（大额退款，等待管理员审核） */
    WAIT(0, "待审核"),

    /** 1-通过（小额自动通过 / 管理员审核通过） */
    PASS(1, "通过"),

    /** 2-驳回（审核不通过，退款终止） */
    REJECT(2, "驳回");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static RefundAuditStatusEnum of(Integer code) {
        for (RefundAuditStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
