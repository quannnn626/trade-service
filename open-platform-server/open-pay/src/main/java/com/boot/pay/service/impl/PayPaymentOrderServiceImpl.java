package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.service.PayPaymentOrderService;
import com.boot.pay.mapper.PayPaymentOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_payment_order(支付订单表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayPaymentOrderServiceImpl extends ServiceImpl<PayPaymentOrderMapper, PayPaymentOrder>
    implements PayPaymentOrderService{

}




