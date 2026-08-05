package com.boot.pay.auth.service;

import com.boot.pay.auth.dto.LoginDTO;
import com.boot.pay.auth.dto.RefreshDTO;
import com.boot.pay.auth.dto.RegisterDTO;
import com.boot.pay.auth.vo.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    LoginVO login(LoginDTO dto);

    void register(RegisterDTO dto);

    String refresh(RefreshDTO dto);

    void logout(Long userId);
}
