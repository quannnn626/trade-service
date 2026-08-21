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
import com.boot.pay.refund.constants.RefundConstants;
import com.boot.pay.refund.dto.AuditRefundDTO;
import com.boot.pay.refund.dto.RefundCreateDTO;
import com.boot.pay.refund.enums.RefundAuditStatusEnum;
import com.boot.pay.refund.enums.RefundStatusEnum;
import com.boot.pay.refund.enums.RefundTypeEnum;
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
        // 事务已提交、锁已释放：仅退款成功（含自动通过的小额）触发回调通知，待审核的大额不通知
        if (result != null && RefundStatusEnum.SUCCESS.getCode().equals(result.getStatus())) {
            try {
                payPaymentNotifyService.triggerRefundNotify(result.getRefundNo());
            } catch (Exception e) {
                log.error("触发退款回调通知异常 refundNo={}", result.getRefundNo(), e);
            }
            return result;
        }
        if (result != null) {
            return result;
        }
        throw new BusinessException("系统异常，请稍后重试");
    }

    /**
     * 退款申请（独立事务）：校验 + 创建退款单 + 按金额决定是否直接执行
     * <p>
     * 大额（超过 10000 元）退款创建后置待审核，不执行动账；小额自动通过直接执行。
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
        if (DateUtil.offsetDay(order.getPayTime(), RefundConstants.REFUND_PERIOD_DAYS).before(new Date())) {
            throw new BusinessException("已过退款期限（支付成功后 90 天内可退）");
        }

        // ② 金额校验：≤ 订单金额；累计已退（处理中+成功，待审核的同样占额度）+ 本次 ≤ 订单金额
        BigDecimal refundAmount = dto.getRefundAmount();
        if (refundAmount.compareTo(order.getAmount()) > 0) {
            throw new BusinessException("退款金额超出订单金额");
        }
        BigDecimal refunded = nvl(baseMapper.sumRefundedAmount(order.getPaymentNo(), occupiedStatuses()));
        if (refunded.add(refundAmount).compareTo(order.getAmount()) > 0) {
            throw new BusinessException("累计退款金额超出订单可退金额（已退 " + refunded + " 元）");
        }

        // ③ 用户账户校验（退款入账方）
        PayUserAccount userAccount = payUserAccountMapper.selectOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, order.getUserId()));
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }
        if (AccountStatusEnum.FROZEN.getCode().equals(userAccount.getStatus())) {
            throw new BusinessException("账户已被冻结");
        }

        // ④ 手续费退还：全额按订单手续费，部分按退款比例（平台承担退还部分）
        BigDecimal feeRefund = nvl(order.getFeeAmount())
                .multiply(refundAmount)
                .divide(order.getAmount(), 2, RoundingMode.HALF_UP);

        Date now = new Date();

        // ⑤ 创建退款单（唯一索引 uk_merchant_refund 兜底防重）
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
        refundOrder.setRefundChannel(RefundConstants.REFUND_CHANNEL_ORIGINAL);
        refundOrder.setApplyTime(now);
        refundOrder.setStatus(RefundStatusEnum.PROCESSING.getCode());
        // 大额退款（超过 10000 元）需人工审核，小额自动通过
        int auditStatus = refundAmount.compareTo(RefundConstants.AUDIT_LIMIT) > 0
                ? RefundAuditStatusEnum.WAIT.getCode() : RefundAuditStatusEnum.PASS.getCode();
        refundOrder.setAuditStatus(auditStatus);
        refundOrder.setNotifyUrl(order.getNotifyUrl());
        refundOrder.setRefundReason(dto.getRefundReason());
        baseMapper.insert(refundOrder);

        // 小额：自动通过，直接执行动账
        if (RefundAuditStatusEnum.PASS.getCode().equals(auditStatus)) {
            return executeRefundTx(refundOrder);
        }

        // 大额：订单置退款中（REFUNDING），等待管理员审核
        int orderRows = payPaymentOrderMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, order.getPaymentNo())
                .eq(PayPaymentOrder::getStatus, PayStatusEnum.SUCCESS.getCode())
                .set(PayPaymentOrder::getStatus, PayStatusEnum.REFUNDING.getCode()));
        if (orderRows == 0) {
            throw new BusinessException("订单状态已变更");
        }
        log.info("大额退款待审核 refundNo={} paymentNo={} refundAmount={}",
                refundNo, order.getPaymentNo(), refundAmount);
        return buildVO(refundOrder);
    }

    /**
     * 退款动账（独立事务）：小额自动通过与大额审核通过共用
     * <p>
     * ① 订单状态条件更新（SUCCESS 或 REFUNDING → REFUNDING，防并发超退兜底）
     * ② 商户扣款（乐观锁，扣净额）③ 用户加款（乐观锁）④ 双流水 ⑤ 退款单成功 ⑥ 订单终态
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundVO executeRefundTx(PayRefundOrder refundOrder) {
        PayPaymentOrder order = payPaymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PayPaymentOrder>()
                        .eq(PayPaymentOrder::getPaymentNo, refundOrder.getPaymentNo()));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        boolean canRefund = PayStatusEnum.SUCCESS.getCode().equals(order.getStatus())
                || PayStatusEnum.REFUNDING.getCode().equals(order.getStatus());
        if (!canRefund) {
            PayStatusEnum statusEnum = PayStatusEnum.of(order.getStatus());
            throw new BusinessException("订单状态异常（当前：" + (statusEnum != null ? statusEnum.getDesc() : order.getStatus()) + "）");
        }

        // ① 订单状态条件更新：小额路径 SUCCESS → REFUNDING；审核通过路径已是 REFUNDING 则原样覆盖
        // 受影响 0 行说明订单已被其他退款置为 REFUNDED 等终态，拒绝执行
        int orderRows = payPaymentOrderMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, order.getPaymentNo())
                .in(PayPaymentOrder::getStatus,
                        PayStatusEnum.SUCCESS.getCode(), PayStatusEnum.REFUNDING.getCode())
                .set(PayPaymentOrder::getStatus, PayStatusEnum.REFUNDING.getCode()));
        if (orderRows == 0) {
            throw new BusinessException("订单状态已变更");
        }

        BigDecimal refundAmount = refundOrder.getActualAmount();
        BigDecimal merchantDeduct = refundAmount.subtract(nvl(refundOrder.getFeeRefund()));

        // ② 商户账户：存在 / 状态正常 / 余额充足（动账前最后校验，审核等待期余额可能变动）
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

        // ③ 用户账户校验（动账前重查）
        PayUserAccount userAccount = payUserAccountMapper.selectOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, order.getUserId()));
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }
        if (AccountStatusEnum.FROZEN.getCode().equals(userAccount.getStatus())) {
            throw new BusinessException("账户已被冻结");
        }

        // ④ 扣减商户余额（乐观锁，扣净额）
        int merchantRows = payMerchantAccountMapper.deductBalance(
                order.getMerchantId(), merchantAccount.getVersion(), merchantDeduct);
        if (merchantRows == 0) {
            throw new PayOptimisticLockException("商户账户变动");
        }

        // ⑤ 增加用户余额（乐观锁）
        int userRows = payUserAccountMapper.addBalance(
                userAccount.getUserId(), userAccount.getVersion(), refundAmount);
        if (userRows == 0) {
            throw new PayOptimisticLockException("用户账户变动");
        }

        // ⑥ 写 2 条资金流水（商户退款支出 + 用户退款收入），记录变更前后余额
        PayMerchantAccount merchantAfter = payMerchantAccountMapper.selectById(merchantAccount.getId());
        PayUserAccount userAfter = payUserAccountMapper.selectById(userAccount.getId());
        String subject = StrUtil.blankToDefault(order.getSubject(), order.getOrderNo());
        insertFlow(AccountConstants.ACCOUNT_TYPE_MERCHANT, merchantAccount.getId(), order.getPaymentNo(),
                AccountFlowTypeEnum.REFUND_EXPENSE.getCode(), merchantDeduct.negate(),
                merchantAccount.getBalance(), merchantAfter.getBalance(), "退款支出-" + subject);
        insertFlow(AccountConstants.ACCOUNT_TYPE_USER, userAccount.getId(), order.getPaymentNo(),
                AccountFlowTypeEnum.REFUND_INCOME.getCode(), refundAmount,
                userAccount.getBalance(), userAfter.getBalance(), "退款收入-" + subject);

        // ⑦ 更新退款单为成功（同步刷新内存对象，返回结果与库一致）
        Date finishTime = new Date();
        baseMapper.update(null, new LambdaUpdateWrapper<PayRefundOrder>()
                .eq(PayRefundOrder::getRefundNo, refundOrder.getRefundNo())
                .set(PayRefundOrder::getStatus, RefundStatusEnum.SUCCESS.getCode())
                .set(PayRefundOrder::getFinishTime, finishTime));
        refundOrder.setStatus(RefundStatusEnum.SUCCESS.getCode());
        refundOrder.setFinishTime(finishTime);

        // ⑧ 订单终态：累计退满 → REFUNDED；部分退款 → 回到 SUCCESS（可继续退）
        BigDecimal refundedNow = nvl(baseMapper.sumRefundedAmount(order.getPaymentNo(), occupiedStatuses()));
        int orderFinalStatus = refundedNow.compareTo(order.getAmount()) >= 0
                ? PayStatusEnum.REFUNDED.getCode() : PayStatusEnum.SUCCESS.getCode();
        payPaymentOrderMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, order.getPaymentNo())
                .set(PayPaymentOrder::getStatus, orderFinalStatus));

        log.info("退款成功 refundNo={} paymentNo={} merchantId={} userId={} refundAmount={} feeRefund={} orderStatus={}",
                refundOrder.getRefundNo(), order.getPaymentNo(), order.getMerchantId(), order.getUserId(),
                refundAmount, refundOrder.getFeeRefund(), orderFinalStatus);

        return buildVO(refundOrder);
    }

    @Override
    public RefundVO audit(AuditRefundDTO dto, Long auditorId) {
        // 获取分布式锁：同一退款单的审核串行化（审核时退款单号已存在，锁 refundNo）
        RLock lock = redissonClient.getLock("pay:lock:refund:" + dto.getRefundNo());
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
                throw new BusinessException("退款审核处理中，请勿重复操作");
            }

            // 预检：退款单存在 / 待审核 / 处理中
            PayRefundOrder refundOrder = baseMapper.selectOne(
                    new LambdaQueryWrapper<PayRefundOrder>()
                            .eq(PayRefundOrder::getRefundNo, dto.getRefundNo()));
            if (refundOrder == null) {
                throw new BusinessException("退款单不存在");
            }
            if (!RefundAuditStatusEnum.WAIT.getCode().equals(refundOrder.getAuditStatus())) {
                throw new BusinessException("退款单已审核");
            }
            if (!RefundStatusEnum.PROCESSING.getCode().equals(refundOrder.getStatus())) {
                throw new BusinessException("退款单状态异常");
            }

            if (RefundAuditStatusEnum.PASS.getCode().equals(dto.getAuditResult())) {
                // 审核通过：动账（乐观锁冲突重试 3 次）
                for (int i = 0; i < 3; i++) {
                    try {
                        result = self.auditPassTx(refundOrder.getId(), auditorId);
                        break;
                    } catch (PayOptimisticLockException e) {
                        log.warn("退款审核动账乐观锁冲突 refundNo={} 第{}次重试，原因: {}",
                                dto.getRefundNo(), i + 1, e.getMessage());
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
            } else if (RefundAuditStatusEnum.REJECT.getCode().equals(dto.getAuditResult())) {
                if (StrUtil.isBlank(dto.getAuditRemark())) {
                    throw new BusinessException("驳回必须填写原因");
                }
                result = self.auditRejectTx(refundOrder.getId(), auditorId, dto.getAuditRemark());
            } else {
                throw new BusinessException("审核结果不合法");
            }
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        // 事务已提交、锁已释放：审核通过触发退款回调通知，驳回不通知
        if (result != null && RefundAuditStatusEnum.PASS.getCode().equals(dto.getAuditResult())) {
            try {
                payPaymentNotifyService.triggerRefundNotify(result.getRefundNo());
            } catch (Exception e) {
                log.error("触发退款回调通知异常 refundNo={}", result.getRefundNo(), e);
            }
        }
        return result;
    }

    /**
     * 退款审核通过（独立事务）：更新审核字段 + 执行退款动账
     * <p>
     * 只在 audit 锁内调用，不对外暴露。
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundVO auditPassTx(Long refundOrderId, Long auditorId) {
        PayRefundOrder refundOrder = baseMapper.selectById(refundOrderId);
        if (refundOrder == null) {
            throw new BusinessException("退款单不存在");
        }
        // 审核字段（条件更新防重复审核）
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<PayRefundOrder>()
                .eq(PayRefundOrder::getId, refundOrderId)
                .eq(PayRefundOrder::getAuditStatus, RefundAuditStatusEnum.WAIT.getCode())
                .set(PayRefundOrder::getAuditStatus, RefundAuditStatusEnum.PASS.getCode())
                .set(PayRefundOrder::getAuditorId, auditorId)
                .set(PayRefundOrder::getAuditTime, new Date()));
        if (rows == 0) {
            throw new BusinessException("退款单已审核");
        }
        // 同步刷新内存对象（审核字段），动账成功后返回结果与库一致
        refundOrder.setAuditStatus(RefundAuditStatusEnum.PASS.getCode());
        refundOrder.setAuditorId(auditorId);
        refundOrder.setAuditTime(new Date());
        // 动账（同 bean 调用，并入当前事务，失败整体回滚）
        return executeRefundTx(refundOrder);
    }

    /**
     * 退款审核驳回（独立事务）：退款单置失败，订单回到 SUCCESS（可重新申请退款）
     * <p>
     * 只在 audit 锁内调用，不对外暴露。
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundVO auditRejectTx(Long refundOrderId, Long auditorId, String reason) {
        PayRefundOrder refundOrder = baseMapper.selectById(refundOrderId);
        if (refundOrder == null) {
            throw new BusinessException("退款单不存在");
        }
        Date auditTime = new Date();
        int rows = baseMapper.update(null, new LambdaUpdateWrapper<PayRefundOrder>()
                .eq(PayRefundOrder::getId, refundOrderId)
                .eq(PayRefundOrder::getAuditStatus, RefundAuditStatusEnum.WAIT.getCode())
                .set(PayRefundOrder::getAuditStatus, RefundAuditStatusEnum.REJECT.getCode())
                .set(PayRefundOrder::getAuditorId, auditorId)
                .set(PayRefundOrder::getAuditTime, auditTime)
                .set(PayRefundOrder::getStatus, RefundStatusEnum.FAILED.getCode())
                .set(PayRefundOrder::getFailReason, reason));
        if (rows == 0) {
            throw new BusinessException("退款单已审核");
        }
        // 同步刷新内存对象，返回结果与库一致
        refundOrder.setAuditStatus(RefundAuditStatusEnum.REJECT.getCode());
        refundOrder.setAuditorId(auditorId);
        refundOrder.setAuditTime(auditTime);
        refundOrder.setStatus(RefundStatusEnum.FAILED.getCode());
        refundOrder.setFailReason(reason);
        // 订单 REFUNDING → SUCCESS（条件更新：仅退款中状态可回退）
        int orderRows = payPaymentOrderMapper.update(null, new LambdaUpdateWrapper<PayPaymentOrder>()
                .eq(PayPaymentOrder::getPaymentNo, refundOrder.getPaymentNo())
                .eq(PayPaymentOrder::getStatus, PayStatusEnum.REFUNDING.getCode())
                .set(PayPaymentOrder::getStatus, PayStatusEnum.SUCCESS.getCode()));
        if (orderRows == 0) {
            throw new BusinessException("订单状态已变更，请重新确认");
        }

        log.info("退款审核驳回 refundNo={} paymentNo={} reason={} auditorId={}",
                refundOrder.getRefundNo(), refundOrder.getPaymentNo(), reason, auditorId);
        return buildVO(refundOrder);
    }

    /**
     * 已占用退款额度的退款单状态集合（处理中 + 成功，待审核的同样占额度）
     */
    private List<Integer> occupiedStatuses() {
        return List.of(RefundStatusEnum.PROCESSING.getCode(), RefundStatusEnum.SUCCESS.getCode());
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
