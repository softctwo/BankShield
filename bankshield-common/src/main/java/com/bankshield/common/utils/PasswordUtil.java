package com.bankshield.common.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 */
public class PasswordUtil {
    
    /**
     * 盐值长度
     */
    private static final int SALT_LENGTH = 16;
    
    /**
     * 迭代次数
     */
    private static final int ITERATIONS = 10000;
    
    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 加密密码（使用随机盐值）
     */
    public static String encode(String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        return salt + ":" + hashedPassword;
    }
    
    /**
     * 加密密码（使用指定盐值）
     */
    public static String encode(String password, String salt) {
        return hashPassword(password, salt);
    }
    
    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        try {
            if (encodedPassword == null || !encodedPassword.contains(":")) {
                return false;
            }
            
            String[] parts = encodedPassword.split(":", 2);
            String salt = parts[0];
            String hashedPassword = parts[1];
            
            String newHash = hashPassword(rawPassword, salt);
            return newHash.equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 哈希密码
     */
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // 添加盐值
            md.update(Base64.getDecoder().decode(salt));
            
            // 多次迭代
            byte[] hash = password.getBytes("UTF-8");
            for (int i = 0; i < ITERATIONS; i++) {
                md.update(hash);
                hash = md.digest();
            }
            
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 生成随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
    
    /**
     * 验证密码强度
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
