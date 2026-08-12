package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.domain.PayPaymentChannel;
import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.mapper.PayPaymentChannelMapper;
import com.boot.pay.mapper.PayPaymentOrderMapper;
import com.boot.pay.payment.dto.CreatePayDTO;
import com.boot.pay.payment.enums.PayStatusEnum;
import com.boot.pay.payment.vo.CreatePayVO;
import com.boot.pay.payment.vo.PayOrderVO;
import com.boot.pay.service.PayPaymentOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
* @author quannnn
* @description 针对表【pay_payment_order(支付订单表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Slf4j
@Service
public class PayPaymentOrderServiceImpl extends ServiceImpl<PayPaymentOrderMapper, PayPaymentOrder>
        implements PayPaymentOrderService {

    @Resource
    private PayMerchantMapper payMerchantMapper;

    @Resource
    private PayPaymentChannelMapper payPaymentChannelMapper;

    @Override
    public CreatePayVO createPayment(CreatePayDTO dto, Long merchantId, String merchantNo, String clientIp) {
        PayMerchant merchant = payMerchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException("商户已禁用");
        }

        // 幂等：同商户同订单号已存在则直接返回
        PayPaymentOrder exist = baseMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getMerchantId, merchantId)
                        .eq(PayPaymentOrder::getMerchantPaymentNo, dto.getOrderNo())
        );
        if (exist != null) {
            log.info("订单已存在，直接返回 paymentNo={} orderNo={}", exist.getPaymentNo(), dto.getOrderNo());
            return buildVO(exist);
        }

        if (merchant.getSingleLimit() != null
                && dto.getAmount().compareTo(merchant.getSingleLimit()) > 0) {
            throw new BusinessException("超出商户单笔交易限额：" + merchant.getSingleLimit() + " 元");
        }

        // 校验支付渠道是否可用
        PayPaymentChannel channel = payPaymentChannelMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentChannel>()
                        .eq(PayPaymentChannel::getChannelCode, dto.getChannelCode())
        );
        if (channel == null) {
            throw new BusinessException("支付渠道不存在：" + dto.getChannelCode());
        }
        if (channel.getStatus() == null || channel.getStatus() != 1) {
            throw new BusinessException("支付渠道未启用：" + dto.getChannelCode());
        }

        // 生成支付流水号：PAY + 日期 + Snowflake后10位
        String paymentNo = "PAY"
                + DateUtil.format(new Date(), "yyyyMMdd")
                + String.valueOf(IdUtil.getSnowflake(1, 1).nextId()).substring(10);

        int expireMinutes = dto.getExpireMinutes() != null && dto.getExpireMinutes() > 0
                ? dto.getExpireMinutes() : 30;
        Date timeoutExpire = DateUtil.offsetMinute(new Date(), expireMinutes);

        // 手续费 = 金额 * 商户费率，结算金额 = 金额 - 手续费
        BigDecimal feeRate = merchant.getSettleFeeRate() != null
                ? merchant.getSettleFeeRate() : BigDecimal.ZERO;
        BigDecimal feeAmount = dto.getAmount().multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal settleAmount = dto.getAmount().subtract(feeAmount);

        PayPaymentOrder order = new PayPaymentOrder();
        order.setPaymentNo(paymentNo);
        order.setMerchantId(merchantId);
        order.setOrderNo(dto.getOrderNo());
        order.setMerchantPaymentNo(dto.getOrderNo());
        order.setUserId(null);
        order.setAmount(dto.getAmount());
        order.setStatus(PayStatusEnum.WAIT_PAY.getCode());
        order.setChannelId(channel.getId());
        order.setClientIp(clientIp);
        order.setSubject(dto.getSubject());
        order.setDescription(dto.getDescription());
        // 订单级回调优先，未填则用商户默认回调
        order.setNotifyUrl(dto.getNotifyUrl() != null && !dto.getNotifyUrl().isBlank()
                ? dto.getNotifyUrl() : merchant.getNotifyUrl());
        order.setReturnUrl(dto.getReturnUrl());
        order.setAttach(dto.getAttach());
        order.setExpireTime(timeoutExpire);
        order.setTimeoutExpire(timeoutExpire);
        order.setFeeAmount(feeAmount);
        order.setSettleAmount(settleAmount);
        order.setSettleStatus(0);

        baseMapper.insert(order);
        log.info("创建支付订单成功 paymentNo={} orderNo={} amount={} fee={}",
                paymentNo, dto.getOrderNo(), dto.getAmount(), feeAmount);

        return buildVO(order);
    }

    @Override
    public PayOrderVO queryByPaymentNo(String paymentNo, Long merchantId) {
        PayPaymentOrder order = baseMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, paymentNo)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权查看该订单");
        }
        return buildOrderVO(order);
    }

    @Override
    public PayOrderVO queryByOrderNo(String orderNo, Long merchantId) {
        PayPaymentOrder order = baseMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getMerchantId, merchantId)
                        .eq(PayPaymentOrder::getMerchantPaymentNo, orderNo)
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return buildOrderVO(order);
    }

    private PayOrderVO buildOrderVO(PayPaymentOrder order) {
        PayStatusEnum statusEnum = PayStatusEnum.of(order.getStatus());

        // 查询渠道名称
        String channelName = null;
        if (order.getChannelId() != null) {
            PayPaymentChannel channel = payPaymentChannelMapper.selectById(order.getChannelId());
            if (channel != null) {
                channelName = channel.getChannelName();
            }
        }

        return PayOrderVO.builder()
                .paymentNo(order.getPaymentNo())
                .orderNo(order.getOrderNo())
                .amount(order.getAmount())
                .feeAmount(order.getFeeAmount())
                .settleAmount(order.getSettleAmount())
                .status(order.getStatus())
                .statusDesc(statusEnum != null ? statusEnum.getDesc() : "未知")
                .subject(order.getSubject())
                .description(order.getDescription())
                .channelName(channelName)
                .clientIp(order.getClientIp())
                .notifyUrl(order.getNotifyUrl())
                .attach(order.getAttach())
                .expireTime(order.getExpireTime())
                .payTime(order.getPayTime())
                .createTime(order.getCreateTime())
                .build();
    }

    private CreatePayVO buildVO(PayPaymentOrder order) {
        PayStatusEnum statusEnum = PayStatusEnum.of(order.getStatus());
        return CreatePayVO.builder()
                .paymentNo(order.getPaymentNo())
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusDesc(statusEnum != null ? statusEnum.getDesc() : "未知")
                .expireTime(order.getExpireTime())
                .build();
    }
}
