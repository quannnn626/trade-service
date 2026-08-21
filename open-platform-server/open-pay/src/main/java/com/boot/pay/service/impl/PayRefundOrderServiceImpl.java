package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.account.constants.AccountConstants;
import com.boot.pay.account.enums.AccountFlowTypeEnum;
import com.boot.pay.account.enums.AccountStatusEnum;
import com.boot.pay.domain.PayAccountFlow;
import com.boot.pay.domain.PayMerchantAccount;
import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.domain.PayRefundOrder;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.mapper.PayAccountFlowMapper;
import com.boot.pay.mapper.PayMerchantAccountMapper;
import com.boot.pay.mapper.PayPaymentOrderMapper;
import com.boot.pay.mapper.PayRefundOrderMapper;
import com.boot.pay.mapper.PayUserAccountMapper;
import com.boot.pay.payment.enums.PayStatusEnum;
import com.boot.pay.payment.exception.PayOptimisticLockException;
import com.boot.pay.refund.dto.RefundCreateDTO;
import com.boot.pay.refund.enums.RefundStatusEnum;
import com.boot.pay.refund.vo.RefundVO;
import com.boot.pay.service.PayPaymentNotifyService;
import com.boot.pay.service.PayRefundOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 退款订单 Service 实现
 *
 * @author quannnn
 * @description 针对表【pay_refund_order(退款订单表)】的数据库操作Service实现
 * @createDate 2026-08-03 12:26:43
 */
@Slf4j
@Service
public class PayRefundOrderServiceImpl extends ServiceImpl<PayRefundOrderMapper, PayRefundOrder>
        implements PayRefundOrderService {

    /** 退款期限（天）：支付成功后 90 天内可退 */
    private static final int REFUND_PERIOD_DAYS = 90;

    /** 部分退款时订单保持可退状态，全额退完才置 REFUNDED */
    private static final int REFUND_TYPE_FULL = 1;

    @Resource
    private PayPaymentOrderMapper payPaymentOrderMapper;

    @Resource
    private PayUserAccountMapper payUserAccountMapper;

    @Resource
    private PayMerchantAccountMapper payMerchantAccountMapper;

    @Resource
    private PayAccountFlowMapper payAccountFlowMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private PayPaymentNotifyService payPaymentNotifyService;

    /**
     * 自注入调用事务方法：锁必须等事务提交后再释放，跨 bean 调用才能让 @Transactional 生效
     */
    @Resource
    @Lazy
    private PayRefundOrderService self;

    @Override
    public RefundVO refund(RefundCreateDTO dto, Long merchantId) {
        // 获取分布式锁：同一订单的退款串行化，防并发超退（锁 paymentNo，退款单号此时还未生成）
        RLock lock = redissonClient.getLock("pay:lock:refund:" + dto.getPaymentNo());
        boolean locked = false;
        RefundVO result = null;
        try {
            try {
                locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new BusinessException("获取锁被中断，请稍后重试");
            }
            if (!locked) {
                throw new BusinessException("退款处理中，请勿重复操作");
            }
            // 乐观锁冲突重试（最多 3 次）：事务方法整体重跑，每次都是新事务、读到最新版本号
            for (int i = 0; i < 3; i++) {
                try {
                    result = self.refundTx(dto, merchantId);
                    break;
                } catch (PayOptimisticLockException e) {
                    log.warn("退款乐观锁冲突 paymentNo={} 第{}次重试，原因: {}",
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
            // 事务提交后释放锁（self.refundTx 返回即事务已提交）
            if (locked) {
                lock.unlock();
            }
        }
        // 事务已提交、锁已释放：触发退款回调通知（失败不影响退款结果，由重试任务兜底）
        if (result != null) {
            try {
                payPaymentNotifyService.triggerRefundNotify(result.getRefundNo());
            } catch (Exception e) {
                log.error("触发退款回调通知异常 refundNo={}", result.getRefundNo(), e);
            }
            return result;
        }
        throw new BusinessException("系统异常，请稍后重试");
    }

    /**
     * 退款资金操作（独立事务）：动账必须全部成功或全部回滚
     * <p>
     * 只在 refund 锁内调用，不对外暴露。
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundVO refundTx(RefundCreateDTO dto, Long merchantId) {
        // 幂等：同商户同商户退款单号已存在则直接返回
        PayRefundOrder exist = baseMapper.selectOne(
                new LambdaQueryWrapper<PayRefundOrder>()
                        .eq(PayRefundOrder::getMerchantId, merchantId)
                        .eq(PayRefundOrder::getMerchantRefundNo, dto.getMerchantRefundNo()));
        if (exist != null) {
            log.info("退款单已存在，直接返回 refundNo={} merchantRefundNo={}",
                    exist.getRefundNo(), dto.getMerchantRefundNo());
            return buildVO(exist);
        }

        // ① 查原支付订单：存在性 / 归属 / 状态机 / 退款期限
        PayPaymentOrder order = payPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, dto.getPaymentNo()));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!PayStatusEnum.SUCCESS.getCode().equals(order.getStatus())) {
            PayStatusEnum statusEnum = PayStatusEnum.of(order.getStatus());
            throw new BusinessException("订单状态异常（当前：" + (statusEnum != null ? statusEnum.getDesc() : order.getStatus()) + "）");
        }
        if (order.getPayTime() == null) {
            throw new BusinessException("订单支付时间缺失，无法退款");
        }
        if (DateUtil.offsetDay(order.getPayTime(), REFUND_PERIOD_DAYS).before(new Date())) {
            throw new BusinessException("已过退款期限（支付成功后 90 天内可退）");
        }

        // ② 金额校验：≤ 订单金额；累计已退（处理中+成功，待审核的同样占额度）+ 本次 ≤ 订单金额
        BigDecimal refundAmount = dto.getRefundAmount();
        if (refundAmount.compareTo(order.getAmount()) > 0) {
            throw new BusinessException("退款金额超出订单金额");
        }
        List<Integer> occupiedStatuses = List.of(
                RefundStatusEnum.PROCESSING.getCode(), RefundStatusEnum.SUCCESS.getCode());
        BigDecimal refunded = nvl(baseMapper.sumRefundedAmount(order.getPaymentNo(), occupiedStatuses));
        if (refunded.add(refundAmount).compareTo(order.getAmount()) > 0) {
            throw new BusinessException("累计退款金额超出订单可退金额（已退 " + refunded + " 元）");
        }

        // ③ 商户账户校验：存在 / 状态正常 / 余额充足（按退款金额校验，与支付宝口径一致）
        PayMerchantAccount merchantAccount = payMerchantAccountMapper.selectOne(
                new LambdaQueryWrapper<PayMerchantAccount>()
                        .eq(PayMerchantAccount::getMerchantId, order.getMerchantId()));
        if (merchantAccount == null) {
            throw new BusinessException("商户账户异常");
        }
        if (!AccountStatusEnum.NORMAL.getCode().equals(merchantAccount.getStatus())) {
            throw new BusinessException("商户账户不可用");
        }
        if (nvl(merchantAccount.getBalance()).compareTo(refundAmount) < 0) {
            throw new BusinessException("商户余额不足，请充值后重试");
        }

        // ④ 用户账户校验
        PayUserAccount userAccount = payUserAccountMapper.selectOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, order.getUserId()));
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }
        if (AccountStatusEnum.FROZEN.getCode().equals(userAccount.getStatus())) {
            throw new BusinessException("账户已被冻结");
        }

        // ⑤ 手续费退还：全额按订单手续费，部分按退款比例（平台承担退还部分）
        BigDecimal feeRefund = nvl(order.getFeeAmount())
                .multiply(refundAmount)
                .divide(order.getAmount(), 2, RoundingMode.HALF_UP);
        // 商户实际扣减 = 退款金额 - 退还手续费（商户拿回多少退多少）
        BigDecimal merchantDeduct = refundAmount.subtract(feeRefund);

        Date now = new Date();

        // ⑥ 创建退款单（唯一索引 uk_merchant_refund 兜底防重）
        String refundNo = "RFD" + DateUtil.format(now, "yyyyMMdd")
                + String.valueOf(IdUtil.getSnowflake(1, 1).nextId()).substring(10);
        PayRefundOrder refundOrder = new PayRefundOrder();
        refundOrder.setRefundNo(refundNo);
        refundOrder.setPaymentNo(order.getPaymentNo());
        refundOrder.setMerchantId(order.getMerchantId());
        refundOrder.setUserId(order.getUserId());
        refundOrder.setMerchantRefundNo(dto.getMerchantRefundNo());
        refundOrder.setRefundType(dto.getRefundType());
        refundOrder.setApplyAmount(refundAmount);
        refundOrder.setActualAmount(refundAmount);
        refundOrder.setFeeRefund(feeRefund);
        refundOrder.setRefundChannel(1);
        refundOrder.setApplyTime(now);
        refundOrder.setStatus(RefundStatusEnum.PROCESSING.getCode());
        refundOrder.setAuditStatus(1);
        refundOrder.setNotifyUrl(order.getNotifyUrl());
        refundOrder.setRefundReason(dto.getRefundReason());
        baseMapper.insert(refundOrder);

        // ⑦ 订单状态 SUCCESS → REFUNDING（条件更新，并发超退的 DB 层兜底）
        int orderRows = payPaymentOrderMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, order.getPaymentNo())
                .eq(PayPaymentOrder::getStatus, PayStatusEnum.SUCCESS.getCode())
                .set(PayPaymentOrder::getStatus, PayStatusEnum.REFUNDING.getCode()));
        if (orderRows == 0) {
            throw new BusinessException("订单状态已变更");
        }

        // ⑧ 扣减商户余额（乐观锁，扣净额）
        int merchantRows = payMerchantAccountMapper.deductBalance(
                order.getMerchantId(), merchantAccount.getVersion(), merchantDeduct);
        if (merchantRows == 0) {
            throw new PayOptimisticLockException("商户账户变动");
        }

        // ⑨ 增加用户余额（乐观锁）
        int userRows = payUserAccountMapper.addBalance(
                userAccount.getUserId(), userAccount.getVersion(), refundAmount);
        if (userRows == 0) {
            throw new PayOptimisticLockException("用户账户变动");
        }

        // ⑩ 写 2 条资金流水（商户退款支出 + 用户退款收入），记录变更前后余额
        PayMerchantAccount merchantAfter = payMerchantAccountMapper.selectById(merchantAccount.getId());
        PayUserAccount userAfter = payUserAccountMapper.selectById(userAccount.getId());
        String subject = StrUtil.blankToDefault(order.getSubject(), order.getOrderNo());
        insertFlow(AccountConstants.ACCOUNT_TYPE_MERCHANT, merchantAccount.getId(), order.getPaymentNo(),
                AccountFlowTypeEnum.REFUND_EXPENSE.getCode(), merchantDeduct.negate(),
                merchantAccount.getBalance(), merchantAfter.getBalance(), "退款支出-" + subject);
        insertFlow(AccountConstants.ACCOUNT_TYPE_USER, userAccount.getId(), order.getPaymentNo(),
                AccountFlowTypeEnum.REFUND_INCOME.getCode(), refundAmount,
                userAccount.getBalance(), userAfter.getBalance(), "退款收入-" + subject);

        // ⑪ 更新退款单为成功
        baseMapper.update(null, new LambdaUpdateWrapper<PayRefundOrder>()
                .eq(PayRefundOrder::getRefundNo, refundNo)
                .set(PayRefundOrder::getStatus, RefundStatusEnum.SUCCESS.getCode())
                .set(PayRefundOrder::getFinishTime, now));

        // ⑫ 订单终态：累计退满 → REFUNDED；部分退款 → 回到 SUCCESS（可继续退）
        boolean fullRefunded = REFUND_TYPE_FULL == dto.getRefundType()
                || refunded.add(refundAmount).compareTo(order.getAmount()) >= 0;
        int orderFinalStatus = fullRefunded
                ? PayStatusEnum.REFUNDED.getCode() : PayStatusEnum.SUCCESS.getCode();
        payPaymentOrderMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, order.getPaymentNo())
                .set(PayPaymentOrder::getStatus, orderFinalStatus));

        log.info("退款成功 refundNo={} paymentNo={} merchantId={} userId={} refundAmount={} feeRefund={} orderStatus={}",
                refundNo, order.getPaymentNo(), order.getMerchantId(), order.getUserId(),
                refundAmount, feeRefund, orderFinalStatus);

        return buildVO(refundOrder);
    }

    /**
     * 写入一条资金流水
     */
    private void insertFlow(int accountType, Long accountId, String paymentNo, int flowType,
                            BigDecimal amount, BigDecimal beforeBalance, BigDecimal afterBalance, String remark) {
        PayAccountFlow flow = new PayAccountFlow();
        flow.setFlowNo("FLW" + DateUtil.format(new Date(), "yyyyMMdd")
                + String.valueOf(IdUtil.getSnowflake(1, 1).nextId()).substring(10));
        flow.setAccountType(accountType);
        flow.setAccountId(accountId);
        flow.setPaymentNo(paymentNo);
        flow.setFlowType(flowType);
        flow.setAmount(amount);
        flow.setBeforeBalance(beforeBalance);
        flow.setAfterBalance(afterBalance);
        flow.setRemark(remark);
        payAccountFlowMapper.insert(flow);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private RefundVO buildVO(PayRefundOrder refundOrder) {
        RefundStatusEnum statusEnum = RefundStatusEnum.of(refundOrder.getStatus());
        return RefundVO.builder()
                .refundNo(refundOrder.getRefundNo())
                .paymentNo(refundOrder.getPaymentNo())
                .refundAmount(refundOrder.getActualAmount())
                .feeRefund(refundOrder.getFeeRefund())
                .status(refundOrder.getStatus())
                .statusDesc(statusEnum != null ? statusEnum.getDesc() : "未知")
                .auditStatus(refundOrder.getAuditStatus())
                .finishTime(refundOrder.getFinishTime())
                .build();
    }
}
