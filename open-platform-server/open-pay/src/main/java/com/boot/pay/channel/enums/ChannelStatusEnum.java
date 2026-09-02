package com.boot.pay.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道状态枚举
 *
 * @author quannnn
 */
@Getter
@AllArgsConstructor
public enum ChannelStatusEnum {

    DISABLE(0, "停用"),
    ENABLE(1, "启用");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static ChannelStatusEnum of(Integer code) {
        for (ChannelStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
