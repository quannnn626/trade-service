package com.boot.pay.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
* @author quannnn
* @description 解冻账户资金请求（金额必填，且不能超过已冻结金额）
* @createDate 2026-08-25
*/
@Data
public class UnfreezeAccountDTO {

    /**
     * 解冻金额（必填，且不能超过已冻结金额）
     */
    @NotNull(message = "解冻金额不能为空")
    @DecimalMin(value = "0.01", message = "解冻金额必须大于0")
    private BigDecimal amount;
}
