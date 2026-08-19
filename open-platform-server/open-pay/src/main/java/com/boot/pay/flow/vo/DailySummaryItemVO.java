package com.boot.pay.flow.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @author quannnn
* @description 日汇总明细（按流水类型分组）
* @createDate 2026-08-18
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryItemVO {

    /**
     * 流水类型 1-支出 2-收入 3-手续费 4-退款支出 5-退款收入 6-充值 7-冻结 8-解冻 9-调整
     */
    private Integer flowType;

    /**
     * 流水类型名称
     */
    private String flowTypeName;

    /**
     * 笔数
     */
    private Integer count;

    /**
     * 金额合计
     */
    private BigDecimal amount;
}
