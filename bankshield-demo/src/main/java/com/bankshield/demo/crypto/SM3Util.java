package com.bankshield.demo.crypto;

import cn.hutool.crypto.SmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * SM3工具类 - 国密哈希算法
 * 用于数据完整性校验和密码存储
 */
public class SM3Util {
    private static final Logger log = LoggerFactory.getLogger(SM3Util.class);
    
    private static final String SALT_PREFIX = "$SM3$";
    
    /**
     * SM3哈希计算
     */
    public static String hash(String data) {
        try {
            return SmUtil.sm3(data);
        } catch (Exception e) {
            log.error("SM3哈希计算失败: {}", e.getMessage());
            throw new RuntimeException("SM3哈希计算失败", e);
        }
    }
    
    /**
     * SM3哈希计算（字节数组）
     */
    public static byte[] hash(byte[] data) {
        try {
            // 将字节数组转换为字符串进行哈希计算
            String hexData = bytesToHex(data);
            String hashResult = SmUtil.sm3(hexData);
            return hexToBytes(hashResult);
        } catch (Exception e) {
            log.error("SM3哈希计算失败: {}", e.getMessage());
            throw new RuntimeException("SM3哈希计算失败", e);
        }
    }
    
    /**
     * 生成随机盐
     */
    public static byte[] generateSalt(int length) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[length];
            secureRandom.nextBytes(salt);
            return salt;
        } catch (Exception e) {
            log.error("生成盐失败: {}", e.getMessage());
            throw new RuntimeException("生成盐失败", e);
        }
    }
    
    /**
     * 加盐哈希
     */
    public static String hashWithSalt(String data, byte[] salt) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] combined = new byte[dataBytes.length + salt.length];
            System.arraycopy(dataBytes, 0, combined, 0, dataBytes.length);
            System.arraycopy(salt, 0, combined, dataBytes.length, salt.length);
            
            String hexSalt = bytesToHex(salt);
            String hexData = bytesToHex(combined);
            return SALT_PREFIX + hexSalt + "$" + SmUtil.sm3(hexData);
        } catch (Exception e) {
            log.error("加盐哈希计算失败: {}", e.getMessage());
            throw new RuntimeException("加盐哈希计算失败", e);
        }
    }
    
    /**
     * 验证加盐哈希
     */
    public static boolean verifyWithSalt(String data, String saltedHash) {
        try {
            if (!saltedHash.startsWith(SALT_PREFIX)) {
                return false;
            }
            
            String content = saltedHash.substring(SALT_PREFIX.length());
            String[] parts = content.split("\\$", 2);
            if (parts.length != 2) {
                return false;
            }
            
            String hexSalt = parts[0];
            String storedHash = parts[1];
            
            byte[] salt = hexToBytes(hexSalt);
            String computedHash = SmUtil.sm3(bytesToHex(data.getBytes(StandardCharsets.UTF_8)) + hexSalt);
            
            return storedHash.equals(computedHash);
        } catch (Exception e) {
            log.error("验证加盐哈希失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * 十六进制字符串转字节数组
     */
    private static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}