package com.boot.pay.service;

import com.boot.pay.domain.PayRechargeOrder;
import com.boot.pay.recharge.dto.RechargeCreateDTO;
import com.boot.pay.recharge.vo.RechargeCreateVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author quannnn
* @description 针对表【pay_recharge_order(账户充值订单表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayRechargeOrderService extends IService<PayRechargeOrder> {

    /**
     * 创建充值单：账户校验 + 生成充值单（WAIT_PAY）+ 路由充值渠道
     *
     * @param dto    充值请求
     * @param userId 当前登录用户ID
     * @return 充值单号 + 模拟银行收银台地址
     */
    RechargeCreateVO create(RechargeCreateDTO dto, Long userId);
}
