package com.boot.pay.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资金流水类型枚举
 * <p>
 * 与计划 6.1 流水类型定义一致。阶段五使用 EXPENSE（用户支出）与 INCOME（商户收入）；
 * 阶段四 4.5 冻结/解冻使用 FREEZE / UNFREEZE。
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum AccountFlowTypeEnum {

    /** 1-支出（用户支付） */
    EXPENSE(1, "支出"),

    /** 2-收入（商户收款） */
    INCOME(2, "收入"),

    /** 3-手续费（平台抽成） */
    FEE(3, "手续费"),

    /** 4-退款支出（商户退款给用户） */
    REFUND_EXPENSE(4, "退款支出"),

    /** 5-退款收入（用户收到退款） */
    REFUND_INCOME(5, "退款收入"),

    /** 6-充值（用户充值入账） */
    RECHARGE(6, "充值"),

    /** 7-冻结（资金冻结） */
    FREEZE(7, "冻结"),

    /** 8-解冻（资金解冻） */
    UNFREEZE(8, "解冻"),

    /** 9-调整（人工调账，超级管理员权限） */
    ADJUST(9, "调整");

    private final Integer code;
    private final String desc;

    /**
     * 根据流水类型码反查枚举（不存在返回 null）
     */
    public static AccountFlowTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AccountFlowTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
