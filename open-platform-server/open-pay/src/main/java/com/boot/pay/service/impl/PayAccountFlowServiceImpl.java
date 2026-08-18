package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.account.constants.AccountConstants;
import com.boot.pay.account.enums.AccountFlowTypeEnum;
import com.boot.pay.domain.PayAccountFlow;
import com.boot.pay.domain.PayMerchantAccount;
import com.boot.pay.domain.PayUserAccount;
import com.boot.pay.flow.vo.FlowVO;
import com.boot.pay.mapper.PayAccountFlowMapper;
import com.boot.pay.mapper.PayMerchantAccountMapper;
import com.boot.pay.mapper.PayUserAccountMapper;
import com.boot.pay.service.PayAccountFlowService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_account_flow(账户资金流水表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
@RequiredArgsConstructor
public class PayAccountFlowServiceImpl extends ServiceImpl<PayAccountFlowMapper, PayAccountFlow>
    implements PayAccountFlowService {

    private final PayUserAccountMapper payUserAccountMapper;
    private final PayMerchantAccountMapper payMerchantAccountMapper;

    @Override
    public IPage<FlowVO> listPage(Integer page, Integer pageSize, Integer accountType, Integer flowType,
                                  LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<PayAccountFlow> wrapper = new LambdaQueryWrapper<>();
        if (accountType != null) {
            wrapper.eq(PayAccountFlow::getAccountType, accountType);
        }
        if (flowType != null) {
            wrapper.eq(PayAccountFlow::getFlowType, flowType);
        }
        if (startTime != null) {
            wrapper.ge(PayAccountFlow::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(PayAccountFlow::getCreateTime, endTime);
        }
        wrapper.orderByDesc(PayAccountFlow::getCreateTime);

        Page<PayAccountFlow> pageParam = new Page<>(page, pageSize);
        Page<PayAccountFlow> result = this.page(pageParam, wrapper);

        Map<String, String> accountNoMap = buildAccountNoMap(result.getRecords());

        return result.convert(f -> FlowVO.builder()
                .flowNo(f.getFlowNo())
                .accountType(f.getAccountType())
                .accountTypeName(buildAccountTypeName(f.getAccountType()))
                .accountNo(accountNoMap.get(buildAccountNoKey(f.getAccountType(), f.getAccountId())))
                .paymentNo(f.getPaymentNo())
                .flowType(f.getFlowType())
                .flowTypeName(buildFlowTypeName(f.getFlowType()))
                .amount(f.getAmount())
                .beforeBalance(f.getBeforeBalance())
                .afterBalance(f.getAfterBalance())
                .remark(f.getRemark())
                .createTime(f.getCreateTime() != null ? f.getCreateTime().toString() : null)
                .build());
    }

    /**
     * 批量回填账户编号：按账户类型分别查用户钱包/商户资金账户
     */
    private Map<String, String> buildAccountNoMap(List<PayAccountFlow> flows) {
        Set<Long> userIds = flows.stream()
                .filter(f -> AccountConstants.ACCOUNT_TYPE_USER == f.getAccountType())
                .map(PayAccountFlow::getAccountId)
                .collect(Collectors.toSet());
        Set<Long> merchantIds = flows.stream()
                .filter(f -> AccountConstants.ACCOUNT_TYPE_MERCHANT == f.getAccountType())
                .map(PayAccountFlow::getAccountId)
                .collect(Collectors.toSet());

        Map<Long, String> userNos = userIds.isEmpty() ? Map.of()
                : payUserAccountMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(PayUserAccount::getId, PayUserAccount::getAccountNo));
        Map<Long, String> merchantNos = merchantIds.isEmpty() ? Map.of()
                : payMerchantAccountMapper.selectBatchIds(merchantIds).stream()
                        .collect(Collectors.toMap(PayMerchantAccount::getId, PayMerchantAccount::getAccountNo));

        return flows.stream().collect(Collectors.toMap(
                f -> buildAccountNoKey(f.getAccountType(), f.getAccountId()),
                f -> f.getAccountType() == AccountConstants.ACCOUNT_TYPE_USER
                        ? userNos.get(f.getAccountId())
                        : merchantNos.get(f.getAccountId())));
    }

    private String buildAccountNoKey(Integer accountType, Long accountId) {
        return accountType + ":" + accountId;
    }

    private String buildAccountTypeName(Integer accountType) {
        if (accountType == null) {
            return null;
        }
        return accountType == AccountConstants.ACCOUNT_TYPE_USER ? "用户" : "商户";
    }

    private String buildFlowTypeName(Integer flowType) {
        AccountFlowTypeEnum e = AccountFlowTypeEnum.fromCode(flowType);
        return e == null ? null : e.getDesc();
    }
}
