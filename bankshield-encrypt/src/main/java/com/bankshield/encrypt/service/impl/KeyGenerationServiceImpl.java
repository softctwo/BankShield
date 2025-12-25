package com.bankshield.encrypt.service.impl;

import com.bankshield.common.crypto.SM2Util;
import com.bankshield.common.crypto.SM4Util;
import com.bankshield.common.utils.EncryptUtil;
import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;
import com.bankshield.encrypt.service.KeyGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 密钥生成服务实现类
 */
@Slf4j
@Service
public class KeyGenerationServiceImpl implements KeyGenerationService {
    
    private static final int DEFAULT_SM2_KEY_LENGTH = 256;
    private static final int DEFAULT_SM4_KEY_LENGTH = 128;
    private static final int DEFAULT_AES_KEY_LENGTH = 256;
    private static final int DEFAULT_RSA_KEY_LENGTH = 2048;
    
    @Override
    public String generateKey(KeyType keyType, KeyUsage keyUsage, Integer keyLength) {
        log.info("生成密钥，类型：{}，用途：{}，长度：{}", keyType, keyUsage, keyLength);
        
        try {
            switch (keyType) {
                case SM4:
                    return generateSM4Key(keyLength);
                case AES:
                    return generateAESKey(keyLength);
                case SM3:
                    throw new IllegalArgumentException("SM3是哈希算法，不需要生成密钥");
                default:
                    throw new IllegalArgumentException("对称加密算法不支持生成单个密钥：" + keyType);
            }
        } catch (Exception e) {
            log.error("生成密钥失败，类型：{}，用途：{}", keyType, keyUsage, e);
            throw new RuntimeException("生成密钥失败", e);
        }
    }
    
    @Override
    public KeyPair generateKeyPair(KeyType keyType, KeyUsage keyUsage, Integer keyLength) {
        log.info("生成密钥对，类型：{}，用途：{}，长度：{}", keyType, keyUsage, keyLength);
        
        try {
            switch (keyType) {
                case SM2:
                    return generateSM2KeyPair(keyLength);
                case RSA:
                    return generateRSAKeyPair(keyLength);
                default:
                    throw new IllegalArgumentException("非对称加密算法不支持生成密钥对：" + keyType);
            }
        } catch (Exception e) {
            log.error("生成密钥对失败，类型：{}，用途：{}", keyType, keyUsage, e);
            throw new RuntimeException("生成密钥对失败", e);
        }
    }
    
    @Override
    public String calculateKeyFingerprint(String keyMaterial) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyMaterial.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("计算密钥指纹失败", e);
            throw new RuntimeException("计算密钥指纹失败", e);
        }
    }
    
    /**
     * 生成SM4密钥
     */
    private String generateSM4Key(Integer keyLength) {
        if (keyLength != null && keyLength != DEFAULT_SM4_KEY_LENGTH) {
            log.warn("SM4密钥长度必须为128位，忽略指定长度：{}", keyLength);
        }
        return SM4Util.generateKey();
    }
    
    /**
     * 生成AES密钥
     */
    private String generateAESKey(Integer keyLength) {
        int actualKeyLength = keyLength != null ? keyLength : DEFAULT_AES_KEY_LENGTH;
        if (actualKeyLength != 128 && actualKeyLength != 192 && actualKeyLength != 256) {
            throw new IllegalArgumentException("AES密钥长度必须是128、192或256位");
        }
        return EncryptUtil.generateAesKey();
    }
    
    /**
     * 生成SM2密钥对
     */
    private KeyPair generateSM2KeyPair(Integer keyLength) {
        if (keyLength != null && keyLength != DEFAULT_SM2_KEY_LENGTH) {
            log.warn("SM2密钥长度必须为256位，忽略指定长度：{}", keyLength);
        }
        
        java.security.KeyPair keyPair = SM2Util.generateKeyPair();
        String publicKey = SM2Util.publicKeyToString(keyPair.getPublic());
        String privateKey = SM2Util.privateKeyToString(keyPair.getPrivate());
        
        return new KeyPair(publicKey, privateKey);
    }
    
    /**
     * 生成RSA密钥对
     */
    private KeyPair generateRSAKeyPair(Integer keyLength) {
        int actualKeyLength = keyLength != null ? keyLength : DEFAULT_RSA_KEY_LENGTH;
        if (actualKeyLength < 1024) {
            throw new IllegalArgumentException("RSA密钥长度必须至少为1024位");
        }
        
        try {
            java.security.KeyPair keyPair = EncryptUtil.generateRsaKeyPair();
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }
}