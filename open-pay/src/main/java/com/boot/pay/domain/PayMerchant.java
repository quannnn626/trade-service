package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 交易平台商户表
 * @TableName pay_merchant
 */
@TableName(value ="pay_merchant")
@Data
public class PayMerchant {
    /**
     * 商户ID
     */
    @TableId
    private Long id;

    /**
     * 商户编号
     */
    private String merchantNo;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 商户类型
     */
    private Integer merchantType;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 状态
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
     * 默认回调通知地址
     */
    private String notifyUrl;

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
        PayMerchant other = (PayMerchant) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMerchantNo() == null ? other.getMerchantNo() == null : this.getMerchantNo().equals(other.getMerchantNo()))
            && (this.getMerchantName() == null ? other.getMerchantName() == null : this.getMerchantName().equals(other.getMerchantName()))
            && (this.getMerchantType() == null ? other.getMerchantType() == null : this.getMerchantType().equals(other.getMerchantType()))
            && (this.getAppKey() == null ? other.getAppKey() == null : this.getAppKey().equals(other.getAppKey()))
            && (this.getAppSecret() == null ? other.getAppSecret() == null : this.getAppSecret().equals(other.getAppSecret()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getNotifyUrl() == null ? other.getNotifyUrl() == null : this.getNotifyUrl().equals(other.getNotifyUrl()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMerchantNo() == null) ? 0 : getMerchantNo().hashCode());
        result = prime * result + ((getMerchantName() == null) ? 0 : getMerchantName().hashCode());
        result = prime * result + ((getMerchantType() == null) ? 0 : getMerchantType().hashCode());
        result = prime * result + ((getAppKey() == null) ? 0 : getAppKey().hashCode());
        result = prime * result + ((getAppSecret() == null) ? 0 : getAppSecret().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getNotifyUrl() == null) ? 0 : getNotifyUrl().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", merchantNo=").append(merchantNo);
        sb.append(", merchantName=").append(merchantName);
        sb.append(", merchantType=").append(merchantType);
        sb.append(", appKey=").append(appKey);
        sb.append(", appSecret=").append(appSecret);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", notifyUrl=").append(notifyUrl);
        sb.append("]");
        return sb.toString();
    }
}