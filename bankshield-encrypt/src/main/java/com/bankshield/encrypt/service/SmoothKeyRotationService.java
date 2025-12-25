package com.bankshield.encrypt.service;

import com.bankshield.common.result.Result;
import com.bankshield.encrypt.entity.KeyRotationPlan;

/**
 * 平滑密钥轮换服务接口
 * 实现零业务中断的密钥轮换机制
 */
public interface SmoothKeyRotationService {
    
    /**
     * 执行平滑密钥轮换
     * 
     * @param oldKeyId 旧密钥ID
     * @param rotationReason 轮换原因
     * @return 轮换计划结果
     */
    Result<KeyRotationPlan> executeSmoothRotation(Long oldKeyId, String rotationReason);
    
    /**
     * 进入双密钥活跃期
     * 
     * @param plan 轮换计划
     */
    void enterDualActivePhase(KeyRotationPlan plan);
    
    /**
     * 进入仅解密期
     * 
     * @param plan 轮换计划
     */
    void enterDecryptOnlyPhase(KeyRotationPlan plan);
    
    /**
     * 完成轮换
     * 
     * @param plan 轮换计划
     */
    void completeRotation(KeyRotationPlan plan);
    
    /**
     * 取消轮换
     * 
     * @param planId 轮换计划ID
     * @param reason 取消原因
     * @return 取消结果
     */
    Result<Void> cancelRotation(String planId, String reason);
    
    /**
     * 获取轮换计划详情
     * 
     * @param planId 轮换计划ID
     * @return 轮换计划详情
     */
    Result<KeyRotationPlan> getRotationPlan(String planId);
    
    /**
     * 检查轮换状态
     * 
     * @param keyId 密钥ID
     * @return 轮换状态
     */
    Result<String> checkRotationStatus(Long keyId);
    
    /**
     * 获取活跃的轮换计划
     * 
     * @return 活跃的轮换计划列表
     */
    Result<java.util.List<KeyRotationPlan>> getActiveRotationPlans();
}