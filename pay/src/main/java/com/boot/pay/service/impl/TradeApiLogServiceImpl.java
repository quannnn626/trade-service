package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.TradeApiLog;
import com.boot.pay.service.TradeApiLogService;
import com.boot.pay.mapper.TradeApiLogMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【trade_api_log(接口调用日志表)】的数据库操作Service实现
* @createDate 2026-08-02 19:57:29
*/
@Service
public class TradeApiLogServiceImpl extends ServiceImpl<TradeApiLogMapper, TradeApiLog>
    implements TradeApiLogService{

}




