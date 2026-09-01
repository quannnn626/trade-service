package com.boot.pay.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 接口日志验签结果枚举
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum SignResultEnum {

    PASS(0, "通过"),
    FAIL(1, "失败");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static SignResultEnum of(Integer code) {
        for (SignResultEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
