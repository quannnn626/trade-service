package com.boot.pay.payment.exception;

/**
 * 账户乐观锁冲突异常
 * <p>
 * 仅作为事务内部的回滚信号：资金操作因 version 不匹配或余额不足导致更新 0 行时抛出，
 * 由支付执行的外层重试循环捕获后重新读取版本号重试，不对外暴露给调用方。
 *
 * @author quannnn
 */
public class PayOptimisticLockException extends RuntimeException {

    public PayOptimisticLockException(String message) {
        super(message);
    }
}
