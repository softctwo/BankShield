package com.bankshield.api.job;

import com.bankshield.api.service.AlertRuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 告警检查任务
 * 每分钟执行一次
 */
@Slf4j
@Component
public class AlertCheckJob implements Job {

    @Autowired
    private AlertRuleEngine alertRuleEngine;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行告警检查任务");
        
        try {
            // 执行告警检查
            alertRuleEngine.executeAlertCheck();
            log.info("告警检查任务执行完成");
        } catch (Exception e) {
            log.error("告警检查任务执行失败", e);
            throw new JobExecutionException("告警检查任务执行失败", e);
        }
    }
}