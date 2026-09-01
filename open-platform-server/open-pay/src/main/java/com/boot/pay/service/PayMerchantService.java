package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.domain.PayMerchant;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.merchant.dto.MerchantApplyDTO;
import com.boot.pay.merchant.dto.MerchantAuditDTO;
import com.boot.pay.merchant.vo.MerchantApplyVO;
import com.boot.pay.merchant.vo.MerchantAuditVO;
import com.boot.pay.merchant.vo.MerchantDetailVO;
import com.boot.pay.merchant.vo.MerchantAccountVO;
import com.boot.pay.merchant.vo.MerchantListVO;
import com.boot.pay.merchant.vo.MerchantSecretVO;

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
     * 商户资金账户查询（运营后台：商户账户列表页）
     * <p>
     * 返回完整资金信息（余额/冻结/累计收入/支出/手续费），比详情接口的账户字段更全。
     *
     * @param merchantNo 商户号
     * @return 商户资金账户信息
     */
    MerchantAccountVO getAccount(String merchantNo);

    /**
     * 商户分页列表
     */
    IPage<MerchantListVO> listPage(Integer page, Integer pageSize, String merchantName, Integer status, Integer auditStatus);

    /**
     * 密钥轮换
     */
    MerchantSecretVO rotateSecret(String merchantNo);
}
