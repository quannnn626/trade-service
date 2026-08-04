package com.boot.pay.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String accessToken;

    private String refreshToken;

    private Long userId;

    private String username;

    private String nickname;
}
