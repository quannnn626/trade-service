package com.boot.pay.account.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Data;

/**
* @author quannnn
* @description 冻结账户资金请求（金额为空则冻结全部可用余额）
* @createDate 2026-08-25
*/
@Data
public class FreezeAccountDTO {

    /**
     * 冻结金额（可选，为空则冻结全部可用余额；有值时必须大于0）
     */
    @DecimalMin(value = "0.01", message = "冻结金额必须大于0")
    private BigDecimal amount;
}
