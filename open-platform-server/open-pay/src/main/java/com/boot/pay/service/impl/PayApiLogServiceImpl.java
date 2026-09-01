package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayApiLog;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.log.enums.SignResultEnum;
import com.boot.pay.log.vo.ApiLogListVO;
import com.boot.pay.mapper.PayApiLogMapper;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.service.PayApiLogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_api_log(接口调用日志表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
@RequiredArgsConstructor
public class PayApiLogServiceImpl extends ServiceImpl<PayApiLogMapper, PayApiLog>
    implements PayApiLogService {

    private final PayMerchantMapper payMerchantMapper;

    @Override
    public IPage<ApiLogListVO> listPage(Integer page, Integer pageSize, String merchantNo, String apiName,
                                        Integer signResult, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<PayApiLog> wrapper = new LambdaQueryWrapper<>();
        if (merchantNo != null && !merchantNo.isBlank()) {
            wrapper.like(PayApiLog::getMerchantNo, merchantNo);
        }
        if (apiName != null && !apiName.isBlank()) {
            wrapper.like(PayApiLog::getApiName, apiName);
        }
        if (signResult != null) {
            wrapper.eq(PayApiLog::getSignResult, signResult);
        }
        if (startTime != null) {
            wrapper.ge(PayApiLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(PayApiLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(PayApiLog::getCreateTime);

        Page<PayApiLog> result = this.page(new Page<>(page, pageSize), wrapper);

        // 批量回填商户名称
        Map<Long, PayMerchant> merchantMap = buildMerchantMap(result.getRecords());

        return result.convert(o -> {
            SignResultEnum signEnum = SignResultEnum.of(o.getSignResult());
            PayMerchant merchant = merchantMap.get(o.getMerchantId());
            return ApiLogListVO.builder()
                    .id(o.getId())
                    .merchantNo(o.getMerchantNo())
                    .merchantName(merchant != null ? merchant.getMerchantName() : null)
                    .apiName(o.getApiName())
                    .requestMethod(o.getRequestMethod())
                    .requestUrl(o.getRequestUrl())
                    .signResult(o.getSignResult())
                    .signResultName(signEnum != null ? signEnum.getDesc() : "未知")
                    .costTime(o.getCostTime())
                    .requestParam(o.getRequestParam())
                    .responseResult(o.getResponseResult())
                    .errorMsg(o.getErrorMsg())
                    .createTime(o.getCreateTime() != null ? o.getCreateTime().toString() : null)
                    .build();
        });
    }

    /**
     * 批量查询日志所属商户，按商户ID组装 Map
     */
    private Map<Long, PayMerchant> buildMerchantMap(List<PayApiLog> logs) {
        Set<Long> merchantIds = logs.stream()
                .map(PayApiLog::getMerchantId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return payMerchantMapper.selectBatchIds(merchantIds).stream()
                .collect(Collectors.toMap(PayMerchant::getId, m -> m));
    }
}
