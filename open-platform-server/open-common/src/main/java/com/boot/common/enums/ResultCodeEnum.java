package com.boot.common.enums;

import lombok.Getter;

/**
 * 结果码枚举
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(0, "success"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "拒绝访问"),
    PARAM_ERROR(400, "参数错误"),
    SERVER_ERROR(500, "系统异常");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
