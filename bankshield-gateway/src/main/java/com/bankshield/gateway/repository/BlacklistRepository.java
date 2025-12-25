package com.bankshield.gateway.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

/**
 * 黑名单存储Repository（基于Redis）
 * 
 * @author BankShield
 */
@Repository
public class BlacklistRepository {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:ip:";
    
    /**
     * 添加IP到黑名单
     * 
     * @param ipAddress IP地址
     * @param blockReason 封禁原因
     * @param blockDuration 封禁时长（秒）
     */
    public void addToBlacklist(String ipAddress, String blockReason, long blockDuration) {
        String key = BLACKLIST_KEY_PREFIX + ipAddress;
        String value = blockReason != null ? blockReason : "恶意访问";
        
        if (blockDuration > 0) {
            redisTemplate.opsForValue().set(key, value, blockDuration, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }
    
    /**
     * 从黑名单中移除IP
     * 
     * @param ipAddress IP地址
     */
    public void removeFromBlacklist(String ipAddress) {
        String key = BLACKLIST_KEY_PREFIX + ipAddress;
        redisTemplate.delete(key);
    }
    
    /**
     * 检查IP是否在黑名单中
     * 
     * @param ipAddress IP地址
     * @return true: 在黑名单中, false: 不在黑名单中
     */
    public boolean isBlacklisted(String ipAddress) {
        String key = BLACKLIST_KEY_PREFIX + ipAddress;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 获取IP的封禁原因
     * 
     * @param ipAddress IP地址
     * @return 封禁原因
     */
    public String getBlockReason(String ipAddress) {
        String key = BLACKLIST_KEY_PREFIX + ipAddress;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 获取IP的剩余封禁时间
     * 
     * @param ipAddress IP地址
     * @return 剩余封禁时间（秒）
     */
    public long getRemainingBlockTime(String ipAddress) {
        String key = BLACKLIST_KEY_PREFIX + ipAddress;
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }
    
    /**
     * 批量检查IP是否在黑名单中
     * 
     * @param ipAddresses IP地址列表
     * @return 在黑名单中的IP数量
     */
    public long countBlacklistedIps(String... ipAddresses) {
        long count = 0;
        for (String ipAddress : ipAddresses) {
            if (isBlacklisted(ipAddress)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取所有黑名单IP
     * 
     * @return 黑名单IP列表
     */
    public java.util.Set<String> getAllBlacklistedIps() {
        return redisTemplate.keys(BLACKLIST_KEY_PREFIX + "*");
    }
    
    /**
     * 批量添加IP到黑名单
     * 
     * @param ipAddresses IP地址列表
     * @param blockReason 封禁原因
     * @param blockDuration 封禁时长（秒）
     */
    public void addToBlacklistBatch(java.util.List<String> ipAddresses, String blockReason, long blockDuration) {
        for (String ipAddress : ipAddresses) {
            addToBlacklist(ipAddress, blockReason, blockDuration);
        }
    }
    
    /**
     * 批量从黑名单中移除IP
     * 
     * @param ipAddresses IP地址列表
     */
    public void removeFromBlacklistBatch(java.util.List<String> ipAddresses) {
        for (String ipAddress : ipAddresses) {
            removeFromBlacklist(ipAddress);
        }
    }
    
    /**
     * 清空黑名单
     */
    public void clearBlacklist() {
        java.util.Set<String> keys = redisTemplate.keys(BLACKLIST_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}