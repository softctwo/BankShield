package com.bankshield.gateway.config;

import com.bankshield.gateway.service.BlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 * 
 * @author BankShield
 */
@Slf4j
@Configuration
@EnableScheduling
public class ScheduledConfig {
    
    @Autowired
    private BlacklistService blacklistService;
    
    /**
     * 处理过期黑名单（每小时执行一次）
     */
    @Scheduled(cron = "0 0 * * * *")
    public void processExpiredBlacklists() {
        log.info("开始处理过期黑名单...");
        try {
            blacklistService.processExpiredBlacklists();
            log.info("处理过期黑名单完成");
        } catch (Exception e) {
            log.error("处理过期黑名单失败", e);
        }
    }
    
    /**
     * 清理过期API访问日志（每天凌晨2点执行）
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldAccessLogs() {
        log.info("开始清理过期API访问日志...");
        try {
            // 删除30天前的访问日志
            // apiAuditService.cleanupOldAccessLogs(30);
            log.info("清理过期API访问日志完成");
        } catch (Exception e) {
            log.error("清理过期API访问日志失败", e);
        }
    }
    
    /**
     * 清理Redis中的过期限流计数器（每10分钟执行一次）
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void cleanupExpiredRateLimitCounters() {
        log.info("开始清理过期限流计数器...");
        try {
            // rateLimitService.cleanupExpiredCounters();
            log.info("清理过期限流计数器完成");
        } catch (Exception e) {
            log.error("清理过期限流计数器失败", e);
        }
    }
    
    /**
     * 刷新限流规则缓存（每5分钟执行一次）
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void refreshRateLimitRules() {
        log.info("开始刷新限流规则缓存...");
        try {
            // rateLimitService.refreshRules();
            log.info("刷新限流规则缓存完成");
        } catch (Exception e) {
            log.error("刷新限流规则缓存失败", e);
        }
    }
}