package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.channel.enums.ChannelStatusEnum;
import com.boot.pay.channel.vo.ChannelListVO;
import com.boot.pay.domain.PayPaymentChannel;
import com.boot.pay.service.PayPaymentChannelService;
import com.boot.pay.mapper.PayPaymentChannelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author quannnn
* @description 针对表【pay_payment_channel(支付渠道表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Slf4j
@Service
public class PayPaymentChannelServiceImpl extends ServiceImpl<PayPaymentChannelMapper, PayPaymentChannel>
    implements PayPaymentChannelService{

    @Override
    public List<ChannelListVO> listAll() {
        List<PayPaymentChannel> channels = list(new LambdaQueryWrapper<PayPaymentChannel>()
                .orderByAsc(PayPaymentChannel::getId));
        return channels.stream().map(o -> ChannelListVO.builder()
                        .id(o.getId())
                        .channelCode(o.getChannelCode())
                        .channelName(o.getChannelName())
                        .status(o.getStatus())
                        .statusName(ChannelStatusEnum.of(o.getStatus()) != null
                                ? ChannelStatusEnum.of(o.getStatus()).getDesc() : "未知")
                        .createTime(o.getCreateTime() != null ? o.getCreateTime().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void enable(String channelCode) {
        PayPaymentChannel channel = getOne(new LambdaQueryWrapper<PayPaymentChannel>()
                .eq(PayPaymentChannel::getChannelCode, channelCode));
        if (channel == null) {
            throw new BusinessException("支付渠道不存在: " + channelCode);
        }
        if (ChannelStatusEnum.ENABLE.getCode().equals(channel.getStatus())) {
            return;
        }
        boolean updated = lambdaUpdate()
                .eq(PayPaymentChannel::getChannelCode, channelCode)
                .set(PayPaymentChannel::getStatus, ChannelStatusEnum.ENABLE.getCode())
                .update();
        if (!updated) {
            throw new BusinessException("启用渠道失败: " + channelCode);
        }
        log.info("支付渠道已启用: channelCode={}", channelCode);
    }

    @Override
    public void disable(String channelCode) {
        PayPaymentChannel channel = getOne(new LambdaQueryWrapper<PayPaymentChannel>()
                .eq(PayPaymentChannel::getChannelCode, channelCode));
        if (channel == null) {
            throw new BusinessException("支付渠道不存在: " + channelCode);
        }
        if (ChannelStatusEnum.DISABLE.getCode().equals(channel.getStatus())) {
            return;
        }
        boolean updated = lambdaUpdate()
                .eq(PayPaymentChannel::getChannelCode, channelCode)
                .set(PayPaymentChannel::getStatus, ChannelStatusEnum.DISABLE.getCode())
                .update();
        if (!updated) {
            throw new BusinessException("停用渠道失败: " + channelCode);
        }
        log.info("支付渠道已停用: channelCode={}", channelCode);
    }
}




