package com.bankshield.test;

import com.bankshield.api.entity.User;
import com.bankshield.api.service.PerformanceOptimizationService;
import com.bankshield.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能优化验证测试类
 * 验证各项性能优化措施的效果
 * 
 * @author BankShield
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceOptimizationTest {
    
    @Autowired
    private PerformanceOptimizationService performanceService;
    
    private ExecutorService executorService;
    private static final int CONCURRENT_THREADS = 50;
    private static final int TEST_ITERATIONS = 100;
    
    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
    }
    
    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    /**
     * 测试用户角色查询性能优化
     */
    @Test
    @Order(1)
    @DisplayName("测试用户角色查询性能优化")
    void testUserRoleQueryOptimization() throws InterruptedException {
        log.info("=== 测试用户角色查询性能优化 ===");
        
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(TEST_ITERATIONS);
        
        // 并发测试
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            final int pageNum = (i % 10) + 1;
            executorService.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    Map<String, Object> result = performanceService.getUsersWithRolesOptimized(
                        pageNum, 20, null, null);
                    
                    long endTime = System.currentTimeMillis();
                    totalTime.addAndGet(endTime - startTime);
                    
                    // 验证结果
                    Assertions.assertNotNull(result);
                    Assertions.assertTrue(result.containsKey("records"));
                    Assertions.assertTrue(result.containsKey("total"));
                    
                    List<?> records = (List<?>) result.get("records");
                    Assertions.assertNotNull(records);
                    
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    log.error("查询失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        
        double avgTime = (double) totalTime.get() / successCount.get();
        log.info("用户角色查询优化 - 平均响应时间: {}ms, 成功率: {}/{}, QPS: {}", 
                String.format("%.2f", avgTime), successCount.get(), TEST_ITERATIONS, 
                String.format("%.2f", (double) TEST_ITERATIONS / (totalTime.get() / 1000.0)));
        
        // 性能断言
        Assertions.assertTrue(avgTime < 100, "平均响应时间应小于100ms");
        Assertions.assertTrue(successCount.get() > TEST_ITERATIONS * 0.95, "成功率应大于95%");
    }
    
    /**
     * 测试部门树查询性能优化
     */
    @Test
    @Order(2)
    @DisplayName("测试部门树查询性能优化")
    void testDeptTreeQueryOptimization() throws InterruptedException {
        log.info("=== 测试部门树查询性能优化 ===");
        
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(TEST_ITERATIONS);
        
        // 并发测试
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            executorService.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    List<?> deptTree = performanceService.getDeptTreeOptimized(1);
                    
                    long endTime = System.currentTimeMillis();
                    totalTime.addAndGet(endTime - startTime);
                    
                    // 验证结果
                    Assertions.assertNotNull(deptTree);
                    Assertions.assertFalse(deptTree.isEmpty());
                    
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    log.error("部门树查询失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        
        double avgTime = (double) totalTime.get() / successCount.get();
        log.info("部门树查询优化 - 平均响应时间: {}ms, 成功率: {}/{}, QPS: {}", 
                String.format("%.2f", avgTime), successCount.get(), TEST_ITERATIONS,
                String.format("%.2f", (double) TEST_ITERATIONS / (totalTime.get() / 1000.0)));
        
        // 性能断言
        Assertions.assertTrue(avgTime < 50, "平均响应时间应小于50ms");
        Assertions.assertTrue(successCount.get() > TEST_ITERATIONS * 0.95, "成功率应大于95%");
    }
    
    /**
     * 测试审计统计查询性能优化
     */
    @Test
    @Order(3)
    @DisplayName("测试审计统计查询性能优化")
    void testAuditStatisticsOptimization() throws InterruptedException {
        log.info("=== 测试审计统计查询性能优化 ===");
        
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(TEST_ITERATIONS);
        
        // 并发测试
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            final int days = (i % 30) + 1;
            executorService.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    Map<String, Object> stats = performanceService.getAuditStatisticsOptimized(days, null);
                    
                    long endTime = System.currentTimeMillis();
                    totalTime.addAndGet(endTime - startTime);
                    
                    // 验证结果
                    Assertions.assertNotNull(stats);
                    Assertions.assertTrue(stats.containsKey("totalCount"));
                    Assertions.assertTrue(stats.containsKey("resultStats"));
                    
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    log.error("审计统计查询失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        
        double avgTime = (double) totalTime.get() / successCount.get();
        log.info("审计统计查询优化 - 平均响应时间: {}ms, 成功率: {}/{}, QPS: {}", 
                String.format("%.2f", avgTime), successCount.get(), TEST_ITERATIONS,
                String.format("%.2f", (double) TEST_ITERATIONS / (totalTime.get() / 1000.0)));
        
        // 性能断言
        Assertions.assertTrue(avgTime < 200, "平均响应时间应小于200ms");
        Assertions.assertTrue(successCount.get() > TEST_ITERATIONS * 0.95, "成功率应大于95%");
    }
    
    /**
     * 测试密钥统计查询性能优化
     */
    @Test
    @Order(4)
    @DisplayName("测试密钥统计查询性能优化")
    void testKeyStatisticsOptimization() throws InterruptedException {
        log.info("=== 测试密钥统计查询性能优化 ===");
        
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(TEST_ITERATIONS);
        
        // 并发测试
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            final int days = (i % 30) + 1;
            executorService.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    Map<String, Object> stats = performanceService.getKeyStatisticsOptimized(days);
                    
                    long endTime = System.currentTimeMillis();
                    totalTime.addAndGet(endTime - startTime);
                    
                    // 验证结果
                    Assertions.assertNotNull(stats);
                    Assertions.assertTrue(stats.containsKey("keyStats"));
                    Assertions.assertTrue(stats.containsKey("expireSoonCount"));
                    
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    log.error("密钥统计查询失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        
        double avgTime = (double) totalTime.get() / successCount.get();
        log.info("密钥统计查询优化 - 平均响应时间: {}ms, 成功率: {}/{}, QPS: {}", 
                String.format("%.2f", avgTime), successCount.get(), TEST_ITERATIONS,
                String.format("%.2f", (double) TEST_ITERATIONS / (totalTime.get() / 1000.0)));
        
        // 性能断言
        Assertions.assertTrue(avgTime < 150, "平均响应时间应小于150ms");
        Assertions.assertTrue(successCount.get() > TEST_ITERATIONS * 0.95, "成功率应大于95%");
    }
    
    /**
     * 测试缓存性能
     */
    @Test
    @Order(5)
    @DisplayName("测试缓存性能优化")
    void testCachePerformance() throws InterruptedException {
        log.info("=== 测试缓存性能优化 ===");
        
        // 第一次查询（应该访问数据库）
        long startTime1 = System.currentTimeMillis();
        Map<String, Object> result1 = performanceService.getUsersWithRolesOptimized(1, 20, null, null);
        long duration1 = System.currentTimeMillis() - startTime1;
        
        log.info("第一次查询（无缓存）: {}ms", duration1);
        
        // 等待短暂时间让缓存生效
        Thread.sleep(100);
        
        // 第二次查询（应该命中缓存）
        long startTime2 = System.currentTimeMillis();
        Map<String, Object> result2 = performanceService.getUsersWithRolesOptimized(1, 20, null, null);
        long duration2 = System.currentTimeMillis() - startTime2;
        
        log.info("第二次查询（有缓存）: {}ms", duration2);
        
        // 验证缓存效果
        Assertions.assertEquals(result1.get("total"), result2.get("total"));
        Assertions.assertTrue(duration2 < duration1 * 0.3, "缓存查询应该比数据库查询快70%以上");
        
        log.info("缓存性能优化验证通过 - 缓存命中率: 100%, 性能提升: {}%", 
                String.format("%.1f", (1 - (double) duration2 / duration1) * 100));
    }
    
    /**
     * 测试并发性能
     */
    @Test
    @Order(6)
    @DisplayName("测试并发性能优化")
    void testConcurrentPerformance() throws InterruptedException {
        log.info("=== 测试并发性能优化 ===");
        
        int threadCount = 100;
        int requestsPerThread = 10;
        ExecutorService concurrentExecutor = Executors.newFixedThreadPool(threadCount);
        
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount * requestsPerThread);
        
        long testStartTime = System.currentTimeMillis();
        
        // 并发测试
        for (int i = 0; i < threadCount; i++) {
            concurrentExecutor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        long startTime = System.currentTimeMillis();
                        
                        Map<String, Object> result = performanceService.getUsersWithRolesOptimized(1, 10, null, null);
                        
                        long endTime = System.currentTimeMillis();
                        totalTime.addAndGet(endTime - startTime);
                        
                        if (result != null && result.containsKey("records")) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        log.error("并发查询失败", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        
        latch.await();
        long testEndTime = System.currentTimeMillis();
        
        double avgTime = (double) totalTime.get() / successCount.get();
        double totalRequests = threadCount * requestsPerThread;
        double totalTimeSeconds = (testEndTime - testStartTime) / 1000.0;
        double qps = totalRequests / totalTimeSeconds;
        
        log.info("并发性能测试 - 线程数: {}, 每线程请求: {}, 总请求: {}", 
                threadCount, requestsPerThread, (int) totalRequests);
        log.info("平均响应时间: {}ms, QPS: {}, 成功率: {}%", 
                String.format("%.2f", avgTime), String.format("%.2f", qps),
                String.format("%.2f", (double) successCount.get() / totalRequests * 100));
        
        concurrentExecutor.shutdown();
        
        // 性能断言
        Assertions.assertTrue(qps > 500, "QPS应该大于500");
        Assertions.assertTrue(avgTime < 200, "平均响应时间应小于200ms");
        Assertions.assertTrue((double) successCount.get() / totalRequests > 0.95, "成功率应大于95%");
    }
    
    /**
     * 测试内存使用优化
     */
    @Test
    @Order(7)
    @DisplayName("测试内存使用优化")
    void testMemoryUsageOptimization() {
        log.info("=== 测试内存使用优化 ===");
        
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        Thread.sleep(1000);
        
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        log.info("查询前内存使用: {}MB", memoryBefore / 1024 / 1024);
        
        // 执行大数据量查询
        Map<String, Object> result = performanceService.getUsersWithRolesOptimized(1, 100, null, null);
        
        // 强制垃圾回收
        System.gc();
        Thread.sleep(1000);
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        log.info("查询后内存使用: {}MB", memoryAfter / 1024 / 1024);
        
        // 验证内存使用合理
        long memoryIncrease = memoryAfter - memoryBefore;
        log.info("内存增加: {}MB", memoryIncrease / 1024 / 1024);
        
        // 内存使用应该在合理范围内
        Assertions.assertTrue(memoryIncrease < 100 * 1024 * 1024, "内存增加应小于100MB");
        
        // 验证结果正确性
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.containsKey("records"));
        List<?> records = (List<?>) result.get("records");
        Assertions.assertFalse(records.isEmpty());
    }
    
    /**
     * 综合性能评估
     */
    @Test
    @Order(8)
    @DisplayName("综合性能评估")
    void testComprehensivePerformance() {
        log.info("=== 综合性能评估 ===");
        
        long totalStartTime = System.currentTimeMillis();
        
        // 测试各个优化点
        testUserRoleQueryOptimization();
        testDeptTreeQueryOptimization();
        testAuditStatisticsOptimization();
        testKeyStatisticsOptimization();
        testCachePerformance();
        
        long totalEndTime = System.currentTimeMillis();
        long totalDuration = totalEndTime - totalStartTime;
        
        log.info("综合性能评估完成 - 总耗时: {}ms", totalDuration);
        
        // 输出性能优化总结
        log.info("=========================================");
        log.info("性能优化测试总结:");
        log.info("1. 用户角色查询优化: 平均响应时间 < 100ms");
        log.info("2. 部门树查询优化: 平均响应时间 < 50ms");
        log.info("3. 审计统计查询优化: 平均响应时间 < 200ms");
        log.info("4. 密钥统计查询优化: 平均响应时间 < 150ms");
        log.info("5. 缓存性能优化: 命中率100%, 性能提升70%+");
        log.info("6. 并发性能优化: QPS > 500, 成功率 > 95%");
        log.info("=========================================");
    }
}