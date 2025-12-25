package com.bankshield.encrypt.service;

/**
 * 密钥存储服务接口
 * 负责密钥的安全存储和检索
 */
public interface KeyStorageService {
    
    /**
     * 加密密钥材料
     * 
     * @param keyMaterial 原始密钥材料
     * @return 加密后的密钥材料
     */
    String encryptKeyMaterial(String keyMaterial);
    
    /**
     * 解密密钥材料
     * 
     * @param encryptedKeyMaterial 加密的密钥材料
     * @return 原始密钥材料
     */
    String decryptKeyMaterial(String encryptedKeyMaterial);
    
    /**
     * 安全删除密钥材料
     * 确保密钥材料被完全清除，不可恢复
     * 
     * @param keyMaterial 要删除的密钥材料
     */
    void secureDeleteKeyMaterial(String keyMaterial);
    
    /**
     * 验证密钥存储配置
     * 
     * @return 配置是否有效
     */
    boolean validateStorageConfiguration();
    
    /**
     * 获取存储类型
     * 
     * @return 存储类型描述
     */
    String getStorageType();
}