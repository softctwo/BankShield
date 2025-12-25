package com.bankshield.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解
 * 用于标记需要脱敏处理的字段或方法
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskData {
    
    /**
     * 敏感数据类型
     */
    String sensitiveType();
    
    /**
     * 应用场景（默认：页面展示）
     */
    String scenario() default "DISPLAY";
    
    /**
     * 是否启用脱敏（默认：启用）
     */
    boolean enabled() default true;
}