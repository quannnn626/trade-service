package com.boot.pay.task;

import com.boot.pay.service.PayPaymentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付订单定时任务
 *
 * @author quannnn
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOrderTask {

    private final PayPaymentOrderService payPaymentOrderService;

    /**
     * 关闭超时未支付的订单（每分钟执行）
     */
    @Scheduled(fixedDelay = 60_000)
    public void closeExpiredOrders() {
        try {
            payPaymentOrderService.closeExpiredOrders();
        } catch (Exception e) {
            log.error("超时关单任务执行失败", e);
        }
    }
}
