package com.bankshield.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bankshield.api.entity.ReportTemplate;
import com.bankshield.api.entity.ReportGenerationTask;
import com.bankshield.api.entity.ReportContent;
import com.bankshield.api.enums.TaskStatus;
import com.bankshield.api.mapper.ReportTemplateMapper;
import com.bankshield.api.mapper.ReportGenerationTaskMapper;
import com.bankshield.api.mapper.ReportContentMapper;
import com.bankshield.api.service.ReportGenerationTaskService;
import com.bankshield.api.service.ReportGenerationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 报表生成任务服务实现类
 */
@Slf4j
@Service
public class ReportGenerationTaskServiceImpl extends ServiceImpl<ReportGenerationTaskMapper, ReportGenerationTask> implements ReportGenerationTaskService {
    
    @Autowired
    private ReportTemplateMapper reportTemplateMapper;
    
    @Autowired
    private ReportGenerationTaskMapper reportGenerationTaskMapper;
    
    @Autowired
    private ReportContentMapper reportContentMapper;
    
    @Autowired
    private ReportGenerationEngine reportGenerationEngine;
    
    @Override
    public IPage<ReportGenerationTask> getTaskPage(Page<ReportGenerationTask> page, Long templateId, String status, String createdBy) {
        LambdaQueryWrapper<ReportGenerationTask> wrapper = new LambdaQueryWrapper<>();
        
        if (templateId != null) {
            wrapper.eq(ReportGenerationTask::getTemplateId, templateId);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(ReportGenerationTask::getStatus, status);
        }
        
        if (createdBy != null && !createdBy.trim().isEmpty()) {
            wrapper.eq(ReportGenerationTask::getCreatedBy, createdBy);
        }
        
        wrapper.orderByDesc(ReportGenerationTask::getCreateTime);
        return reportGenerationTaskMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ReportGenerationTask getTaskById(Long id) {
        return reportGenerationTaskMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public ReportGenerationTask createTask(Long templateId, String createdBy, String reportPeriod) {
        // 检查模板是否存在
        ReportTemplate template = reportTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("报表模板不存在");
        }
        
        if (!template.getEnabled()) {
            throw new RuntimeException("报表模板已禁用");
        }
        
        // 检查是否已有正在运行的任务
        if (reportGenerationTaskMapper.countRunningTasks() > 0) {
            throw new RuntimeException("已有正在运行的报表生成任务");
        }
        
        // 创建任务
        ReportGenerationTask task = new ReportGenerationTask();
        task.setTemplateId(templateId);
        task.setStatus(TaskStatus.PENDING.name());
        task.setCreatedBy(createdBy);
        task.setReportPeriod(reportPeriod);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        reportGenerationTaskMapper.insert(task);
        log.info("创建报表生成任务成功: templateId={}, taskId={}", templateId, task.getId());
        
        return task;
    }
    
    @Override
    @Async
    public Future<ReportGenerationTask> generateReportAsync(Long taskId) {
        try {
            ReportGenerationTask task = generateReport(taskId);
            return new AsyncResult<>(task);
        } catch (Exception e) {
            log.error("异步生成报表失败: {}", e.getMessage(), e);
            throw new RuntimeException("异步生成报表失败", e);
        }
    }
    
    @Override
    @Transactional
    public ReportGenerationTask generateReport(Long taskId) {
        ReportGenerationTask task = reportGenerationTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("报表生成任务不存在");
        }
        
        ReportTemplate template = reportTemplateMapper.selectById(task.getTemplateId());
        if (template == null) {
            throw new RuntimeException("报表模板不存在");
        }
        
        try {
            // 更新任务状态为运行中
            task.setStatus(TaskStatus.RUNNING.name());
            task.setStartTime(LocalDateTime.now());
            reportGenerationTaskMapper.updateById(task);
            
            log.info("开始生成报表: taskId={}, templateName={}", taskId, template.getTemplateName());
            
            // 生成报表内容
            Map<String, Object> additionalData = new HashMap<>();
            String htmlContent = reportGenerationEngine.generateReport(template, task, additionalData);
            
            // 保存报表章节内容
            saveReportContent(task, htmlContent);
            
            // 生成PDF文件
            byte[] pdfContent = reportGenerationEngine.generatePdfReport(htmlContent);
            String pdfFilePath = saveReportFile(pdfContent, template.getTemplateName(), "pdf");
            
            // 更新任务状态为成功
            task.setStatus(TaskStatus.SUCCESS.name());
            task.setEndTime(LocalDateTime.now());
            task.setReportFilePath(pdfFilePath);
            task.setReportData(htmlContent);
            reportGenerationTaskMapper.updateById(task);
            
            log.info("报表生成成功: taskId={}, filePath={}", taskId, pdfFilePath);
            return task;
            
        } catch (Exception e) {
            log.error("报表生成失败: {}", e.getMessage(), e);
            
            // 更新任务状态为失败
            task.setStatus(TaskStatus.FAILED.name());
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
            reportGenerationTaskMapper.updateById(task);
            
            throw new RuntimeException("报表生成失败", e);
        }
    }
    
    private void saveReportContent(ReportGenerationTask task, String htmlContent) {
        // 这里简化处理，实际应该解析HTML内容并保存各个章节
        ReportContent content = new ReportContent();
        content.setTaskId(task.getId());
        content.setChapterName("完整报表");
        content.setChapterData(htmlContent);
        content.setSortOrder(1);
        content.setGenerationTime(LocalDateTime.now());
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        
        reportContentMapper.insert(content);
    }
    
    private String saveReportFile(byte[] content, String fileName, String fileType) {
        try {
            // 确保输出目录存在
            File outputDir = new File("reports");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 生成文件路径
            String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMdd_HHmmss");
            String filePath = outputDir.getAbsolutePath() + File.separator + fileName + "_" + timestamp + "." + fileType;

            // 保存文件 - 使用文件路径
            java.nio.file.Files.write(Paths.get(filePath), content);

            log.info("报表文件保存成功: {}", filePath);
            return filePath;
            
        } catch (Exception e) {
            log.error("保存报表文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存报表文件失败", e);
        }
    }
    
    @Override
    public String getTaskStatus(Long taskId) {
        ReportGenerationTask task = reportGenerationTaskMapper.selectById(taskId);
        return task != null ? task.getStatus() : null;
    }
    
    @Override
    public byte[] downloadReport(Long taskId) {
        ReportGenerationTask task = reportGenerationTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("报表生成任务不存在");
        }
        
        if (!TaskStatus.SUCCESS.name().equals(task.getStatus())) {
            throw new RuntimeException("报表尚未生成完成");
        }
        
        String filePath = task.getReportFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new RuntimeException("报表文件路径不存在");
        }

        try {
            return java.nio.file.Files.readAllBytes(Paths.get(filePath));
        } catch (Exception e) {
            log.error("读取报表文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("读取报表文件失败", e);
        }
    }
    
    @Override
    public int countRunningTasks() {
        return reportGenerationTaskMapper.countRunningTasks();
    }
    
    @Override
    public ReportGenerationTask getLatestCompletedTask(Long templateId) {
        return reportGenerationTaskMapper.selectLatestCompletedTask(templateId);
    }
}