package com.boot.pay.service;

import com.boot.pay.domain.PayPaymentNotify;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author quannnn
* @description 针对表【pay_payment_notify(支付回调通知记录表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayPaymentNotifyService extends IService<PayPaymentNotify> {

    /**
     * 触发回调通知：创建通知记录并立即发送第一次通知。
     * 须在支付事务提交、分布式锁释放后调用；通知失败不影响支付结果，由重试任务兜底。
     *
     * @param paymentNo 支付单号
     */
    void triggerNotify(String paymentNo);

}
