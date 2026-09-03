package com.boot.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.channel.vo.ChannelListVO;
import com.boot.pay.domain.PayPaymentChannel;

import java.util.List;

/**
* @author quannnn
* @description 针对表【pay_payment_channel(支付渠道表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayPaymentChannelService extends IService<PayPaymentChannel> {

    /**
     * 支付渠道列表（运营后台），按 ID 升序返回
     */
    List<ChannelListVO> listAll();

    /**
     * 启用支付渠道（已启用时幂等返回）
     *
     * @param channelCode 渠道编码
     */
    void enable(String channelCode);

    /**
     * 停用支付渠道（已停用时幂等返回）。
     * 停用后商户新创建的支付订单不可再选该渠道；存量订单不受影响。
     *
     * @param channelCode 渠道编码
     */
    void disable(String channelCode);

}
