package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.TradePaymentNotify;
import com.boot.pay.service.TradePaymentNotifyService;
import com.boot.pay.mapper.TradePaymentNotifyMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【trade_payment_notify(支付回调通知记录表)】的数据库操作Service实现
* @createDate 2026-08-02 19:57:29
*/
@Service
public class TradePaymentNotifyServiceImpl extends ServiceImpl<TradePaymentNotifyMapper, TradePaymentNotify>
    implements TradePaymentNotifyService{

}




