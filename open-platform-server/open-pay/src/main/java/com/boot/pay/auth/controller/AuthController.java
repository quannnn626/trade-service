package com.boot.pay.auth.controller;

import com.boot.common.result.Result;
import com.boot.pay.auth.dto.LoginDTO;
import com.boot.pay.auth.dto.RefreshDTO;
import com.boot.pay.auth.dto.RegisterDTO;
import com.boot.pay.auth.service.AuthService;
import com.boot.pay.auth.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<String> refresh(@RequestBody RefreshDTO dto) {
        return Result.success(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestParam Long userId) {
        authService.logout(userId);
        return Result.success();
    }
}
