package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 账户资金流水表
 * @TableName trade_account_flow
 */
@TableName(value ="trade_account_flow")
@Data
public class TradeAccountFlow {
    /**
     * 流水ID
     */
    @TableId
    private Long id;

    /**
     * 流水号
     */
    private String flowNo;

    /**
     * 账户类型 1用户 2商户
     */
    private Integer accountType;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 关联支付单号
     */
    private String paymentNo;

    /**
     * 流水类型
     */
    private Integer flowType;

    /**
     * 变动金额
     */
    private BigDecimal amount;

    /**
     * 变更前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 变更后余额
     */
    private BigDecimal afterBalance;

    /**
     * 备注
     */
    private String remark;

    /**
     * 
     */
    private Date createTime;

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
        TradeAccountFlow other = (TradeAccountFlow) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFlowNo() == null ? other.getFlowNo() == null : this.getFlowNo().equals(other.getFlowNo()))
            && (this.getAccountType() == null ? other.getAccountType() == null : this.getAccountType().equals(other.getAccountType()))
            && (this.getAccountId() == null ? other.getAccountId() == null : this.getAccountId().equals(other.getAccountId()))
            && (this.getPaymentNo() == null ? other.getPaymentNo() == null : this.getPaymentNo().equals(other.getPaymentNo()))
            && (this.getFlowType() == null ? other.getFlowType() == null : this.getFlowType().equals(other.getFlowType()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getBeforeBalance() == null ? other.getBeforeBalance() == null : this.getBeforeBalance().equals(other.getBeforeBalance()))
            && (this.getAfterBalance() == null ? other.getAfterBalance() == null : this.getAfterBalance().equals(other.getAfterBalance()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFlowNo() == null) ? 0 : getFlowNo().hashCode());
        result = prime * result + ((getAccountType() == null) ? 0 : getAccountType().hashCode());
        result = prime * result + ((getAccountId() == null) ? 0 : getAccountId().hashCode());
        result = prime * result + ((getPaymentNo() == null) ? 0 : getPaymentNo().hashCode());
        result = prime * result + ((getFlowType() == null) ? 0 : getFlowType().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getBeforeBalance() == null) ? 0 : getBeforeBalance().hashCode());
        result = prime * result + ((getAfterBalance() == null) ? 0 : getAfterBalance().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", flowNo=").append(flowNo);
        sb.append(", accountType=").append(accountType);
        sb.append(", accountId=").append(accountId);
        sb.append(", paymentNo=").append(paymentNo);
        sb.append(", flowType=").append(flowType);
        sb.append(", amount=").append(amount);
        sb.append(", beforeBalance=").append(beforeBalance);
        sb.append(", afterBalance=").append(afterBalance);
        sb.append(", remark=").append(remark);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}