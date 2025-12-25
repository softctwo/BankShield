package com.bankshield.lineage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 调度器配置
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        
        // 设置调度器属性
        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "BankShieldLineageScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        
        // 设置线程池属性
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        
        // 设置作业存储属性
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        
        factory.setQuartzProperties(properties);
        factory.setStartupDelay(10);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        
        return factory;
    }
}