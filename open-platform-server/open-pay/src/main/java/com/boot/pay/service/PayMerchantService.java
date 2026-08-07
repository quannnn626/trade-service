package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.domain.PayMerchant;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.merchant.dto.MerchantApplyDTO;
import com.boot.pay.merchant.dto.MerchantAuditDTO;
import com.boot.pay.merchant.vo.MerchantApplyVO;
import com.boot.pay.merchant.vo.MerchantAuditVO;
import com.boot.pay.merchant.vo.MerchantDetailVO;
import com.boot.pay.merchant.vo.MerchantListVO;

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

    /**
     * 启用商户
     */
    void enable(String merchantNo);

    /**
     * 禁用商户
     */
    void disable(String merchantNo);

    /**
     * 商户详情（含资金账户）
     */
    MerchantDetailVO detail(String merchantNo);

    /**
     * 商户分页列表
     */
    IPage<MerchantListVO> listPage(Integer page, Integer pageSize, String merchantName, Integer status, Integer auditStatus);
}
