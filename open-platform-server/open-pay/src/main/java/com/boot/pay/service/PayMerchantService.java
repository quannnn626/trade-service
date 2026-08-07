package com.boot.pay.service;

import com.boot.pay.domain.PayMerchant;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.merchant.dto.MerchantApplyDTO;
import com.boot.pay.merchant.dto.MerchantAuditDTO;
import com.boot.pay.merchant.vo.MerchantApplyVO;
import com.boot.pay.merchant.vo.MerchantAuditVO;

/**
* @author quannnn
* @description 针对表【pay_merchant(交易平台商户表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayMerchantService extends IService<PayMerchant> {

    /**
     * 商户入驻申请
     */
    MerchantApplyVO apply(MerchantApplyDTO dto);

    /**
     * 商户审核
     */
    MerchantAuditVO audit(MerchantAuditDTO dto);
}
