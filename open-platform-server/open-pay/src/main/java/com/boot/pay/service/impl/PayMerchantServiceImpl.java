package com.boot.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.common.exception.BusinessException;
import com.boot.pay.domain.PayMerchant;
import com.boot.pay.domain.PayMerchantAccount;
import com.boot.pay.mapper.PayMerchantAccountMapper;
import com.boot.pay.mapper.PayMerchantMapper;
import com.boot.pay.merchant.dto.MerchantApplyDTO;
import com.boot.pay.merchant.dto.MerchantAuditDTO;
import com.boot.pay.merchant.vo.MerchantApplyVO;
import com.boot.pay.merchant.vo.MerchantAuditVO;
import com.boot.pay.service.PayMerchantService;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author quannnn
* @description 针对表【pay_merchant(交易平台商户表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Slf4j
@Service
public class PayMerchantServiceImpl extends ServiceImpl<PayMerchantMapper, PayMerchant>
        implements PayMerchantService {

    @Resource
    private PayMerchantAccountMapper payMerchantAccountMapper;

    @Override
    public MerchantApplyVO apply(MerchantApplyDTO dto) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long snowflakeId = IdUtil.getSnowflake(1, 1).nextId();
        String merchantNo = "M" + dateStr + String.valueOf(snowflakeId).substring(10);

        String rawKey = merchantNo + System.currentTimeMillis();
        String appKey = SecureUtil.md5(rawKey).substring(0, 16);

        String appSecret = IdUtil.fastSimpleUUID();

        PayMerchant merchant = new PayMerchant();
        merchant.setMerchantNo(merchantNo);
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setMerchantType(dto.getMerchantType() != null ? dto.getMerchantType() : 1);
        merchant.setAppKey(appKey);
        merchant.setAppSecret(appSecret);
        merchant.setStatus(0);
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

        merchant.setAuditStatus(0);
        merchant.setSecretVersion(1);

        boolean saved = this.save(merchant);
        if (!saved) {
            throw new BusinessException("商户入驻失败，请稍后重试");
        }

        log.info("商户入驻申请成功: merchantNo={}, merchantName={}", merchantNo, dto.getMerchantName());

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantAuditVO audit(MerchantAuditDTO dto) {
        PayMerchant merchant = this.getOne(
                new LambdaQueryWrapper<PayMerchant>()
                        .eq(PayMerchant::getMerchantNo, dto.getMerchantNo())
        );
        if (merchant == null) {
            throw new BusinessException("商户不存在: " + dto.getMerchantNo());
        }

        if (merchant.getAuditStatus() != 0) {
            throw new BusinessException("该商户已审核，不可重复审核");
        }

        Integer auditStatus = dto.getAuditStatus();

        if (auditStatus == 1) {
            return approve(merchant, dto);
        } else if (auditStatus == 2) {
            return reject(merchant, dto);
        } else {
            throw new BusinessException("审核结果无效，仅支持 1-通过 2-驳回");
        }
    }

    /**
     * 审核通过：启用商户 + 创建资金账户
     */
    private MerchantAuditVO approve(PayMerchant merchant, MerchantAuditDTO dto) {
        String merchantNo = merchant.getMerchantNo();

        merchant.setAuditStatus(1);
        merchant.setStatus(1);
        merchant.setAuditRemark(dto.getAuditRemark());
        boolean updated = this.updateById(merchant);
        if (!updated) {
            throw new BusinessException("更新商户状态失败");
        }

        String accountNo = "MA" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.valueOf(IdUtil.getSnowflake(1, 1).nextId()).substring(10);

        PayMerchantAccount account = new PayMerchantAccount();
        account.setMerchantId(merchant.getId());
        account.setAccountNo(accountNo);
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setStatus(1);
        account.setVersion(0);

        int inserted = payMerchantAccountMapper.insert(account);
        if (inserted == 0) {
            throw new BusinessException("创建商户资金账户失败");
        }

        log.info("商户审核通过: merchantNo={}, accountNo={}", merchantNo, accountNo);

        return MerchantAuditVO.builder()
                .merchantNo(merchantNo)
                .auditStatus(1)
                .status(1)
                .accountNo(accountNo)
                .auditRemark(dto.getAuditRemark())
                .build();
    }

    /**
     * 审核驳回：记录驳回原因，商户保持禁用状态
     */
    private MerchantAuditVO reject(PayMerchant merchant, MerchantAuditDTO dto) {
        if (dto.getAuditRemark() == null || dto.getAuditRemark().isBlank()) {
            throw new BusinessException("审核驳回时，驳回原因不能为空");
        }

        merchant.setAuditStatus(2);
        merchant.setStatus(0);
        merchant.setAuditRemark(dto.getAuditRemark());
        boolean updated = this.updateById(merchant);
        if (!updated) {
            throw new BusinessException("更新商户审核状态失败");
        }

        log.info("商户审核驳回: merchantNo={}, reason={}", merchant.getMerchantNo(), dto.getAuditRemark());

        return MerchantAuditVO.builder()
                .merchantNo(merchant.getMerchantNo())
                .auditStatus(2)
                .status(0)
                .accountNo(null)
                .auditRemark(dto.getAuditRemark())
                .build();
    }
}
