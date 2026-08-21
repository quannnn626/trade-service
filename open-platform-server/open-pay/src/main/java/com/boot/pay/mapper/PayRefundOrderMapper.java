package com.boot.pay.mapper;

import com.boot.pay.domain.PayRefundOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

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
}




