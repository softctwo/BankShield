package com.bankshield.api.job;

import com.bankshield.api.mapper.AlertRecordMapper;
import com.bankshield.api.mapper.MonitorMetricMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 历史数据清理任务
 * 每天凌晨执行
 */
@Slf4j
@Component
public class DataRetentionJob implements Job {

    @Autowired
    private MonitorMetricMapper monitorMetricMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Value("${monitor.data-retention.days:30}")
    private int dataRetentionDays;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行历史数据清理任务");
        
        try {
            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().minusDays(dataRetentionDays);
            
            log.info("清理{}之前的历史数据", expireTime);
            
            // 清理监控指标历史数据
            int metricsCleaned = monitorMetricMapper.cleanExpiredData(expireTime);
            log.info("清理监控指标历史数据完成，共清理{}条记录", metricsCleaned);
            
            // 清理告警记录历史数据
            int alertsCleaned = alertRecordMapper.cleanExpiredData(expireTime);
            log.info("清理告警记录历史数据完成，共清理{}条记录", alertsCleaned);
            
            log.info("历史数据清理任务执行完成，总共清理{}条记录", metricsCleaned + alertsCleaned);
        } catch (Exception e) {
            log.error("历史数据清理任务执行失败", e);
            throw new JobExecutionException("历史数据清理任务执行失败", e);
        }
    }
}