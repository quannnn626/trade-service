package com.boot.pay.account.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * 用户账户列表项 VO（运营后台用）
 *
 * @author quannnn
 */
@Data
@Builder
public class AccountListVO {

    /** 账户编号 */
    private String accountNo;

    /** 用户编号（关联 auth_user 回填） */
    private String userNo;

    /** 用户名（关联 auth_user 回填） */
    private String username;

    /** 手机号（关联 auth_user 回填） */
    private String phone;

    /** 账户余额 */
    private BigDecimal balance;

    /** 冻结金额 */
    private BigDecimal frozenAmount;

    /** 可用余额（balance - frozenAmount） */
    private BigDecimal availableBalance;

    /** 累计收入 */
    private BigDecimal totalIncome;

    /** 累计支出 */
    private BigDecimal totalExpense;

    /** 是否已实名认证 */
    private Boolean realNameAuth;

    /** 账户状态 code（1正常 0冻结） */
    private Integer status;

    /** 账户状态名称 */
    private String statusName;

    /** 创建时间 */
    private String createTime;
}
