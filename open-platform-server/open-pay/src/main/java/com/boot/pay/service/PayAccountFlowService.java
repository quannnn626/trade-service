package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.domain.PayAccountFlow;
import com.boot.pay.flow.vo.DailySummaryVO;
import com.boot.pay.flow.vo.FlowVO;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
* @author quannnn
* @description 针对表【pay_account_flow(账户资金流水表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayAccountFlowService extends IService<PayAccountFlow> {

    /**
     * 分页查询资金流水（可按账户类型、流水类型、时间范围筛选）
     *
     * @param page        页码，从 1 开始
     * @param pageSize    每页条数
     * @param accountType 账户类型 1-用户 2-商户（可空）
     * @param flowType    流水类型（可空）
     * @param startTime   开始时间（可空）
     * @param endTime     结束时间（可空）
     */
    IPage<FlowVO> listPage(Integer page, Integer pageSize, Integer accountType, Integer flowType,
                           LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按账户编号分页查询资金流水（用户钱包 UA 开头，商户账户 MA 开头）
     *
     * @param accountNo 账户编号
     * @param page      页码，从 1 开始
     * @param pageSize  每页条数
     * @param flowType  流水类型（可空）
     * @param startTime 开始时间（可空）
     * @param endTime   结束时间（可空）
     */
    IPage<FlowVO> listPageByAccountNo(String accountNo, Integer page, Integer pageSize,
                                      Integer flowType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按支付单号分页查询资金流水（一笔支付对应多条流水：用户支出 + 商户收入 + 手续费）
     *
     * @param paymentNo 支付单号
     * @param page      页码，从 1 开始
     * @param pageSize  每页条数
     * @param flowType  流水类型（可空）
     * @param startTime 开始时间（可空）
     * @param endTime   结束时间（可空）
     */
    IPage<FlowVO> listPageByPaymentNo(String paymentNo, Integer page, Integer pageSize,
                                      Integer flowType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 日汇总报表：按流水类型分组汇总指定日期的流水（默认今天）
     *
     * @param date 汇总日期 yyyy-MM-dd（可空，默认当天）
     */
    DailySummaryVO dailySummary(LocalDate date);
}
