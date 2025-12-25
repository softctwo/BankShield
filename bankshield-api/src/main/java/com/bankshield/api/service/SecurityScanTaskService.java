package com.bankshield.api.service;

import com.bankshield.api.entity.SecurityScanTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 安全扫描任务服务接口
 * @author BankShield
 */
public interface SecurityScanTaskService extends IService<SecurityScanTask> {

    /**
     * 创建扫描任务
     */
    SecurityScanTask createScanTask(SecurityScanTask task);

    /**
     * 执行扫描任务（异步）
     */
    void executeScanTask(Long taskId);

    /**
     * 停止扫描任务
     */
    void stopScanTask(Long taskId);

    /**
     * 获取扫描任务进度
     */
    int getScanProgress(Long taskId);

    /**
     * 获取扫描任务统计信息
     */
    Map<String, Object> getScanTaskStatistics();

    /**
     * 生成扫描报告
     */
    String generateScanReport(Long taskId);

    /**
     * 获取最近的扫描任务
     */
    List<SecurityScanTask> getRecentTasks(int limit);

    /**
     * 根据状态查询扫描任务
     */
    List<SecurityScanTask> getTasksByStatus(String status);

    /**
     * 更新扫描任务状态
     */
    void updateTaskStatus(Long taskId, String status, Integer progress, String errorMessage);

    /**
     * 完成扫描任务
     */
    void completeTask(Long taskId, String status, Integer riskCount, String reportPath);

    /**
     * 删除扫描任务
     */
    boolean deleteScanTask(Long taskId);

    /**
     * 批量删除扫描任务
     */
    boolean batchDeleteTasks(List<Long> taskIds);

    /**
     * 获取任务执行日志
     */
    List<String> getTaskExecutionLog(Long taskId);

    /**
     * 分页查询扫描任务
     */
    com.baomidou.mybatisplus.core.metadata.IPage<SecurityScanTask> getScanTasks(int page, int size, String taskName, String scanType, String status);
}