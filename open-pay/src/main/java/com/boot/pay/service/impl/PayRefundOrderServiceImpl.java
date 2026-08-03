package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayRefundOrder;
import com.boot.pay.service.PayRefundOrderService;
import com.boot.pay.mapper.PayRefundOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_refund_order(退款订单表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayRefundOrderServiceImpl extends ServiceImpl<PayRefundOrderMapper, PayRefundOrder>
    implements PayRefundOrderService{

}




