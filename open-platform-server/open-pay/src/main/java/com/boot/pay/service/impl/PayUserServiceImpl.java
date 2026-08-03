package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayUser;
import com.boot.pay.service.PayUserService;
import com.boot.pay.mapper.PayUserMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_user(交易平台用户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayUserServiceImpl extends ServiceImpl<PayUserMapper, PayUser>
    implements PayUserService{

}




