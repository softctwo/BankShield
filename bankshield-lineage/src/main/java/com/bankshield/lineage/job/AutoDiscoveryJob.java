package com.bankshield.lineage.job;

import com.bankshield.lineage.service.LineageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 自动发现血缘关系任务
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoDiscoveryJob implements Job {

    private final LineageService lineageService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行自动发现血缘关系任务");
        
        try {
            lineageService.discoverFromSqlLogs();
            log.info("自动发现血缘关系任务执行完成");
        } catch (Exception e) {
            log.error("自动发现血缘关系任务执行失败", e);
            throw new JobExecutionException("自动发现血缘关系任务执行失败", e);
        }
    }
}