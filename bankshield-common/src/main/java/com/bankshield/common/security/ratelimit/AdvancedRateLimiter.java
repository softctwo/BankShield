package com.bankshield.common.security.ratelimit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高级限流器
 * 基于Redis的分布式令牌桶限流算法，支持多种限流策略
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
public class AdvancedRateLimiter {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 不同接口的限流规则
    private final Map<String, RateLimitRule> rules = new ConcurrentHashMap<>();
    
    // Redis Lua脚本，用于原子化限流操作
    private static final String RATE_LIMIT_SCRIPT = 
        "local key = KEYS[1] " +
        "local rate = tonumber(ARGV[1]) " +
        "local capacity = tonumber(ARGV[2]) " +
        "local now = tonumber(ARGV[3]) " +
        "local requested = tonumber(ARGV[4]) " +
        "\n" +
        "local fill_time = capacity/rate " +
        "local ttl = math.floor(fill_time*2) " +
        "\n" +
        "local last_tokens = tonumber(redis.call('get', key) or capacity) " +
        "local last_refreshed = tonumber(redis.call('get', key .. ':timestamp') or 0) " +
        "\n" +
        "local delta = math.max(0, now-last_refreshed) " +
        "local filled_tokens = math.min(capacity, last_tokens + (delta*rate)/1000) " +
        "local allowed = filled_tokens >= requested " +
        "local new_tokens = filled_tokens " +
        "local allowed_num = 0 " +
        "\n" +
        "if allowed then " +
        "  new_tokens = filled_tokens - requested " +
        "  allowed_num = 1 " +
        "end " +
        "\n" +
        "redis.call('setex', key, ttl, new_tokens) " +
        "redis.call('setex', key .. ':timestamp', ttl, now) " +
        "\n" +
        "return { allowed_num, new_tokens }";
    
    @PostConstruct
    public void init() {
        // 用户管理接口
        rules.put("/api/user/*", new RateLimitRule(100, 1000, Duration.ofSeconds(1), "user"));
        rules.put("/api/user/login", new RateLimitRule(10, 50, Duration.ofSeconds(1), "login"));
        rules.put("/api/user/logout", new RateLimitRule(20, 100, Duration.ofSeconds(1), "logout"));
        rules.put("/api/user/register", new RateLimitRule(5, 20, Duration.ofMinutes(1), "register"));
        
        // 密钥管理接口（更严格）
        rules.put("/api/key/*", new RateLimitRule(20, 50, Duration.ofSeconds(1), "key"));
        rules.put("/api/key/generate", new RateLimitRule(5, 10, Duration.ofMinutes(1), "key_generate"));
        rules.put("/api/key/rotate", new RateLimitRule(2, 5, Duration.ofMinutes(1), "key_rotate"));
        
        // 审计导出接口（最严格）
        rules.put("/api/audit/export", new RateLimitRule(5, 10, Duration.ofMinutes(1), "export"));
        rules.put("/api/report/generate", new RateLimitRule(3, 6, Duration.ofMinutes(1), "report"));
        
        // 数据操作接口
        rules.put("/api/data/*", new RateLimitRule(200, 500, Duration.ofSeconds(1), "data"));
        rules.put("/api/encrypt/*", new RateLimitRule(150, 300, Duration.ofSeconds(1), "encrypt"));
        rules.put("/api/decrypt/*", new RateLimitRule(150, 300, Duration.ofSeconds(1), "decrypt"));
        
        // 监控接口
        rules.put("/api/monitor/*", new RateLimitRule(50, 100, Duration.ofSeconds(1), "monitor"));
        rules.put("/api/dashboard/*", new RateLimitRule(30, 60, Duration.ofSeconds(1), "dashboard"));
        
        // 系统管理接口
        rules.put("/api/system/*", new RateLimitRule(30, 60, Duration.ofSeconds(1), "system"));
        rules.put("/api/config/*", new RateLimitRule(20, 40, Duration.ofSeconds(1), "config"));
        
        // 默认规则
        rules.put("default", new RateLimitRule(100, 200, Duration.ofSeconds(1), "default"));
    }
    
    /**
     * 尝试获取访问权限
     * 
     * @param path 请求路径
     * @param userId 用户ID
     * @return 是否允许访问
     */
    public boolean tryAcquire(String path, String userId) {
        RateLimitRule rule = findRule(path);
        if (rule == null) {
            rule = rules.get("default");
        }
        
        // 构建限流Key
        String key = String.format("rate_limit:%s:%s:%s", rule.getType(), userId, path);
        
        try {
            // 执行Lua脚本进行原子化限流判断
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(RATE_LIMIT_SCRIPT);
            script.setResultType(Long.class);
            
            long now = System.currentTimeMillis();
            Long result = redisTemplate.execute(
                script,
                Arrays.asList(key),
                rule.getRate(),                    // 每秒生成令牌数
                rule.getCapacity(),                // 令牌桶容量
                now,                               // 当前时间戳
                1L                                   // 请求的令牌数
            );
            
            boolean allowed = result != null && result > 0;
            
            if (!allowed) {
                log.warn("用户{}访问{}被限流，规则类型: {}, 路径: {}", 
                        userId, path, rule.getType(), path);
                recordRateLimitEvent(userId, path, rule.getType());
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("限流处理失败: {}", e.getMessage(), e);
            // 限流失败时默认允许访问，避免影响正常业务
            return true;
        }
    }
    
    /**
     * 获取剩余令牌数
     * 
     * @param path 请求路径
     * @param userId 用户ID
     * @return 剩余令牌数
     */
    public long getRemainingTokens(String path, String userId) {
        RateLimitRule rule = findRule(path);
        if (rule == null) {
            rule = rules.get("default");
        }
        
        String key = String.format("rate_limit:%s:%s:%s", rule.getType(), userId, path);
        Long tokens = (Long) redisTemplate.opsForValue().get(key);
        return tokens != null ? tokens : rule.getCapacity();
    }
    
    /**
     * 重置限流计数
     * 
     * @param path 请求路径
     * @param userId 用户ID
     */
    public void resetRateLimit(String path, String userId) {
        RateLimitRule rule = findRule(path);
        if (rule == null) {
            rule = rules.get("default");
        }
        
        String key = String.format("rate_limit:%s:%s:%s", rule.getType(), userId, path);
        redisTemplate.delete(key);
        redisTemplate.delete(key + ":timestamp");
        
        log.info("重置用户{}路径{}的限流计数", userId, path);
    }
    
    /**
     * 查找匹配的限流规则
     * 
     * @param path 请求路径
     * @return 限流规则
     */
    private RateLimitRule findRule(String path) {
        return rules.entrySet().stream()
            .filter(entry -> pathMatcher(entry.getKey(), path))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 路径匹配器（支持通配符）
     * 
     * @param pattern 模式
     * @param path 路径
     * @return 是否匹配
     */
    private boolean pathMatcher(String pattern, String path) {
        if (pattern.equals(path)) {
            return true;
        }
        
        // 支持通配符匹配
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix);
        }
        
        return false;
    }
    
    /**
     * 记录限流事件
     * 
     * @param userId 用户ID
     * @param path 请求路径
     * @param ruleType 规则类型
     */
    private void recordRateLimitEvent(String userId, String path, String ruleType) {
        String eventKey = String.format("rate_limit_event:%s:%s:%s", userId, ruleType, System.currentTimeMillis());
        String eventData = String.format("用户:%s, 路径:%s, 规则:%s, 时间:%s", 
                userId, path, ruleType, new java.util.Date());
        
        // 存储限流事件，有效期1小时
        redisTemplate.opsForValue().set(eventKey, eventData, Duration.ofHours(1));
    }
    
    /**
     * 获取限流统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getRateLimitStats(String userId) {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        long totalEvents = 0;
        for (Map.Entry<String, RateLimitRule> entry : rules.entrySet()) {
            String key = String.format("rate_limit:%s:%s:%s", entry.getValue().getType(), userId, entry.getKey());
            Long tokens = (Long) redisTemplate.opsForValue().get(key);
            if (tokens != null) {
                totalEvents++;
            }
        }
        
        stats.put("totalActiveRules", totalEvents);
        stats.put("timestamp", System.currentTimeMillis());
        
        return stats;
    }
    
    /**
     * 限流规则
     */
    @Data
    public static class RateLimitRule {
        private final long rate;              // 每秒生成令牌数
        private final long capacity;          // 令牌桶容量
        private final Duration period;        // 时间周期
        private final String type;            // 规则类型
        
        public RateLimitRule(long rate, long capacity, Duration period, String type) {
            this.rate = rate;
            this.capacity = capacity;
            this.period = period;
            this.type = type;
        }
        
        public RateLimitRule(long rate, Duration period, String type) {
            this(rate, rate * 10, period, type); // 默认容量为速率的10倍
        }
    }
}