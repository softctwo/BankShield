package com.bankshield.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 通用加密工具类
 */
public class EncryptUtil {
    
    private static final String AES_ALGORITHM = "AES";
    private static final String MD5_ALGORITHM = "MD5";
    private static final String SHA256_ALGORITHM = "SHA-256";
    
    /**
     * 生成AES密钥
     */
    public static String generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(256, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }
    
    /**
     * 生成AES密钥（指定长度）
     */
    public static String generateAesKey(int keyLength) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(keyLength, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }
    
    /**
     * 生成RSA密钥对
     */
    public static KeyPair generateRsaKeyPair(int keyLength) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keyLength, new SecureRandom());
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }
    
    /**
     * AES加密
     */
    public static String encryptAES(String data, String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }
    
    /**
     * AES解密
     */
    public static String decryptAES(String encryptedData, String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }
    
    /**
     * MD5哈希
     */
    public static String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] hashBytes = md.digest(data.getBytes("UTF-8"));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("MD5哈希失败", e);
        }
    }
    
    /**
     * SHA256哈希
     */
    public static String sha256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hashBytes = md.digest(data.getBytes("UTF-8"));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("SHA256哈希失败", e);
        }
    }
    
    /**
     * SM3哈希（简化实现，使用SHA256代替）
     */
    public static String sm3Hash(String data) {
        try {
            // 简化实现：使用SHA256代替SM3
            // 生产环境应使用Bouncy Castle的SM3实现
            MessageDigest md = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hashBytes = md.digest(data.getBytes("UTF-8"));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM3哈希失败", e);
        }
    }
    
    /**
     * Base64编码
     */
    public static String base64Encode(String data) {
        try {
            return Base64.getEncoder().encodeToString(data.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Base64编码失败", e);
        }
    }
    
    /**
     * Base64解码
     */
    public static String base64Decode(String encodedData) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedData);
            return new String(decodedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Base64解码失败", e);
        }
    }
    
    /**
     * BCrypt加密（使用PasswordUtil）
     */
    public static String bcryptEncrypt(String password) {
        return com.bankshield.common.utils.PasswordUtil.encode(password);
    }
    
    /**
     * BCrypt验证（使用PasswordUtil）
     */
    public static boolean bcryptCheck(String rawPassword, String encodedPassword) {
        return com.bankshield.common.utils.PasswordUtil.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
