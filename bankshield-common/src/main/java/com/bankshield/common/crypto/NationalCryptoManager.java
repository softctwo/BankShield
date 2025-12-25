package com.bankshield.common.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 国密算法管理器
 * 统一管理所有国密算法操作
 */
@Slf4j
@Component
public class NationalCryptoManager {
    
    @Autowired
    private NationalCryptoConfig config;
    
    private SM3PasswordEncoder passwordEncoder;
    private KeyPair sm2KeyPair;
    
    @PostConstruct
    public void init() {
        if (config.isEnabled()) {
            log.info("国密算法管理器初始化...");
            
            // 初始化密码编码器
            if ("SM3".equals(config.getPasswordEncoderType())) {
                passwordEncoder = new SM3PasswordEncoder();
                log.info("使用SM3密码编码器");
            }
            
            // 生成SM2密钥对
            sm2KeyPair = SM2Util.generateKeyPair();
            log.info("SM2密钥对生成成功");
            
            log.info("国密算法管理器初始化完成");
        } else {
            log.warn("国密算法已禁用");
        }
    }
    
    /**
     * 获取密码编码器
     */
    public SM3PasswordEncoder getPasswordEncoder() {
        if (passwordEncoder == null) {
            throw new RuntimeException("SM3密码编码器未初始化");
        }
        return passwordEncoder;
    }
    
    /**
     * 获取SM2密钥对
     */
    public KeyPair getSm2KeyPair() {
        return sm2KeyPair;
    }
    
    /**
     * 获取SM2公钥
     */
    public PublicKey getSm2PublicKey() {
        return sm2KeyPair.getPublic();
    }
    
    /**
     * 获取SM2私钥
     */
    public PrivateKey getSm2PrivateKey() {
        return sm2KeyPair.getPrivate();
    }
    
    /**
     * 加密数据（使用SM4）
     */
    public String encryptData(String data, String key, String mode, String iv) {
        if (!config.isEnabled()) {
            throw new RuntimeException("国密算法已禁用");
        }
        return SM4Util.encrypt(key, data, mode, iv);
    }
    
    /**
     * 解密数据（使用SM4）
     */
    public String decryptData(String encryptedData, String key, String mode, String iv) {
        if (!config.isEnabled()) {
            throw new RuntimeException("国密算法已禁用");
        }
        return SM4Util.decrypt(key, encryptedData, mode, iv);
    }
    
    /**
     * 计算哈希（使用SM3）
     */
    public String hashData(String data) {
        if (!config.isEnabled()) {
            throw new RuntimeException("国密算法已禁用");
        }
        return SM3Util.hash(data);
    }
    
    /**
     * 签名数据（使用SM2）
     */
    public String signData(String data) {
        if (!config.isEnabled()) {
            throw new RuntimeException("国密算法已禁用");
        }
        String privateKey = SM2Util.privateKeyToString(sm2KeyPair.getPrivate());
        return SM2Util.sign(privateKey, data);
    }
    
    /**
     * 验证签名（使用SM2）
     */
    public boolean verifySignature(String data, String signature) {
        if (!config.isEnabled()) {
            throw new RuntimeException("国密算法已禁用");
        }
        String publicKey = SM2Util.publicKeyToString(sm2KeyPair.getPublic());
        return SM2Util.verify(publicKey, data, signature);
    }
    
    /**
     * 加密密码
     */
    public String encodePassword(String rawPassword) {
        if (!config.isEnabled()) {
            throw new RuntimeException("国密算法已禁用");
        }
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     */
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (!config.isEnabled()) {
            throw new RuntimeException("国密算法已禁用");
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 生成新的SM2密钥对
     */
    public KeyPair generateNewSm2KeyPair() {
        sm2KeyPair = SM2Util.generateKeyPair();
        return sm2KeyPair;
    }
    
    /**
     * 获取国密算法状态
     */
    public boolean isNationalCryptoEnabled() {
        return config.isEnabled();
    }
    
    /**
     * 获取配置
     */
    public NationalCryptoConfig getConfig() {
        return config;
    }
}