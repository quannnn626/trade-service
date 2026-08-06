package com.boot.pay.merchant.controller;

import com.boot.common.result.Result;
import com.boot.pay.merchant.dto.MerchantApplyDTO;
import com.boot.pay.merchant.vo.MerchantApplyVO;
import com.boot.pay.service.PayMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
