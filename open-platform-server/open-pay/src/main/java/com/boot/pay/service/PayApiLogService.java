package com.boot.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.pay.domain.PayApiLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boot.pay.log.vo.ApiLogListVO;
import java.time.LocalDateTime;

/**
* @author quannnn
* @description 针对表【pay_api_log(接口调用日志表)】的数据库操作Service
* @createDate 2026-08-03 12:26:43
*/
public interface PayApiLogService extends IService<PayApiLog> {

    /**
     * 接口日志分页列表（运营后台）
     * <p>
     * 筛选：商户号（模糊）/接口名（模糊）/验签结果/调用时间范围。
     *
     * @param page      页码
     * @param pageSize  每页条数
     * @param merchantNo 商户号（模糊）
     * @param apiName   接口名（模糊）
     * @param signResult 验签结果（0通过 1失败）
     * @param startTime 开始时间（可空）
     * @param endTime   结束时间（可空）
     * @return 分页列表
     */
    IPage<ApiLogListVO> listPage(Integer page, Integer pageSize, String merchantNo, String apiName,
                                 Integer signResult, LocalDateTime startTime, LocalDateTime endTime);
}
