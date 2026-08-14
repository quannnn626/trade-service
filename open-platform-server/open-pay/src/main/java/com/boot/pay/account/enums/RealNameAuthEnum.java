package com.boot.pay.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
* @author quannnn
* @description 实名认证状态枚举
* @createDate 2026-08-13
*/
@Getter
@AllArgsConstructor
public enum RealNameAuthEnum {

    UNREAL(0, "未认证"),
    REAL(1, "已认证");

    private final Integer code;
    private final String desc;
}
