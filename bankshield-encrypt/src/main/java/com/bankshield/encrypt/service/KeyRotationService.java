package com.bankshield.encrypt.service;

import java.util.List;

/**
 * 密钥轮换服务接口
 */
public interface KeyRotationService {
    
    /**
     * 执行密钥轮换任务
     * 检查所有需要轮换的密钥并执行轮换
     * 
     * @return 轮换结果
     */
    RotationResult performRotationTask();
    
    /**
     * 检查即将过期的密钥
     * 
     * @param daysAhead 提前多少天检查
     * @return 检查结果
     */
    ExpirationCheckResult checkExpiringKeys(int daysAhead);
    
    /**
     * 轮换结果
     */
    class RotationResult {
        private int totalKeys;
        private int rotatedKeys;
        private int failedKeys;
        private String message;
        
        public RotationResult(int totalKeys, int rotatedKeys, int failedKeys, String message) {
            this.totalKeys = totalKeys;
            this.rotatedKeys = rotatedKeys;
            this.failedKeys = failedKeys;
            this.message = message;
        }
        
        public int getTotalKeys() { return totalKeys; }
        public int getRotatedKeys() { return rotatedKeys; }
        public int getFailedKeys() { return failedKeys; }
        public String getMessage() { return message; }
    }
    
    /**
     * 过期检查结果
     */
    class ExpirationCheckResult {
        private int totalKeys;
        private int expiringKeys;
        private List<String> expiringKeyNames;
        private String message;
        
        public ExpirationCheckResult(int totalKeys, int expiringKeys, List<String> expiringKeyNames, String message) {
            this.totalKeys = totalKeys;
            this.expiringKeys = expiringKeys;
            this.expiringKeyNames = expiringKeyNames;
            this.message = message;
        }
        
        public int getTotalKeys() { return totalKeys; }
        public int getExpiringKeys() { return expiringKeys; }
        public List<String> getExpiringKeyNames() { return expiringKeyNames; }
        public String getMessage() { return message; }
    }
}