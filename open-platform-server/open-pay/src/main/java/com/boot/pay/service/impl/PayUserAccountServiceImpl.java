package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.service.PayUserAccountService;
import com.boot.pay.mapper.PayUserAccountMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_user_account(用户钱包账户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayUserAccountServiceImpl extends ServiceImpl<PayUserAccountMapper, PayUserAccount>
    implements PayUserAccountService{

}




