package com.bankshield.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.security.SecureRandom;

/**
 * 统一审计服务
 * 实现审计日志的异步处理、持久化和上链机制
 */
@Slf4j
@Service
public class UnifiedAuditService {

    // 审计队列容量 - 可配置
    private static final int QUEUE_CAPACITY = 10000;
    private static final int BATCH_SIZE = 100;
    private static final int FLUSH_INTERVAL_SECONDS = 5;

    // 阻塞队列代替有界队列，避免丢弃日志
    private final BlockingQueue<AuditLog> auditQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor executor;
    private final List<AuditLog> buffer = new ArrayList<>();
    private final Object bufferLock = new Object();

    // 统计信息
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger totalFailed = new AtomicInteger(0);

    @Autowired(required = false)
    private AuditPersistenceService persistenceService;

    public UnifiedAuditService() {
        // 创建专用线程池处理审计日志
        this.executor = new ThreadPoolExecutor(
            2, // 核心线程数
            5, // 最大线程数
            60L, // 空闲线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), // 使用无界队列
            new ThreadFactoryBuilder(). daemon(true),
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：使用调用者运行
        );

        // 启动后台线程处理审计队列
        startAuditProcessor();
        startPeriodicFlush();
    }

    /**
     * 提交审计日志
     * 使用offer方法返回false时，将日志放入备用队列或持久化
     */
    public void submitAuditLog(String operation, String userId, String target, String status, String clientIp, String description) {
        AuditLog log = new AuditLog(
            System.currentTimeMillis(),
            operation,
            userId,
            target,
            status,
            clientIp,
            description
        );

        // 使用offer避免阻塞，如果队列满则持久化
        if (!auditQueue.offer(log)) {
            log.warn("审计队列已满，将直接持久化日志");
            // 队列满时直接持久化（降级处理）
            persistDirectly(log);
        }
    }

    /**
     * 直接持久化审计日志
     */
    private void persistDirectly(AuditLog log) {
        try {
            if (persistenceService != null) {
                persistenceService.persistAuditLog(log);
                totalProcessed.incrementAndGet();
            } else {
                // 如果没有持久化服务，使用文件存储
                persistToFile(log);
            }
        } catch (Exception e) {
            log.error("持久化审计日志失败", e);
            totalFailed.incrementAndGet();
        }
    }

    /**
     * 启动审计处理器
     */
    private void startAuditProcessor() {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 批量处理审计日志
                    List<AuditLog> batch = new ArrayList<>();
                    auditQueue.drainTo(batch, BATCH_SIZE);

                    if (!batch.isEmpty()) {
                        processBatch(batch);
                    }

                    // 如果队列为空，等待一段时间
                    if (auditQueue.isEmpty()) {
                        Thread.sleep(100);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理审计日志批次失败", e);
                }
            }
        });
    }

    /**
     * 启动定期刷新线程
     */
    private void startPeriodicFlush() {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(FLUSH_INTERVAL_SECONDS * 1000);
                    flushBuffer();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("定期刷新审计缓冲区失败", e);
                }
            }
        });
    }

    /**
     * 批量处理审计日志
     */
    private void processBatch(List<AuditLog> batch) {
        synchronized (bufferLock) {
            buffer.addAll(batch);

            if (buffer.size() >= BATCH_SIZE) {
                flushBuffer();
            }
        }
    }

    /**
     * 刷新缓冲区到持久化层
     */
    private void flushBuffer() {
        List<AuditLog> toFlush = null;
        synchronized (bufferLock) {
            if (buffer.isEmpty()) {
                return;
            }

            try {
                toFlush = new ArrayList<>(buffer);
                buffer.clear();

                if (persistenceService != null) {
                    persistenceService.persistAuditLogs(toFlush);
                } else {
                    for (AuditLog log : toFlush) {
                        persistToFile(log);
                    }
                }

                totalProcessed.addAndGet(toFlush.size());
                log.debug("刷新审计日志批次，大小: {}", toFlush.size());

            } catch (Exception e) {
                log.error("刷新审计缓冲区失败", e);
                totalFailed.incrementAndGet();
                // 回滚到缓冲区
                if (toFlush != null) {
                    buffer.addAll(0, toFlush);
                }
            }
        }
    }

    /**
     * 文件持久化（备用方案）
     */
    private void persistToFile(AuditLog log) {
        // 简化实现：记录到日志文件
        log.info("AUDIT_LOG: {}", log.toString());
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("queueSize", auditQueue.size());
        stats.put("bufferSize", buffer.size());
        stats.put("totalProcessed", totalProcessed.get());
        stats.put("totalFailed", totalFailed.get());
        stats.put("executorActiveThreads", executor.getActiveCount());
        return stats;
    }

    /**
     * 关闭审计服务
     */
    public void shutdown() {
        executor.shutdown();
        try {
            // 等待现有任务完成
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            // 最后刷新一次缓冲区
            flushBuffer();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 审计日志实体类
     */
    public static class AuditLog {
        private final String id; // 使用UUID作为唯一ID
        private final long timestamp;
        private final String operation;
        private final String userId;
        private final String target;
        private final String status;
        private final String clientIp;
        private final String description;

        public AuditLog(long timestamp, String operation, String userId, String target,
                       String status, String clientIp, String description) {
            // 使用UUID.randomUUID()生成全局唯一ID，避免碰撞
            this.id = UUID.randomUUID().toString().replace("-", "");
            this.timestamp = timestamp;
            this.operation = operation;
            this.userId = userId;
            this.target = target;
            this.status = status;
            this.clientIp = clientIp;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return String.format("AuditLog{id='%s', timestamp=%d, operation='%s', userId='%s', target='%s', status='%s', clientIp='%s', description='%s'}",
                id, timestamp, operation, userId, target, status, clientIp, description);
        }
    }
}

// 线程工厂构建器
class ThreadFactoryBuilder {
    private boolean daemon = false;

    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactory build() {
        return new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "AuditProcessor-" + counter.incrementAndGet());
                thread.setDaemon(daemon);
                return thread;
            }
        };
    }
}

// 审计持久化服务接口
interface AuditPersistenceService {
    void persistAuditLog(UnifiedAuditService.AuditLog log);
    void persistAuditLogs(List<UnifiedAuditService.AuditLog> logs);
}
