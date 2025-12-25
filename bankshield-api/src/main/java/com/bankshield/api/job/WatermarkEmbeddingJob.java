package com.bankshield.api.job;

import com.bankshield.api.entity.WatermarkTask;
import com.bankshield.api.enums.TaskStatus;
import com.bankshield.api.service.WatermarkTaskService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 水印嵌入任务
 * 使用Quartz定时任务框架异步执行水印嵌入
 * 
 * @author BankShield
 */
@Slf4j
@Component
public class WatermarkEmbeddingJob implements Job {

    @Autowired
    private WatermarkTaskService taskService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行水印嵌入任务");
        
        try {
            // 获取待处理的任务
            List<WatermarkTask> pendingTasks = taskService.getPendingTasks(10);
            
            if (pendingTasks.isEmpty()) {
                log.info("没有待处理的水印任务");
                return;
            }
            
            log.info("找到 {} 个待处理的水印任务", pendingTasks.size());
            
            // 依次处理每个任务
            for (WatermarkTask task : pendingTasks) {
                try {
                    processTask(task);
                } catch (Exception e) {
                    log.error("处理水印任务失败，任务ID: {}", task.getId(), e);
                    // 更新任务状态为失败
                    task.setStatus(TaskStatus.FAILED.getCode());
                    task.setErrorMessage(e.getMessage());
                    task.setEndTime(LocalDateTime.now());
                    taskService.updateById(task);
                }
            }
            
            log.info("水印嵌入任务执行完成");
            
        } catch (Exception e) {
            log.error("执行水印嵌入任务失败", e);
            throw new JobExecutionException("执行水印嵌入任务失败", e);
        }
    }

    /**
     * 处理单个任务
     * 
     * @param task 水印任务
     */
    private void processTask(WatermarkTask task) {
        log.info("开始处理水印任务，任务ID: {}, 任务名称: {}", task.getId(), task.getTaskName());
        
        // 更新任务状态为运行中
        task.setStatus(TaskStatus.RUNNING.getCode());
        task.setStartTime(LocalDateTime.now());
        taskService.updateById(task);
        
        try {
            // 执行任务
            taskService.executeTask(task.getId());
            
            // 更新任务状态为成功
            task.setStatus(TaskStatus.SUCCESS.getCode());
            task.setEndTime(LocalDateTime.now());
            taskService.updateById(task);
            
            log.info("水印任务处理成功，任务ID: {}", task.getId());
            
        } catch (Exception e) {
            log.error("水印任务处理失败，任务ID: {}", task.getId(), e);
            
            // 更新任务状态为失败
            task.setStatus(TaskStatus.FAILED.getCode());
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            taskService.updateById(task);
        }
    }
}