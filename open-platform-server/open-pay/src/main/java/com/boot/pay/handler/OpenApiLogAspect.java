package com.boot.pay.handler;

import com.alibaba.fastjson2.JSON;
import com.boot.pay.annotation.OpenApi;
import com.boot.pay.context.MerchantContext;
import com.boot.pay.domain.PayApiLog;
import com.boot.pay.service.PayApiLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 开放 API 日志切面 — 拦截 @OpenApi 注解的方法，自动记录请求日志到 pay_api_log
 *
 * @author quannnn
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OpenApiLogAspect {

    private final PayApiLogService payApiLogService;

    private final NonceValidator nonceValidator;

    @Around("@annotation(openApi)")
    public Object around(ProceedingJoinPoint joinPoint, OpenApi openApi) throws Throwable {
        long start = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        PayApiLog apiLog = new PayApiLog();
        apiLog.setApiName(openApi.value());

        // 请求基本信息
        if (request != null) {
            apiLog.setRequestMethod(request.getMethod());
            apiLog.setRequestUrl(request.getRequestURI());
            apiLog.setUserAgent(request.getHeader("User-Agent"));

            MerchantContext ctx = (MerchantContext) request.getAttribute("merchantContext");
            if (ctx != null) {
                apiLog.setMerchantId(ctx.getMerchantId());
                apiLog.setMerchantNo(ctx.getMerchantNo());
            }
        }

        // 请求参数
        try {
            apiLog.setRequestParam(JSON.toJSONString(joinPoint.getArgs()));
        } catch (Exception e) {
            apiLog.setRequestParam("[serialize error]");
        }

        // 执行方法
        Object result;
        try {
            result = joinPoint.proceed();
            apiLog.setSignResult(0);
            apiLog.setResponseResult(JSON.toJSONString(result));

            // 业务成功后标记 nonce 已使用
            if (request != null) {
                String nonce = (String) request.getAttribute("openApiNonce");
                MerchantContext ctx = (MerchantContext) request.getAttribute("merchantContext");
                if (nonce != null && ctx != null) {
                    nonceValidator.mark(ctx.getAppKey(), nonce);
                }
            }
        } catch (Throwable t) {
            apiLog.setSignResult(0);
            apiLog.setErrorMsg(truncate(t.getMessage(), 500));
            apiLog.setResponseResult("[exception: " + t.getClass().getSimpleName() + "]");
            throw t;
        } finally {
            apiLog.setCostTime((int) (System.currentTimeMillis() - start));
            saveLog(apiLog);
        }

        return result;
    }

    private void saveLog(PayApiLog apiLog) {
        try {
            payApiLogService.save(apiLog);
        } catch (Exception e) {
            log.warn("保存接口日志失败: apiName={}", apiLog.getApiName(), e);
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
