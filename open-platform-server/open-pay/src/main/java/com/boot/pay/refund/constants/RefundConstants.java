package com.boot.pay.refund.constants;

import java.math.BigDecimal;

/**
 * 退款业务规则常量（对应开发计划 8.3 退款规则）
 *
 * @author quannnn
 */
public final class RefundConstants {

    /** 退款期限（天）：支付成功后 90 天内可退 */
    public static final int REFUND_PERIOD_DAYS = 90;

    /** 大额退款审核阈值（元）：超过需人工审核 */
    public static final BigDecimal AUDIT_LIMIT = new BigDecimal("10000.00");

    /** 退款渠道：原路退回 */
    public static final int REFUND_CHANNEL_ORIGINAL = 1;

    private RefundConstants() {
    }
}
