package com.bankshield.ai.service;

import com.bankshield.ai.model.ResourcePrediction;
import com.bankshield.ai.model.ThreatPrediction;

/**
 * 威胁预测服务接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
public interface ThreatPredictionService {

    /**
     * 预测下周资源使用情况
     * 
     * @return 资源使用预测结果
     */
    ResourcePrediction predictNextWeek();

    /**
     * 预测特定资源类型的使用情况
     * 
     * @param resourceType 资源类型
     * @param days 预测天数
     * @return 资源使用预测结果
     */
    ResourcePrediction predictResourceUsage(String resourceType, Integer days);

    /**
     * 预测威胁
     * 
     * @param days 预测天数
     * @return 威胁预测结果
     */
    ThreatPrediction predictThreats(Integer days);
}