package com.boot.pay.recharge.controller;

import com.boot.common.result.Result;
import com.boot.pay.recharge.dto.RechargeCreateDTO;
import com.boot.pay.recharge.vo.RechargeCreateVO;
import com.boot.pay.service.PayRechargeOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 充值接口 — 用户向钱包充值（模拟银行卡 → 银行系统）
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/api/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final PayRechargeOrderService payRechargeOrderService;

    /**
     * 创建充值单
     */
    @PostMapping("/create")
    public Result<RechargeCreateVO> create(@Valid @RequestBody RechargeCreateDTO dto,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(payRechargeOrderService.create(dto, userId));
    }
}
