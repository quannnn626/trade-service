package com.boot.pay.service;

import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.payment.dto.CreatePayDTO;
import com.boot.pay.payment.vo.CreatePayVO;
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
     * @param merchantId 商户 ID（从拦截器上下文获取）
     * @param merchantNo 商户编号
     * @param clientIp   客户端 IP
     * @return 支付单号等信息
     */
    CreatePayVO createPayment(CreatePayDTO dto, Long merchantId, String merchantNo, String clientIp);
}
