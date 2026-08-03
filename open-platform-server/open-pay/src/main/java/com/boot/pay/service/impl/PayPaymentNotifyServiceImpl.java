package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayPaymentNotify;
import com.boot.pay.service.PayPaymentNotifyService;
import com.boot.pay.mapper.PayPaymentNotifyMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_payment_notify(支付回调通知记录表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayPaymentNotifyServiceImpl extends ServiceImpl<PayPaymentNotifyMapper, PayPaymentNotify>
    implements PayPaymentNotifyService{

}




