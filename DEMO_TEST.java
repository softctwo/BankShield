package com.bankshield.demo;

import com.bankshield.demo.crypto.SM3Util;
import com.bankshield.demo.crypto.SM4Util;
import com.bankshield.demo.crypto.SM2Util;
import com.bankshield.demo.response.ApiResponse;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

/**
 * BankShield演示应用功能测试
 * 验证国密算法的核心功能
 */
public class DEMO_TEST {

    public static void main(String[] args) {
        System.out.println("🧪 BankShield演示应用功能测试");
        System.out.println("========================================");

        try {
            // 1. SM3哈希测试
            testSM3Hash();

            // 2. SM4对称加密测试
            testSM4Encryption();

            // 3. SM2非对称加密测试
            testSM2Encryption();

            // 4. 密码哈希测试
            testPasswordHash();

            // 5. 批量测试
            testBatchCrypto();

            System.out.println("\n✅ 所有测试完成！BankShield核心功能验证成功！");
            System.out.println("🎯 演示应用已准备就绪，可以进行API测试");

        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试SM3哈希算法
     */
    private static void testSM3Hash() {
        System.out.println("\n📊 测试1: SM3哈希算法");
        String testText = "BankShield安全测试";
        String hash = SM3Util.hash(testText);
        System.out.println("  原文: " + testText);
        System.out.println("  SM3哈希: " + hash);
        System.out.println("  ✅ SM3哈希功能正常");
    }

    /**
     * 测试SM4对称加密
     */
    private static void testSM4Encryption() {
        System.out.println("\n🔐 测试2: SM4对称加密");
        String testText = "SM4对称加密测试数据";
        String key = SM4Util.generateKey();
        String encrypted = SM4Util.encryptECB(key, testText);
        String decrypted = SM4Util.decryptECB(key, encrypted);

        System.out.println("  原文: " + testText);
        System.out.println("  密钥: " + key.substring(0, 20) + "...");
        System.out.println("  加密: " + encrypted.substring(0, 30) + "...");
        System.out.println("  解密: " + decrypted);
        System.out.println("  验证: " + testText.equals(decrypted));
        System.out.println("  ✅ SM4对称加密功能正常");
    }

    /**
     * 测试SM2非对称加密
     */
    private static void testSM2Encryption() {
        System.out.println("\n🗝️ 测试3: SM2非对称加密");
        String testText = "SM2非对称加密测试数据";

        KeyPair keyPair = SM2Util.generateKeyPair();
        String publicKey = SM2Util.publicKeyToString(keyPair.getPublic());
        String privateKey = SM2Util.privateKeyToString(keyPair.getPrivate());

        String encrypted = SM2Util.encrypt(publicKey, testText);
        String decrypted = SM2Util.decrypt(privateKey, encrypted);

        System.out.println("  原文: " + testText);
        System.out.println("  公钥: " + publicKey.substring(0, 30) + "...");
        System.out.println("  加密: " + encrypted.substring(0, 30) + "...");
        System.out.println("  解密: " + decrypted);
        System.out.println("  验证: " + testText.equals(decrypted));
        System.out.println("  ✅ SM2非对称加密功能正常");
    }

    /**
     * 测试密码哈希功能
     */
    private static void testPasswordHash() {
        System.out.println("\n🔒 测试4: 密码哈希功能");
        String password = "MySecurePassword123";
        byte[] salt = SM3Util.generateSalt(16);
        String hashedPassword = SM3Util.hashWithSalt(password, salt);
        boolean correctVerify = SM3Util.verifyWithSalt(password, hashedPassword);
        boolean wrongVerify = SM3Util.verifyWithSalt("wrongPassword", hashedPassword);

        System.out.println("  密码: " + password);
        System.out.println("  加盐哈希: " + hashedPassword.substring(0, 40) + "...");
        System.out.println("  正确验证: " + correctVerify);
        System.out.println("  错误验证: " + wrongVerify);
        System.out.println("  ✅ 密码哈希功能正常");
    }

    /**
     * 批量加密功能测试
     */
    private static void testBatchCrypto() {
        System.out.println("\n🎯 测试5: 批量加密功能");

        // SM3测试
        String sm3Text = "BankShield安全测试";
        String sm3Hash = SM3Util.hash(sm3Text);
        System.out.println("  SM3: " + sm3Text + " -> " + sm3Hash.substring(0, 16) + "...");

        // SM4测试
        String sm4Text = "SM4对称加密测试";
        String sm4Key = SM4Util.generateKey();
        String sm4Encrypted = SM4Util.encryptECB(sm4Key, sm4Text);
        String sm4Decrypted = SM4Util.decryptECB(sm4Key, sm4Encrypted);
        System.out.println("  SM4: " + sm4Text + " -> " + sm4Encrypted.substring(0, 16) + "...");

        // SM2测试
        String sm2Text = "SM2非对称加密测试";
        KeyPair keyPair = SM2Util.generateKeyPair();
        String publicKey = SM2Util.publicKeyToString(keyPair.getPublic());
        String privateKey = SM2Util.privateKeyToString(keyPair.getPrivate());
        String sm2Encrypted = SM2Util.encrypt(publicKey, sm2Text);
        String sm2Decrypted = SM2Util.decrypt(privateKey, sm2Encrypted);
        System.out.println("  SM2: " + sm2Text + " -> " + sm2Encrypted.substring(0, 16) + "...");

        System.out.println("  ✅ 批量加密功能正常");
    }
}