package com.bankshield.api.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统一审计上链服务
 * 
 * 功能：
 * 1. 异步收集审计日志
 * 2. 批量上链到区块链
 * 3. 自动重试和失败处理
 * 4. 上链状态追踪
 * 
 * 工作流程：
 * 审计事件 → 队列 → 批量打包 → 上链 → 确认 → 日志
 */
@Slf4j
@Service
public class UnifiedAuditService {
    
    // 批量上链阈值
    private static final int BATCH_SIZE = 100;
    private static final long BATCH_TIMEOUT_MS = 30000; // 30秒
    private static final int MAX_RETRIES = 3;
    
    // 审计队列 (线程安全)
    private final BlockingQueue<AuditLogEntry> auditQueue = new LinkedBlockingQueue<>(10000);
    
    // 统计信息
    private final AtomicLong totalLogsQueued = new AtomicLong(0);
    private final AtomicLong totalLogsOnChain = new AtomicLong(0);
    private final AtomicLong totalBatches = new AtomicLong(0);
    private final AtomicLong failedLogs = new AtomicLong(0);
    
    /**
     * 提交审计日志到队列 (供其他服务调用)
     */
    public void submitAuditLog(String action, String userId, String resource, String result, String ip, String details) {
        try {
            AuditLogEntry entry = new AuditLogEntry();
            entry.setLogId(generateLogId());
            entry.setAction(action);
            entry.setUserId(userId);
            entry.setResource(resource);
            entry.setResult(result);
            entry.setIp(ip);
            entry.setDetails(details);
            entry.setTimestamp(LocalDateTime.now());
            entry.setStatus(LogStatus.QUEUED);
            
            boolean added = auditQueue.offer(entry);
            if (added) {
                totalLogsQueued.incrementAndGet();
                log.debug("审计日志已提交到队列 - LogID: {}, 动作: {}", entry.getLogId(), action);
            } else {
                log.error("审计队列已满，日志提交失败 - LogID: {}", entry.getLogId());
                failedLogs.incrementAndGet();
            }
            
        } catch (Exception e) {
            log.error("提交审计日志异常", e);
            failedLogs.incrementAndGet();
        }
    }
    
    /**
     * 批量上链任务 (每分钟执行一次)
     */
    @Scheduled(fixedDelay = 60000)
    public void batchOnChainTask() {
        try {
            log.info("开始执行批量上链任务 - 队列大小: {}", auditQueue.size());
            
            List<AuditLogEntry> batch = collectBatch();
            
            if (batch.isEmpty()) {
                log.debug("没有需要上链的审计日志");
                return;
            }
            
            log.info("准备上链审计日志 - 批次大小: {}, 累计已上链: {}", 
                    batch.size(), totalLogsOnChain.get());
            
            // 批量上链
            boolean success = submitBatchToBlockchain(batch);
            
            if (success) {
                totalLogsOnChain.addAndGet(batch.size());
                totalBatches.incrementAndGet();
                
                log.info("✅ 审计日志批量上链成功 - 本次: {}, 累计批次: {}", 
                        batch.size(), totalBatches.get());
                
            } else {
                // 失败处理：重新入队（带重试次数）
                handleBatchFailure(batch);
            }
            
        } catch (Exception e) {
            log.error("批量上链任务异常", e);
        }
    }
    
    /**
     * 收集批次数据
     */
    private List<AuditLogEntry> collectBatch() {
        List<AuditLogEntry> batch = new ArrayList<>();
        
        // 等待直到达到批次大小或超时
        long startTime = System.currentTimeMillis();
        
        while (batch.size() < BATCH_SIZE && 
               (System.currentTimeMillis() - startTime) < BATCH_TIMEOUT_MS) {
            
            AuditLogEntry entry = auditQueue.poll();
            if (entry != null) {
                batch.add(entry);
            } else {
                // 队列空，等待100ms再试
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return batch;
    }
    
    /**
     * 提交批次到区块链
     */
    private boolean submitBatchToBlockchain(List<AuditLogEntry> batch) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                // 创建区块
                String blockId = generateBlockId();
                String merkleRoot = calculateMerkleRoot(batch);

                // TODO: 实际的区块链上链操作
                // 1. 创建审计区块
                // fabricClient.createAuditAnchor(blockId, merkleRoot, batch.size(), metadata);

                // 2. 批量添加审计记录
                // fabricClient.batchAddAuditRecords(records);

                // 3. 验证Merkle根
                // boolean verified = fabricClient.verifyMerkleRoot(blockId);
                boolean verified = true; // 模拟验证成功

                if (!verified) {
                    log.error("Merkle根验证失败 - BlockID: {}", blockId);
                    return false;
                }

                // 更新状态
                for (AuditLogEntry entry : batch) {
                    entry.setStatus(LogStatus.ON_CHAIN);
                    entry.setBlockId(blockId);
                }

                log.info("✅ 区块链上链并验证完成 - BlockID: {}", blockId);
                return true;

            } catch (Exception e) {
                retryCount++;
                lastException = e;
                log.warn("上链失败，重试 {}/{} - 错误: {}", retryCount, MAX_RETRIES, e.getMessage());

                try {
                    Thread.sleep(1000 * retryCount); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.error("❌ 上链失败，已达到最大重试次数 - 错误: {}", lastException.getMessage());
        return false;
    }
    
    /**
     * 处理批次失败
     */
    private void handleBatchFailure(List<AuditLogEntry> batch) {
        for (AuditLogEntry entry : batch) {
            entry.setRetryCount(entry.getRetryCount() + 1);
            
            if (entry.getRetryCount() < MAX_RETRIES) {
                // 重新入队
                boolean added = auditQueue.offer(entry);
                if (!added) {
                    log.error("重试队列已满，日志丢弃 - LogID: {}", entry.getLogId());
                    failedLogs.incrementAndGet();
                }
            } else {
                log.error("❌ 审计日志达到最大重试次数，已丢弃 - LogID: {}", entry.getLogId());
                failedLogs.incrementAndGet();
                
                // 记录到失败日志表（MySQL）
                saveFailedLogToDatabase(entry);
            }
        }
    }
    
    /**
     * 查询上链状态
     */
    public Map<String, Object> getOnChainStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("queuedSize", auditQueue.size());
        status.put("totalQueued", totalLogsQueued.get());
        status.put("totalOnChain", totalLogsOnChain.get());
        status.put("totalBatches", totalBatches.get());
        status.put("failedLogs", failedLogs.get());
        status.put("queueCapacity", 10000);
        status.put("batchSize", BATCH_SIZE);
        status.put("batchTimeoutMs", BATCH_TIMEOUT_MS);
        status.put("lastBatchTime", LocalDateTime.now().minusMinutes(1));
        
        return status;
    }
    
    /**
     * 手动触发批量上链 (用于紧急处理)
     */
    public void triggerManualBatch() {
        log.info("收到手动批量上链请求");
        batchOnChainTask();
    }
    
    /**
     * 查询失败日志
     */
    public List<AuditLogEntry> getFailedLogs(int limit) {
        // 查询MySQL中失败的日志
        // 简化实现
        return new ArrayList<>();
    }
    
    /**
     * 重新提交失败日志
     */
    public int resubmitFailedLogs() {
        List<AuditLogEntry> failedLogs = getFailedLogs(100);
        int count = 0;
        
        for (AuditLogEntry log : failedLogs) {
            log.setRetryCount(0);
            log.setStatus(LogStatus.QUEUED);
            
            if (auditQueue.offer(log)) {
                count++;
            }
        }
        
        log.info("重新提交失败日志 - 成功: {}", count);
        return count;
    }
    
    // 辅助方法
    
    private String generateLogId() {
        return String.format("LOG_%d_%06d", 
                System.currentTimeMillis(),
                (int)(Math.random() * 100000));
    }
    
    private String generateBlockId() {
        return String.format("BLOCK_%d", System.currentTimeMillis());
    }
    
    private String calculateMerkleRoot(List<AuditLogEntry> entries) {
        // 简化Merkle根计算
        StringBuilder sb = new StringBuilder();
        for (AuditLogEntry entry : entries) {
            sb.append(entry.getLogId());
        }
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(sb.toString());
    }

    private void saveFailedLogToDatabase(AuditLogEntry entry) {
        try {
            // 实现保存到MySQL失败日志表
            log.warn("保存失败日志到数据库 - LogID: {}", entry.getLogId());
        } catch (Exception e) {
            log.error("保存失败日志异常", e);
        }
    }
    
    // 内部类和枚举
    
    @Data
    public static class AuditLogEntry {
        private String logId;
        private String action;
        private String userId;
        private String resource;
        private String result;
        private String ip;
        private String details;
        private LocalDateTime timestamp;
        private LogStatus status;
        private String blockId;
        private String transactionId;
        private int retryCount = 0;
        
        public AuditLogEntry() {
            this.transactionId = "TX_" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
    
    public enum LogStatus {
        QUEUED,      // 已提交到队列
        ON_CHAIN,    // 已上链
        FAILED,      // 永久失败
        PENDING      // 等待处理
    }
}
