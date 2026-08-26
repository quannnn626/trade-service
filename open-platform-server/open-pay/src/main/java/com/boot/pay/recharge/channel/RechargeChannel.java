package com.boot.pay.recharge.channel;

import com.boot.pay.domain.PayRechargeOrder;

/**
 * 充值渠道抽象 — 未来接入真实银行时新增实现类，不动充值主流程
 *
 * @author quannnn
 */
public interface RechargeChannel {

    /**
     * 支持的充值方式（对应 pay_recharge_order.recharge_way）
     */
    int rechargeWay();

    /**
     * 发起充值，返回跳转/页面参数（模拟阶段返回模拟银行收银台地址）
     */
    String create(PayRechargeOrder order);

    /**
     * 到账确认处理（模拟阶段无实际确认动作，真实银行接入时在此校验银行侧到账结果）
     */
    void handleArrival(PayRechargeOrder order);
}
