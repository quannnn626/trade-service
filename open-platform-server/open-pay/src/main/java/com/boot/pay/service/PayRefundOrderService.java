package com.boot.pay.service;

import com.boot.pay.domain.PayRefundOrder;
import com.boot.pay.refund.dto.RefundCreateDTO;
import com.boot.pay.refund.vo.RefundVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author quannnn
* @description 针对表【pay_refund_order(退款订单表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayRefundOrderService extends IService<PayRefundOrder> {

    /**
     * 发起退款（开放 API）
     * <p>
     * 幂等：同商户同商户退款单号只执行一次；校验订单状态/退款期限/可退金额/商户余额后，
     * 事务内完成"商户扣款 + 用户加款 + 双流水 + 订单状态流转"，事务提交后触发退款回调通知。
     *
     * @param dto        退款请求参数
     * @param merchantId 当前商户 ID（拦截器认证上下文）
     * @return 退款结果
     */
    RefundVO refund(RefundCreateDTO dto, Long merchantId);

    /**
     * 退款资金操作（独立事务，动账必须全部成功或全部回滚）
     * <p>
     * 仅供 {@link #refund} 在锁内调用，通过代理调用使 @Transactional 生效，不对外暴露。
     *
     * @param dto        退款请求参数
     * @param merchantId 当前商户 ID（校验订单归属）
     * @return 退款结果
     */
    RefundVO refundTx(RefundCreateDTO dto, Long merchantId);

}
