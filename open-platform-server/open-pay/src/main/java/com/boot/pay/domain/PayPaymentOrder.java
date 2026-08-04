package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 支付订单表
 * @TableName pay_payment_order
 */
@TableName(value ="pay_payment_order")
@Data
public class PayPaymentOrder {
    /**
     * 支付订单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 付款用户
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态
     */
    private Integer status;

    /**
     * 支付过期时间
     */
    private Date expireTime;

    /**
     * 支付完成时间
     */
    private Date payTime;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 商城支付单号
     */
    private String merchantPaymentNo;

    /**
     * 支付渠道ID
     */
    private Long channelId;

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
        PayPaymentOrder other = (PayPaymentOrder) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPaymentNo() == null ? other.getPaymentNo() == null : this.getPaymentNo().equals(other.getPaymentNo()))
            && (this.getMerchantId() == null ? other.getMerchantId() == null : this.getMerchantId().equals(other.getMerchantId()))
            && (this.getOrderNo() == null ? other.getOrderNo() == null : this.getOrderNo().equals(other.getOrderNo()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getExpireTime() == null ? other.getExpireTime() == null : this.getExpireTime().equals(other.getExpireTime()))
            && (this.getPayTime() == null ? other.getPayTime() == null : this.getPayTime().equals(other.getPayTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getMerchantPaymentNo() == null ? other.getMerchantPaymentNo() == null : this.getMerchantPaymentNo().equals(other.getMerchantPaymentNo()))
            && (this.getChannelId() == null ? other.getChannelId() == null : this.getChannelId().equals(other.getChannelId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPaymentNo() == null) ? 0 : getPaymentNo().hashCode());
        result = prime * result + ((getMerchantId() == null) ? 0 : getMerchantId().hashCode());
        result = prime * result + ((getOrderNo() == null) ? 0 : getOrderNo().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getExpireTime() == null) ? 0 : getExpireTime().hashCode());
        result = prime * result + ((getPayTime() == null) ? 0 : getPayTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getMerchantPaymentNo() == null) ? 0 : getMerchantPaymentNo().hashCode());
        result = prime * result + ((getChannelId() == null) ? 0 : getChannelId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", paymentNo=").append(paymentNo);
        sb.append(", merchantId=").append(merchantId);
        sb.append(", orderNo=").append(orderNo);
        sb.append(", userId=").append(userId);
        sb.append(", amount=").append(amount);
        sb.append(", status=").append(status);
        sb.append(", expireTime=").append(expireTime);
        sb.append(", payTime=").append(payTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", merchantPaymentNo=").append(merchantPaymentNo);
        sb.append(", channelId=").append(channelId);
        sb.append("]");
        return sb.toString();
    }
}