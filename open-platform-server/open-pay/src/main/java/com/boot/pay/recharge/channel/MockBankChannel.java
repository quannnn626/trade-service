package com.boot.pay.recharge.channel;

import com.boot.pay.domain.PayRechargeOrder;
import com.boot.pay.recharge.constants.RechargeConstants;
import org.springframework.stereotype.Component;

/**
 * 模拟银行渠道 — 银行系统未开发，页面后期实现，先返回占位地址
 *
 * @author quannnn
 */
@Component
public class MockBankChannel implements RechargeChannel {

    @Override
    public int rechargeWay() {
        return RechargeConstants.RECHARGE_WAY_BANK;
    }

    @Override
    public String create(PayRechargeOrder order) {
        return RechargeConstants.MOCK_BANK_PAGE_PREFIX + order.getRechargeNo();
    }

    @Override
    public void handleArrival(PayRechargeOrder order) {
        // 模拟银行无确认动作，真实银行接入时在此校验银行侧到账结果
    }
}
