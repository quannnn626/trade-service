package com.boot.pay.auth.config;

import com.boot.pay.auth.JwtTokenUtil;
import com.boot.pay.auth.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 注册 JWT 拦截器 + 白名单
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAuthInterceptor(jwtTokenUtil))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login", "/auth/register", "/auth/refresh", "/auth/logout",
                        "/api/merchant/**",
                        "/error"
                );
    }
}
