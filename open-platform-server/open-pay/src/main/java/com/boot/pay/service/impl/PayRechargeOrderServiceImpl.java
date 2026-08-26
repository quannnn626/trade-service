package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.account.enums.AccountStatusEnum;
import com.boot.pay.domain.PayRechargeOrder;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.mapper.PayRechargeOrderMapper;
import com.boot.pay.mapper.PayUserAccountMapper;
import com.boot.pay.recharge.channel.RechargeChannel;
import com.boot.pay.recharge.constants.RechargeConstants;
import com.boot.pay.recharge.dto.RechargeCreateDTO;
import com.boot.pay.recharge.enums.RechargeStatusEnum;
import com.boot.pay.recharge.vo.RechargeCreateVO;
import com.boot.pay.service.PayRechargeOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    /** 充值渠道列表（当前仅 MockBankChannel，未来接真实银行时新增实现即可） */
    @Resource
    private List<RechargeChannel> rechargeChannels;

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
}
