package com.bankshield.api.interceptor;

import com.bankshield.api.annotation.MaskData;
import com.bankshield.api.service.DataMaskingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.*;

/**
 * MyBatis数据脱敏拦截器
 * 自动对查询结果中的敏感数据进行脱敏处理
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@RequiredArgsConstructor
public class DataMaskingInterceptor implements Interceptor {

    private final DataMaskingEngine maskingEngine;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 执行原始方法，获取查询结果
        Object resultObject = invocation.proceed();
        
        if (resultObject == null) {
            return resultObject;
        }

        try {
            // 处理结果集
            if (resultObject instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> resultList = (List<Object>) resultObject;
                for (Object item : resultList) {
                    processObject(item);
                }
            } else {
                processObject(resultObject);
            }
        } catch (Exception e) {
            log.error("数据脱敏处理失败", e);
            // 脱敏失败时不影响原始数据
        }

        return resultObject;
    }

    /**
     * 处理单个对象的数据脱敏
     */
    private void processObject(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // 检查是否有脱敏注解
            MaskData maskData = field.getAnnotation(MaskData.class);
            if (maskData != null && maskData.enabled()) {
                processField(obj, field, maskData);
            }
        }
    }

    /**
     * 处理单个字段的脱敏
     */
    private void processField(Object obj, Field field, MaskData maskData) {
        try {
            field.setAccessible(true);
            Object value = field.get(obj);
            
            if (value == null || !(value instanceof String)) {
                return;
            }

            String originalData = (String) value;
            String sensitiveType = maskData.sensitiveType();
            String scenario = maskData.scenario();

            // 使用脱敏引擎进行脱敏
            String maskedData = maskingEngine.maskByType(originalData, sensitiveType, scenario);
            
            // 更新字段值
            if (!originalData.equals(maskedData)) {
                field.set(obj, maskedData);
                log.debug("字段脱敏完成：{} -> {}", originalData, maskedData);
            }
        } catch (Exception e) {
            log.error("字段脱敏失败：{}.{}", obj.getClass().getSimpleName(), field.getName(), e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以在这里配置一些属性
    }
}