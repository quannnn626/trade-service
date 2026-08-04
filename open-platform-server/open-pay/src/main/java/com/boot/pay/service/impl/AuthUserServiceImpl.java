package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.AuthUser;
import com.boot.pay.service.AuthUserService;
import com.boot.pay.mapper.AuthUserMapper;
import org.springframework.stereotype.Service;

/**
 * @description 针对表【auth_user(认证用户表)】的数据库操作Service实现
 */
@Service
public class AuthUserServiceImpl extends ServiceImpl<AuthUserMapper, AuthUser>
    implements AuthUserService {

}
