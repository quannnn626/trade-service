package com.boot.pay.recharge.constants;

/**
 * 充值业务常量
 *
 * @author quannnn
 */
public final class RechargeConstants {

    /** 充值方式：银行卡（当前仅此一种） */
    public static final int RECHARGE_WAY_BANK = 1;

    /** 模拟银行收银台地址前缀（页面后期实现） */
    public static final String MOCK_BANK_PAGE_PREFIX = "/mock/bank/page/";

    private RechargeConstants() {
    }
}
