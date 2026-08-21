package com.boot.pay.refund.controller;

import com.boot.common.result.Result;
import com.boot.pay.annotation.OpenApi;
import com.boot.pay.context.MerchantContext;
import com.boot.pay.refund.dto.RefundCreateDTO;
import com.boot.pay.refund.vo.RefundVO;
import com.boot.pay.service.PayRefundOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放 API — 退款接口
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/open/pay")
@RequiredArgsConstructor
public class RefundController {

    private final PayRefundOrderService payRefundOrderService;

    /**
     * 发起退款
     */
    @OpenApi("pay.refund")
    @PostMapping("/refund")
    public Result<RefundVO> refund(@Valid @RequestBody RefundCreateDTO dto,
                                   HttpServletRequest request) {
        MerchantContext ctx = (MerchantContext) request.getAttribute("merchantContext");
        return Result.success(payRefundOrderService.refund(dto, ctx.getMerchantId()));
    }
}
