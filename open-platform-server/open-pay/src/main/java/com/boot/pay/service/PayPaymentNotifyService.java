package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.domain.PayPaymentNotify;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.notify.vo.NotifyListVO;

/**
* @author quannnn
* @description 针对表【pay_payment_notify(支付回调通知记录表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayPaymentNotifyService extends IService<PayPaymentNotify> {

    /**
     * 回调通知分页列表（运营后台）
     * <p>
     * 筛选：支付单号（模糊）/商户号（模糊，转商户ID集合）/通知类型/通知状态。
     *
     * @param page         页码
     * @param pageSize     每页条数
     * @param paymentNo    支付单号（模糊）
     * @param merchantNo   商户号（模糊）
     * @param notifyType   通知类型（可空）
     * @param notifyStatus 通知状态（可空）
     * @return 分页列表
     */
    IPage<NotifyListVO> listPage(Integer page, Integer pageSize, String paymentNo, String merchantNo,
                                 Integer notifyType, Integer notifyStatus);

    /**
     * 触发回调通知：创建通知记录并立即发送第一次通知。
     * 须在支付事务提交、分布式锁释放后调用；通知失败不影响支付结果，由重试任务兜底。
     *
     * @param paymentNo 支付单号
     */
    void triggerNotify(String paymentNo);

    /**
     * 触发退款回调通知：创建退款成功通知记录并立即发送第一次通知。
     * 须在退款事务提交、分布式锁释放后调用；通知失败不影响退款结果，由重试任务兜底。
     *
     * @param refundNo 退款单号
     */
    void triggerRefundNotify(String refundNo);

    /**
     * 重试待通知的回调：扫描到期的通知记录并重新发送（定时任务调用）
     */
    void retryNotify();

}
