package com.boot.pay.merchant.controller;

import com.boot.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.merchant.dto.MerchantApplyDTO;
import com.boot.pay.merchant.dto.MerchantAuditDTO;
import com.boot.pay.merchant.vo.MerchantApplyVO;
import com.boot.pay.merchant.vo.MerchantAuditVO;
import com.boot.pay.merchant.vo.MerchantDetailVO;
import com.boot.pay.merchant.vo.MerchantListVO;
import com.boot.pay.service.PayMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户管理接口
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final PayMerchantService payMerchantService;

    /**
     * 商户入驻申请
     */
    @PostMapping("/apply")
    public Result<MerchantApplyVO> apply(@Valid @RequestBody MerchantApplyDTO dto) {
        return Result.success(payMerchantService.apply(dto));
    }

    /**
     * 商户审核
     */
    @PostMapping("/audit")
    public Result<MerchantAuditVO> audit(@Valid @RequestBody MerchantAuditDTO dto) {
        return Result.success(payMerchantService.audit(dto));
    }

    /**
     * 启用商户
     */
    @PutMapping("/{merchantNo}/enable")
    public Result<Void> enable(@PathVariable String merchantNo) {
        payMerchantService.enable(merchantNo);
        return Result.success();
    }

    /**
     * 禁用商户
     */
    @PutMapping("/{merchantNo}/disable")
    public Result<Void> disable(@PathVariable String merchantNo) {
        payMerchantService.disable(merchantNo);
        return Result.success();
    }

    /**
     * 商户详情（含资金账户）
     */
    @GetMapping("/{merchantNo}")
    public Result<MerchantDetailVO> detail(@PathVariable String merchantNo) {
        return Result.success(payMerchantService.detail(merchantNo));
    }

    /**
     * 商户分页列表
     */
    @GetMapping("/list")
    public Result<IPage<MerchantListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(payMerchantService.listPage(page, pageSize, merchantName, status, auditStatus));
    }
}
