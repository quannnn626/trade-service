package com.boot.common.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.core.util.IdUtil;
import java.util.Map;
import java.util.TreeMap;

/**
 * 开放 API 签名工具类 — HMAC-SHA256
 * <p>
 * 参数按 key 字母序排序后拼接，以 appSecret 为密钥做 HMAC-SHA256 生成签名。
 * 服务端验签时按同样规则重算后比对，一致则通过。
 *
 * @author quannnn
 */
public class SignUtil {

    private SignUtil() {
        // 工具类，禁止实例化
    }

    public static String generateSign(Map<String, Object> params, String secret) {
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("appSecret 不能为空");
        }

        // TreeMap 自动按 key 字母序升序排列
        TreeMap<String, Object> sortedParams = new TreeMap<>(params);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 剔除 sign 字段、空值和 null 值
            if ("sign".equals(key) || value == null) {
                continue;
            }

            String strValue = value.toString();
            if (strValue.isEmpty()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(strValue);
        }

        String rawString = sb.toString();

        // HMAC-SHA256，密钥为 appSecret
        return SecureUtil.hmacSha256(secret).digestHex(rawString).toUpperCase();
    }

    /**
     * 验证签名
     */
    public static boolean verifySign(Map<String, Object> params, String secret, String sign) {
        if (sign == null || sign.isBlank()) {
            return false;
        }
        String calcSign = generateSign(params, secret);
        return calcSign.equals(sign);
    }

    public static String createNonce() {
        return IdUtil.fastSimpleUUID();
    }
}
