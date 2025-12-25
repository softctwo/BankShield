package com.bankshield.encrypt.job;

import com.bankshield.encrypt.service.KeyRotationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 密钥轮换定时任务
 * 每天凌晨2点执行
 */
@Slf4j
@Component
public class KeyRotationJob implements Job {
    
    @Autowired
    private KeyRotationService keyRotationService;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("密钥轮换定时任务开始执行");
        
        try {
            // 执行密钥轮换
            KeyRotationService.RotationResult rotationResult = keyRotationService.performRotationTask();
            log.info("密钥轮换任务执行完成：{}", rotationResult.getMessage());
            
            // 检查即将过期的密钥（30天内）
            KeyRotationService.ExpirationCheckResult expirationResult = keyRotationService.checkExpiringKeys(30);
            log.info("过期密钥检查完成：{}", expirationResult.getMessage());
            
            // 可以在这里添加发送报告的逻辑
            sendJobReport(rotationResult, expirationResult);
            
        } catch (Exception e) {
            log.error("密钥轮换定时任务执行失败", e);
            throw new JobExecutionException("密钥轮换定时任务执行失败", e);
        }
    }
    
    /**
     * 发送任务执行报告
     */
    private void sendJobReport(KeyRotationService.RotationResult rotationResult,
                              KeyRotationService.ExpirationCheckResult expirationResult) {
        try {
            // TODO: 实现报告发送逻辑
            // 可以发送邮件、保存到数据库、发送到监控系统等等
            log.info("发送密钥轮换任务报告：轮换结果={}, 过期检查结果={}", 
                    rotationResult.getMessage(), expirationResult.getMessage());
            
        } catch (Exception e) {
            log.error("发送任务报告失败", e);
        }
    }
}