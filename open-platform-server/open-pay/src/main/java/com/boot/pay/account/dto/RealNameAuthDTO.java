package com.boot.pay.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
* @author quannnn
* @description 实名认证请求
* @createDate 2026-08-14
*/
@Data
public class RealNameAuthDTO {

    /**
     * 实名认证姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 32, message = "姓名最多32位")
    private String realName;

    /**
     * 身份证号（18位，末位可为X）
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    /**
     * 支付密码（6位数字，用于身份确认）
     */
    @NotBlank(message = "支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    private String payPassword;
}
