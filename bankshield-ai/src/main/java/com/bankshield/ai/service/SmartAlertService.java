package com.bankshield.ai.service;

import com.bankshield.ai.model.AlertClassificationResult;

/**
 * 智能告警服务接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
public interface SmartAlertService {

    /**
     * 智能分类告警
     * 
     * @param alertId 告警ID
     * @return 告警分类结果
     */
    AlertClassificationResult classifyAlert(Long alertId);
}