package com.bankshield.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * API限流配置
 */
@Slf4j
@Configuration
public class RateLimitConfig {

    /**
     * 限流器存储
     * key: API路径, value: 限流器
     */
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    /**
     * 获取或创建限流器
     *
     * @param apiPath API路径
     * @param permitsPerSecond 每秒允许的请求数
     * @return 限流器
     */
    public RateLimiter getRateLimiter(String apiPath, int permitsPerSecond) {
        return rateLimiters.computeIfAbsent(apiPath, 
            k -> new RateLimiter(permitsPerSecond));
    }

    /**
     * 简单的限流器实现
     */
    public static class RateLimiter {
        private final int permitsPerSecond;
        private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

        public RateLimiter(int permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
        }

        /**
         * 尝试获取令牌
         *
         * @param key 限流键（通常是用户ID或IP）
         * @return 是否获取成功
         */
        public boolean tryAcquire(String key) {
            TokenBucket bucket = buckets.computeIfAbsent(key, 
                k -> new TokenBucket(permitsPerSecond));
            return bucket.tryConsume();
        }

        /**
         * 获取剩余令牌数
         *
         * @param key 限流键
         * @return 剩余令牌数
         */
        public int getAvailablePermits(String key) {
            TokenBucket bucket = buckets.get(key);
            return bucket != null ? bucket.getAvailableTokens() : permitsPerSecond;
        }
    }

    /**
     * 令牌桶实现
     */
    private static class TokenBucket {
        private final int capacity;
        private final long refillIntervalNanos;
        private int availableTokens;
        private long lastRefillTime;

        public TokenBucket(int tokensPerSecond) {
            this.capacity = tokensPerSecond;
            this.refillIntervalNanos = TimeUnit.SECONDS.toNanos(1) / tokensPerSecond;
            this.availableTokens = tokensPerSecond;
            this.lastRefillTime = System.nanoTime();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (availableTokens > 0) {
                availableTokens--;
                return true;
            }
            return false;
        }

        public synchronized int getAvailableTokens() {
            refill();
            return availableTokens;
        }

        private void refill() {
            long now = System.nanoTime();
            long timePassed = now - lastRefillTime;
            int tokensToAdd = (int) (timePassed / refillIntervalNanos);
            
            if (tokensToAdd > 0) {
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }

    /**
     * 默认限流配置
     */
    @Bean
    public Map<String, Integer> defaultRateLimits() {
        Map<String, Integer> limits = new ConcurrentHashMap<>();
        
        // API限流配置（每秒请求数）
        limits.put("/api/compliance/**", 100);
        limits.put("/api/security/**", 100);
        limits.put("/api/audit/**", 200);
        limits.put("/api/encryption/**", 50);
        limits.put("/api/lineage/**", 50);
        limits.put("/api/scan/**", 30);
        limits.put("/api/health/**", 1000);
        
        // 默认限流
        limits.put("default", 100);
        
        return limits;
    }
}
