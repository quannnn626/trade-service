package com.boot.pay.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * nonce 防重放验证器
 * <p>
 * 拆为两步：check 只查不存（拦截器用），mark 在业务成功后标记（AOP 切面用）。
 * 避免业务失败后 nonce 已被消耗导致无法重试的问题。
 *
 * @author quannnn
 */
@Component
@RequiredArgsConstructor
public class NonceValidator {

    private static final String NONCE_PREFIX = "pay:nonce:";
    private static final long NONCE_TTL_MINUTES = 5;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 检查 nonce 是否已被使用（只查不存）
     *
     * @return true 表示未使用可放行，false 表示已使用需拒绝
     */
    public boolean check(String appKey, String nonce) {
        String key = NONCE_PREFIX + appKey + ":" + nonce;
        // 从redis中查找nonce是否存在，底层调用exists查找是否存在，比get更快
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 标记 nonce 已使用（业务成功后调用）
     */
    public void mark(String appKey, String nonce) {
        String key = NONCE_PREFIX + appKey + ":" + nonce;
        stringRedisTemplate.opsForValue().set(key, "1", NONCE_TTL_MINUTES, TimeUnit.MINUTES);
    }
}
