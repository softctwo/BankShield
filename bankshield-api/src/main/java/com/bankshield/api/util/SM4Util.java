package com.bankshield.api.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;

/**
 * SM4国密对称加密工具类
 */
@Slf4j
public class SM4Util {
    
    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/ECB/PKCS5Padding";
    private static final String DEFAULT_KEY = "BankShield2024SM"; // 16字节密钥（实际应从密钥管理系统获取）
    
    static {
        // 添加BouncyCastle Provider
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    
    /**
     * 生成SM4密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            keyGen.init(128, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            return Base64.encode(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("生成SM4密钥失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * SM4加密（使用默认密钥）
     */
    public static String encrypt(String data) {
        return encrypt(data, DEFAULT_KEY);
    }
    
    /**
     * SM4加密
     */
    public static String encrypt(String data, String key) {
        if (StrUtil.isBlank(data)) {
            return data;
        }
        
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.encode(encrypted);
        } catch (Exception e) {
            log.error("SM4加密失败: {}", e.getMessage(), e);
            return data;
        }
    }
    
    /**
     * SM4解密（使用默认密钥）
     */
    public static String decrypt(String encryptedData) {
        return decrypt(encryptedData, DEFAULT_KEY);
    }
    
    /**
     * SM4解密
     */
    public static String decrypt(String encryptedData, String key) {
        if (StrUtil.isBlank(encryptedData)) {
            return encryptedData;
        }
        
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] encrypted = Base64.decode(encryptedData);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            log.error("SM4解密失败: {}", e.getMessage(), e);
            return encryptedData;
        }
    }
}
