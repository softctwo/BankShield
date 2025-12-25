package com.bankshield.api.service;

import com.bankshield.api.entity.SecurityScanTask;
import com.bankshield.api.enums.ScanStatus;
import com.bankshield.api.enums.ScanType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全扫描任务并发测试
 * 验证线程安全性和日志完整性
 */
@SpringBootTest
@ActiveProfiles("test")
public class SecurityScanTaskConcurrencyTest {
    
    @Autowired
    private SecurityScanTaskService scanTaskService;
    
    @Autowired
    private SecurityScanLogService scanLogService;
    
    @Test
    public void testConcurrentTaskExecution() throws InterruptedException {
        // 创建测试任务
        SecurityScanTask task = createTestTask("并发测试任务");
        SecurityScanTask createdTask = scanTaskService.createScanTask(task);
        Long taskId = createdTask.getId();
        
        // 使用线程池模拟并发执行
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // 并发记录日志
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    // 每个线程记录多条日志
                    for (int j = 0; j < 5; j++) {
                        scanLogService.info(taskId, String.format("线程%d-日志%d", threadIndex, j));
                        Thread.sleep(10); // 小延迟增加并发冲突概率
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "测试超时");
        executor.shutdown();
        
        // 验证结果
        assertEquals(threadCount, successCount.get(), "成功执行的线程数不正确");
        assertEquals(0, errorCount.get(), "有线程执行失败");
        
        // 验证日志完整性
        List<String> logs = scanTaskService.getTaskExecutionLog(taskId);
        assertEquals(threadCount * 5, logs.size(), "日志数量不正确，可能存在数据丢失");
        
        // 清理测试数据
        scanTaskService.deleteScanTask(taskId);
    }
    
    @Test
    public void testAsyncTaskExecution() throws InterruptedException {
        // 创建多个测试任务
        int taskCount = 5;
        List<Long> taskIds = new ArrayList<>();
        
        for (int i = 0; i < taskCount; i++) {
            SecurityScanTask task = createTestTask("异步测试任务" + i);
            SecurityScanTask createdTask = scanTaskService.createScanTask(task);
            taskIds.add(createdTask.getId());
        }
        
        // 异步执行所有任务
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Long taskId : taskIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                scanTaskService.executeScanTask(taskId);
            });
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 验证每个任务的日志完整性
        for (Long taskId : taskIds) {
            List<String> logs = scanTaskService.getTaskExecutionLog(taskId);
            assertNotNull(logs, "任务 " + taskId + " 的日志不应为null");
            assertFalse(logs.isEmpty(), "任务 " + taskId + " 的日志不应为空");
            
            // 验证包含关键日志
            boolean hasStartLog = logs.stream().anyMatch(log -> log.contains("开始执行扫描任务"));
            boolean hasEndLog = logs.stream().anyMatch(log -> log.contains("扫描任务执行成功") || log.contains("扫描任务执行失败"));
            assertTrue(hasStartLog, "任务 " + taskId + " 应包含开始日志");
            assertTrue(hasEndLog, "任务 " + taskId + " 应包含结束日志");
        }
        
        // 清理测试数据
        for (Long taskId : taskIds) {
            scanTaskService.deleteScanTask(taskId);
        }
    }
    
    @Test
    public void testMemoryLeakPrevention() throws InterruptedException {
        // 创建大量任务并删除，验证内存是否正确释放
        int taskCount = 100;
        List<Long> taskIds = new ArrayList<>();
        
        // 创建任务并记录日志
        for (int i = 0; i < taskCount; i++) {
            SecurityScanTask task = createTestTask("内存测试任务" + i);
            SecurityScanTask createdTask = scanTaskService.createScanTask(task);
            taskIds.add(createdTask.getId());
            
            // 记录一些日志
            for (int j = 0; j < 10; j++) {
                scanLogService.info(createdTask.getId(), "测试日志" + j);
            }
        }
        
        // 验证日志存在
        for (Long taskId : taskIds) {
            List<String> logs = scanTaskService.getTaskExecutionLog(taskId);
            assertEquals(10, logs.size(), "任务 " + taskId + " 的日志数量不正确");
        }
        
        // 批量删除任务
        scanTaskService.batchDeleteTasks(taskIds);
        
        // 验证日志也被清理（通过服务层方法验证）
        for (Long taskId : taskIds) {
            List<String> logs = scanTaskService.getTaskExecutionLog(taskId);
            assertTrue(logs.isEmpty(), "任务 " + taskId + " 的日志应已被清理");
        }
    }
    
    private SecurityScanTask createTestTask(String taskName) {
        SecurityScanTask task = new SecurityScanTask();
        task.setTaskName(taskName);
        task.setScanType(ScanType.VULNERABILITY.name());
        task.setScanTarget("test-target");
        task.setCreatedBy("test-user");
        task.setDescription("测试任务");
        task.setStatus(ScanStatus.PENDING.name());
        task.setProgress(0);
        task.setRiskCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        return task;
    }
}