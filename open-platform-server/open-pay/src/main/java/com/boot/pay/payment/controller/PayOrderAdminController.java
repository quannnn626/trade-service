package com.boot.pay.payment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.common.result.Result;
import com.boot.pay.payment.vo.PayOrderListVO;
import com.boot.pay.service.PayPaymentOrderService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台内部 API — 支付订单管理接口（JWT 认证）
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/api/pay/order")
@RequiredArgsConstructor
public class PayOrderAdminController {

    private final PayPaymentOrderService payPaymentOrderService;

    /**
     * 支付订单分页列表（运营后台）
     * 筛选：支付单号/商户订单号/商户号（模糊）、状态（精确）、创建时间范围
     */
    @GetMapping("/list")
    public Result<IPage<PayOrderListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String merchantNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(payPaymentOrderService.listPage(page, pageSize, paymentNo, orderNo, merchantNo, status, startTime, endTime));
    }
}
