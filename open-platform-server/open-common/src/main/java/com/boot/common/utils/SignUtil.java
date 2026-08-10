package com.boot.common.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.core.util.IdUtil;
import java.util.Map;
import java.util.TreeMap;

/**
 * 开放 API 签名工具类
 * <p>
 * 签名算法：HMAC-SHA256
 * <pre>
 * ① 剔除 sign 字段
 * ② 参数按 key 字母序升序排列
 * ③ 拼接成 key1=value1&key2=value2...
 * ④ HMAC-SHA256(appSecret, rawString)
 * </pre>
 *
 * @author quannnn
 */
public class SignUtil {

    private SignUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 生成签名
     *
     * @param params 请求参数（不含 sign）
     * @param secret 商户 appSecret
     * @return 签名字符串（大写十六进制）
     */
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
     *
     * @param params 请求参数（不含 sign）
     * @param secret 商户 appSecret
     * @param sign   请求中的签名
     * @return true-验证通过，false-验证失败
     */
    public static boolean verifySign(Map<String, Object> params, String secret, String sign) {
        if (sign == null || sign.isBlank()) {
            return false;
        }
        String calcSign = generateSign(params, secret);
        return calcSign.equals(sign);
    }

    /**
     * 生成 32 位随机 nonce
     */
    public static String createNonce() {
        return IdUtil.fastSimpleUUID();
    }
}
