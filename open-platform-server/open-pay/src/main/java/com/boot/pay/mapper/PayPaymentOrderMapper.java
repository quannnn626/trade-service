package com.boot.pay.mapper;

import com.boot.pay.domain.PayPaymentOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* @author quannnn
* @description 针对表【pay_payment_order(支付订单表)】的数据库操作Mapper
* @createDate 2026-08-03 12:26:43
* @Entity com.boot.pay.domain.PayPaymentOrder
*/
public interface PayPaymentOrderMapper extends BaseMapper<PayPaymentOrder> {

    /**
     * 日汇总：按支付时间归日统计交易额、手续费、结算额与笔数
     * <p>
     * 手续费只记录在订单表的 fee_amount，报表口径必须从这里取（流水表没有手续费记录，见开发计划 10.3.1）。
     *
     * @param startTime 开始时间（含）
     * @param endTime   结束时间（不含）
     * @param statuses  计入统计的订单状态（支付成功/退款中/已退款）
     * @return payCount 笔数、tradeAmount 交易额、feeAmount 手续费、settleAmount 结算额；无记录时金额为 0
     */
    Map<String, Object> sumDailyTrade(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("statuses") List<Integer> statuses);
}




