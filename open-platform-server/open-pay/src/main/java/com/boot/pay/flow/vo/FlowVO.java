package com.boot.pay.flow.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @author quannnn
* @description 资金流水返回对象
* @createDate 2026-08-18
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowVO {

    /**
     * 流水号
     */
    private String flowNo;

    /**
     * 账户类型 1-用户 2-商户
     */
    private Integer accountType;

    /**
     * 账户类型名称
     */
    private String accountTypeName;

    /**
     * 账户编号（用户钱包/商户资金账户编号）
     */
    private String accountNo;

    /**
     * 关联支付单号
     */
    private String paymentNo;

    /**
     * 流水类型 1-支出 2-收入 3-手续费 4-退款支出 5-退款收入 6-充值 7-冻结 8-解冻 9-调整
     */
    private Integer flowType;

    /**
     * 流水类型名称
     */
    private String flowTypeName;

    /**
     * 变动金额（支出为负、收入为正）
     */
    private BigDecimal amount;

    /**
     * 变更前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 变更后余额
     */
    private BigDecimal afterBalance;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private String createTime;
}
