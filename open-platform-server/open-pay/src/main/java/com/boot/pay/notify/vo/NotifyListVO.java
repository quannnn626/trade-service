package com.boot.pay.notify.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 回调通知列表项 VO（运营后台用）
 *
 * @author quannnn
 */
@Data
@Builder
public class NotifyListVO {

    /** 通知记录 ID（手动重试用） */
    private Long id;

    /** 支付单号 */
    private String paymentNo;

    /** 商户号（关联 pay_merchant 回填） */
    private String merchantNo;

    /** 商户名称（关联 pay_merchant 回填） */
    private String merchantName;

    /** 通知地址 */
    private String notifyUrl;

    /** 通知类型 code（1支付成功 2退款成功 3退款失败） */
    private Integer notifyType;

    /** 通知类型名称 */
    private String notifyTypeName;

    /** 通知状态 code（0待通知 1成功 2失败达上限） */
    private Integer notifyStatus;

    /** 通知状态名称 */
    private String notifyStatusName;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetry;

    /** 下次重试时间（成功或达上限后为 null） */
    private String nextRetryTime;

    /** 最后一次错误信息 */
    private String lastError;

    /** 请求参数 */
    private String requestData;

    /** 商户响应结果 */
    private String responseData;

    /** 创建时间 */
    private String createTime;

    /** 更新时间 */
    private String updateTime;
}
