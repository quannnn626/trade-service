package com.boot.pay.flow.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @author quannnn
* @description 资金流水日汇总报表
* @createDate 2026-08-18
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryVO {

    /**
     * 汇总日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 总笔数
     */
    private Integer totalCount;

    /**
     * 按流水类型汇总明细
     */
    private List<DailySummaryItemVO> items;
}
