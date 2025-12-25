package com.bankshield.api.job;

import com.bankshield.api.service.MonitorDataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监控数据采集任务
 * 每5分钟执行一次
 */
@Slf4j
@Component
public class MonitorDataCollectionJob implements Job {

    @Autowired
    private MonitorDataCollectionService monitorDataCollectionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行监控数据采集任务");
        
        try {
            // 采集所有监控指标
            monitorDataCollectionService.collectAllMetrics();
            log.info("监控数据采集任务执行完成");
        } catch (Exception e) {
            log.error("监控数据采集任务执行失败", e);
            throw new JobExecutionException("监控数据采集任务执行失败", e);
        }
    }
}