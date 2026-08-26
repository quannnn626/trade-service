package com.boot.pay.service;

import com.boot.pay.domain.PayRechargeOrder;
import com.boot.pay.recharge.dto.RechargeCallbackDTO;
import com.boot.pay.recharge.dto.RechargeCreateDTO;
import com.boot.pay.recharge.vo.RechargeCallbackVO;
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

    /**
     * 充值到账（模拟银行通知）：锁 + 幂等 + 乐观锁加余额 + 写充值流水
     *
     * @param dto 到账通知参数
     * @return 到账结果（重复通知幂等返回成功）
     */
    RechargeCallbackVO callback(RechargeCallbackDTO dto);

    /**
     * 充值到账事务方法（幂等检查 + 渠道确认 + 加余额 + 写流水）
     * <p>
     * 只在 callback 锁内跨 bean 调用，保证锁在事务提交后释放，不对外暴露。
     *
     * @param rechargeNo 充值单号
     * @return 到账结果
     */
    RechargeCallbackVO callbackTx(String rechargeNo);
}
