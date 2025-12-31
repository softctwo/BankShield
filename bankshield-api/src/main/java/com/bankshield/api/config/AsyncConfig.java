package com.bankshield.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步处理配置类
 * 配置审计日志异步写入的线程池和通用异步任务线程池
 * 优化线程池参数以支持高并发场景
 * 
 * @author BankShield
 */
//@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数 = CPU核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数 = 核心数 * 2
        executor.setMaxPoolSize(corePoolSize * 2);
        
        // 队列容量
        executor.setQueueCapacity(5000);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("BankShield-Async-");
        
        // 拒绝策略：使用调用者线程执行，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 优雅关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    /**
     * 角色检查专用线程池
     * 用于执行角色互斥检查任务，防止无限制创建线程导致OOM
     */
    @Bean("roleCheckExecutor")
    public ThreadPoolTaskExecutor roleCheckExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：5
        executor.setCorePoolSize(5);
        
        // 最大线程数：20
        executor.setMaxPoolSize(20);
        
        // 队列容量：100
        executor.setQueueCapacity(100);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("role-check-");
        
        // 拒绝策略：使用调用者线程执行，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 优雅关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}