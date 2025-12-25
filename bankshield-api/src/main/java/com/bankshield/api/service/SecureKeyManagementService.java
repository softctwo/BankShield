package com.bankshield.api.service;

import com.bankshield.api.config.VaultConfig;
import com.bankshield.common.result.Result;
import com.bankshield.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全密钥管理服务
 * 集成HashiCorp Vault进行密钥安全管理
 */
@Slf4j
@Service
public class SecureKeyManagementService {
    
    @Autowired
    private VaultOperations vaultOperations;
    
    @Autowired
    private VaultConfig vaultConfig;
    
    @Autowired
    private AuditService auditService;
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    
    /**
     * 从Vault获取主密钥
     */
    public String getMasterKeyFromVault() {
        try {
            log.debug("Attempting to retrieve master key from Vault: {}", vaultConfig.getMasterKeyPath());
            
            VaultResponse response = vaultOperations.read(vaultConfig.getMasterKeyPath());
            if (response != null && response.getData() != null) {
                String masterKey = (String) response.getData().get("key");
                if (masterKey != null && !masterKey.trim().isEmpty()) {
                    log.info("Master key successfully retrieved from Vault");
                    auditService.logKeyRetrieval("MASTER_KEY", "SUCCESS", "Retrieved master key from Vault");
                    return masterKey;
                }
            }
            
            log.error("Failed to retrieve master key from Vault: key not found or empty");
            auditService.logKeyRetrieval("MASTER_KEY", "FAILED", "Master key not found in Vault");
            throw new RuntimeException("无法从Vault获取主密钥");
            
        } catch (Exception e) {
            log.error("Vault访问失败", e);
            auditService.logKeyRetrieval("MASTER_KEY", "ERROR", "Vault access failed: " + e.getMessage());
            throw new RuntimeException("密钥管理服务不可用", e);
        }
    }
    
    /**
     * 生成新的加密密钥
     */
    public Result<String> generateEncryptionKey(String algorithm, int keyLength) {
        try {
            log.info("Generating new encryption key: algorithm={}, length={}", algorithm, keyLength);
            
            String keyMaterial;
            switch (algorithm.toUpperCase()) {
                case "SM4":
                    keyMaterial = generateSM4Key(keyLength);
                    break;
                case "AES":
                    keyMaterial = generateAESKey(keyLength);
                    break;
                case "SM2":
                    keyMaterial = generateSM2KeyPair();
                    break;
                case "RSA":
                    keyMaterial = generateRSAKeyPair(keyLength);
                    break;
                default:
                    return Result.error("不支持的算法: " + algorithm);
            }
            
            // 使用Vault中的主密钥加密新生成的密钥
            String masterKey = getMasterKeyFromVault();
            String encryptedKeyMaterial = encryptKeyMaterial(keyMaterial, masterKey);
            
            log.info("Encryption key generated successfully");
            auditService.logKeyGeneration(algorithm, keyLength, "SUCCESS", "Generated new encryption key");
            
            return Result.success(encryptedKeyMaterial);
            
        } catch (Exception e) {
            log.error("Failed to generate encryption key", e);
            auditService.logKeyGeneration(algorithm, keyLength, "FAILED", "Key generation failed: " + e.getMessage());
            return Result.error("生成加密密钥失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成SM4密钥
     */
    private String generateSM4Key(int keyLength) throws Exception {
        if (keyLength != 128 && keyLength != 256) {
            throw new IllegalArgumentException("SM4仅支持128位或256位密钥长度");
        }
        
        int byteLength = keyLength / 8;
        byte[] keyBytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(keyBytes);
        
        return BASE64_ENCODER.encodeToString(keyBytes);
    }
    
    /**
     * 生成AES密钥
     */
    private String generateAESKey(int keyLength) throws Exception {
        if (keyLength != 128 && keyLength != 192 && keyLength != 256) {
            throw new IllegalArgumentException("AES仅支持128位、192位或256位密钥长度");
        }
        
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keyLength);
        SecretKey secretKey = keyGenerator.generateKey();
        
        return BASE64_ENCODER.encodeToString(secretKey.getEncoded());
    }
    
    /**
     * 生成SM2密钥对
     */
    private String generateSM2KeyPair() throws Exception {
        // 这里应该集成国密SM2密钥对生成
        // 暂时返回模拟数据，实际实现需要集成国密算法库
        String privateKey = generateSecureRandomKey(256);
        String publicKey = generateSecureRandomKey(512);
        
        return privateKey + "|" + publicKey;
    }
    
    /**
     * 生成RSA密钥对
     */
    private String generateRSAKeyPair(int keyLength) throws Exception {
        if (keyLength < 1024 || keyLength > 4096) {
            throw new IllegalArgumentException("RSA密钥长度必须在1024-4096位之间");
        }
        
        // 这里应该集成RSA密钥对生成
        // 暂时返回模拟数据，实际实现需要集成RSA算法库
        String privateKey = generateSecureRandomKey(keyLength);
        String publicKey = generateSecureRandomKey(keyLength * 2);
        
        return privateKey + "|" + publicKey;
    }
    
    /**
     * 生成安全随机密钥
     */
    private String generateSecureRandomKey(int bitLength) {
        int byteLength = bitLength / 8;
        byte[] keyBytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(keyBytes);
        
        return BASE64_ENCODER.encodeToString(keyBytes);
    }
    
    /**
     * 使用主密钥加密密钥材料
     */
    private String encryptKeyMaterial(String keyMaterial, String masterKey) throws Exception {
        // 这里应该使用国密SM4或其他对称加密算法
        // 暂时返回模拟加密结果，实际实现需要集成加密算法
        return "ENCRYPTED_" + keyMaterial + "_WITH_" + masterKey.substring(0, 8);
    }
    
    /**
     * 解密密钥材料
     */
    public Result<String> decryptKeyMaterial(String encryptedKeyMaterial) {
        try {
            String masterKey = getMasterKeyFromVault();
            
            // 这里应该使用相应的解密算法
            // 暂时返回模拟解密结果，实际实现需要集成解密算法
            if (encryptedKeyMaterial.startsWith("ENCRYPTED_")) {
                String decrypted = encryptedKeyMaterial.substring(10);
                decrypted = decrypted.replace("_WITH_" + masterKey.substring(0, 8), "");
                
                auditService.logKeyDecryption("SUCCESS", "Key material decrypted successfully");
                return Result.success(decrypted);
            } else {
                auditService.logKeyDecryption("FAILED", "Invalid encrypted key material format");
                return Result.error("无效的加密密钥材料格式");
            }
            
        } catch (Exception e) {
            log.error("Failed to decrypt key material", e);
            auditService.logKeyDecryption("ERROR", "Key decryption failed: " + e.getMessage());
            return Result.error("解密密钥材料失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证Vault连接状态
     */
    public Result<Map<String, Object>> checkVaultStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 尝试从Vault读取主密钥以验证连接
            String masterKey = getMasterKeyFromVault();
            
            status.put("vaultAddress", vaultConfig.getAddress());
            status.put("roleId", vaultConfig.getRoleId() != null ? 
                    vaultConfig.getRoleId().substring(0, 8) + "..." : "null");
            status.put("masterKeyAvailable", masterKey != null);
            status.put("timestamp", LocalDateTime.now());
            status.put("status", "CONNECTED");
            
            return Result.success(status);
            
        } catch (Exception e) {
            log.error("Vault connection check failed", e);

            Map<String, Object> status = new HashMap<>();
            status.put("vaultAddress", vaultConfig.getAddress());
            status.put("error", e.getMessage());
            status.put("timestamp", LocalDateTime.now());
            status.put("status", "DISCONNECTED");

            return Result.error("Vault连接失败: " + e.getMessage());
        }
    }
}