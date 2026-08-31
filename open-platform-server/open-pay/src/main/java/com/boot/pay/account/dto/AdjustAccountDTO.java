package com.boot.pay.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 超管调账请求（运营后台）
 * <p>
 * accountType 1-用户账户 2-商户账户；amount 正数为加款、负数为扣款。
 *
 * @author quannnn
 */
@Data
public class AdjustAccountDTO {

    /** 账户类型 1-用户 2-商户 */
    @NotNull(message = "账户类型不能为空")
    private Integer accountType;

    /** 账户编号（UA 开头用户账户 / MA 开头商户账户） */
    @NotBlank(message = "账户编号不能为空")
    private String accountNo;

    /** 调账金额（正数加款、负数扣款，不能为 0） */
    @NotNull(message = "调账金额不能为空")
    private BigDecimal amount;

    /** 调账原因（审计留痕） */
    @NotBlank(message = "调账原因不能为空")
    @Size(max = 255, message = "调账原因不能超过255字")
    private String remark;
}
