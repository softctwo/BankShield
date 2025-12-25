package com.bankshield.test;

import com.bankshield.common.crypto.SM3Util;
import com.bankshield.common.utils.JwtUtil;
import com.bankshield.common.crypto.SM2Util;
import com.bankshield.common.utils.EncryptUtil;
import java.security.KeyPair;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * BankShield核心功能测试类
 */
public class CoreFunctionalityTest {
    
    public static void main(String[] args) {
        System.out.println("=== BankShield 核心功能测试 ===\n");
        
        try {
            // 测试1: SM3哈希功能
            testSM3Hash();
            
            // 测试2: JWT工具功能
            testJWTUtils();
            
            // 测试3: SM2非对称加密
            testSM2Crypto();
            
            // 测试4: SM4对称加密
            testSM4Crypto();
            
            // 测试5: 密码编码功能
            testPasswordEncoder();
            
            System.out.println("\n✅ 所有核心功能测试通过！");
            
        } catch (Exception e) {
            System.err.println("\n❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试SM3哈希功能
     */
    private static void testSM3Hash() {
        System.out.println("🔐 测试SM3哈希功能...");
        
        // 测试字符串哈希
        String input = "Hello BankShield!";
        String hash1 = SM3Util.hash(input);
        System.out.println("  字符串哈希: " + hash1);
        assert hash1 != null && hash1.length() == 64 : "SM3字符串哈希失败";
        
        // 测试字节数组哈希
        byte[] inputBytes = input.getBytes();
        byte[] hash2 = SM3Util.hash(inputBytes);
        System.out.println("  字节数组哈希长度: " + hash2.length + " 字节");
        assert hash2 != null && hash2.length == 32 : "SM3字节数组哈希失败";
        
        // 测试加盐哈希
        byte[] salt = SM3Util.generateSalt(16);
        String saltedHash = SM3Util.hashWithSalt(input, salt);
        System.out.println("  加盐哈希: " + saltedHash.substring(0, 20) + "...");
        assert saltedHash.startsWith("$SM3$") : "SM3加盐哈希格式错误";
        
        // 测试验证
        boolean verified = SM3Util.verifyWithSalt(input, saltedHash);
        System.out.println("  加盐哈希验证: " + (verified ? "✅ 通过" : "❌ 失败"));
        assert verified : "SM3加盐哈希验证失败";
        
        System.out.println("  ✅ SM3哈希功能测试通过\n");
    }
    
    /**
     * 测试JWT工具功能
     */
    private static void testJWTUtils() {
        System.out.println("🔑 测试JWT工具功能...");
        
        JwtUtil jwtUtil = new JwtUtil();
        
        // 设置测试参数（使用反射设置私有字段）
        try {
            Field secretField = JwtUtil.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(jwtUtil, "test-secret-key-for-bankshield-testing-only");
            
            Field expirationField = JwtUtil.class.getDeclaredField("expiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtUtil, 3600L);
            
            Field refreshExpirationField = JwtUtil.class.getDeclaredField("refreshExpiration");
            refreshExpirationField.setAccessible(true);
            refreshExpirationField.set(jwtUtil, 604800L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set JWT fields", e);
        }
        
        // 生成测试数据
        Long userId = 12345L;
        String username = "testuser";
        List<String> authorities = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        
        // 生成Token
        String token = jwtUtil.generateToken(userId, username, authorities);
        System.out.println("  Token生成: " + token.substring(0, 20) + "...");
        assert token != null && token.length() > 50 : "Token生成失败";
        
        // 解析Token
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);
        List<String> extractedAuthorities = jwtUtil.getAuthoritiesFromToken(token);
        
        System.out.println("  解析用户名: " + extractedUsername);
        System.out.println("  解析用户ID: " + extractedUserId);
        System.out.println("  解析权限: " + extractedAuthorities);
        
        assert extractedUsername.equals(username) : "用户名解析失败";
        assert extractedUserId.equals(userId) : "用户ID解析失败";
        assert extractedAuthorities.equals(authorities) : "权限解析失败";
        
        // 测试Token验证
        boolean isValid = jwtUtil.validateToken(token);
        System.out.println("  Token有效性验证: " + (isValid ? "✅ 通过" : "❌ 失败"));
        assert isValid : "Token验证失败";
        
        System.out.println("  ✅ JWT工具功能测试通过\n");
    }
    
    /**
     * 测试SM2非对称加密功能
     */
    private static void testSM2Crypto() {
        System.out.println("🔒 测试SM2非对称加密功能...");
        
        // 生成密钥对
        KeyPair keyPair = SM2Util.generateKeyPair();
        String publicKey = java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        
        System.out.println("  密钥对生成: 公钥长度=" + publicKey.length() + ", 私钥长度=" + privateKey.length());
        
        // 测试加密解密
        String originalText = "BankShield SM2加密测试数据";
        String encryptedText = SM2Util.encrypt(publicKey, originalText);
        String decryptedText = SM2Util.decrypt(privateKey, encryptedText);
        
        System.out.println("  原始文本: " + originalText);
        System.out.println("  加密文本: " + encryptedText.substring(0, 20) + "...");
        System.out.println("  解密文本: " + decryptedText);
        
        assert originalText.equals(decryptedText) : "SM2加密解密失败";
        
        // 测试数字签名
        String signature = SM2Util.sign(privateKey, originalText);
        boolean isValid = SM2Util.verify(publicKey, originalText, signature);
        
        System.out.println("  数字签名: " + signature.substring(0, 20) + "...");
        System.out.println("  签名验证: " + (isValid ? "✅ 通过" : "❌ 失败"));
        assert isValid : "SM2数字签名验证失败";
        
        System.out.println("  ✅ SM2非对称加密功能测试通过\n");
    }
    
    /**
     * 测试SM4对称加密功能
     */
    private static void testSM4Crypto() {
        System.out.println("🔐 测试SM4对称加密功能...");
        
        String key = "1234567890123456"; // 16字节密钥
        String originalText = "BankShield SM4加密测试数据";
        
        // 加密
        String encryptedText = EncryptUtil.sm4Encrypt(key, originalText);
        System.out.println("  原始文本: " + originalText);
        System.out.println("  加密文本: " + encryptedText.substring(0, 20) + "...");
        
        // 解密
        String decryptedText = EncryptUtil.sm4Decrypt(key, encryptedText);
        System.out.println("  解密文本: " + decryptedText);
        
        assert originalText.equals(decryptedText) : "SM4加密解密失败";
        
        System.out.println("  ✅ SM4对称加密功能测试通过\n");
    }
    
    /**
     * 测试密码编码功能
     */
    private static void testPasswordEncoder() {
        System.out.println("🔑 测试密码编码功能...");
        
        com.bankshield.common.crypto.SM3PasswordEncoder encoder = 
            new com.bankshield.common.crypto.SM3PasswordEncoder();
        
        String password = "TestPassword123!";
        
        // 编码密码
        String encodedPassword = encoder.encode(password);
        System.out.println("  原始密码: " + password);
        System.out.println("  编码密码: " + encodedPassword);
        assert encodedPassword.startsWith("$SM3$") : "密码编码格式错误";
        
        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        boolean wrongPasswordMatches = encoder.matches("WrongPassword", encodedPassword);
        
        System.out.println("  正确密码验证: " + (matches ? "✅ 通过" : "❌ 失败"));
        System.out.println("  错误密码验证: " + (!wrongPasswordMatches ? "✅ 通过" : "❌ 失败"));
        
        assert matches : "正确密码验证失败";
        assert !wrongPasswordMatches : "错误密码验证失败";
        
        System.out.println("  ✅ 密码编码功能测试通过\n");
    }
}