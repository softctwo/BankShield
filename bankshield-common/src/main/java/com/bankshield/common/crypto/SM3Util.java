package com.bankshield.common.crypto;

import cn.hutool.crypto.SmUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * SM3工具类 - 国密哈希算法
 * 用于数据完整性校验和密码存储
 */
@Slf4j
public class SM3Util {
    
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
     * 加盐哈希（字节数组版本）
     */
    public static String hashWithSalt(byte[] data, byte[] salt) {
        try {
            byte[] combined = new byte[data.length + salt.length];
            System.arraycopy(data, 0, combined, 0, data.length);
            System.arraycopy(salt, 0, combined, data.length, salt.length);

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

    /**
     * 生成SM3密钥派生函数(KDF)密钥
     * 用于从密钥材料生成SM4加密密钥
     */
    public static byte[] sm3Kdf(byte[] keyMaterial, int keyLength) {
        try {
            byte[] result = new byte[keyLength];
            int counter = 1;
            int generated = 0;

            while (generated < keyLength) {
                byte[] counterBytes = new byte[4];
                counterBytes[0] = (byte) ((counter >> 24) & 0xFF);
                counterBytes[1] = (byte) ((counter >> 16) & 0xFF);
                counterBytes[2] = (byte) ((counter >> 8) & 0xFF);
                counterBytes[3] = (byte) (counter & 0xFF);

                byte[] input = new byte[keyMaterial.length + counterBytes.length];
                System.arraycopy(keyMaterial, 0, input, 0, keyMaterial.length);
                System.arraycopy(counterBytes, 0, input, keyMaterial.length, counterBytes.length);

                byte[] hash = hash(input);
                int toCopy = Math.min(32, keyLength - generated);
                System.arraycopy(hash, 0, result, generated, toCopy);
                generated += toCopy;
                counter++;
            }
            return result;
        } catch (Exception e) {
            log.error("SM3 KDF生成失败: {}", e.getMessage());
            throw new RuntimeException("SM3 KDF生成失败", e);
        }
    }

    /**
     * 生成SM3密钥派生函数(KDF)密钥 (字符串版本)
     */
    public static String sm3Kdf(String keyMaterial, int keyLength) {
        return bytesToHex(sm3Kdf(keyMaterial.getBytes(StandardCharsets.UTF_8), keyLength));
    }

    /**
     * 生成SM3消息认证码(MAC)
     * 使用HMAC-SM3算法
     */
    public static String generateMAC(String key, String message) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("SM3withSM2");
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "SM3withSM2");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            log.error("SM3 MAC生成失败: {}", e.getMessage());
            throw new RuntimeException("SM3 MAC生成失败", e);
        }
    }

    /**
     * 验证SM3消息认证码(MAC)
     */
    public static boolean verifyMAC(String key, String message, String mac) {
        try {
            String computedMac = generateMAC(key, message);
            return computedMac.equalsIgnoreCase(mac);
        } catch (Exception e) {
            log.error("SM3 MAC验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 计算SM3-HMAC (兼容旧接口)
     */
    public static String hmac(String key, String data) {
        return generateMAC(key, data);
    }

    /**
     * 计算SM3-HMAC (字节数组版本)
     */
    public static byte[] hmac(byte[] key, byte[] data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("SM3withSM2");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(key, "SM3withSM2");
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (Exception e) {
            log.error("SM3 HMAC计算失败: {}", e.getMessage());
            throw new RuntimeException("SM3 HMAC计算失败", e);
        }
    }

    /**
     * 生成密码哈希
     * 使用加盐哈希算法
     */
    public static String generatePasswordHash(String password) {
        byte[] salt = generateSalt(16);
        return hashWithSalt(password, salt);
    }

    /**
     * 验证密码哈希
     */
    public static boolean verifyPasswordHash(String password, String hashedPassword) {
        return verifyWithSalt(password, hashedPassword);
    }

    /**
     * 计算文件哈希
     * 用于验证文件完整性
     */
    public static String fileHash(byte[] fileData) {
        return hash(bytesToHex(fileData));
    }
}