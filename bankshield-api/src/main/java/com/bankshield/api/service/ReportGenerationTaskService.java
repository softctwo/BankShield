package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ReportGenerationTask;

import java.util.concurrent.Future;

/**
 * 报表生成任务服务接口
 */
public interface ReportGenerationTaskService {
    
    /**
     * 分页查询报表生成任务
     */
    IPage<ReportGenerationTask> getTaskPage(Page<ReportGenerationTask> page, Long templateId, String status, String createdBy);
    
    /**
     * 根据ID查询任务
     */
    ReportGenerationTask getTaskById(Long id);
    
    /**
     * 创建生成任务
     */
    ReportGenerationTask createTask(Long templateId, String createdBy, String reportPeriod);
    
    /**
     * 异步生成报表
     */
    Future<ReportGenerationTask> generateReportAsync(Long taskId);
    
    /**
     * 同步生成报表
     */
    ReportGenerationTask generateReport(Long taskId);
    
    /**
     * 查询任务状态
     */
    String getTaskStatus(Long taskId);
    
    /**
     * 下载报表文件
     */
    byte[] downloadReport(Long taskId);
    
    /**
     * 查询正在运行的任务数量
     */
    int countRunningTasks();
    
    /**
     * 获取最近完成的任务
     */
    ReportGenerationTask getLatestCompletedTask(Long templateId);
}