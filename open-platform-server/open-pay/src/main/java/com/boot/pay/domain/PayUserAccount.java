package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户钱包账户表
 * @TableName pay_user_account
 */
@TableName(value ="pay_user_account")
@Data
public class PayUserAccount {
    /**
     * 账户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 平台用户ID
     */
    private Long userId;

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
     * 账户状态 1正常 0冻结
     */
    private Integer status;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 关联商城用户ID
     */
    private Long mallUserId;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 累计收入
     */
    private BigDecimal totalIncome;

    /**
     * 累计支出
     */
    private BigDecimal totalExpense;

    /**
     * 支付密码（BCrypt加密）
     */
    private String payPassword;

    /**
     * 实名认证姓名
     */
    private String realName;

    /**
     * 实名认证身份证号
     */
    private String idCard;

    /**
     * 实名认证状态 0-未认证 1-已认证
     */
    private Integer realNameAuth;

    /**
     * 单日支付限额（未实名1000，实名后50000）
     */
    private BigDecimal dailyLimit;

    /**
     * 今日已支付金额
     */
    private BigDecimal dailyUsed;

    /**
     * 限额日期（用于重置日限额）
     */
    private Date dailyDate;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PayUserAccount other = (PayUserAccount) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAccountNo() == null ? other.getAccountNo() == null : this.getAccountNo().equals(other.getAccountNo()))
            && (this.getBalance() == null ? other.getBalance() == null : this.getBalance().equals(other.getBalance()))
            && (this.getFrozenAmount() == null ? other.getFrozenAmount() == null : this.getFrozenAmount().equals(other.getFrozenAmount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getMallUserId() == null ? other.getMallUserId() == null : this.getMallUserId().equals(other.getMallUserId()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getTotalIncome() == null ? other.getTotalIncome() == null : this.getTotalIncome().equals(other.getTotalIncome()))
            && (this.getTotalExpense() == null ? other.getTotalExpense() == null : this.getTotalExpense().equals(other.getTotalExpense()))
            && (this.getPayPassword() == null ? other.getPayPassword() == null : this.getPayPassword().equals(other.getPayPassword()))
            && (this.getRealName() == null ? other.getRealName() == null : this.getRealName().equals(other.getRealName()))
            && (this.getIdCard() == null ? other.getIdCard() == null : this.getIdCard().equals(other.getIdCard()))
            && (this.getRealNameAuth() == null ? other.getRealNameAuth() == null : this.getRealNameAuth().equals(other.getRealNameAuth()))
            && (this.getDailyLimit() == null ? other.getDailyLimit() == null : this.getDailyLimit().equals(other.getDailyLimit()))
            && (this.getDailyUsed() == null ? other.getDailyUsed() == null : this.getDailyUsed().equals(other.getDailyUsed()))
            && (this.getDailyDate() == null ? other.getDailyDate() == null : this.getDailyDate().equals(other.getDailyDate()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAccountNo() == null) ? 0 : getAccountNo().hashCode());
        result = prime * result + ((getBalance() == null) ? 0 : getBalance().hashCode());
        result = prime * result + ((getFrozenAmount() == null) ? 0 : getFrozenAmount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getMallUserId() == null) ? 0 : getMallUserId().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getTotalIncome() == null) ? 0 : getTotalIncome().hashCode());
        result = prime * result + ((getTotalExpense() == null) ? 0 : getTotalExpense().hashCode());
        result = prime * result + ((getPayPassword() == null) ? 0 : getPayPassword().hashCode());
        result = prime * result + ((getRealName() == null) ? 0 : getRealName().hashCode());
        result = prime * result + ((getIdCard() == null) ? 0 : getIdCard().hashCode());
        result = prime * result + ((getRealNameAuth() == null) ? 0 : getRealNameAuth().hashCode());
        result = prime * result + ((getDailyLimit() == null) ? 0 : getDailyLimit().hashCode());
        result = prime * result + ((getDailyUsed() == null) ? 0 : getDailyUsed().hashCode());
        result = prime * result + ((getDailyDate() == null) ? 0 : getDailyDate().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", accountNo=").append(accountNo);
        sb.append(", balance=").append(balance);
        sb.append(", frozenAmount=").append(frozenAmount);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", mallUserId=").append(mallUserId);
        sb.append(", version=").append(version);
        sb.append(", totalIncome=").append(totalIncome);
        sb.append(", totalExpense=").append(totalExpense);
        sb.append(", payPassword=").append(payPassword);
        sb.append(", realName=").append(realName);
        sb.append(", idCard=").append(idCard);
        sb.append(", realNameAuth=").append(realNameAuth);
        sb.append(", dailyLimit=").append(dailyLimit);
        sb.append(", dailyUsed=").append(dailyUsed);
        sb.append(", dailyDate=").append(dailyDate);
        sb.append("]");
        return sb.toString();
    }
}
