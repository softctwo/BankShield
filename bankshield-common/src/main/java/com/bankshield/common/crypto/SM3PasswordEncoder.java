package com.bankshield.common.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * SM3密码编码器 - 国密密码存储
 * 替代BCryptPasswordEncoder，使用SM3加盐哈希
 */
@Slf4j
public class SM3PasswordEncoder implements PasswordEncoder {
    
    private static final String SALT_PREFIX = "$SM3$";
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 1; // SM3不需要多次迭代
    
    /**
     * 编码密码
     */
    public String encode(CharSequence rawPassword) {
        try {
            // 空密码验证
            if (rawPassword == null) {
                log.error("SM3密码编码失败: 密码为空");
                throw new IllegalArgumentException("密码不能为空");
            }

            // 空字符串验证
            String passwordStr = rawPassword.toString();
            if (passwordStr.isEmpty()) {
                log.error("SM3密码编码失败: 密码为空字符串");
                throw new IllegalArgumentException("密码不能为空字符串");
            }

            // 生成随机盐
            byte[] salt = generateSalt();
            // SM3(盐+密码)
            String hash = SM3Util.hashWithSalt(passwordStr, salt);
            // 存储格式：$SM3$salt$hash
            return SALT_PREFIX + Base64.getEncoder().encodeToString(salt) + "$" + hash;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("SM3密码编码失败: {}", e.getMessage());
            throw new RuntimeException("SM3密码编码失败", e);
        }
    }
    
    /**
     * 验证密码是否匹配
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            // 空参数验证
            if (rawPassword == null) {
                log.error("SM3密码验证失败: 原始密码为空");
                return false;
            }

            if (encodedPassword == null || !encodedPassword.startsWith(SALT_PREFIX)) {
                return false;
            }

            // 解析存储格式
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 3) {
                return false;
            }

            String saltStr = parts[1];
            String expectedHash = parts[2];

            // 解码盐
            byte[] salt = Base64.getDecoder().decode(saltStr);

            // 计算实际哈希
            String actualHash = SM3Util.hashWithSalt(rawPassword.toString(), salt);

            // 比较哈希值
            return expectedHash.equals(actualHash);
        } catch (IllegalArgumentException e) {
            log.error("SM3密码验证失败: 参数错误 - {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("SM3密码验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成随机盐
     */
    private byte[] generateSalt() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);
            return salt;
        } catch (Exception e) {
            log.error("生成随机盐失败: {}", e.getMessage());
            throw new RuntimeException("生成随机盐失败", e);
        }
    }
    
    /**
     * 获取算法名称
     */
    public String getAlgorithmName() {
        return "SM3";
    }
    
    /**
     * 检查是否需要重新编码（升级到新版本）
     */
    public boolean upgradeEncoding(String encodedPassword) {
        // 如果密码格式不是最新的，可能需要重新编码
        return encodedPassword == null || !encodedPassword.startsWith(SALT_PREFIX);
    }
    
    /**
     * 批量编码密码（用于密码迁移）
     */
    public String[] encodeBatch(CharSequence... rawPasswords) {
        String[] encodedPasswords = new String[rawPasswords.length];
        for (int i = 0; i < rawPasswords.length; i++) {
            encodedPasswords[i] = encode(rawPasswords[i]);
        }
        return encodedPasswords;
    }
    
    /**
     * 批量验证密码
     */
    public boolean[] matchesBatch(String[] encodedPasswords, CharSequence... rawPasswords) {
        if (encodedPasswords.length != rawPasswords.length) {
            throw new IllegalArgumentException("密码数组长度不匹配");
        }
        
        boolean[] results = new boolean[rawPasswords.length];
        for (int i = 0; i < rawPasswords.length; i++) {
            results[i] = matches(rawPasswords[i], encodedPasswords[i]);
        }
        return results;
    }
    
    /**
     * 获取编码强度（用于密码策略）
     */
    public int getEncodingStrength() {
        return 256; // SM3输出256位哈希
    }
    
    /**
     * 检查密码是否符合编码格式
     */
    public boolean isValidEncodedPassword(String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.startsWith(SALT_PREFIX)) {
            return false;
        }
        
        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 3) {
                return false;
            }
            
            // 验证Base64编码的盐
            Base64.getDecoder().decode(parts[1]);
            
            // 验证哈希值长度（SM3输出64个十六进制字符）
            String hash = parts[2];
            return hash.length() == 64 && hash.matches("[0-9a-fA-F]{64}");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取盐值（用于调试和测试）
     */
    public String extractSalt(String encodedPassword) {
        if (!isValidEncodedPassword(encodedPassword)) {
            return null;
        }
        
        String[] parts = encodedPassword.split("\\$");
        return parts[1];
    }
    
    /**
     * 获取哈希值（用于调试和测试）
     */
    public String extractHash(String encodedPassword) {
        if (!isValidEncodedPassword(encodedPassword)) {
            return null;
        }
        
        String[] parts = encodedPassword.split("\\$");
        return parts[2];
    }
}