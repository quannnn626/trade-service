package com.boot.pay.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
* @author quannnn
* @description 设置/修改支付密码请求
* @createDate 2026-08-13
*/
@Data
public class SetPayPasswordDTO {

    /**
     * 原支付密码（首次设置时可不传，已设置过的账户修改时必须传）
     */
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    private String oldPayPassword;

    /**
     * 新支付密码（6位数字）
     */
    @NotBlank(message = "支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    private String payPassword;
}
