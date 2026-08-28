package com.boot.pay.refund.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.common.result.Result;
import com.boot.pay.refund.dto.AuditRefundDTO;
import com.boot.pay.refund.vo.RefundDetailVO;
import com.boot.pay.refund.vo.RefundListVO;
import com.boot.pay.refund.vo.RefundVO;
import com.boot.pay.service.PayRefundOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 退款订单分页列表（运营后台）
     * 筛选：退款单号/支付单号/商户号（模糊）、退款状态/审核状态（精确）、创建时间范围
     */
    @GetMapping("/list")
    public Result<IPage<RefundListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String refundNo,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) String merchantNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(payRefundOrderService.listPage(page, pageSize, refundNo, paymentNo, merchantNo, status, auditStatus, startTime, endTime));
    }
}
