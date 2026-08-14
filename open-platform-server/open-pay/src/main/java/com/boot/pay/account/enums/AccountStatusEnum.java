package com.boot.pay.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
* @author quannnn
* @description 用户账户状态枚举
* @createDate 2026-08-13
*/
@Getter
@AllArgsConstructor
public enum AccountStatusEnum {

    FROZEN(0, "冻结"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String desc;
}
