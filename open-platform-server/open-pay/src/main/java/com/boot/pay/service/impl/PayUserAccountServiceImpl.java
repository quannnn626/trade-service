package com.boot.pay.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.account.dto.SetPayPasswordDTO;
import com.boot.pay.account.enums.RealNameAuthEnum;
import com.boot.pay.account.vo.AccountVO;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.mapper.PayUserAccountMapper;
import com.boot.pay.service.PayUserAccountService;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_user_account(用户钱包账户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Slf4j
@Service
public class PayUserAccountServiceImpl extends ServiceImpl<PayUserAccountMapper, PayUserAccount>
    implements PayUserAccountService {

    @Override
    public AccountVO getMyAccount(Long userId) {
        return buildVO(getByUserId(userId));
    }

    @Override
    public AccountVO getByAccountNo(String accountNo) {
        PayUserAccount account = this.getOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getAccountNo, accountNo)
        );
        if (account == null) {
            throw new BusinessException("账户不存在: " + accountNo);
        }
        return buildVO(account);
    }

    @Override
    public void setPayPassword(Long userId, SetPayPasswordDTO dto) {
        PayUserAccount account = getByUserId(userId);
        // 已设置过密码的账户，修改时必须先验证原密码
        if (account.getPayPassword() != null && !account.getPayPassword().isEmpty()) {
            if (dto.getOldPayPassword() == null || dto.getOldPayPassword().isBlank()) {
                throw new BusinessException("请提供原支付密码");
            }
            if (!BCrypt.checkpw(dto.getOldPayPassword(), account.getPayPassword())) {
                throw new BusinessException("原支付密码错误");
            }
        }
        boolean updated = this.lambdaUpdate()
                .eq(PayUserAccount::getUserId, userId)
                .set(PayUserAccount::getPayPassword, BCrypt.hashpw(dto.getPayPassword()))
                .update();
        if (!updated) {
            throw new BusinessException("设置支付密码失败，请稍后重试");
        }
        log.info("支付密码设置成功 userId={} accountNo={}", userId, account.getAccountNo());
    }

    private PayUserAccount getByUserId(Long userId) {
        PayUserAccount account = this.getOne(
                new LambdaQueryWrapper<PayUserAccount>()
                        .eq(PayUserAccount::getUserId, userId)
        );
        if (account == null) {
            throw new BusinessException("账户不存在");
        }
        return account;
    }

    private AccountVO buildVO(PayUserAccount account) {
        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal frozen = account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO;
        return AccountVO.builder()
                .accountNo(account.getAccountNo())
                .balance(balance)
                .frozenAmount(frozen)
                .availableBalance(balance.subtract(frozen))
                .totalIncome(account.getTotalIncome())
                .totalExpense(account.getTotalExpense())
                .realNameAuth(RealNameAuthEnum.REAL.getCode().equals(account.getRealNameAuth()))
                .status(account.getStatus())
                .build();
    }
}
