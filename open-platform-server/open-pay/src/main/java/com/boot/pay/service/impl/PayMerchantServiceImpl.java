package com.boot.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.merchant.dto.MerchantApplyDTO;
import com.boot.pay.merchant.vo.MerchantApplyVO;
import com.boot.pay.service.PayMerchantService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_merchant(交易平台商户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Slf4j
@Service
public class PayMerchantServiceImpl extends ServiceImpl<PayMerchantMapper, PayMerchant>
        implements PayMerchantService {

    @Override
    public MerchantApplyVO apply(MerchantApplyDTO dto) {
        // 1. 生成商户编号: M + yyyyMMdd + Snowflake后6位
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long snowflakeId = IdUtil.getSnowflake(1, 1).nextId();
        String merchantNo = "M" + dateStr + String.valueOf(snowflakeId).substring(10);

        // 2. 生成 appKey = MD5(merchantNo + 当前时间戳) 取前16位
        String rawKey = merchantNo + System.currentTimeMillis();
        String appKey = SecureUtil.md5(rawKey).substring(0, 16);

        // 3. 生成 appSecret = UUID 无横线 32位
        String appSecret = IdUtil.fastSimpleUUID();

        // 4. 组装商户实体
        PayMerchant merchant = new PayMerchant();
        merchant.setMerchantNo(merchantNo);
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setMerchantType(dto.getMerchantType() != null ? dto.getMerchantType() : 1);
        merchant.setAppKey(appKey);
        merchant.setAppSecret(appSecret);
        merchant.setStatus(0); // 审核通过后才启用
        merchant.setNotifyUrl(dto.getNotifyUrl());

        merchant.setCompanyName(dto.getCompanyName());
        merchant.setBusinessLicense(dto.getBusinessLicense());
        merchant.setContactName(dto.getContactName());
        merchant.setContactPhone(dto.getContactPhone());
        merchant.setContactEmail(dto.getContactEmail());
        merchant.setSettleType(dto.getSettleType() != null ? dto.getSettleType() : 1);
        merchant.setSettleFeeRate(dto.getSettleFeeRate());
        merchant.setDailyLimit(dto.getDailyLimit());
        merchant.setSingleLimit(dto.getSingleLimit());
        merchant.setWhiteIpList(dto.getWhiteIpList());
        merchant.setRemark(dto.getRemark());

        merchant.setAuditStatus(0); // 待审核
        merchant.setSecretVersion(1);

        // 5. 写入数据库
        boolean saved = this.save(merchant);
        if (!saved) {
            throw new BusinessException("商户入驻失败，请稍后重试");
        }

        log.info("商户入驻申请成功: merchantNo={}, merchantName={}", merchantNo, dto.getMerchantName());

        // 6. 组装返回
        return MerchantApplyVO.builder()
                .merchantNo(merchantNo)
                .merchantName(dto.getMerchantName())
                .appKey(appKey)
                .appSecret(appSecret)
                .auditStatus(0)
                .settleFeeRate(dto.getSettleFeeRate())
                .tip("appSecret仅展示一次，请妥善保管！审核通过后商户才能正常使用。")
                .build();
    }
}
