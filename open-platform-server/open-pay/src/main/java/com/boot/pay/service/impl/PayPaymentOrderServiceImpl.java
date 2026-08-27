package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.account.constants.AccountConstants;
import com.boot.pay.account.enums.AccountFlowTypeEnum;
import com.boot.pay.account.enums.AccountStatusEnum;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.domain.PayMerchantAccount;
import com.boot.pay.domain.PayPaymentChannel;
import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.mapper.PayMerchantAccountMapper;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.mapper.PayPaymentChannelMapper;
import com.boot.pay.mapper.PayPaymentOrderMapper;
import com.boot.pay.mapper.PayUserAccountMapper;
import com.boot.pay.payment.dto.CreatePayDTO;
import com.boot.pay.payment.dto.ExecutePayDTO;
import com.boot.pay.payment.enums.PayStatusEnum;
import com.boot.pay.payment.exception.PayOptimisticLockException;
import com.boot.pay.payment.vo.CreatePayVO;
import com.boot.pay.payment.vo.ExecutePayVO;
import com.boot.pay.payment.vo.PayOrderListVO;
import com.boot.pay.payment.vo.PayOrderVO;
import com.boot.pay.service.PayAccountFlowService;
import com.boot.pay.service.PayPaymentNotifyService;
import com.boot.pay.service.PayPaymentOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    private PayUserAccountMapper payUserAccountMapper;

    @Resource
    private PayMerchantAccountMapper payMerchantAccountMapper;

    @Resource
    private PayAccountFlowService payAccountFlowService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private PayPaymentNotifyService payPaymentNotifyService;

    /**
     * 自注入调用事务方法：锁必须等事务提交后再释放，跨 bean 调用才能让 @Transactional 生效
     */
    @Resource
    @Lazy
    private PayPaymentOrderService self;

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

    @Override
    public int closeExpiredOrders() {
        // 批量关闭 status=WAIT_PAY 且已过超时时间的订单
        Date now = new Date();
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getStatus, PayStatusEnum.WAIT_PAY.getCode())
                .lt(PayPaymentOrder::getTimeoutExpire, now)
                .set(PayPaymentOrder::getStatus, PayStatusEnum.CLOSED.getCode())
                .set(PayPaymentOrder::getCloseTime, now)
                .set(PayPaymentOrder::getCloseReason, "超时关闭"));
        log.info("超时关单任务执行完成，关闭订单数: {}", rows);
        return rows;
    }

    @Override
    public ExecutePayVO executePayment(ExecutePayDTO dto, Long merchantId) {
        // 支付密码校验（锁外校验，失败不占用锁）
        PayUserAccount account = payUserAccountMapper.selectOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, dto.getUserId()));
        if (account == null) {
            throw new BusinessException("用户账户不存在");
        }
        if (account.getPayPassword() == null || account.getPayPassword().isEmpty()) {
            throw new BusinessException("请先设置支付密码");
        }
        if (!BCrypt.checkpw(dto.getPayPassword(), account.getPayPassword())) {
            throw new BusinessException("支付密码错误");
        }

        // 获取分布式锁，同一订单禁止并发执行
        RLock lock = redissonClient.getLock("pay:lock:" + dto.getPaymentNo());
        boolean locked = false;
        ExecutePayVO result = null;
        try {
            try {
                locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new BusinessException("获取锁被中断，请稍后重试");
            }
            if (!locked) {
                throw new BusinessException("订单处理中，请勿重复操作");
            }
            // 乐观锁冲突重试（最多 3 次）：事务方法整体重跑，每次都是新事务、读到最新版本号
            for (int i = 0; i < 3; i++) {
                try {
                    result = self.executePaymentTx(dto, merchantId);
                    break;
                } catch (PayOptimisticLockException e) {
                    log.warn("支付乐观锁冲突 paymentNo={} 第{}次重试，原因: {}",
                            dto.getPaymentNo(), i + 1, e.getMessage());
                    if (i == 2) {
                        throw new BusinessException("账户变动频繁，请稍后重试");
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("系统异常，请稍后重试");
                    }
                }
            }
        } finally {
            // 事务提交后释放锁（self.executePaymentTx 返回即事务已提交）
            if (locked) {
                lock.unlock();
            }
        }
        // 事务已提交、锁已释放：触发回调通知（HTTP 最长 10 秒，不能在锁内执行；失败不影响支付结果，由重试任务兜底）
        if (result != null) {
            try {
                payPaymentNotifyService.triggerNotify(result.getPaymentNo());
            } catch (Exception e) {
                log.error("触发回调通知异常 paymentNo={}", result.getPaymentNo(), e);
            }
            return result;
        }
        throw new BusinessException("系统异常，请稍后重试");
    }

    /**
     * 支付资金操作（独立事务）：动账必须全部成功或全部回滚
     * <p>
     * 只在 executePayment 锁内调用，不对外暴露。
     */
    @Transactional(rollbackFor = Exception.class)
    public ExecutePayVO executePaymentTx(ExecutePayDTO dto, Long merchantId) {
        // ① 订单校验：存在性 / 归属 / 状态机 / 过期
        PayPaymentOrder order = baseMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, dto.getPaymentNo()));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!PayStatusEnum.WAIT_PAY.getCode().equals(order.getStatus())) {
            PayStatusEnum statusEnum = PayStatusEnum.of(order.getStatus());
            throw new BusinessException("订单状态异常（当前：" + (statusEnum != null ? statusEnum.getDesc() : order.getStatus()) + "）");
        }
        if (order.getExpireTime() != null && order.getExpireTime().before(new Date())) {
            throw new BusinessException("订单已过期");
        }

        // ② 用户账户校验
        PayUserAccount userAccount = payUserAccountMapper.selectOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, dto.getUserId()));
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }
        if (AccountStatusEnum.FROZEN.getCode().equals(userAccount.getStatus())) {
            throw new BusinessException("账户已被冻结");
        }

        // ③ 商户账户校验
        PayMerchantAccount merchantAccount = payMerchantAccountMapper.selectOne(
                new LambdaQueryWrapper<PayMerchantAccount>()
                        .eq(PayMerchantAccount::getMerchantId, order.getMerchantId()));
        if (merchantAccount == null) {
            throw new BusinessException("商户账户异常");
        }
        if (!AccountStatusEnum.NORMAL.getCode().equals(merchantAccount.getStatus())) {
            throw new BusinessException("商户账户不可用");
        }

        // ④ 余额校验：可用余额 = balance - frozen_amount
        BigDecimal available = nvl(userAccount.getBalance()).subtract(nvl(userAccount.getFrozenAmount()));
        if (available.compareTo(order.getAmount()) < 0) {
            throw new BusinessException("余额不足");
        }

        // ⑤ 用户日限额校验（跨天重置由阶段十三定时任务处理）
        if (nvl(userAccount.getDailyUsed()).add(order.getAmount())
                .compareTo(nvl(userAccount.getDailyLimit())) > 0) {
            throw new BusinessException("超出单日支付限额");
        }

        Date now = new Date();

        // ⑥ 更新订单为 PAYING（条件更新 + 状态机，防重复支付）
        int payRows = baseMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, order.getPaymentNo())
                .eq(PayPaymentOrder::getStatus, PayStatusEnum.WAIT_PAY.getCode())
                .set(PayPaymentOrder::getStatus, PayStatusEnum.PAYING.getCode())
                .set(PayPaymentOrder::getUserId, userAccount.getUserId())
                .set(PayPaymentOrder::getPayTime, now));
        if (payRows == 0) {
            throw new BusinessException("订单状态已变更");
        }

        // ⑦ 扣减用户余额（乐观锁 + balance >= amount 二次兜底）
        int userRows = payUserAccountMapper.deductBalance(
                userAccount.getUserId(), userAccount.getVersion(), order.getAmount());
        if (userRows == 0) {
            throw new PayOptimisticLockException("用户账户余额变动");
        }

        // ⑧ 增加商户余额（乐观锁，按结算金额入账）
        int merchantRows = payMerchantAccountMapper.addBalance(
                order.getMerchantId(), merchantAccount.getVersion(), order.getSettleAmount());
        if (merchantRows == 0) {
            throw new PayOptimisticLockException("商户账户变动");
        }

        // ⑨ 写入资金流水 ×2（用户支出 + 商户收入），记录变更前后余额
        PayUserAccount userAfter = payUserAccountMapper.selectById(userAccount.getId());
        PayMerchantAccount merchantAfter = payMerchantAccountMapper.selectById(merchantAccount.getId());
        payAccountFlowService.recordFlow(AccountConstants.ACCOUNT_TYPE_USER, userAccount.getId(), order.getPaymentNo(),
                AccountFlowTypeEnum.EXPENSE.getCode(), order.getAmount().negate(),
                userAccount.getBalance(), userAfter.getBalance(), "支付消费-" + order.getSubject());
        payAccountFlowService.recordFlow(AccountConstants.ACCOUNT_TYPE_MERCHANT, merchantAccount.getId(), order.getPaymentNo(),
                AccountFlowTypeEnum.INCOME.getCode(), order.getSettleAmount(),
                merchantAccount.getBalance(), merchantAfter.getBalance(), "收款-" + order.getSubject());

        // ⑩ 更新订单为 SUCCESS
        baseMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, order.getPaymentNo())
                .set(PayPaymentOrder::getStatus, PayStatusEnum.SUCCESS.getCode())
                .set(PayPaymentOrder::getPayTime, now));

        log.info("支付成功 paymentNo={} orderNo={} userId={} merchantId={} amount={} fee={} settle={}",
                order.getPaymentNo(), order.getOrderNo(), userAccount.getUserId(),
                order.getMerchantId(), order.getAmount(), order.getFeeAmount(), order.getSettleAmount());

        return ExecutePayVO.builder()
                .paymentNo(order.getPaymentNo())
                .amount(order.getAmount())
                .status(PayStatusEnum.SUCCESS.getCode())
                .statusDesc(PayStatusEnum.SUCCESS.getDesc())
                .payTime(now)
                .build();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private PayOrderVO buildOrderVO(PayPaymentOrder order) {
        PayStatusEnum statusEnum = PayStatusEnum.of(order.getStatus());

        // 查询渠道信息
        String channelName = null;
        String channelCode = null;
        if (order.getChannelId() != null) {
            PayPaymentChannel channel = payPaymentChannelMapper.selectById(order.getChannelId());
            if (channel != null) {
                channelName = channel.getChannelName();
                channelCode = channel.getChannelCode();
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
                .channelCode(channelCode)
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

    @Override
    public IPage<PayOrderListVO> listPage(Integer page, Integer pageSize, String paymentNo, String orderNo,
                                          String merchantNo, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<PayPaymentOrder> wrapper = new LambdaQueryWrapper<>();
        if (paymentNo != null && !paymentNo.isBlank()) {
            wrapper.like(PayPaymentOrder::getPaymentNo, paymentNo);
        }
        if (orderNo != null && !orderNo.isBlank()) {
            wrapper.like(PayPaymentOrder::getOrderNo, orderNo);
        }
        if (merchantNo != null && !merchantNo.isBlank()) {
            // 订单表只存 merchant_id，按商户号模糊匹配先转出商户ID集合
            List<Long> merchantIds = payMerchantMapper.selectList(
                            new LambdaQueryWrapper<PayMerchant>()
                                    .like(PayMerchant::getMerchantNo, merchantNo)
                                    .select(PayMerchant::getId))
                    .stream().map(PayMerchant::getId).collect(Collectors.toList());
            if (merchantIds.isEmpty()) {
                // 无匹配商户，直接返回空页
                Page<PayOrderListVO> empty = new Page<>(page, pageSize);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(PayPaymentOrder::getMerchantId, merchantIds);
        }
        if (status != null) {
            wrapper.eq(PayPaymentOrder::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(PayPaymentOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(PayPaymentOrder::getCreateTime, endTime);
        }
        wrapper.orderByDesc(PayPaymentOrder::getCreateTime);

        Page<PayPaymentOrder> result = this.page(new Page<>(page, pageSize), wrapper);

        // 批量回填商户编号/名称
        Map<Long, PayMerchant> merchantMap = buildMerchantMap(result.getRecords());

        return result.convert(o -> {
            PayMerchant merchant = merchantMap.get(o.getMerchantId());
            PayStatusEnum statusEnum = PayStatusEnum.of(o.getStatus());
            return PayOrderListVO.builder()
                    .paymentNo(o.getPaymentNo())
                    .orderNo(o.getOrderNo())
                    .merchantNo(merchant != null ? merchant.getMerchantNo() : null)
                    .merchantName(merchant != null ? merchant.getMerchantName() : null)
                    .subject(o.getSubject())
                    .amount(o.getAmount())
                    .feeAmount(o.getFeeAmount())
                    .settleAmount(o.getSettleAmount())
                    .status(o.getStatus())
                    .statusName(statusEnum != null ? statusEnum.getDesc() : "未知")
                    .payTime(o.getPayTime() != null ? o.getPayTime().toString() : null)
                    .createTime(o.getCreateTime() != null ? o.getCreateTime().toString() : null)
                    .build();
        });
    }

    /**
     * 批量查询订单涉及的商户，按商户ID组装 Map
     */
    private Map<Long, PayMerchant> buildMerchantMap(List<PayPaymentOrder> orders) {
        Set<Long> merchantIds = orders.stream()
                .map(PayPaymentOrder::getMerchantId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return payMerchantMapper.selectBatchIds(merchantIds).stream()
                .collect(Collectors.toMap(PayMerchant::getId, m -> m));
    }
}
