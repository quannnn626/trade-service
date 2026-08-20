package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.utils.SignUtil;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.domain.PayPaymentNotify;
import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.mapper.PayPaymentNotifyMapper;
import com.boot.pay.mapper.PayPaymentOrderMapper;
import com.boot.pay.service.PayPaymentNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调通知 Service 实现
 *
 * @author quannnn
 * @description 针对表【pay_payment_notify(支付回调通知记录表)】的数据库操作Service实现
 * @createDate 2026-08-03 12:26:43
 */
@Slf4j
@Service
public class PayPaymentNotifyServiceImpl extends ServiceImpl<PayPaymentNotifyMapper, PayPaymentNotify>
        implements PayPaymentNotifyService {

    /**
     * 退避间隔（分钟）：第 i 次失败后的等待时长
     */
    private static final int[] RETRY_DELAY_MINUTES = {1, 2, 5, 10, 30, 60, 120, 360, 720};

    /**
     * 回调 HTTP 超时时间（毫秒）
     */
    private static final int NOTIFY_TIMEOUT_MS = 10_000;

    @Resource
    private PayPaymentOrderMapper payPaymentOrderMapper;

    @Resource
    private PayMerchantMapper payMerchantMapper;

    @Override
    public void triggerNotify(String paymentNo) {
        PayPaymentOrder order = payPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, paymentNo));
        if (order == null) {
            log.warn("回调触发失败：订单不存在 paymentNo={}", paymentNo);
            return;
        }
        if (StrUtil.isBlank(order.getNotifyUrl())) {
            log.warn("回调触发失败：商户未配置回调地址 paymentNo={} merchantId={}",
                    paymentNo, order.getMerchantId());
            return;
        }

        // 创建待通知记录，立即执行
        PayPaymentNotify record = new PayPaymentNotify();
        record.setPaymentNo(paymentNo);
        record.setMerchantId(order.getMerchantId());
        record.setNotifyUrl(order.getNotifyUrl());
        record.setNotifyType(1); // 1-支付成功
        record.setNotifyStatus(0); // 0-待通知
        record.setRetryCount(0);
        record.setMaxRetry(10);
        record.setNextRetryTime(new Date());
        save(record);

        log.info("创建回调通知记录并立即发送 paymentNo={} notifyUrl={}", paymentNo, order.getNotifyUrl());
        sendNotify(record);
    }

    /**
     * 发送一次回调通知，并按结果更新通知记录
     *
     * @param record 通知记录（内部更新后落库）
     */
    private void sendNotify(PayPaymentNotify record) {
        PayPaymentOrder order = payPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, record.getPaymentNo()));
        if (order == null) {
            log.warn("回调发送失败：订单不存在 paymentNo={}", record.getPaymentNo());
            markFailed(record, "订单不存在");
            return;
        }
        PayMerchant merchant = payMerchantMapper.selectById(record.getMerchantId());
        if (merchant == null || StrUtil.isBlank(merchant.getAppSecret())) {
            log.warn("回调发送失败：商户或签名密钥不存在 merchantId={}", record.getMerchantId());
            markFailed(record, "商户 appSecret 不存在");
            return;
        }
        if (StrUtil.isBlank(record.getNotifyUrl())) {
            markFailed(record, "未配置回调地址");
            return;
        }

        // 组装参数并签名（HMAC-SHA256，复用 SignUtil）
        Map<String, Object> params = new HashMap<>();
        params.put("notifyType", "PAY_SUCCESS");
        params.put("paymentNo", order.getPaymentNo());
        params.put("orderNo", order.getOrderNo());
        params.put("tradeStatus", "SUCCESS");
        params.put("amount", order.getAmount());
        if (order.getPayTime() != null) {
            params.put("payTime", DateUtil.formatDateTime(order.getPayTime()));
        }
        if (StrUtil.isNotBlank(order.getAttach())) {
            params.put("attach", order.getAttach());
        }
        params.put("sign", SignUtil.generateSign(params, merchant.getAppSecret()));
        String requestBody = JSON.toJSONString(params);

        // 发送（JSON，10 秒超时）
        String responseBody = null;
        int httpStatus = -1;
        try {
            HttpResponse response = HttpRequest.post(record.getNotifyUrl())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .timeout(NOTIFY_TIMEOUT_MS)
                    .execute();
            httpStatus = response.getStatus();
            responseBody = response.body();
        } catch (Exception e) {
            log.warn("回调 HTTP 发送异常 paymentNo={} notifyUrl={} 原因: {}",
                    record.getPaymentNo(), record.getNotifyUrl(), e.getMessage());
            responseBody = e.getMessage();
        }
        record.setRequestData(requestBody);
        record.setResponseData(responseBody);

        // 判定成功：HTTP 200 且 body JSON code == 0（与平台 Result 成功码约定一致）
        if (httpStatus == 200 && isSuccessCode(responseBody)) {
            record.setNotifyStatus(1); // 1-成功
            record.setNextRetryTime(null);
            updateById(record);
            log.info("回调通知成功 paymentNo={} notifyUrl={}", record.getPaymentNo(), record.getNotifyUrl());
            return;
        }

        log.warn("回调通知失败 paymentNo={} httpStatus={} response={}",
                record.getPaymentNo(), httpStatus, responseBody);
        markFailed(record, "HTTP " + httpStatus + " 响应: " + StrUtil.maxLength(responseBody, 500));
    }

    /**
     * 判定回调响应成功：body 为 JSON 且 code == 0（与平台 Result 成功码约定一致）
     */
    private boolean isSuccessCode(String responseBody) {
        if (StrUtil.isBlank(responseBody)) {
            return false;
        }
        try {
            JSONObject json = JSON.parseObject(responseBody);
            return json != null && json.getIntValue("code") == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 标记失败：重试 +1、退避算下次时间；达上限标记人工介入
     */
    private void markFailed(PayPaymentNotify record, String errorMsg) {
        int retryCount = record.getRetryCount() == null ? 0 : record.getRetryCount() + 1;
        record.setRetryCount(retryCount);
        record.setLastError(errorMsg);

        int maxRetry = record.getMaxRetry() == null ? 10 : record.getMaxRetry();
        if (retryCount >= maxRetry) {
            record.setNotifyStatus(2); // 2-失败（达上限）
            record.setNextRetryTime(null);
            log.error("回调通知达重试上限，待人工介入 paymentNo={} retryCount={} error={}",
                    record.getPaymentNo(), retryCount, errorMsg);
        } else {
            int delayMinutes = RETRY_DELAY_MINUTES[Math.min(retryCount - 1, RETRY_DELAY_MINUTES.length - 1)];
            record.setNextRetryTime(DateUtil.offsetMinute(new Date(), delayMinutes));
            log.info("回调通知失败，{} 分钟后重试 paymentNo={} retryCount={} error={}",
                    delayMinutes, record.getPaymentNo(), retryCount, errorMsg);
        }
        updateById(record);
    }
}
