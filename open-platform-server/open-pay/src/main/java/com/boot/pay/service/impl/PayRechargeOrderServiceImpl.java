package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayRechargeOrder;
import com.boot.pay.service.PayRechargeOrderService;
import com.boot.pay.mapper.PayRechargeOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_recharge_order(账户充值订单表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayRechargeOrderServiceImpl extends ServiceImpl<PayRechargeOrderMapper, PayRechargeOrder>
    implements PayRechargeOrderService{

}




