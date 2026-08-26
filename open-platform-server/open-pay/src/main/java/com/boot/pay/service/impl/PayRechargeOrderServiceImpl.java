package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.account.constants.AccountConstants;
import com.boot.pay.account.enums.AccountFlowTypeEnum;
import com.boot.pay.account.enums.AccountStatusEnum;
import com.boot.pay.domain.PayRechargeOrder;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.mapper.PayRechargeOrderMapper;
import com.boot.pay.mapper.PayUserAccountMapper;
import com.boot.pay.payment.exception.PayOptimisticLockException;
import com.boot.pay.recharge.channel.RechargeChannel;
import com.boot.pay.recharge.constants.RechargeConstants;
import com.boot.pay.recharge.dto.RechargeCallbackDTO;
import com.boot.pay.recharge.dto.RechargeCreateDTO;
import com.boot.pay.recharge.enums.RechargeStatusEnum;
import com.boot.pay.recharge.vo.RechargeCallbackVO;
import com.boot.pay.recharge.vo.RechargeCreateVO;
import com.boot.pay.service.PayAccountFlowService;
import com.boot.pay.service.PayRechargeOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author quannnn
* @description 针对表【pay_recharge_order(账户充值订单表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Slf4j
@Service
public class PayRechargeOrderServiceImpl extends ServiceImpl<PayRechargeOrderMapper, PayRechargeOrder>
        implements PayRechargeOrderService {

    @Resource
    private PayUserAccountMapper payUserAccountMapper;

    @Resource
    private PayAccountFlowService payAccountFlowService;

    @Resource
    private RedissonClient redissonClient;

    /** 充值渠道列表（当前仅 MockBankChannel，未来接真实银行时新增实现即可） */
    @Resource
    private List<RechargeChannel> rechargeChannels;

    /** 自注入调用事务方法：锁必须等事务提交后再释放，跨 bean 调用才能让 @Transactional 生效 */
    @Resource
    @Lazy
    private PayRechargeOrderService self;

    @Override
    public RechargeCreateVO create(RechargeCreateDTO dto, Long userId) {
        // 账户校验：存在且状态正常
        PayUserAccount userAccount = payUserAccountMapper.selectOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, userId));
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }
        if (!AccountStatusEnum.NORMAL.getCode().equals(userAccount.getStatus())) {
            throw new BusinessException("账户已被冻结");
        }

        // 生成充值单（状态 WAIT_PAY，银行到账在 callback 阶段）
        Date now = new Date();
        String rechargeNo = "RCG" + DateUtil.format(now, "yyyyMMdd")
                + String.valueOf(IdUtil.getSnowflake(1, 1).nextId()).substring(10);
        PayRechargeOrder order = new PayRechargeOrder();
        order.setRechargeNo(rechargeNo);
        order.setUserId(userId);
        order.setAmount(dto.getAmount());
        order.setStatus(RechargeStatusEnum.WAIT_PAY.getCode());
        order.setRechargeWay(RechargeConstants.RECHARGE_WAY_BANK);
        order.setBankName(dto.getBankName());
        order.setCardNoTail(dto.getCardNoTail());
        baseMapper.insert(order);

        // 路由充值渠道：发起充值，取模拟银行收银台地址（页面后期实现，先占位）
        RechargeChannel channel = rechargeChannels.stream()
                .filter(c -> c.rechargeWay() == RechargeConstants.RECHARGE_WAY_BANK)
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的充值方式"));
        String bankPageUrl = channel.create(order);

        log.info("创建充值单 rechargeNo={} userId={} amount={}",
                rechargeNo, userId, dto.getAmount());
        return RechargeCreateVO.builder()
                .rechargeNo(rechargeNo)
                .bankPageUrl(bankPageUrl)
                .build();
    }

    @Override
    public RechargeCallbackVO callback(RechargeCallbackDTO dto) {
        // 模拟阶段仅支持成功到账，真实银行接入后在此扩展失败流程
        if (!RechargeConstants.PAY_STATUS_SUCCESS.equals(dto.getPayStatus())) {
            throw new BusinessException("不支持的充值状态（模拟阶段仅支持 SUCCESS）");
        }
        // 获取分布式锁：同一充值单的到账串行化，防重复到账
        RLock lock = redissonClient.getLock("pay:lock:recharge:" + dto.getRechargeNo());
        boolean locked = false;
        RechargeCallbackVO result = null;
        try {
            try {
                locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new BusinessException("获取锁被中断，请稍后重试");
            }
            if (!locked) {
                throw new BusinessException("充值处理中，请勿重复操作");
            }
            // 乐观锁冲突重试（最多 3 次）：事务方法整体重跑，每次都是新事务、读到最新版本号
            for (int i = 0; i < 3; i++) {
                try {
                    result = self.callbackTx(dto.getRechargeNo());
                    break;
                } catch (PayOptimisticLockException e) {
                    log.warn("充值到账乐观锁冲突 rechargeNo={} 第{}次重试，原因: {}",
                            dto.getRechargeNo(), i + 1, e.getMessage());
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
            // 事务提交后释放锁（self.callbackTx 返回即事务已提交）
            if (locked) {
                lock.unlock();
            }
        }
        if (result == null) {
            throw new BusinessException("系统异常，请稍后重试");
        }
        return result;
    }

    /**
     * 充值到账（独立事务）：幂等检查 + 渠道确认 + 乐观锁加余额 + 写流水
     * <p>
     * 只在 callback 锁内调用，不对外暴露。
     */
    @Transactional(rollbackFor = Exception.class)
    public RechargeCallbackVO callbackTx(String rechargeNo) {
        // 幂等：仅 WAIT_PAY 可到账，重复通知直接返回成功（条件更新兜底）
        PayRechargeOrder order = baseMapper.selectOne(
                new LambdaQueryWrapper<PayRechargeOrder>()
                        .eq(PayRechargeOrder::getRechargeNo, rechargeNo));
        if (order == null) {
            throw new BusinessException("充值单不存在");
        }
        if (RechargeStatusEnum.SUCCESS.getCode().equals(order.getStatus())) {
            log.info("充值单已到账，幂等返回 rechargeNo={}", rechargeNo);
            return buildCallbackVO(order);
        }
        if (!RechargeStatusEnum.WAIT_PAY.getCode().equals(order.getStatus())) {
            RechargeStatusEnum statusEnum = RechargeStatusEnum.of(order.getStatus());
            throw new BusinessException("充值单状态异常（当前："
                    + (statusEnum != null ? statusEnum.getDesc() : order.getStatus()) + "）");
        }

        // 渠道到账确认（模拟阶段无实际动作，真实银行接入时在此校验银行侧到账结果）
        RechargeChannel channel = rechargeChannels.stream()
                .filter(c -> c.rechargeWay() == order.getRechargeWay())
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的充值方式"));
        channel.handleArrival(order);

        // 到账金额（模拟阶段无手续费，到账 = 充值金额）
        BigDecimal arrivalAmount = order.getAmount();

        // 充值单条件更新：仅 WAIT_PAY 可到账（幂等兜底），同步到账金额与时间
        Date finishTime = new Date();
        int orderRows = baseMapper.update(null, new LambdaUpdateWrapper<PayRechargeOrder>()
                .eq(PayRechargeOrder::getRechargeNo, rechargeNo)
                .eq(PayRechargeOrder::getStatus, RechargeStatusEnum.WAIT_PAY.getCode())
                .set(PayRechargeOrder::getStatus, RechargeStatusEnum.SUCCESS.getCode())
                .set(PayRechargeOrder::getArrivalAmount, arrivalAmount)
                .set(PayRechargeOrder::getFinishTime, finishTime));
        if (orderRows == 0) {
            throw new BusinessException("充值单状态已变更");
        }

        // 用户账户：存在且状态正常，乐观锁加余额（同时累计 total_income）
        PayUserAccount userAccount = payUserAccountMapper.selectOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, order.getUserId()));
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }
        if (!AccountStatusEnum.NORMAL.getCode().equals(userAccount.getStatus())) {
            throw new BusinessException("账户已被冻结");
        }
        int rows = payUserAccountMapper.addBalance(
                userAccount.getUserId(), userAccount.getVersion(), arrivalAmount);
        if (rows == 0) {
            throw new PayOptimisticLockException("用户账户变动");
        }

        // 写充值流水（flow_type=6），记录变更前后余额
        PayUserAccount userAfter = payUserAccountMapper.selectById(userAccount.getId());
        payAccountFlowService.recordFlow(AccountConstants.ACCOUNT_TYPE_USER, userAccount.getId(),
                rechargeNo, AccountFlowTypeEnum.RECHARGE.getCode(), arrivalAmount,
                userAccount.getBalance(), userAfter.getBalance(), "充值入账");

        // 同步刷新内存对象，返回结果与库一致
        order.setStatus(RechargeStatusEnum.SUCCESS.getCode());
        order.setArrivalAmount(arrivalAmount);
        order.setFinishTime(finishTime);

        log.info("充值到账 rechargeNo={} userId={} arrivalAmount={}",
                rechargeNo, order.getUserId(), arrivalAmount);
        return buildCallbackVO(order);
    }

    private RechargeCallbackVO buildCallbackVO(PayRechargeOrder order) {
        return RechargeCallbackVO.builder()
                .rechargeNo(order.getRechargeNo())
                .arrivalAmount(order.getArrivalAmount())
                .build();
    }
}
