package com.boot.pay.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.boot.common.utils.SignUtil;
import com.boot.pay.context.MerchantContext;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.service.PayMerchantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 开放 API 签名验证拦截器 — 拦截 /open/** 路径
 * <p>
 * 逐项校验：提取认证参数 → 查商户（Redis 缓存 → DB）→ 商户状态 → IP 白名单 → 授权有效期 → timestamp(5 分钟窗口) → nonce(防重放) → HMAC-SHA256 验签。
 * 任意一步失败返回 401，全部通过后挂 MerchantContext 到 request。
 *
 * @author quannnn
 */
@Slf4j
public class OpenApiAuthInterceptor implements HandlerInterceptor {

    private static final long TIME_WINDOW_MILLIS = 5 * 60 * 1000L;
    private static final String MERCHANT_CACHE_PREFIX = "pay:merchant:";
    private static final long MERCHANT_CACHE_TTL_MINUTES = 30;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PayMerchantService payMerchantService;
    private final NonceValidator nonceValidator;
    private final StringRedisTemplate stringRedisTemplate;

    public OpenApiAuthInterceptor(PayMerchantService payMerchantService,
                                   NonceValidator nonceValidator,
                                   StringRedisTemplate stringRedisTemplate) {
        this.payMerchantService = payMerchantService;
        this.nonceValidator = nonceValidator;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 提取参数
        Map<String, Object> params = extractParams(request);
        String appKey = getString(params, "appKey");
        String timestampStr = getString(params, "timestamp");
        String nonce = getString(params, "nonce");
        String sign = getString(params, "sign");
        if (anyBlank(appKey, timestampStr, nonce, sign)) {
            write401(response, "缺少认证参数");
            return false;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            write401(response, "timestamp 格式错误");
            return false;
        }

        // 查商户
        PayMerchant merchant = getMerchantByAppKey(appKey);
        if (merchant == null) {
            log.warn("商户不存在: appKey={}", appKey);
            write401(response, "商户不存在");
            return false;
        }

        // 校验商户状态
        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            log.warn("商户已禁用: appKey={}", appKey);
            write401(response, "商户已被禁用");
            return false;
        }

        // 校验 IP 白名单
        if (!checkIpWhitelist(merchant, request)) {
            log.warn("IP 未授权: appKey={}, ip={}", appKey, request.getRemoteAddr());
            write401(response, "IP 未授权");
            return false;
        }

        // 校验授权有效期
        if (merchant.getExpireTime() != null
                && System.currentTimeMillis() > merchant.getExpireTime().getTime()) {
            log.warn("授权已过期: appKey={}", appKey);
            write401(response, "授权已过期");
            return false;
        }

        // 校验 timestamp
        if (Math.abs(System.currentTimeMillis() - timestamp) > TIME_WINDOW_MILLIS) {
            log.warn("请求已过期: appKey={}", appKey);
            write401(response, "请求已过期");
            return false;
        }

        // 校验 nonce
        if (!nonceValidator.validate(appKey, nonce)) {
            log.warn("nonce 重复: appKey={}", appKey);
            write401(response, "重复请求");
            return false;
        }

        // 验签
        if (!SignUtil.verifySign(params, merchant.getAppSecret(), sign)) {
            log.warn("签名验证失败: appKey={}", appKey);
            write401(response, "签名验证失败");
            return false;
        }

        // 通过
        request.setAttribute("merchantContext",
                new MerchantContext(merchant.getId(), merchant.getMerchantNo(), merchant.getAppKey()));
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParams(HttpServletRequest request) throws Exception {
        if ("POST".equalsIgnoreCase(request.getMethod())
                && request.getContentType() != null
                && request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)
                && request instanceof CachedBodyRequestWrapper wrapper) {
            String body = wrapper.getBodyAsString();
            if (body != null && !body.isBlank()) {
                return JSON.parseObject(body, new TypeReference<Map<String, Object>>() {});
            }
        }
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }
        return params;
    }

    private PayMerchant getMerchantByAppKey(String appKey) {
        String cacheKey = MERCHANT_CACHE_PREFIX + appKey;
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            return JSON.parseObject(cachedJson, PayMerchant.class);
        }
        PayMerchant merchant = payMerchantService.getOne(
                new LambdaQueryWrapper<PayMerchant>().eq(PayMerchant::getAppKey, appKey));
        if (merchant != null) {
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(merchant),
                    MERCHANT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return merchant;
    }

    private boolean checkIpWhitelist(PayMerchant merchant, HttpServletRequest request) {
        String whiteIpList = merchant.getWhiteIpList();
        if (whiteIpList == null || whiteIpList.isBlank()) {
            return true;
        }
        try {
            java.util.List<String> allowedIps = JSON.parseArray(whiteIpList, String.class);
            return allowedIps == null || allowedIps.isEmpty()
                    || allowedIps.contains(request.getRemoteAddr());
        } catch (Exception e) {
            log.warn("解析 IP 白名单失败: {}", whiteIpList, e);
            return true;
        }
    }

    private String getString(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    private boolean anyBlank(String... values) {
        for (String v : values) {
            if (v == null || v.isBlank()) return true;
        }
        return false;
    }

    private void write401(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("data", null);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(body));
    }
}
