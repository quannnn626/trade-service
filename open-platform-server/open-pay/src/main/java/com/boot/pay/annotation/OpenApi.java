package com.boot.pay.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开放 API 注解 — 打在 Controller 方法上，AOP 自动记录接口调用日志
 *
 * @author quannnn
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenApi {

    // 接口名称
    String value();
}
