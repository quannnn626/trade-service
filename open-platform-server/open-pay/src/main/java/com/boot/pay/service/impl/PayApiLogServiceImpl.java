package com.boot.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boot.pay.domain.PayApiLog;
import com.boot.pay.service.PayApiLogService;
import com.boot.pay.mapper.PayApiLogMapper;
import org.springframework.stereotype.Service;

/**
* @author quannnn
* @description 针对表【pay_api_log(接口调用日志表)】的数据库操作Service实现
* @createDate 2026-08-03 12:26:43
*/
@Service
public class PayApiLogServiceImpl extends ServiceImpl<PayApiLogMapper, PayApiLog>
    implements PayApiLogService{

}




