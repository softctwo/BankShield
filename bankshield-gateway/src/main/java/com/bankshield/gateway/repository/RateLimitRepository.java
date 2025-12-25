package com.bankshield.gateway.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 限流计数存储Repository（基于Redis）
 * 
 * @author BankShield
 */
@Repository
public class RateLimitRepository {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 限流Lua脚本
     */
    private static final String RATE_LIMIT_LUA_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local limit = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local current = redis.call('get', key)\n" +
        "if current == false then\n" +
        "    redis.call('set', key, 1)\n" +
        "    redis.call('expire', key, window)\n" +
        "    return 1\n" +
        "elseif tonumber(current) < limit then\n" +
        "    redis.call('incr', key)\n" +
        "    return tonumber(current) + 1\n" +
        "else\n" +
        "    return 0\n" +
        "end";
    
    /**
     * 检查是否允许访问
     * 
     * @param key 限流键
     * @param limit 限流阈值
     * @param window 时间窗口（秒）
     * @return true: 允许访问, false: 拒绝访问
     */
    public boolean isAllowed(String key, int limit, int window) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(RATE_LIMIT_LUA_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, Arrays.asList(key), String.valueOf(limit), String.valueOf(window));
        return result != null && result > 0;
    }
    
    /**
     * 获取当前计数
     * 
     * @param key 限流键
     * @return 当前计数
     */
    public long getCurrentCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }
    
    /**
     * 设置计数
     * 
     * @param key 限流键
     * @param count 计数
     * @param timeout 过期时间（秒）
     */
    public void setCount(String key, long count, long timeout) {
        redisTemplate.opsForValue().set(key, String.valueOf(count), timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 增加计数
     * 
     * @param key 限流键
     * @param delta 增量
     * @param timeout 过期时间（秒）
     * @return 增加后的计数
     */
    public long incrementCount(String key, long delta, long timeout) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        if (result != null && result == delta) {
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }
    
    /**
     * 删除计数
     * 
     * @param key 限流键
     */
    public void deleteCount(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * 获取剩余过期时间
     * 
     * @param key 限流键
     * @return 剩余过期时间（秒）
     */
    public long getExpireTime(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }
    
    /**
     * 批量删除计数
     * 
     * @param keys 限流键列表
     */
    public void deleteCounts(String... keys) {
        redisTemplate.delete(Arrays.asList(keys));
    }
}