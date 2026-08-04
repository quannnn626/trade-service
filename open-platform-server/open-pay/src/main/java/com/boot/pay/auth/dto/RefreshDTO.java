package com.boot.pay.auth.dto;

import lombok.Data;

/**
 * 刷新 Token 请求
 */
@Data
public class RefreshDTO {

    private Long userId;

    private String refreshToken;
}
