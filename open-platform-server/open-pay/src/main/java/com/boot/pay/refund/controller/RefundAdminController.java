package com.boot.pay.refund.controller;

import com.boot.common.result.Result;
import com.boot.pay.refund.dto.AuditRefundDTO;
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
 * 平台内部 API — 退款审核接口（JWT 认证）
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundAdminController {

    private final PayRefundOrderService payRefundOrderService;

    /**
     * 退款审核（大额退款，通过则执行退款，驳回则退款终止）
     */
    @PostMapping("/audit")
    public Result<RefundVO> audit(@Valid @RequestBody AuditRefundDTO dto,
                                  HttpServletRequest request) {
        Long auditorId = (Long) request.getAttribute("userId");
        return Result.success(payRefundOrderService.audit(dto, auditorId));
    }
}
