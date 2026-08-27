package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.domain.PayPaymentOrder;
import com.boot.pay.payment.dto.CreatePayDTO;
import com.boot.pay.payment.dto.ExecutePayDTO;
import com.boot.pay.payment.vo.CreatePayVO;
import com.boot.pay.payment.vo.ExecutePayVO;
import com.boot.pay.payment.vo.PayOrderDetailVO;
import com.boot.pay.payment.vo.PayOrderListVO;
import com.boot.pay.payment.vo.PayOrderVO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDateTime;

/**
* @author quannnn
* @description 针对表【pay_payment_order(支付订单表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayPaymentOrderService extends IService<PayPaymentOrder> {

    /**
     * 创建支付订单
     *
     * @param dto        请求参数
     * @param merchantId 商户 ID
     * @param merchantNo 商户编号
     * @param clientIp   客户端 IP
     * @return 支付单号等信息
     */
    CreatePayVO createPayment(CreatePayDTO dto, Long merchantId, String merchantNo, String clientIp);

    /**
     * 按支付单号查询
     *
     * @param paymentNo  支付单号
     * @param merchantId 商户 ID（校验订单归属）
     * @return 订单详情
     */
    PayOrderVO queryByPaymentNo(String paymentNo, Long merchantId);

    /**
     * 按商户订单号查询
     *
     * @param orderNo    商户订单号
     * @param merchantId 商户 ID（校验订单归属）
     * @return 订单详情
     */
    PayOrderVO queryByOrderNo(String orderNo, Long merchantId);

    /**
     * 关闭超时未支付的订单
     *
     * @return 本次关闭的订单数量
     */
    int closeExpiredOrders();

    /**
     * 手动关单（运营后台）
     * <p>
     * 仅 WAIT_PAY 状态可关；条件更新（WHERE status = WAIT_PAY）保证并发下的幂等。
     *
     * @param paymentNo   支付单号
     * @param closeReason 关单原因（为空默认"手动关闭"）
     */
    void close(String paymentNo, String closeReason);

    /**
     * 支付订单分页列表（运营后台）
     * <p>
     * 筛选条件：支付单号/商户订单号/商户号模糊匹配，状态精确匹配，创建时间范围
     *
     * @param page      页码
     * @param pageSize  每页条数
     * @param paymentNo 支付单号（模糊）
     * @param orderNo   商户订单号（模糊）
     * @param merchantNo 商户编号（模糊，先转 merchantId 再查订单表）
     * @param status    支付状态
     * @param startTime 创建开始时间
     * @param endTime   创建结束时间
     * @return 分页列表
     */
    IPage<PayOrderListVO> listPage(Integer page, Integer pageSize, String paymentNo, String orderNo,
                                   String merchantNo, Integer status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 支付订单详情（运营后台）
     * <p>
     * 无归属校验，可查任意商户订单；回填商户/用户/渠道信息。
     * 关联的流水/回调/退款记录由 flow/notify/refund 查询接口按 paymentNo 复用，不在此返回。
     *
     * @param paymentNo 支付单号
     * @return 订单详情
     */
    PayOrderDetailVO detail(String paymentNo);

    /**
     * 执行支付（支付执行引擎核心入口）
     * <p>
     * 外层负责支付密码校验、分布式锁与乐观锁重试；资金操作在独立事务中执行，
     * 事务提交后释放锁，保证"锁内处理、事务内动账"。
     *
     * @param dto        请求参数
     * @param merchantId 商户 ID（校验订单归属）
     * @return 支付结果
     */
    ExecutePayVO executePayment(ExecutePayDTO dto, Long merchantId);

    /**
     * 支付资金操作（独立事务，动账必须全部成功或全部回滚）
     * <p>
     * 仅供 {@link #executePayment} 在锁内调用，通过代理调用使 @Transactional 生效，不对外暴露。
     *
     * @param dto        请求参数
     * @param merchantId 商户 ID（校验订单归属）
     * @return 支付结果
     */
    ExecutePayVO executePaymentTx(ExecutePayDTO dto, Long merchantId);
}
