package com.bankshield.api.service;

import java.util.List;
import java.util.Map;

/**
 * 脱敏服务接口
 */
public interface DesensitizationService {
    
    /**
     * 单个值脱敏
     */
    String desensitize(String value, String ruleCode);
    
    /**
     * 批量值脱敏
     */
    List<String> batchDesensitize(List<String> values, String ruleCode);
    
    /**
     * 根据数据类型自动脱敏
     */
    String autoDesensitize(String value, String dataType);
    
    /**
     * 快捷脱敏 - 手机号
     */
    String desensitizePhone(String phone);
    
    /**
     * 快捷脱敏 - 身份证
     */
    String desensitizeIdCard(String idCard);
    
    /**
     * 快捷脱敏 - 银行卡
     */
    String desensitizeBankCard(String bankCard);
    
    /**
     * 快捷脱敏 - 邮箱
     */
    String desensitizeEmail(String email);
    
    /**
     * 快捷脱敏 - 姓名
     */
    String desensitizeName(String name);
    
    /**
     * 快捷脱敏 - 地址
     */
    String desensitizeAddress(String address);
    
    /**
     * 记录脱敏日志
     */
    void logDesensitization(String logType, String ruleCode, String userId, String userName, 
                           String targetTable, String targetField, String originalValue, 
                           String desensitizedValue, String algorithmType, int recordCount);
    
    /**
     * 获取脱敏统计
     */
    Map<String, Object> getStatistics(int days);
    
    /**
     * 获取用户脱敏统计
     */
    List<Map<String, Object>> getUserStatistics(int days);
    
    /**
     * 分页查询脱敏日志
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.bankshield.api.entity.DesensitizationLog> pageLogs(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.bankshield.api.entity.DesensitizationLog> page,
            String userName, String targetTable, String logType, String status, String startTime, String endTime);
    
    /**
     * 根据ID查询脱敏日志
     */
    com.bankshield.api.entity.DesensitizationLog getLogById(Long id);
    
    /**
     * 获取脱敏日志统计
     */
    Map<String, Object> getLogStatistics(String startTime, String endTime);
    
    /**
     * 导出脱敏日志
     */
    String exportLogs(String userName, String targetTable, String logType, String status, String startTime, String endTime);
    
    /**
     * 单条数据脱敏
     */
    String desensitizeSingle(String ruleCode, String testData);
    
    /**
     * 批量数据脱敏
     */
    List<Map<String, Object>> desensitizeBatch(String ruleCode, List<String> testDataList);
    
    /**
     * 快捷脱敏
     */
    String quickDesensitize(String dataType, String testData);
}
