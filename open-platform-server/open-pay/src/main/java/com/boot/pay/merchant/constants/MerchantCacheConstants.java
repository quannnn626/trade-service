package com.boot.pay.merchant.constants;

/**
 * 商户信息缓存常量
 * <p>
 * 写入方在 {@code handler.OpenApiAuthInterceptor}（验签时查商户），失效方在 {@code service.impl.PayMerchantServiceImpl}
 * （改商户状态 / 密钥时）。key 拼接统一走本类，避免两边各拼一份导致漏删缓存。
 *
 * @author quannnn
 */
public final class MerchantCacheConstants {

    /** 缓存 key 前缀，完整 key 形如 pay:merchant:{appKey} */
    public static final String MERCHANT_CACHE_PREFIX = "pay:merchant:";

    /** 缓存有效期（分钟） */
    public static final long MERCHANT_CACHE_TTL_MINUTES = 30;

    public static String cacheKey(String appKey) {
        return MERCHANT_CACHE_PREFIX + appKey;
    }

    private MerchantCacheConstants() {
    }
}
