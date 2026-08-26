package com.boot.pay.recharge.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 创建充值单返回
 *
 * @author quannnn
 */
@Data
@Builder
public class RechargeCreateVO {

    /** 充值单号 */
    private String rechargeNo;

    /** 模拟银行收银台地址（页面后期实现，先占位） */
    private String bankPageUrl;
}
