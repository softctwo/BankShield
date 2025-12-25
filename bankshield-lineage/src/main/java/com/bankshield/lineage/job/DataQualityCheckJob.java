package com.bankshield.lineage.job;

import com.bankshield.lineage.service.DataQualityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 数据质量检查任务
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataQualityCheckJob implements Job {

    private final DataQualityService dataQualityService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行数据质量检查任务");
        
        try {
            dataQualityService.executeQualityChecks();
            log.info("数据质量检查任务执行完成");
        } catch (Exception e) {
            log.error("数据质量检查任务执行失败", e);
            throw new JobExecutionException("数据质量检查任务执行失败", e);
        }
    }
}