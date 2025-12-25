package com.bankshield.encrypt.config;

import com.bankshield.encrypt.job.KeyRotationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz定时任务配置
 */
@Configuration
public class QuartzConfig {
    
    /**
     * 密钥轮换任务
     */
    @Bean
    public JobDetail keyRotationJobDetail() {
        return JobBuilder.newJob(KeyRotationJob.class)
                .withIdentity("keyRotationJob")
                .storeDurably()
                .build();
    }
    
    /**
     * 密钥轮换任务触发器
     * 每天凌晨2点执行
     */
    @Bean
    public Trigger keyRotationJobTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 2 * * ?");
        
        return TriggerBuilder.newTrigger()
                .forJob(keyRotationJobDetail())
                .withIdentity("keyRotationTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}