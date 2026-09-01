package com.boot.pay.merchant.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * 商户资金账户 VO（运营后台：商户账户列表页）
 *
 * @author quannnn
 */
@Data
@Builder
public class MerchantAccountVO {

    /** 商户号（关联 pay_merchant 回填） */
    private String merchantNo;

    /** 商户名称（关联 pay_merchant 回填） */
    private String merchantName;

    /** 资金账户编号 */
    private String accountNo;

    /** 账户余额 */
    private BigDecimal balance;

    /** 冻结金额 */
    private BigDecimal frozenAmount;

    /** 可用余额（balance - frozenAmount） */
    private BigDecimal availableBalance;

    /** 累计收入（用户付款） */
    private BigDecimal totalIncome;

    /** 累计支出（退款） */
    private BigDecimal totalExpense;

    /** 累计手续费（平台抽成） */
    private BigDecimal totalFee;

    /** 账户状态（1正常） */
    private Integer status;

    /** 创建时间 */
    private String createTime;
}
