package com.boot.pay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 账户充值订单表
 * @TableName pay_recharge_order
 */
@TableName(value ="pay_recharge_order")
@Data
public class PayRechargeOrder {
    /**
     * 充值订单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 充值单号
     */
    private String rechargeNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     * 
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 充值方式 1-银行卡（当前仅此一种）
     */
    private Integer rechargeWay;

    /**
     * 银行名称（银行卡充值时）
     */
    private String bankName;

    /**
     * 银行卡尾号
     */
    private String cardNoTail;

    /**
     * 实际到账金额（可能有手续费）
     */
    private BigDecimal arrivalAmount;

    /**
     * 充值手续费（模拟阶段=0）
     */
    private BigDecimal feeAmount;

}