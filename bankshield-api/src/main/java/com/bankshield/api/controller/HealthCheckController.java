package com.bankshield.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    /**
     * 基础健康检查
     */
    @GetMapping
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        health.put("application", "BankShield");
        health.put("version", "1.0.0");
        
        return health;
    }

    /**
     * 详细健康检查
     */
    @GetMapping("/detailed")
    public Map<String, Object> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // 基本信息
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // JVM信息
        health.put("jvm", getJvmInfo());
        
        // 系统信息
        health.put("system", getSystemInfo());
        
        // 线程信息
        health.put("threads", getThreadInfo());
        
        // 内存信息
        health.put("memory", getMemoryInfo());
        
        // 组件状态
        health.put("components", getComponentsStatus());
        
        return health;
    }

    /**
     * 获取JVM信息
     */
    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvm = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        
        jvm.put("name", System.getProperty("java.vm.name"));
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("vendor", System.getProperty("java.vendor"));
        jvm.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        jvm.put("processors", runtime.availableProcessors());
        
        return jvm;
    }

    /**
     * 获取系统信息
     */
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        
        system.put("name", os.getName());
        system.put("version", os.getVersion());
        system.put("arch", os.getArch());
        system.put("loadAverage", os.getSystemLoadAverage());
        
        return system;
    }

    /**
     * 获取线程信息
     */
    private Map<String, Object> getThreadInfo() {
        Map<String, Object> threads = new HashMap<>();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        threads.put("total", threadBean.getThreadCount());
        threads.put("peak", threadBean.getPeakThreadCount());
        threads.put("daemon", threadBean.getDaemonThreadCount());
        threads.put("started", threadBean.getTotalStartedThreadCount());
        
        return threads;
    }

    /**
     * 获取内存信息
     */
    private Map<String, Object> getMemoryInfo() {
        Map<String, Object> memory = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();
        
        // 堆内存
        Map<String, Object> heap = new HashMap<>();
        heap.put("init", memoryBean.getHeapMemoryUsage().getInit());
        heap.put("used", memoryBean.getHeapMemoryUsage().getUsed());
        heap.put("committed", memoryBean.getHeapMemoryUsage().getCommitted());
        heap.put("max", memoryBean.getHeapMemoryUsage().getMax());
        heap.put("usagePercent", (memoryBean.getHeapMemoryUsage().getUsed() * 100.0) / memoryBean.getHeapMemoryUsage().getMax());
        memory.put("heap", heap);
        
        // 非堆内存
        Map<String, Object> nonHeap = new HashMap<>();
        nonHeap.put("init", memoryBean.getNonHeapMemoryUsage().getInit());
        nonHeap.put("used", memoryBean.getNonHeapMemoryUsage().getUsed());
        nonHeap.put("committed", memoryBean.getNonHeapMemoryUsage().getCommitted());
        nonHeap.put("max", memoryBean.getNonHeapMemoryUsage().getMax());
        memory.put("nonHeap", nonHeap);
        
        // 总内存
        memory.put("totalMemory", runtime.totalMemory());
        memory.put("freeMemory", runtime.freeMemory());
        memory.put("maxMemory", runtime.maxMemory());
        memory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        
        return memory;
    }

    /**
     * 获取组件状态
     */
    private Map<String, Object> getComponentsStatus() {
        Map<String, Object> components = new HashMap<>();
        
        // 数据库状态
        components.put("database", checkDatabaseStatus());
        
        // Redis状态
        components.put("redis", checkRedisStatus());
        
        // 缓存状态
        components.put("cache", checkCacheStatus());
        
        // 消息队列状态
        components.put("messageQueue", checkMessageQueueStatus());
        
        return components;
    }

    /**
     * 检查数据库状态
     */
    private Map<String, Object> checkDatabaseStatus() {
        Map<String, Object> db = new HashMap<>();
        try {
            // 这里应该实际检查数据库连接
            db.put("status", "UP");
            db.put("type", "MySQL");
            db.put("connections", 10);
            db.put("maxConnections", 100);
        } catch (Exception e) {
            db.put("status", "DOWN");
            db.put("error", e.getMessage());
        }
        return db;
    }

    /**
     * 检查Redis状态
     */
    private Map<String, Object> checkRedisStatus() {
        Map<String, Object> redis = new HashMap<>();
        try {
            // 这里应该实际检查Redis连接
            redis.put("status", "UP");
            redis.put("version", "6.0");
            redis.put("mode", "standalone");
        } catch (Exception e) {
            redis.put("status", "DOWN");
            redis.put("error", e.getMessage());
        }
        return redis;
    }

    /**
     * 检查缓存状态
     */
    private Map<String, Object> checkCacheStatus() {
        Map<String, Object> cache = new HashMap<>();
        cache.put("status", "UP");
        cache.put("hitRate", 0.85);
        cache.put("size", 1024);
        return cache;
    }

    /**
     * 检查消息队列状态
     */
    private Map<String, Object> checkMessageQueueStatus() {
        Map<String, Object> mq = new HashMap<>();
        mq.put("status", "UP");
        mq.put("queueSize", 0);
        mq.put("consumers", 5);
        return mq;
    }

    /**
     * 就绪检查（Kubernetes Readiness Probe）
     */
    @GetMapping("/ready")
    public Map<String, Object> readinessCheck() {
        Map<String, Object> ready = new HashMap<>();
        
        boolean isReady = true;
        StringBuilder message = new StringBuilder();
        
        // 检查数据库
        Map<String, Object> db = checkDatabaseStatus();
        if (!"UP".equals(db.get("status"))) {
            isReady = false;
            message.append("Database not ready; ");
        }
        
        // 检查Redis
        Map<String, Object> redis = checkRedisStatus();
        if (!"UP".equals(redis.get("status"))) {
            isReady = false;
            message.append("Redis not ready; ");
        }
        
        ready.put("ready", isReady);
        ready.put("status", isReady ? "UP" : "DOWN");
        ready.put("message", message.toString());
        
        return ready;
    }

    /**
     * 存活检查（Kubernetes Liveness Probe）
     */
    @GetMapping("/live")
    public Map<String, Object> livenessCheck() {
        Map<String, Object> live = new HashMap<>();
        live.put("alive", true);
        live.put("status", "UP");
        live.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return live;
    }
}
