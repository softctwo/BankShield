package com.bankshield.api.aspect;

import com.bankshield.api.annotation.MaskData;
import com.bankshield.api.service.DataMaskingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 数据脱敏切面
 * 对标记了@MaskData注解的方法进行结果脱敏处理
 */
@Slf4j
@Aspect
//@Component
@RequiredArgsConstructor
public class DataMaskingAspect {

    private final DataMaskingEngine maskingEngine;

    /**
     * 定义切点：所有标记了@MaskData注解的方法
     */
    @Pointcut("@annotation(com.bankshield.api.annotation.MaskData)")
    public void maskDataPointcut() {}

    /**
     * 环绕通知：处理方法返回值的数据脱敏
     */
    @Around("maskDataPointcut() && @annotation(maskData)")
    public Object around(ProceedingJoinPoint joinPoint, MaskData maskData) throws Throwable {
        // 执行原始方法
        Object result = joinPoint.proceed();
        
        if (result == null || !maskData.enabled()) {
            return result;
        }

        try {
            // 根据返回类型进行不同的脱敏处理
            if (result instanceof String) {
                return processString((String) result, maskData);
            } else if (result instanceof List) {
                return processList((List<?>) result, maskData);
            } else if (result instanceof Map) {
                return processMap((Map<?, ?>) result, maskData);
            } else {
                // 其他类型，尝试作为单个对象处理
                return processObject(result, maskData);
            }
        } catch (Exception e) {
            log.error("方法级数据脱敏失败：{}", joinPoint.getSignature(), e);
            return result; // 脱敏失败时返回原始结果
        }
    }

    /**
     * 处理字符串类型的脱敏
     */
    private String processString(String data, MaskData maskData) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        return maskingEngine.maskByType(data, maskData.sensitiveType(), maskData.scenario());
    }

    /**
     * 处理List类型的脱敏
     */
    private List<?> processList(List<?> dataList, MaskData maskData) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        List<Object> resultList = new ArrayList<>();
        for (Object item : dataList) {
            if (item instanceof String) {
                resultList.add(processString((String) item, maskData));
            } else {
                resultList.add(processObject(item, maskData));
            }
        }
        
        return resultList;
    }

    /**
     * 处理Map类型的脱敏
     */
    private Map<?, ?> processMap(Map<?, ?> dataMap, MaskData maskData) {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }

        Map<Object, Object> resultMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
            Object value = entry.getValue();
            Object processedValue;
            
            if (value instanceof String) {
                processedValue = processString((String) value, maskData);
            } else if (value instanceof List) {
                processedValue = processList((List<?>) value, maskData);
            } else {
                processedValue = processObject(value, maskData);
            }
            
            resultMap.put(entry.getKey(), processedValue);
        }
        
        return resultMap;
    }

    /**
     * 处理对象类型的脱敏
     */
    private Object processObject(Object obj, MaskData maskData) {
        if (obj == null) {
            return null;
        }

        // 如果是字符串，直接脱敏
        if (obj instanceof String) {
            return processString((String) obj, maskData);
        }

        // 对于复杂对象，这里可以实现更复杂的逻辑
        // 比如检查对象的特定字段，或者使用反射处理等
        
        return obj;
    }
}