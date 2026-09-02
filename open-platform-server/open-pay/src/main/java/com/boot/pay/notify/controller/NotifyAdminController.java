package com.boot.pay.notify.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.common.result.Result;
import com.boot.pay.notify.vo.NotifyListVO;
import com.boot.pay.service.PayPaymentNotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回调通知管理接口（运营后台）
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotifyAdminController {

    private final PayPaymentNotifyService payPaymentNotifyService;

    /**
     * 回调通知分页列表
     * 筛选：支付单号（模糊）/商户号（模糊）/通知类型/通知状态
     */
    @GetMapping("/list")
    public Result<IPage<NotifyListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String paymentNo,
            @RequestParam(required = false) String merchantNo,
            @RequestParam(required = false) Integer notifyType,
            @RequestParam(required = false) Integer notifyStatus) {
        return Result.success(payPaymentNotifyService.listPage(page, pageSize, paymentNo, merchantNo,
                notifyType, notifyStatus));
    }
}
