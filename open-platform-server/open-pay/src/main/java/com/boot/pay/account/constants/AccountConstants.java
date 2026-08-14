package com.boot.pay.account.constants;

import java.math.BigDecimal;

/**
* @author quannnn
* @description 用户账户常量
* @createDate 2026-08-13
*/
public final class AccountConstants {

    /** 未实名用户单日支付限额 */
    public static final BigDecimal DAILY_LIMIT_UNREAL_NAME = new BigDecimal("1000.00");

    /** 实名用户单日支付限额 */
    public static final BigDecimal DAILY_LIMIT_REAL_NAME = new BigDecimal("50000.00");

    private AccountConstants() {
    }
}
