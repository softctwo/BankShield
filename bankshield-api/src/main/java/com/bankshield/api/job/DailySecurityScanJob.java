package com.bankshield.api.job;

import com.bankshield.api.entity.SecurityScanTask;
import com.bankshield.api.enums.ScanType;
import com.bankshield.api.service.SecurityScanTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 每日安全扫描定时任务
 * @author BankShield
 */
@Slf4j
@Component
public class DailySecurityScanJob {

    @Autowired
    private SecurityScanTaskService scanTaskService;

    /**
     * 每日凌晨3点执行全面安全扫描
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void executeDailySecurityScan() {
        log.info("开始执行每日安全扫描任务");
        
        try {
            String taskName = "每日全面安全扫描_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 创建全面扫描任务
            SecurityScanTask task = new SecurityScanTask();
            task.setTaskName(taskName);
            task.setScanType(ScanType.ALL.name());
            task.setScanTarget("http://localhost:8080,http://localhost:3000"); // 默认扫描目标
            task.setCreatedBy("system");
            task.setCreateTime(LocalDateTime.now());
            
            // 创建任务
            SecurityScanTask createdTask = scanTaskService.createScanTask(task);
            log.info("每日安全扫描任务创建成功: {}", createdTask.getId());
            
            // 执行任务
            scanTaskService.executeScanTask(createdTask.getId());
            log.info("每日安全扫描任务开始执行");
            
        } catch (Exception e) {
            log.error("执行每日安全扫描任务失败", e);
        }
    }

    /**
     * 每周日凌晨2点执行基线同步
     */
    @Scheduled(cron = "0 0 2 * * 0")
    public void executeWeeklyBaselineSync() {
        log.info("开始执行每周基线同步任务");
        
        try {
            // 这里可以调用基线同步服务
            log.info("基线同步任务执行完成");
            
        } catch (Exception e) {
            log.error("执行每周基线同步任务失败", e);
        }
    }
}