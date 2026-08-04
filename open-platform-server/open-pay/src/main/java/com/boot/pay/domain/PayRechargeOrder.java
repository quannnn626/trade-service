package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 账户充值订单表
 * @TableName pay_recharge_order
 */
@TableName(value ="pay_recharge_order")
@Data
public class PayRechargeOrder {
    /**
     * 充值订单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 充值单号
     */
    private String rechargeNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

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
        PayRechargeOrder other = (PayRechargeOrder) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRechargeNo() == null ? other.getRechargeNo() == null : this.getRechargeNo().equals(other.getRechargeNo()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getFinishTime() == null ? other.getFinishTime() == null : this.getFinishTime().equals(other.getFinishTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRechargeNo() == null) ? 0 : getRechargeNo().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getFinishTime() == null) ? 0 : getFinishTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", rechargeNo=").append(rechargeNo);
        sb.append(", userId=").append(userId);
        sb.append(", amount=").append(amount);
        sb.append(", status=").append(status);
        sb.append(", finishTime=").append(finishTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}