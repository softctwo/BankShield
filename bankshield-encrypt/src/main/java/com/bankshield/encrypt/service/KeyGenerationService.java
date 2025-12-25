package com.bankshield.encrypt.service;

import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;

/**
 * 密钥生成服务接口
 */
public interface KeyGenerationService {
    
    /**
     * 生成密钥
     * 
     * @param keyType 密钥类型
     * @param keyUsage 密钥用途
     * @param keyLength 密钥长度（可选，为null时使用默认长度）
     * @return 生成的密钥材料（Base64编码）
     */
    String generateKey(KeyType keyType, KeyUsage keyUsage, Integer keyLength);
    
    /**
     * 生成密钥对（用于非对称加密）
     * 
     * @param keyType 密钥类型（必须是SM2或RSA）
     * @param keyUsage 密钥用途
     * @param keyLength 密钥长度（可选，为null时使用默认长度）
     * @return 密钥对，包含公钥和私钥（Base64编码）
     */
    KeyPair generateKeyPair(KeyType keyType, KeyUsage keyUsage, Integer keyLength);
    
    /**
     * 计算密钥指纹
     * 
     * @param keyMaterial 密钥材料
     * @return 密钥指纹（SHA256）
     */
    String calculateKeyFingerprint(String keyMaterial);
    
    /**
     * 密钥对包装类
     */
    class KeyPair {
        private final String publicKey;
        private final String privateKey;
        
        public KeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
        
        public String getPublicKey() {
            return publicKey;
        }
        
        public String getPrivateKey() {
            return privateKey;
        }
    }
}