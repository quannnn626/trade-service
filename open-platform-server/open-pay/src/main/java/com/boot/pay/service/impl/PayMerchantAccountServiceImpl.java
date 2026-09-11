package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.account.enums.AccountStatusEnum;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.domain.PayMerchantAccount;
import com.boot.pay.mapper.PayMerchantAccountMapper;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.merchant.vo.MerchantAccountVO;
import com.boot.pay.service.PayMerchantAccountService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_merchant_account(商户资金账户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
@RequiredArgsConstructor
public class PayMerchantAccountServiceImpl extends ServiceImpl<PayMerchantAccountMapper, PayMerchantAccount>
    implements PayMerchantAccountService {

    private final PayMerchantMapper payMerchantMapper;

    @Override
    public IPage<MerchantAccountVO> listPage(Integer page, Integer pageSize,
                                             String accountNo, String merchantNo, String merchantName) {
        LambdaQueryWrapper<PayMerchantAccount> wrapper = new LambdaQueryWrapper<>();
        if (accountNo != null && !accountNo.isBlank()) {
            wrapper.like(PayMerchantAccount::getAccountNo, accountNo);
        }
        if ((merchantNo != null && !merchantNo.isBlank())
                || (merchantName != null && !merchantName.isBlank())) {
            // 商户号/商户名在 pay_merchant 表，先模糊查出 merchantId 集合
            LambdaQueryWrapper<PayMerchant> merchantWrapper = new LambdaQueryWrapper<>();
            if (merchantNo != null && !merchantNo.isBlank()) {
                merchantWrapper.like(PayMerchant::getMerchantNo, merchantNo);
            }
            if (merchantName != null && !merchantName.isBlank()) {
                merchantWrapper.like(PayMerchant::getMerchantName, merchantName);
            }
            List<Long> merchantIds = payMerchantMapper.selectList(merchantWrapper.select(PayMerchant::getId))
                    .stream().map(PayMerchant::getId).collect(Collectors.toList());
            if (merchantIds.isEmpty()) {
                // 无匹配商户，直接返回空页
                Page<MerchantAccountVO> empty = new Page<>(page, pageSize);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(PayMerchantAccount::getMerchantId, merchantIds);
        }
        wrapper.orderByDesc(PayMerchantAccount::getCreateTime);

        Page<PayMerchantAccount> result = this.page(new Page<>(page, pageSize), wrapper);

        // 批量回填商户号/商户名
        Map<Long, PayMerchant> merchantMap = buildMerchantMap(result.getRecords());

        return result.convert(o -> {
            PayMerchant merchant = merchantMap.get(o.getMerchantId());
            BigDecimal balance = o.getBalance() != null ? o.getBalance() : BigDecimal.ZERO;
            BigDecimal frozen = o.getFrozenAmount() != null ? o.getFrozenAmount() : BigDecimal.ZERO;
            AccountStatusEnum statusEnum = AccountStatusEnum.of(o.getStatus());
            return MerchantAccountVO.builder()
                    .merchantNo(merchant != null ? merchant.getMerchantNo() : null)
                    .merchantName(merchant != null ? merchant.getMerchantName() : null)
                    .accountNo(o.getAccountNo())
                    .balance(balance)
                    .frozenAmount(frozen)
                    .availableBalance(balance.subtract(frozen))
                    .totalIncome(o.getTotalIncome())
                    .totalExpense(o.getTotalExpense())
                    .totalFee(o.getTotalFee())
                    .status(o.getStatus())
                    .statusName(statusEnum != null ? statusEnum.getDesc() : "未知")
                    .createTime(o.getCreateTime() != null ? o.getCreateTime().toString() : null)
                    .build();
        });
    }

    /**
     * 批量查询账户所属商户，按商户ID组装 Map
     */
    private Map<Long, PayMerchant> buildMerchantMap(List<PayMerchantAccount> accounts) {
        Set<Long> merchantIds = accounts.stream()
                .map(PayMerchantAccount::getMerchantId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return payMerchantMapper.selectBatchIds(merchantIds).stream()
                .collect(Collectors.toMap(PayMerchant::getId, m -> m));
    }
}
