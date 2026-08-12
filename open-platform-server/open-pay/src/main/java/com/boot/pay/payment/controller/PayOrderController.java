package com.boot.pay.payment.controller;

import com.boot.pay.annotation.OpenApi;
import com.boot.common.result.Result;
import com.boot.pay.context.MerchantContext;
import com.boot.pay.payment.dto.CreatePayDTO;
import com.boot.pay.payment.vo.CreatePayVO;
import com.boot.pay.service.PayPaymentOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放 API — 支付订单接口
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/open/pay")
@RequiredArgsConstructor
public class PayOrderController {

    private final PayPaymentOrderService payPaymentOrderService;

    /**
     * 创建支付订单
     */
    @OpenApi("pay.create")
    @PostMapping("/create")
    public Result<CreatePayVO> create(@Valid @RequestBody CreatePayDTO dto,
                                      HttpServletRequest request) {
        MerchantContext ctx = (MerchantContext) request.getAttribute("merchantContext");
        String clientIp = request.getRemoteAddr();
        return Result.success(
                payPaymentOrderService.createPayment(dto, ctx.getMerchantId(), ctx.getMerchantNo(), clientIp));
    }
}
