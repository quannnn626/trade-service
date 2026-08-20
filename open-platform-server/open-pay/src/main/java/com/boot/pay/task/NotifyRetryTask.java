package com.boot.pay.task;

import com.boot.pay.service.PayPaymentNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 支付回调重试定时任务
 *
 * @author quannnn
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyRetryTask {

    /**
     * 多实例防重入锁（微服务拆分后多个实例不会同时扫描同一批记录）
     */
    private static final String RETRY_LOCK_KEY = "pay:notify:retry-lock";

    private final PayPaymentNotifyService payPaymentNotifyService;

    private final RedissonClient redissonClient;

    /**
     * 每 30 秒扫描待重试回调，每次最多处理 100 条
     */
    @Scheduled(fixedDelay = 30_000)
    public void retryNotify() {
        RLock lock = redissonClient.getLock(RETRY_LOCK_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, TimeUnit.SECONDS);
            if (!locked) {
                log.info("回调重试任务被其他实例执行，跳过本轮");
                return;
            }
            payPaymentNotifyService.retryNotify();
        } catch (Exception e) {
            log.error("回调重试任务执行失败", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
