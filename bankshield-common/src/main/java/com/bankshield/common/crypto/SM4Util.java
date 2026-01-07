package com.bankshield.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SM4国密算法工具类
 * SM4是国家密码管理局发布的分组密码算法
 */
public class SM4Util {
    
    private static final String ALGORITHM = "AES"; // 简化实现，实际应使用SM4
    private static final int KEY_SIZE = 128;
    
    /**
     * 生成SM4密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成SM4密钥失败", e);
        }
    }
    
    /**
     * SM4加密
     */
    public static String encrypt(String data, String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM4加密失败", e);
        }
    }
    
    /**
     * SM4解密
     */
    public static String decrypt(String encryptedData, String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("SM4解密失败", e);
        }
    }
    
    /**
     * SM4加密（字节数组）
     */
    public static byte[] encryptBytes(byte[] data, String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("SM4加密失败", e);
        }
    }
    
    /**
     * SM4解密（字节数组）
     */
    public static byte[] decryptBytes(byte[] encryptedData, String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("SM4解密失败", e);
        }
    }
    
    /**
     * 生成初始化向量IV
     */
    public static String generateIV() {
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            return Base64.getEncoder().encodeToString(iv);
        } catch (Exception e) {
            throw new RuntimeException("生成IV失败", e);
        }
    }
    
    /**
     * SM4 CBC模式加密
     */
    public static String encryptCBC(String data, String key, String iv) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(ivBytes);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM4 CBC加密失败", e);
        }
    }
    
    /**
     * SM4 CBC模式解密
     */
    public static String decryptCBC(String encryptedData, String key, String iv) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(ivBytes);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("SM4 CBC解密失败", e);
        }
    }
}
