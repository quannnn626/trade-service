package com.boot.pay.account.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @author quannnn
* @description 用户账户返回对象
* @createDate 2026-08-13
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountVO {

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 可用余额（余额 - 冻结金额）
     */
    private BigDecimal availableBalance;

    /**
     * 累计收入
     */
    private BigDecimal totalIncome;

    /**
     * 累计支出
     */
    private BigDecimal totalExpense;

    /**
     * 是否已实名认证
     */
    private Boolean realNameAuth;

    /**
     * 账户状态 1正常 0冻结
     */
    private Integer status;
}
