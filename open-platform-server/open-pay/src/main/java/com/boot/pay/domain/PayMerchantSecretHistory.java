package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 商户密钥历史表
 */
@TableName(value = "pay_merchant_secret_history")
@Data
public class PayMerchantSecretHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 历史密钥 */
    private String secret;

    /** 密钥版本号 */
    private Integer version;

    /** 失效时间 */
    private Date expireTime;

    /** 创建时间 */
    private Date createTime;
}
