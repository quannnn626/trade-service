package com.boot.pay.flow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boot.common.result.Result;
import com.boot.pay.flow.vo.FlowVO;
import com.boot.pay.service.PayAccountFlowService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
* @author quannnn
* @description 资金流水接口（平台内部管理用）
* @createDate 2026-08-18
*/
@RestController
@RequestMapping("/api/flow")
@RequiredArgsConstructor
public class FlowController {

    private final PayAccountFlowService payAccountFlowService;

    /**
     * 分页查询所有流水（可按账户类型、流水类型、时间范围筛选）
     */
    @GetMapping("/list")
    public Result<IPage<FlowVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer accountType,
            @RequestParam(required = false) Integer flowType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(payAccountFlowService.listPage(page, pageSize, accountType, flowType, startTime, endTime));
    }
}
