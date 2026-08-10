package com.boot.pay.merchant.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密钥轮换返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSecretVO {

    /** 商户编号 */
    private String merchantNo;

    /** 新 appSecret（仅此一次展示） */
    private String appSecret;

    /** 新密钥版本号 */
    private Integer secretVersion;

    /** 提示信息 */
    private String tip;
}
