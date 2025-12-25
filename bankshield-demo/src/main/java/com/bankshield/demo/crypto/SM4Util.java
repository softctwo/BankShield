package com.bankshield.demo.crypto;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SM4工具类 - 国密对称加密算法
 * 用于数据加密存储和传输
 */
public class SM4Util {
    private static final Logger log = LoggerFactory.getLogger(SM4Util.class);
    
    private static final String ALGORITHM_ECB = "SM4/ECB/PKCS5Padding";
    private static final String ALGORITHM_CBC = "SM4/CBC/PKCS5Padding";
    private static final String ALGORITHM_CTR = "SM4/CTR/NoPadding";
    private static final int KEY_SIZE = 16; // 128位
    private static final int IV_SIZE = 16;  // 128位
    
    /**
     * 生成SM4密钥
     */
    public static String generateKey() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] key = new byte[KEY_SIZE];
            secureRandom.nextBytes(key);
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            log.error("生成SM4密钥失败: {}", e.getMessage());
            throw new RuntimeException("生成SM4密钥失败", e);
        }
    }
    
    /**
     * 生成初始化向量（IV）
     */
    public static String generateIV() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            return Base64.getEncoder().encodeToString(iv);
        } catch (Exception e) {
            log.error("生成IV失败: {}", e.getMessage());
            throw new RuntimeException("生成IV失败", e);
        }
    }
    
    /**
     * SM4-ECB模式加密
     */
    public static String encryptECB(String key, String plainText) {
        try {
            SM4 sm4 = new SM4(Mode.ECB, Padding.PKCS5Padding, Base64.getDecoder().decode(key));
            return sm4.encryptBase64(plainText);
        } catch (Exception e) {
            log.error("SM4-ECB加密失败: {}", e.getMessage());
            throw new RuntimeException("SM4-ECB加密失败", e);
        }
    }
    
    /**
     * SM4-ECB模式解密
     */
    public static String decryptECB(String key, String cipherText) {
        try {
            SM4 sm4 = new SM4(Mode.ECB, Padding.PKCS5Padding, Base64.getDecoder().decode(key));
            return sm4.decryptStr(cipherText);
        } catch (Exception e) {
            log.error("SM4-ECB解密失败: {}", e.getMessage());
            throw new RuntimeException("SM4-ECB解密失败", e);
        }
    }
    
    /**
     * SM4-CBC模式加密
     */
    public static String encryptCBC(String key, String iv, String plainText) {
        try {
            SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, Base64.getDecoder().decode(key), Base64.getDecoder().decode(iv));
            return sm4.encryptBase64(plainText);
        } catch (Exception e) {
            log.error("SM4-CBC加密失败: {}", e.getMessage());
            throw new RuntimeException("SM4-CBC加密失败", e);
        }
    }
    
    /**
     * SM4-CBC模式解密
     */
    public static String decryptCBC(String key, String iv, String cipherText) {
        try {
            SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, Base64.getDecoder().decode(key), Base64.getDecoder().decode(iv));
            return sm4.decryptStr(cipherText);
        } catch (Exception e) {
            log.error("SM4-CBC解密失败: {}", e.getMessage());
            throw new RuntimeException("SM4-CBC解密失败", e);
        }
    }
    
    /**
     * SM4-CTR模式加密
     */
    public static String encryptCTR(String key, String iv, String plainText) {
        try {
            SM4 sm4 = new SM4(Mode.CTR, Padding.NoPadding, Base64.getDecoder().decode(key), Base64.getDecoder().decode(iv));
            return sm4.encryptBase64(plainText);
        } catch (Exception e) {
            log.error("SM4-CTR加密失败: {}", e.getMessage());
            throw new RuntimeException("SM4-CTR加密失败", e);
        }
    }
    
    /**
     * SM4-CTR模式解密
     */
    public static String decryptCTR(String key, String iv, String cipherText) {
        try {
            SM4 sm4 = new SM4(Mode.CTR, Padding.NoPadding, Base64.getDecoder().decode(key), Base64.getDecoder().decode(iv));
            return sm4.decryptStr(cipherText);
        } catch (Exception e) {
            log.error("SM4-CTR解密失败: {}", e.getMessage());
            throw new RuntimeException("SM4-CTR解密失败", e);
        }
    }
    
    /**
     * 通用加密方法（自动选择模式）
     */
    public static String encrypt(String key, String plainText, String mode, String iv) {
        switch (mode.toUpperCase()) {
            case "ECB":
                return encryptECB(key, plainText);
            case "CBC":
                if (iv == null) {
                    throw new IllegalArgumentException("CBC模式需要提供IV");
                }
                return encryptCBC(key, iv, plainText);
            case "CTR":
                if (iv == null) {
                    throw new IllegalArgumentException("CTR模式需要提供IV");
                }
                return encryptCTR(key, iv, plainText);
            default:
                throw new IllegalArgumentException("不支持的加密模式: " + mode);
        }
    }
    
    /**
     * 通用解密方法（自动选择模式）
     */
    public static String decrypt(String key, String cipherText, String mode, String iv) {
        switch (mode.toUpperCase()) {
            case "ECB":
                return decryptECB(key, cipherText);
            case "CBC":
                if (iv == null) {
                    throw new IllegalArgumentException("CBC模式需要提供IV");
                }
                return decryptCBC(key, iv, cipherText);
            case "CTR":
                if (iv == null) {
                    throw new IllegalArgumentException("CTR模式需要提供IV");
                }
                return decryptCTR(key, iv, cipherText);
            default:
                throw new IllegalArgumentException("不支持的解密模式: " + mode);
        }
    }
    
    /**
     * 字节数组加密（ECB模式）
     */
    public static byte[] encryptBytes(String key, byte[] plainData) {
        try {
            SM4 sm4 = new SM4(Mode.ECB, Padding.PKCS5Padding, Base64.getDecoder().decode(key));
            return sm4.encrypt(plainData);
        } catch (Exception e) {
            log.error("SM4字节数组加密失败: {}", e.getMessage());
            throw new RuntimeException("SM4字节数组加密失败", e);
        }
    }
    
    /**
     * 字节数组解密（ECB模式）
     */
    public static byte[] decryptBytes(String key, byte[] cipherData) {
        try {
            SM4 sm4 = new SM4(Mode.ECB, Padding.PKCS5Padding, Base64.getDecoder().decode(key));
            return sm4.decrypt(cipherData);
        } catch (Exception e) {
            log.error("SM4字节数组解密失败: {}", e.getMessage());
            throw new RuntimeException("SM4字节数组解密失败", e);
        }
    }
    
    /**
     * 生成密钥和IV（用于CBC/CTR模式）
     */
    public static KeyIV generateKeyIV() {
        String key = generateKey();
        String iv = generateIV();
        return new KeyIV(key, iv);
    }
    
    /**
     * 密钥和IV包装类
     */
    public static class KeyIV {
        private final String key;
        private final String iv;
        
        public KeyIV(String key, String iv) {
            this.key = key;
            this.iv = iv;
        }
        
        public String getKey() {
            return key;
        }
        
        public String getIv() {
            return iv;
        }
    }
}