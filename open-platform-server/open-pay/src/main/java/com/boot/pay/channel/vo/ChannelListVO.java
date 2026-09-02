package com.boot.pay.channel.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 支付渠道列表项 VO（运营后台用）
 *
 * @author quannnn
 */
@Data
@Builder
public class ChannelListVO {

    /** 渠道 ID */
    private Long id;

    /** 渠道编码（BALANCE/ALIPAY/WECHAT） */
    private String channelCode;

    /** 渠道名称 */
    private String channelName;

    /** 状态 code（0停用 1启用） */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 创建时间 */
    private String createTime;
}
