package com.boot.pay.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求体缓存过滤器 — 对 /open/** 路径自动包装 CachedBodyRequestWrapper
 * <p>
 * 非 /open/** 路径原样透传，保证只对开放 API 生效。
 *
 * @author quannnn
 */
@Component
public class CachedBodyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith(request.getContextPath() + "/open/")) {
            filterChain.doFilter(new CachedBodyRequestWrapper(request), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
