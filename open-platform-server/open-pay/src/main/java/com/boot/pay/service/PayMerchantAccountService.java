package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.domain.PayMerchantAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.merchant.vo.MerchantAccountVO;

/**
* @author quannnn
* @description 针对表【pay_merchant_account(商户资金账户表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayMerchantAccountService extends IService<PayMerchantAccount> {

    /**
     * 商户账户分页列表（运营后台）
     * 商户号/商户名需先查 pay_merchant 转 merchantId 过滤，展示时回填商户号与商户名
     */
    IPage<MerchantAccountVO> listPage(Integer page, Integer pageSize,
                                      String accountNo, String merchantNo, String merchantName);
}
