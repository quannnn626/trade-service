package com.boot.pay.channel.controller;

import com.boot.common.result.Result;
import com.boot.pay.channel.vo.ChannelListVO;
import com.boot.pay.service.PayPaymentChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 支付渠道管理接口（运营后台）
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelAdminController {

    private final PayPaymentChannelService payPaymentChannelService;

    /**
     * 支付渠道列表
     */
    @GetMapping("/list")
    public Result<List<ChannelListVO>> list() {
        return Result.success(payPaymentChannelService.listAll());
    }
}
