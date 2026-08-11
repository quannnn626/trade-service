package com.boot.pay.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * nonce 防重放验证器
 * <p>
 * 通过 Redis SET NX 确保同一个 nonce 在 5 分钟内只能使用一次。首次出现放行，重复出现拒绝。
 *
 * @author quannnn
 */
@Component
@RequiredArgsConstructor
public class NonceValidator {

    private static final String NONCE_PREFIX = "pay:nonce:";
    private static final long NONCE_TTL_MINUTES = 5;

    private final StringRedisTemplate stringRedisTemplate;

    public boolean validate(String appKey, String nonce) {
        String key = NONCE_PREFIX + appKey + ":" + nonce;
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", NONCE_TTL_MINUTES, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(success);
    }
}
