package com.bankshield.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能测试基类
 * 提供性能测试的通用工具和方法
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class BasePerformanceTest {
    
    protected static final int DEFAULT_THREAD_COUNT = 10;
    protected static final int DEFAULT_ITERATIONS = 100;
    protected static final int DEFAULT_TIMEOUT_SECONDS = 300;
    
    /**
     * 性能测试结果
     */
    protected static class PerformanceResult {
        private final int totalRequests;
        private final int successfulRequests;
        private final int failedRequests;
        private final long totalTimeMs;
        private final double avgResponseTimeMs;
        private final double requestsPerSecond;
        private final double minResponseTimeMs;
        private final double maxResponseTimeMs;
        
        public PerformanceResult(int totalRequests, int successfulRequests, int failedRequests,
                                long totalTimeMs, double avgResponseTimeMs, double requestsPerSecond,
                                double minResponseTimeMs, double maxResponseTimeMs) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.totalTimeMs = totalTimeMs;
            this.avgResponseTimeMs = avgResponseTimeMs;
            this.requestsPerSecond = requestsPerSecond;
            this.minResponseTimeMs = minResponseTimeMs;
            this.maxResponseTimeMs = maxResponseTimeMs;
        }
        
        public void printReport() {
            System.out.println("\n=== 性能测试报告 ===");
            System.out.println("总请求数: " + totalRequests);
            System.out.println("成功请求数: " + successfulRequests);
            System.out.println("失败请求数: " + failedRequests);
            System.out.println("总耗时: " + totalTimeMs + "ms");
            System.out.println("平均响应时间: " + String.format("%.2f", avgResponseTimeMs) + "ms");
            System.out.println("最小响应时间: " + String.format("%.2f", minResponseTimeMs) + "ms");
            System.out.println("最大响应时间: " + String.format("%.2f", maxResponseTimeMs) + "ms");
            System.out.println("吞吐量: " + String.format("%.2f", requestsPerSecond) + " req/s");
            System.out.println("成功率: " + String.format("%.2f%%", (successfulRequests * 100.0 / totalRequests)));
        }
        
        public boolean isSuccess(double maxResponseTimeThreshold, double minSuccessRate) {
            return avgResponseTimeMs <= maxResponseTimeThreshold && 
                   (successfulRequests * 100.0 / totalRequests) >= minSuccessRate;
        }
    }
    
    /**
     * 执行并发性能测试
     */
    protected PerformanceResult executeConcurrentTest(Runnable testTask, int threadCount, int iterations) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        List<Long> responseTimes = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < iterations; j++) {
                        long requestStart = System.currentTimeMillis();
                        
                        try {
                            testTask.run();
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                        
                        long responseTime = System.currentTimeMillis() - requestStart;
                        totalResponseTime.addAndGet(responseTime);
                        
                        synchronized (responseTimes) {
                            responseTimes.add(responseTime);
                            minResponseTime.updateAndGet(v -> Math.min(v, responseTime));
                            maxResponseTime.updateAndGet(v -> Math.max(v, responseTime));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }
        
        // 开始测试
        startLatch.countDown();
        
        try {
            completeLatch.await(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        int totalRequests = threadCount * iterations;
        double avgResponseTime = totalResponseTime.get() / (double) totalRequests;
        double requestsPerSecond = (totalRequests * 1000.0) / totalTime;
        
        return new PerformanceResult(
            totalRequests,
            successCount.get(),
            failCount.get(),
            totalTime,
            avgResponseTime,
            requestsPerSecond,
            minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get(),
            maxResponseTime.get()
        );
    }
    
    /**
     * 执行负载测试（逐步增加并发）
     */
    protected void executeLoadTest(Runnable testTask, int startThreads, int maxThreads, int step) {
        System.out.println("\n=== 负载测试开始 ===");
        
        for (int threads = startThreads; threads <= maxThreads; threads += step) {
            System.out.println("\n当前并发数: " + threads);
            
            PerformanceResult result = executeConcurrentTest(testTask, threads, DEFAULT_ITERATIONS);
            result.printReport();
            
            // 如果性能下降明显，停止测试
            if (!result.isSuccess(2000, 95)) {
                System.out.println("性能下降，停止测试");
                break;
            }
        }
        
        System.out.println("\n=== 负载测试完成 ===");
    }
    
    /**
     * 执行压力测试（长时间运行）
     */
    protected PerformanceResult executeStressTest(Runnable testTask, int durationMinutes) {
        System.out.println("\n=== 压力测试开始，持续时间: " + durationMinutes + "分钟 ===");
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong requestCount = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationMinutes * 60 * 1000);
        
        while (System.currentTimeMillis() < endTime) {
            long requestStart = System.currentTimeMillis();
            
            try {
                testTask.run();
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
            
            long responseTime = System.currentTimeMillis() - requestStart;
            totalResponseTime.addAndGet(responseTime);
            requestCount.incrementAndGet();
            
            minResponseTime.updateAndGet(v -> Math.min(v, responseTime));
            maxResponseTime.updateAndGet(v -> Math.max(v, responseTime));
            
            // 短暂休眠，避免CPU过载
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = totalResponseTime.get() / (double) requestCount.get();
        double requestsPerSecond = (requestCount.get() * 1000.0) / totalTime;
        
        PerformanceResult result = new PerformanceResult(
            requestCount.get(),
            successCount.get(),
            failCount.get(),
            totalTime,
            avgResponseTime,
            requestsPerSecond,
            minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get(),
            maxResponseTime.get()
        );
        
        result.printReport();
        return result;
    }
}