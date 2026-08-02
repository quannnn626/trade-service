package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.TradePaymentOrder;
import com.boot.pay.service.TradePaymentOrderService;
import com.boot.pay.mapper.TradePaymentOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【trade_payment_order(支付订单表)】的数据库操作Service实现
* @createDate 2026-08-02 19:57:29
*/
@Service
public class TradePaymentOrderServiceImpl extends ServiceImpl<TradePaymentOrderMapper, TradePaymentOrder>
    implements TradePaymentOrderService{

}




