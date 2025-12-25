package com.bankshield.gateway.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.connection.RedisCallback;
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
     * 使用SCAN命令替代KEYS，避免阻塞Redis
     *
     * @return 黑名单IP列表
     */
    public java.util.Set<String> getAllBlacklistedIps() {
        return redisTemplate.execute((RedisCallback<java.util.Set<String>>) connection -> {
            java.util.Set<String> keys = new java.util.HashSet<>();
            String pattern = BLACKLIST_KEY_PREFIX + "*";
            long cursor = 0;
            do {
                ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000)
                    .build();

                Cursor<byte[]> scanResult = connection.scan(options);
                while (scanResult.hasNext()) {
                    byte[] keyBytes = scanResult.next();
                    keys.add(new String(keyBytes));
                }
                cursor = scanResult.getCursorId();
                scanResult.close();
            } while (cursor != 0);

            return keys;
        });
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
     * 使用SCAN命令替代KEYS，避免阻塞Redis
     */
    public void clearBlacklist() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            String pattern = BLACKLIST_KEY_PREFIX + "*";
            long cursor = 0;
            java.util.List<byte[]> keysToDelete = new java.util.ArrayList<>();

            do {
                ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000)
                    .build();

                Cursor<byte[]> scanResult = connection.scan(options);
                while (scanResult.hasNext()) {
                    byte[] keyBytes = scanResult.next();
                    keysToDelete.add(keyBytes);

                    // 批量删除，避免单次操作数据量过大
                    if (keysToDelete.size() >= 1000) {
                        connection.del(keysToDelete.toArray(new byte[0][]));
                        keysToDelete.clear();
                    }
                }
                cursor = scanResult.getCursorId();
                scanResult.close();
            } while (cursor != 0);

            // 删除剩余的key
            if (!keysToDelete.isEmpty()) {
                connection.del(keysToDelete.toArray(new byte[0][]));
            }

            return null;
        });
    }
}