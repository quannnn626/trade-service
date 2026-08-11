package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 接口调用日志表
 * @TableName pay_api_log
 */
@TableName(value ="pay_api_log")
@Data
public class PayApiLog {
    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 商户编号
     */
    private String merchantNo;

    /**
     * 请求UA
     */
    private String userAgent;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 验签结果 0-通过 1-失败
     */
    private Integer signResult;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 耗时毫秒
     */
    private Integer costTime;

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
        PayApiLog other = (PayApiLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMerchantId() == null ? other.getMerchantId() == null : this.getMerchantId().equals(other.getMerchantId()))
            && (this.getApiName() == null ? other.getApiName() == null : this.getApiName().equals(other.getApiName()))
            && (this.getRequestMethod() == null ? other.getRequestMethod() == null : this.getRequestMethod().equals(other.getRequestMethod()))
            && (this.getRequestUrl() == null ? other.getRequestUrl() == null : this.getRequestUrl().equals(other.getRequestUrl()))
            && (this.getRequestParam() == null ? other.getRequestParam() == null : this.getRequestParam().equals(other.getRequestParam()))
            && (this.getResponseResult() == null ? other.getResponseResult() == null : this.getResponseResult().equals(other.getResponseResult()))
            && (this.getCostTime() == null ? other.getCostTime() == null : this.getCostTime().equals(other.getCostTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMerchantId() == null) ? 0 : getMerchantId().hashCode());
        result = prime * result + ((getApiName() == null) ? 0 : getApiName().hashCode());
        result = prime * result + ((getRequestMethod() == null) ? 0 : getRequestMethod().hashCode());
        result = prime * result + ((getRequestUrl() == null) ? 0 : getRequestUrl().hashCode());
        result = prime * result + ((getRequestParam() == null) ? 0 : getRequestParam().hashCode());
        result = prime * result + ((getResponseResult() == null) ? 0 : getResponseResult().hashCode());
        result = prime * result + ((getCostTime() == null) ? 0 : getCostTime().hashCode());
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
        sb.append(", merchantId=").append(merchantId);
        sb.append(", apiName=").append(apiName);
        sb.append(", requestMethod=").append(requestMethod);
        sb.append(", requestUrl=").append(requestUrl);
        sb.append(", requestParam=").append(requestParam);
        sb.append(", responseResult=").append(responseResult);
        sb.append(", costTime=").append(costTime);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}