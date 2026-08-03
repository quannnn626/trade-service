package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.service.PayMerchantService;
import com.boot.pay.mapper.PayMerchantMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_merchant(交易平台商户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayMerchantServiceImpl extends ServiceImpl<PayMerchantMapper, PayMerchant>
    implements PayMerchantService{

}




