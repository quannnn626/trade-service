package com.boot.pay.mapper;

import com.boot.pay.domain.PayAccountFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
* @author quannnn
* @description 针对表【pay_account_flow(账户资金流水表)】的数据库操作Mapper
* @createDate 2026-08-03 12:26:43
* @Entity com.boot.pay.domain.PayAccountFlow
*/
public interface PayAccountFlowMapper extends BaseMapper<PayAccountFlow> {

    /**
     * 按流水类型汇总指定时间范围内的流水（笔数 + 金额合计）
     *
     * @param startTime 开始时间（含）
     * @param endTime   结束时间（不含）
     * @return 每行包含 flow_type / count / amount 三个 key
     */
    List<Map<String, Object>> sumByFlowType(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}
