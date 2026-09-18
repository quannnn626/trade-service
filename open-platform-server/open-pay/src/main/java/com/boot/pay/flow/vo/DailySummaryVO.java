package com.boot.pay.flow.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @author quannnn
* @description 日汇总报表
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
     * 支付成功笔数（订单表口径，按支付时间归日）
     */
    private Integer payCount;

    /**
     * 交易额合计 = Σ订单金额（用户实付，未扣手续费）
     */
    private BigDecimal tradeAmount;

    /**
     * 手续费合计 = Σ订单手续费（手续费只记在订单表 fee_amount，见开发计划 10.3.1）
     */
    private BigDecimal feeAmount;

    /**
     * 商户到账合计 = Σ结算金额（= 交易额 - 手续费）
     */
    private BigDecimal settleAmount;

    /**
     * 退款成功笔数（退款单表口径，按退款完成时间归日）
     */
    private Integer refundCount;

    /**
     * 退款金额合计 = Σ实际退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 按流水类型汇总明细（资金流水口径）
     */
    private List<DailySummaryItemVO> items;
}
