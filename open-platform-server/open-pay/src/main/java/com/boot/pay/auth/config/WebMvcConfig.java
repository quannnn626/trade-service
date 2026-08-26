package com.boot.pay.auth.config;

import com.boot.pay.auth.JwtTokenUtil;
import com.boot.pay.auth.interceptor.JwtAuthInterceptor;
import com.boot.pay.handler.NonceValidator;
import com.boot.pay.handler.OpenApiAuthInterceptor;
import com.boot.pay.service.PayMerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 注册 JWT 拦截器 + 开放 API 签名拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUtil jwtTokenUtil;
    private final PayMerchantService payMerchantService;
    private final NonceValidator nonceValidator;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 拦截器 — 拦截 /**，排除公开路径和开放 API
        registry.addInterceptor(new JwtAuthInterceptor(jwtTokenUtil))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login", "/auth/register", "/auth/refresh", "/auth/logout",
                        "/api/merchant/apply",
                        // 充值到账回调：模拟银行通知，无 JWT（接入真实银行时补银行侧验签）
                        "/api/recharge/callback",
                        "/open/**",
                        "/error"
                );

        // 开放 API 签名拦截器 — 只拦截 /open/**
        registry.addInterceptor(new OpenApiAuthInterceptor(
                        payMerchantService, nonceValidator, stringRedisTemplate))
                .addPathPatterns("/open/**");
    }
}
