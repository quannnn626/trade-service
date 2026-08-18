package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.domain.PayAccountFlow;
import com.boot.pay.flow.vo.FlowVO;
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
}
