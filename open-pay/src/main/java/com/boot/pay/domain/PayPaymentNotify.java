package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 支付回调通知记录表
 * @TableName pay_payment_notify
 */
@TableName(value ="pay_payment_notify")
@Data
public class PayPaymentNotify {
    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 通知地址
     */
    private String notifyUrl;

    /**
     * 请求参数
     */
    private String requestData;

    /**
     * 响应结果
     */
    private String responseData;

    /**
     * 通知状态
     */
    private Integer notifyStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 下次重试时间
     */
    private Date nextRetryTime;

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
        PayPaymentNotify other = (PayPaymentNotify) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPaymentNo() == null ? other.getPaymentNo() == null : this.getPaymentNo().equals(other.getPaymentNo()))
            && (this.getMerchantId() == null ? other.getMerchantId() == null : this.getMerchantId().equals(other.getMerchantId()))
            && (this.getNotifyUrl() == null ? other.getNotifyUrl() == null : this.getNotifyUrl().equals(other.getNotifyUrl()))
            && (this.getRequestData() == null ? other.getRequestData() == null : this.getRequestData().equals(other.getRequestData()))
            && (this.getResponseData() == null ? other.getResponseData() == null : this.getResponseData().equals(other.getResponseData()))
            && (this.getNotifyStatus() == null ? other.getNotifyStatus() == null : this.getNotifyStatus().equals(other.getNotifyStatus()))
            && (this.getRetryCount() == null ? other.getRetryCount() == null : this.getRetryCount().equals(other.getRetryCount()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getNextRetryTime() == null ? other.getNextRetryTime() == null : this.getNextRetryTime().equals(other.getNextRetryTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPaymentNo() == null) ? 0 : getPaymentNo().hashCode());
        result = prime * result + ((getMerchantId() == null) ? 0 : getMerchantId().hashCode());
        result = prime * result + ((getNotifyUrl() == null) ? 0 : getNotifyUrl().hashCode());
        result = prime * result + ((getRequestData() == null) ? 0 : getRequestData().hashCode());
        result = prime * result + ((getResponseData() == null) ? 0 : getResponseData().hashCode());
        result = prime * result + ((getNotifyStatus() == null) ? 0 : getNotifyStatus().hashCode());
        result = prime * result + ((getRetryCount() == null) ? 0 : getRetryCount().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getNextRetryTime() == null) ? 0 : getNextRetryTime().hashCode());
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
        sb.append(", notifyUrl=").append(notifyUrl);
        sb.append(", requestData=").append(requestData);
        sb.append(", responseData=").append(responseData);
        sb.append(", notifyStatus=").append(notifyStatus);
        sb.append(", retryCount=").append(retryCount);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", nextRetryTime=").append(nextRetryTime);
        sb.append("]");
        return sb.toString();
    }
}