package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.common.utils.SignUtil;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.domain.PayPaymentNotify;
import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.domain.PayRefundOrder;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.mapper.PayPaymentNotifyMapper;
import com.boot.pay.mapper.PayPaymentOrderMapper;
import com.boot.pay.mapper.PayRefundOrderMapper;
import com.boot.pay.notify.enums.NotifyStatusEnum;
import com.boot.pay.notify.enums.NotifyTypeEnum;
import com.boot.pay.notify.vo.NotifyListVO;
import com.boot.pay.refund.enums.RefundStatusEnum;
import com.boot.pay.service.PayPaymentNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private PayRefundOrderMapper payRefundOrderMapper;

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
        record.setNotifyType(NotifyTypeEnum.PAY_SUCCESS.getCode());
        record.setNotifyStatus(NotifyStatusEnum.WAIT.getCode());
        record.setRetryCount(0);
        record.setMaxRetry(10);
        record.setNextRetryTime(new Date());
        save(record);

        log.info("创建回调通知记录并立即发送 paymentNo={} notifyUrl={}", paymentNo, order.getNotifyUrl());
        sendNotify(record);
    }

    @Override
    public void triggerRefundNotify(String refundNo) {
        PayRefundOrder refund = payRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<PayRefundOrder>()
                        .eq(PayRefundOrder::getRefundNo, refundNo));
        if (refund == null) {
            log.warn("退款回调触发失败：退款单不存在 refundNo={}", refundNo);
            return;
        }
        PayPaymentOrder order = payPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, refund.getPaymentNo()));
        if (order == null || StrUtil.isBlank(order.getNotifyUrl())) {
            log.warn("退款回调触发失败：订单或回调地址不存在 refundNo={} paymentNo={}",
                    refundNo, refund.getPaymentNo());
            return;
        }

        // 创建退款通知记录，立即执行（退款回调复用支付回调的回调地址与重试机制）
        PayPaymentNotify record = new PayPaymentNotify();
        record.setPaymentNo(order.getPaymentNo());
        record.setMerchantId(order.getMerchantId());
        record.setNotifyUrl(order.getNotifyUrl());
        record.setNotifyType(NotifyTypeEnum.REFUND_SUCCESS.getCode());
        record.setNotifyStatus(NotifyStatusEnum.WAIT.getCode());
        record.setRetryCount(0);
        record.setMaxRetry(10);
        record.setNextRetryTime(new Date());
        save(record);

        log.info("创建退款回调通知记录并立即发送 refundNo={} notifyUrl={}", refundNo, order.getNotifyUrl());
        sendRefundNotify(record);
    }

    @Override
    public void retryNotify() {
        // 扫描到期的待通知记录，最早到期优先
        List<PayPaymentNotify> waitList = baseMapper.selectList(
                new LambdaQueryWrapper<PayPaymentNotify>()
                        .eq(PayPaymentNotify::getNotifyStatus, NotifyStatusEnum.WAIT.getCode())
                        .le(PayPaymentNotify::getNextRetryTime, new Date())
                        .orderByAsc(PayPaymentNotify::getNextRetryTime)
                        .last("LIMIT 100"));
        if (waitList.isEmpty()) {
            return;
        }
        log.info("回调重试任务扫描到 {} 条待重试通知", waitList.size());
        for (PayPaymentNotify record : waitList) {
            try {
                // 按通知类型分派：支付回调与退款回调参数不同，必须走各自的组装逻辑
                if (NotifyTypeEnum.PAY_SUCCESS.getCode().equals(record.getNotifyType())) {
                    sendNotify(record);
                } else {
                    sendRefundNotify(record);
                }
            } catch (Exception e) {
                log.error("回调重试发送异常 paymentNo={} 原因: {}",
                        record.getPaymentNo(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void retry(Long notifyId) {
        PayPaymentNotify record = getById(notifyId);
        if (record == null) {
            throw new BusinessException("通知记录不存在: " + notifyId);
        }
        if (NotifyStatusEnum.SUCCESS.getCode().equals(record.getNotifyStatus())) {
            throw new BusinessException("该通知已发送成功，无需重试");
        }
        // 重置重试状态后立即发送一次，失败后由 markFailed 重新排期自动重试
        record.setNotifyStatus(NotifyStatusEnum.WAIT.getCode());
        record.setRetryCount(0);
        record.setNextRetryTime(null);
        record.setLastError(null);
        updateById(record);

        log.info("手动重试回调通知 notifyId={} paymentNo={} notifyType={}",
                notifyId, record.getPaymentNo(), record.getNotifyType());
        if (NotifyTypeEnum.PAY_SUCCESS.getCode().equals(record.getNotifyType())) {
            sendNotify(record);
        } else {
            sendRefundNotify(record);
        }
    }

    @Override
    public IPage<NotifyListVO> listPage(Integer page, Integer pageSize, String paymentNo, String merchantNo,
                                        Integer notifyType, Integer notifyStatus) {
        LambdaQueryWrapper<PayPaymentNotify> wrapper = new LambdaQueryWrapper<>();
        if (paymentNo != null && !paymentNo.isBlank()) {
            wrapper.like(PayPaymentNotify::getPaymentNo, paymentNo);
        }
        if (merchantNo != null && !merchantNo.isBlank()) {
            // 通知表只有 merchant_id，先按商户号模糊查出 ID 集合
            List<Long> merchantIds = payMerchantMapper.selectList(
                            new LambdaQueryWrapper<PayMerchant>()
                                    .like(PayMerchant::getMerchantNo, merchantNo)
                                    .select(PayMerchant::getId))
                    .stream().map(PayMerchant::getId).collect(Collectors.toList());
            if (merchantIds.isEmpty()) {
                // 无匹配商户，直接返回空页
                Page<NotifyListVO> empty = new Page<>(page, pageSize);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(PayPaymentNotify::getMerchantId, merchantIds);
        }
        if (notifyType != null) {
            wrapper.eq(PayPaymentNotify::getNotifyType, notifyType);
        }
        if (notifyStatus != null) {
            wrapper.eq(PayPaymentNotify::getNotifyStatus, notifyStatus);
        }
        wrapper.orderByDesc(PayPaymentNotify::getCreateTime);

        Page<PayPaymentNotify> result = this.page(new Page<>(page, pageSize), wrapper);

        // 批量回填商户编号/名称
        Map<Long, PayMerchant> merchantMap = buildMerchantMap(result.getRecords());

        return result.convert(o -> {
            NotifyTypeEnum typeEnum = NotifyTypeEnum.of(o.getNotifyType());
            NotifyStatusEnum statusEnum = NotifyStatusEnum.of(o.getNotifyStatus());
            PayMerchant merchant = merchantMap.get(o.getMerchantId());
            return NotifyListVO.builder()
                    .id(o.getId())
                    .paymentNo(o.getPaymentNo())
                    .merchantNo(merchant != null ? merchant.getMerchantNo() : null)
                    .merchantName(merchant != null ? merchant.getMerchantName() : null)
                    .notifyUrl(o.getNotifyUrl())
                    .notifyType(o.getNotifyType())
                    .notifyTypeName(typeEnum != null ? typeEnum.getDesc() : "未知")
                    .notifyStatus(o.getNotifyStatus())
                    .notifyStatusName(statusEnum != null ? statusEnum.getDesc() : "未知")
                    .retryCount(o.getRetryCount())
                    .maxRetry(o.getMaxRetry())
                    .nextRetryTime(o.getNextRetryTime() != null ? o.getNextRetryTime().toString() : null)
                    .lastError(o.getLastError())
                    .requestData(o.getRequestData())
                    .responseData(o.getResponseData())
                    .createTime(o.getCreateTime() != null ? o.getCreateTime().toString() : null)
                    .updateTime(o.getUpdateTime() != null ? o.getUpdateTime().toString() : null)
                    .build();
        });
    }

    /**
     * 批量查询通知记录所属商户，按商户ID组装 Map
     */
    private Map<Long, PayMerchant> buildMerchantMap(List<PayPaymentNotify> notifies) {
        Set<Long> merchantIds = notifies.stream()
                .map(PayPaymentNotify::getMerchantId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return payMerchantMapper.selectBatchIds(merchantIds).stream()
                .collect(Collectors.toMap(PayMerchant::getId, m -> m));
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
            record.setNotifyStatus(NotifyStatusEnum.SUCCESS.getCode());
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
     * 发送一次退款回调通知，并按结果更新通知记录
     * <p>
     * 通知记录按 paymentNo 关联退款单（表结构无 refund_no 列），取最近成功的一笔作为通知内容。
     *
     * @param record 通知记录（内部更新后落库）
     */
    private void sendRefundNotify(PayPaymentNotify record) {
        PayRefundOrder refund = payRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<PayRefundOrder>()
                        .eq(PayRefundOrder::getPaymentNo, record.getPaymentNo())
                        .eq(PayRefundOrder::getStatus, RefundStatusEnum.SUCCESS.getCode())
                        .orderByDesc(PayRefundOrder::getCreateTime)
                        .last("LIMIT 1"));
        if (refund == null) {
            log.warn("退款回调发送失败：无成功退款单 paymentNo={}", record.getPaymentNo());
            markFailed(record, "无成功退款单");
            return;
        }
        PayPaymentOrder order = payPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, record.getPaymentNo()));
        if (order == null) {
            markFailed(record, "订单不存在");
            return;
        }
        PayMerchant merchant = payMerchantMapper.selectById(record.getMerchantId());
        if (merchant == null || StrUtil.isBlank(merchant.getAppSecret())) {
            log.warn("退款回调发送失败：商户或签名密钥不存在 merchantId={}", record.getMerchantId());
            markFailed(record, "商户 appSecret 不存在");
            return;
        }
        if (StrUtil.isBlank(record.getNotifyUrl())) {
            markFailed(record, "未配置回调地址");
            return;
        }

        // 组装退款回调参数并签名（HMAC-SHA256，复用 SignUtil）
        Map<String, Object> params = new HashMap<>();
        params.put("notifyType", "REFUND_SUCCESS");
        params.put("refundNo", refund.getRefundNo());
        params.put("paymentNo", order.getPaymentNo());
        params.put("orderNo", order.getOrderNo());
        params.put("tradeStatus", "SUCCESS");
        params.put("refundAmount", refund.getActualAmount());
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
            log.warn("退款回调 HTTP 发送异常 refundNo={} notifyUrl={} 原因: {}",
                    refund.getRefundNo(), record.getNotifyUrl(), e.getMessage());
            responseBody = e.getMessage();
        }
        record.setRequestData(requestBody);
        record.setResponseData(responseBody);

        // 判定成功：HTTP 200 且 body JSON code == 0（与平台 Result 成功码约定一致）
        if (httpStatus == 200 && isSuccessCode(responseBody)) {
            record.setNotifyStatus(NotifyStatusEnum.SUCCESS.getCode());
            record.setNextRetryTime(null);
            updateById(record);
            log.info("退款回调通知成功 refundNo={} notifyUrl={}",
                    refund.getRefundNo(), record.getNotifyUrl());
            return;
        }

        log.warn("退款回调通知失败 refundNo={} httpStatus={} response={}",
                refund.getRefundNo(), httpStatus, responseBody);
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
            record.setNotifyStatus(NotifyStatusEnum.FAILED.getCode());
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
