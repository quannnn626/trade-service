package com.boot.pay.mapper;

import com.boot.pay.domain.PayRefundOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* @author quannnn
* @description 针对表【pay_refund_order(退款订单表)】的数据库操作Mapper
* @createDate 2026-08-03 12:26:43
* @Entity com.boot.pay.domain.PayRefundOrder
*/
public interface PayRefundOrderMapper extends BaseMapper<PayRefundOrder> {

    /**
     * 累计已占用退款金额（处理中 + 成功的退款单，待审核的同样占额度）
     *
     * @param paymentNo 支付单号
     * @param statuses  退款单状态集合（处理中/成功）
     * @return 已占用退款金额，无记录返回 0
     */
    BigDecimal sumRefundedAmount(@Param("paymentNo") String paymentNo,
                                 @Param("statuses") List<Integer> statuses);

    /**
     * 日汇总：按退款完成时间归日统计退款笔数与金额
     *
     * @param startTime 开始时间（含）
     * @param endTime   结束时间（不含）
     * @param status    退款单状态，统计成功退款传 RefundStatusEnum.SUCCESS
     * @return refundCount 笔数、refundAmount 退款金额；无记录时金额为 0
     */
    Map<String, Object> sumDailyRefund(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("status") Integer status);
}




