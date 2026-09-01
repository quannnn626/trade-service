package com.boot.pay.log.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.common.result.Result;
import com.boot.pay.log.vo.ApiLogListVO;
import com.boot.pay.service.PayApiLogService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口日志管理接口（运营后台）
 *
 * @author quannnn
 */
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class ApiLogController {

    private final PayApiLogService payApiLogService;

    /**
     * 接口日志分页列表
     * 筛选：商户号（模糊）/接口名（模糊）/验签结果/调用时间范围
     */
    @GetMapping("/list")
    public Result<IPage<ApiLogListVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String merchantNo,
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) Integer signResult,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(payApiLogService.listPage(page, pageSize, merchantNo, apiName,
                signResult, startTime, endTime));
    }
}
