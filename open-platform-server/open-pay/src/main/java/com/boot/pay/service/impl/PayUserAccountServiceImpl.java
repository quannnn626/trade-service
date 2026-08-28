package com.boot.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.account.constants.AccountConstants;
import com.boot.pay.account.dto.RealNameAuthDTO;
import com.boot.pay.account.dto.SetPayPasswordDTO;
import com.boot.pay.account.enums.AccountFlowTypeEnum;
import com.boot.pay.account.enums.AccountStatusEnum;
import com.boot.pay.account.enums.RealNameAuthEnum;
import com.boot.pay.account.vo.AccountListVO;
import com.boot.pay.account.vo.AccountVO;
import com.boot.pay.domain.AuthUser;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.mapper.AuthUserMapper;
import com.boot.pay.mapper.PayUserAccountMapper;
import com.boot.pay.service.PayAccountFlowService;
import com.boot.pay.service.PayUserAccountService;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author quannnn
* @description 针对表【pay_user_account(用户钱包账户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class PayUserAccountServiceImpl extends ServiceImpl<PayUserAccountMapper, PayUserAccount>
    implements PayUserAccountService {

    private final PayAccountFlowService payAccountFlowService;

    private final AuthUserMapper authUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAccountForUser(Long userId) {
        String accountNo = "UA" + DateUtil.format(new Date(), "yyyyMMdd")
                + String.valueOf(IdUtil.getSnowflake(1, 1).nextId()).substring(10);
        PayUserAccount account = new PayUserAccount();
        account.setUserId(userId);
        account.setAccountNo(accountNo);
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setStatus(AccountStatusEnum.NORMAL.getCode());
        account.setVersion(0);
        account.setTotalIncome(BigDecimal.ZERO);
        account.setTotalExpense(BigDecimal.ZERO);
        account.setRealNameAuth(RealNameAuthEnum.UNREAL.getCode());
        account.setDailyLimit(AccountConstants.DAILY_LIMIT_UNREAL_NAME);
        account.setDailyUsed(BigDecimal.ZERO);
        this.save(account);
        log.info("自动开户成功 userId={} accountNo={}", userId, accountNo);
    }

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

    @Override
    public void realNameAuth(Long userId, RealNameAuthDTO dto) {
        PayUserAccount account = getByUserId(userId);
        // 已实名认证过，幂等直接返回
        if (RealNameAuthEnum.REAL.getCode().equals(account.getRealNameAuth())) {
            log.info("账户已实名认证，跳过重复认证 userId={} accountNo={}", userId, account.getAccountNo());
            return;
        }
        // 支付密码校验（实名需先用支付密码确认身份）
        if (account.getPayPassword() == null || account.getPayPassword().isEmpty()) {
            throw new BusinessException("请先设置支付密码");
        }
        if (!BCrypt.checkpw(dto.getPayPassword(), account.getPayPassword())) {
            throw new BusinessException("支付密码错误");
        }
        // 更新实名信息，实名后日限额提升至 50000
        boolean updated = this.lambdaUpdate()
                .eq(PayUserAccount::getUserId, userId)
                .set(PayUserAccount::getRealName, dto.getRealName())
                .set(PayUserAccount::getIdCard, dto.getIdCard())
                .set(PayUserAccount::getRealNameAuth, RealNameAuthEnum.REAL.getCode())
                .set(PayUserAccount::getDailyLimit, AccountConstants.DAILY_LIMIT_REAL_NAME)
                .update();
        if (!updated) {
            throw new BusinessException("实名认证失败，请稍后重试");
        }
        log.info("实名认证成功 userId={} accountNo={} realName={}", userId, account.getAccountNo(), dto.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal freeze(Long userId, BigDecimal amount) {
        PayUserAccount account = getByUserId(userId);
        checkNormal(account);

        // 冻结金额：为空则冻结全部可用余额（可用余额 = balance - frozen_amount）
        BigDecimal available = nvl(account.getBalance()).subtract(nvl(account.getFrozenAmount()));
        BigDecimal freezeAmount = amount == null ? available : amount;
        if (freezeAmount.compareTo(available) > 0) {
            throw new BusinessException("冻结金额超出可用余额");
        }

        // 乐观锁更新冻结金额，0 行说明账户已被并发修改，本次冻结不生效
        int rows = baseMapper.addFrozenAmount(userId, account.getVersion(), freezeAmount);
        if (rows == 0) {
            throw new BusinessException("账户变动频繁，请重试");
        }

        payAccountFlowService.recordFlow(AccountConstants.ACCOUNT_TYPE_USER, account.getId(), null,
                AccountFlowTypeEnum.FREEZE.getCode(), freezeAmount.negate(),
                account.getBalance(), account.getBalance(), "资金冻结");
        log.info("账户资金冻结成功 userId={} accountNo={} amount={}",
                userId, account.getAccountNo(), freezeAmount);
        return freezeAmount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal unfreeze(Long userId, BigDecimal amount) {
        PayUserAccount account = getByUserId(userId);
        checkNormal(account);

        // 解冻金额不能超过已冻结金额
        if (amount.compareTo(nvl(account.getFrozenAmount())) > 0) {
            throw new BusinessException("解冻金额超出已冻结金额");
        }

        // 乐观锁更新冻结金额，0 行说明账户已被并发修改，本次解冻不生效
        int rows = baseMapper.subtractFrozenAmount(userId, account.getVersion(), amount);
        if (rows == 0) {
            throw new BusinessException("账户变动频繁，请重试");
        }

        payAccountFlowService.recordFlow(AccountConstants.ACCOUNT_TYPE_USER, account.getId(), null,
                AccountFlowTypeEnum.UNFREEZE.getCode(), amount,
                account.getBalance(), account.getBalance(), "资金解冻");
        log.info("账户资金解冻成功 userId={} accountNo={} amount={}",
                userId, account.getAccountNo(), amount);
        return amount;
    }

    /**
     * 校验账户状态正常（冻结操作对非正常账户拒绝）
     */
    private void checkNormal(PayUserAccount account) {
        if (!AccountStatusEnum.NORMAL.getCode().equals(account.getStatus())) {
            throw new BusinessException("账户已被冻结");
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
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

    @Override
    public IPage<AccountListVO> listPage(Integer page, Integer pageSize, String accountNo, String username, String phone) {
        LambdaQueryWrapper<PayUserAccount> wrapper = new LambdaQueryWrapper<>();
        if (accountNo != null && !accountNo.isBlank()) {
            wrapper.like(PayUserAccount::getAccountNo, accountNo);
        }
        if ((username != null && !username.isBlank()) || (phone != null && !phone.isBlank())) {
            // 用户名/手机号在 auth_user 表，先模糊查出 userId 集合
            LambdaQueryWrapper<AuthUser> userWrapper = new LambdaQueryWrapper<>();
            if (username != null && !username.isBlank()) {
                userWrapper.like(AuthUser::getUsername, username);
            }
            if (phone != null && !phone.isBlank()) {
                userWrapper.like(AuthUser::getPhone, phone);
            }
            List<Long> userIds = authUserMapper.selectList(userWrapper.select(AuthUser::getId))
                    .stream().map(AuthUser::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                // 无匹配用户，直接返回空页
                Page<AccountListVO> empty = new Page<>(page, pageSize);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(PayUserAccount::getUserId, userIds);
        }
        wrapper.orderByDesc(PayUserAccount::getCreateTime);

        Page<PayUserAccount> result = this.page(new Page<>(page, pageSize), wrapper);

        // 批量回填用户编号/用户名/手机号
        Map<Long, AuthUser> userMap = buildUserMap(result.getRecords());

        return result.convert(o -> {
            AuthUser user = userMap.get(o.getUserId());
            BigDecimal balance = o.getBalance() != null ? o.getBalance() : BigDecimal.ZERO;
            BigDecimal frozen = o.getFrozenAmount() != null ? o.getFrozenAmount() : BigDecimal.ZERO;
            AccountStatusEnum statusEnum = AccountStatusEnum.of(o.getStatus());
            return AccountListVO.builder()
                    .accountNo(o.getAccountNo())
                    .userNo(user != null ? user.getUserNo() : null)
                    .username(user != null ? user.getUsername() : null)
                    .phone(user != null ? user.getPhone() : null)
                    .balance(balance)
                    .frozenAmount(frozen)
                    .availableBalance(balance.subtract(frozen))
                    .totalIncome(o.getTotalIncome())
                    .totalExpense(o.getTotalExpense())
                    .realNameAuth(RealNameAuthEnum.REAL.getCode().equals(o.getRealNameAuth()))
                    .status(o.getStatus())
                    .statusName(statusEnum != null ? statusEnum.getDesc() : "未知")
                    .createTime(o.getCreateTime() != null ? o.getCreateTime().toString() : null)
                    .build();
        });
    }

    /**
     * 批量查询账户所属用户，按用户ID组装 Map
     */
    private Map<Long, AuthUser> buildUserMap(List<PayUserAccount> accounts) {
        Set<Long> userIds = accounts.stream()
                .map(PayUserAccount::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return authUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(AuthUser::getId, u -> u));
    }
}
