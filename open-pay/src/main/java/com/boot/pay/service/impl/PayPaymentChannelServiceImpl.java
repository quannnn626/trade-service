package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayPaymentChannel;
import com.boot.pay.service.PayPaymentChannelService;
import com.boot.pay.mapper.PayPaymentChannelMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_payment_channel(支付渠道表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayPaymentChannelServiceImpl extends ServiceImpl<PayPaymentChannelMapper, PayPaymentChannel>
    implements PayPaymentChannelService{

}




