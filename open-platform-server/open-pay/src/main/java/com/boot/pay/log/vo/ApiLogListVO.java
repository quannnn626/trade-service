package com.boot.pay.log.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 接口日志列表项 VO（运营后台用）
 *
 * @author quannnn
 */
@Data
@Builder
public class ApiLogListVO {

    /** 日志 ID */
    private Long id;

    /** 商户号 */
    private String merchantNo;

    /** 商户名称（关联 pay_merchant 回填） */
    private String merchantName;

    /** 接口名称（如 pay.create） */
    private String apiName;

    /** 请求方式 */
    private String requestMethod;

    /** 请求地址 */
    private String requestUrl;

    /** 验签结果 code（0通过 1失败） */
    private Integer signResult;

    /** 验签结果名称 */
    private String signResultName;

    /** 耗时毫秒 */
    private Integer costTime;

    /** 请求参数 */
    private String requestParam;

    /** 响应结果 */
    private String responseResult;

    /** 错误信息 */
    private String errorMsg;

    /** 调用时间 */
    private String createTime;
}
