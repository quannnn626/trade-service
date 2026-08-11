package com.boot.pay.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商户认证上下文 — 拦截器验签通过后挂到 request，Controller 用这个拿商户身份
 * <p>
 * 只包含 Controller 需要的三个字段，不暴露 appSecret 等敏感信息。
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public class MerchantContext {

    /** 商户主键 ID */
    private Long merchantId;

    /** 商户编号 */
    private String merchantNo;

    /** 应用 Key */
    private String appKey;
}
