package com.boot.pay.service;

import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.payment.dto.CreatePayDTO;
import com.boot.pay.payment.vo.CreatePayVO;
import com.boot.pay.payment.vo.PayOrderVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author quannnn
* @description 针对表【pay_payment_order(支付订单表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayPaymentOrderService extends IService<PayPaymentOrder> {

    /**
     * 创建支付订单
     *
     * @param dto        请求参数
     * @param merchantId 商户 ID
     * @param merchantNo 商户编号
     * @param clientIp   客户端 IP
     * @return 支付单号等信息
     */
    CreatePayVO createPayment(CreatePayDTO dto, Long merchantId, String merchantNo, String clientIp);

    /**
     * 按支付单号查询
     *
     * @param paymentNo  支付单号
     * @param merchantId 商户 ID（校验订单归属）
     * @return 订单详情
     */
    PayOrderVO queryByPaymentNo(String paymentNo, Long merchantId);

    /**
     * 按商户订单号查询
     *
     * @param orderNo    商户订单号
     * @param merchantId 商户 ID（校验订单归属）
     * @return 订单详情
     */
    PayOrderVO queryByOrderNo(String orderNo, Long merchantId);

    /**
     * 关闭超时未支付的订单
     *
     * @return 本次关闭的订单数量
     */
    int closeExpiredOrders();
}
