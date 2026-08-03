package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayMerchantAccount;
import com.boot.pay.service.PayMerchantAccountService;
import com.boot.pay.mapper.PayMerchantAccountMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_merchant_account(商户资金账户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayMerchantAccountServiceImpl extends ServiceImpl<PayMerchantAccountMapper, PayMerchantAccount>
    implements PayMerchantAccountService{

}




