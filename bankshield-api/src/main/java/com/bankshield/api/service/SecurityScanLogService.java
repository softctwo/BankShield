package com.bankshield.api.service;

import com.bankshield.api.entity.SecurityScanLog;

import java.util.List;

/**
 * 安全扫描任务日志服务接口
 * @author BankShield
 */
public interface SecurityScanLogService {
    
    /**
     * 记录日志
     * @param taskId 任务ID
     * @param message 日志内容
     * @param logLevel 日志级别
     */
    void log(Long taskId, String message, SecurityScanLog.LogLevel logLevel);
    
    /**
     * 记录INFO级别日志
     * @param taskId 任务ID
     * @param message 日志内容
     */
    void info(Long taskId, String message);
    
    /**
     * 记录WARN级别日志
     * @param taskId 任务ID
     * @param message 日志内容
     */
    void warn(Long taskId, String message);
    
    /**
     * 记录ERROR级别日志
     * @param taskId 任务ID
     * @param message 日志内容
     */
    void error(Long taskId, String message);
    
    /**
     * 记录DEBUG级别日志
     * @param taskId 任务ID
     * @param message 日志内容
     */
    void debug(Long taskId, String message);
    
    /**
     * 获取任务日志列表
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<SecurityScanLog> getTaskLogs(Long taskId);
    
    /**
     * 获取任务日志内容列表（用于前端展示）
     * @param taskId 任务ID
     * @return 日志内容列表
     */
    List<String> getTaskLogContents(Long taskId);
    
    /**
     * 删除任务日志
     * @param taskId 任务ID
     */
    void deleteTaskLogs(Long taskId);
    
    /**
     * 批量记录日志
     * @param logs 日志列表
     */
    void batchLog(List<SecurityScanLog> logs);
}