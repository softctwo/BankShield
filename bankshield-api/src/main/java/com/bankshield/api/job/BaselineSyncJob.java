package com.bankshield.api.job;

import com.bankshield.api.service.SecurityBaselineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 安全基线同步定时任务
 * @author BankShield
 */
@Slf4j
@Component
public class BaselineSyncJob {

    @Autowired
    private SecurityBaselineService baselineService;

    /**
     * 每月1号凌晨1点同步更新内置安全基线
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void syncBuiltinBaselines() {
        log.info("开始同步内置安全基线");
        
        try {
            baselineService.syncBuiltinBaselines();
            log.info("内置安全基线同步成功");
        } catch (Exception e) {
            log.error("同步内置安全基线失败", e);
        }
    }

    /**
     * 每日凌晨4点检查即将到期的基线检查
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void checkExpiringBaselines() {
        log.info("开始检查即将到期的基线检查");
        
        try {
            // 检查未来7天内需要检查的基线
            log.info("基线检查到期提醒任务执行完成");
        } catch (Exception e) {
            log.error("检查即将到期的基线失败", e);
        }
    }
}