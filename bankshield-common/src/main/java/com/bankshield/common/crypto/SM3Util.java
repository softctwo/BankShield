package com.bankshield.common.crypto;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * SM3国密哈希算法工具类
 * SM3是国家密码管理局发布的密码杂凑算法
 * 注意：这是简化实现，生产环境应使用完整的BouncyCastle SM3实现
 */
public class SM3Util {
    
    /**
     * SM3哈希（简化实现，使用SHA-256模拟）
     */
    public static String hash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(data.getBytes("UTF-8"));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM3哈希失败", e);
        }
    }
    
    /**
     * SM3哈希（字节数组）
     */
    public static byte[] hashBytes(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            throw new RuntimeException("SM3哈希失败", e);
        }
    }
    
    /**
     * SM3哈希并返回Base64编码
     */
    public static String hashBase64(String data) {
        try {
            byte[] hashBytes = hashBytes(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM3哈希失败", e);
        }
    }
    
    /**
     * 验证SM3哈希
     */
    public static boolean verify(String data, String hash) {
        try {
            String calculatedHash = hash(data);
            return calculatedHash.equals(hash);
        } catch (Exception e) {
            return false;
        }
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
