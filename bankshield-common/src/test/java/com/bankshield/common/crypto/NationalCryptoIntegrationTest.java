package com.bankshield.common.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 国密算法集成测试
 * 验证所有国密算法在系统中的集成情况
 */
@SpringBootTest
@ActiveProfiles("national-crypto")
public class NationalCryptoIntegrationTest {
    
    @Autowired
    private NationalCryptoManager cryptoManager;
    
    @Autowired
    private NationalCryptoConfig cryptoConfig;
    
    /**
     * 测试国密算法管理器初始化
     */
    @Test
    public void testCryptoManagerInitialization() {
        assertNotNull(cryptoManager, "国密算法管理器应该被正确初始化");
        assertTrue(cryptoManager.isNationalCryptoEnabled(), "国密算法应该被启用");
        
        assertNotNull(cryptoManager.getPasswordEncoder(), "密码编码器应该被初始化");
        assertNotNull(cryptoManager.getSm2KeyPair(), "SM2密钥对应该被生成");
        assertNotNull(cryptoManager.getSm2PublicKey(), "SM2公钥应该存在");
        assertNotNull(cryptoManager.getSm2PrivateKey(), "SM2私钥应该存在");
    }
    
    /**
     * 测试国密算法配置
     */
    @Test
    public void testCryptoConfiguration() {
        assertTrue(cryptoConfig.isEnabled(), "国密算法应该在配置中启用");
        assertTrue(cryptoConfig.isForceNationalCrypto(), "应该强制使用国密算法");
        assertEquals("SM3", cryptoConfig.getPasswordEncoderType(), "密码编码器类型应该是SM3");
        assertEquals("CBC", cryptoConfig.getSm4DefaultMode(), "SM4默认模式应该是CBC");
    }
    
    /**
     * 测试完整的数据加密流程
     */
    @Test
    public void testCompleteEncryptionFlow() {
        String originalData = "测试国密算法完整加密流程";
        
        // 1. 使用SM4加密数据
        String sm4Key = SM4Util.generateKey();
        String sm4Iv = SM4Util.generateIV();
        String encryptedData = cryptoManager.encryptData(originalData, sm4Key, "CBC", sm4Iv);
        
        assertNotNull(encryptedData, "加密数据不应该为null");
        assertNotEquals(originalData, encryptedData, "加密数据应该与原始数据不同");
        
        // 2. 使用SM4解密数据
        String decryptedData = cryptoManager.decryptData(encryptedData, sm4Key, "CBC", sm4Iv);
        assertEquals(originalData, decryptedData, "解密数据应该与原始数据相同");
        
        // 3. 使用SM2签名加密后的数据
        String signature = cryptoManager.signData(encryptedData);
        assertNotNull(signature, "签名不应该为null");
        assertTrue(signature.length() > 0, "签名应该有内容");
        
        // 4. 验证签名
        boolean signatureValid = cryptoManager.verifySignature(encryptedData, signature);
        assertTrue(signatureValid, "签名验证应该通过");
        
        // 5. 使用SM3计算哈希
        String hash = cryptoManager.hashData(encryptedData);
        assertNotNull(hash, "哈希不应该为null");
        assertEquals(64, hash.length(), "SM3哈希应该是64个十六进制字符");
    }
    
    /**
     * 测试密码存储流程
     */
    @Test
    public void testPasswordStorageFlow() {
        String originalPassword = "TestPassword123!";
        
        // 1. 编码密码
        String encodedPassword = cryptoManager.encodePassword(originalPassword);
        assertNotNull(encodedPassword, "编码密码不应该为null");
        assertTrue(encodedPassword.startsWith("$SM3$"), "编码密码应该以$SM3$开头");
        
        // 2. 验证密码
        boolean passwordValid = cryptoManager.matchesPassword(originalPassword, encodedPassword);
        assertTrue(passwordValid, "密码验证应该通过");
        
        // 3. 验证错误密码
        boolean wrongPasswordValid = cryptoManager.matchesPassword("WrongPassword", encodedPassword);
        assertFalse(wrongPasswordValid, "错误密码验证应该失败");
    }
    
    /**
     * 测试SM2密钥对生成和使用
     */
    @Test
    public void testSM2KeyPairOperations() {
        // 生成新的密钥对
        KeyPair keyPair = cryptoManager.generateNewSm2KeyPair();
        assertNotNull(keyPair, "新生成的密钥对不应该为null");
        assertNotNull(keyPair.getPublic(), "公钥不应该为null");
        assertNotNull(keyPair.getPrivate(), "私钥不应该为null");
        
        // 测试密钥转换
        String publicKeyStr = SM2Util.publicKeyToString(keyPair.getPublic());
        String privateKeyStr = SM2Util.privateKeyToString(keyPair.getPrivate());
        
        assertNotNull(publicKeyStr, "公钥字符串不应该为null");
        assertNotNull(privateKeyStr, "私钥字符串不应该为null");
        
        // 测试密钥还原
        PublicKey restoredPublicKey = SM2Util.stringToPublicKey(publicKeyStr);
        PrivateKey restoredPrivateKey = SM2Util.stringToPrivateKey(privateKeyStr);
        
        assertEquals(keyPair.getPublic(), restoredPublicKey, "还原的公钥应该与原始公钥相同");
        assertEquals(keyPair.getPrivate(), restoredPrivateKey, "还原的私钥应该与原始私钥相同");
    }
    
    /**
     * 测试SM3哈希操作
     */
    @Test
    public void testSM3HashOperations() {
        String testData = "测试SM3哈希操作";
        
        // 计算哈希
        String hash1 = cryptoManager.hashData(testData);
        String hash2 = cryptoManager.hashData(testData);
        
        assertEquals(hash1, hash2, "相同数据的哈希应该相同");
        assertEquals(64, hash1.length(), "SM3哈希长度应该是64个字符");
        
        // 不同数据的哈希应该不同
        String differentHash = cryptoManager.hashData("不同的测试数据");
        assertNotEquals(hash1, differentHash, "不同数据的哈希应该不同");
    }
    
    /**
     * 测试SM4加密操作
     */
    @Test
    public void testSM4EncryptionOperations() {
        String testData = "测试SM4加密操作";
        
        // 测试ECB模式
        String ecbKey = SM4Util.generateKey();
        String ecbEncrypted = SM4Util.encryptECB(ecbKey, testData);
        String ecbDecrypted = SM4Util.decryptECB(ecbKey, ecbEncrypted);
        assertEquals(testData, ecbDecrypted, "ECB模式解密数据应该与原始数据相同");
        
        // 测试CBC模式
        String cbcKey = SM4Util.generateKey();
        String cbcIv = SM4Util.generateIV();
        String cbcEncrypted = SM4Util.encryptCBC(cbcKey, cbcIv, testData);
        String cbcDecrypted = SM4Util.decryptCBC(cbcKey, cbcIv, cbcEncrypted);
        assertEquals(testData, cbcDecrypted, "CBC模式解密数据应该与原始数据相同");
        
        // 测试CTR模式
        String ctrKey = SM4Util.generateKey();
        String ctrIv = SM4Util.generateIV();
        String ctrEncrypted = SM4Util.encryptCTR(ctrKey, ctrIv, testData);
        String ctrDecrypted = SM4Util.decryptCTR(ctrKey, ctrIv, ctrEncrypted);
        assertEquals(testData, ctrDecrypted, "CTR模式解密数据应该与原始数据相同");
    }
    
    /**
     * 测试国密算法配置加载
     */
    @Test
    public void testConfigurationLoading() {
        assertTrue(cryptoConfig.isEnabled(), "国密算法应该被启用");
        assertEquals("SM3", cryptoConfig.getPasswordEncoderType(), "密码编码器类型应该是SM3");
        assertEquals("CBC", cryptoConfig.getSm4DefaultMode(), "SM4默认模式应该是CBC");
        assertTrue(cryptoConfig.isForceNationalCrypto(), "应该强制使用国密算法");
        
        // 验证SSL配置
        assertTrue(cryptoConfig.getSsl().isEnabled(), "国密SSL应该被启用");
        assertEquals("TLCP", cryptoConfig.getSsl().getProtocol(), "SSL协议应该是TLCP");
        assertNotNull(cryptoConfig.getSsl().getCiphers(), "加密套件不应该为null");
        assertTrue(cryptoConfig.getSsl().getCiphers().length > 0, "加密套件不应该为空");
    }
    
    /**
     * 测试错误处理
     */
    @Test
    public void testErrorHandling() {
        // 测试无效数据
        assertThrows(RuntimeException.class, () -> {
            cryptoManager.encryptData(null, "key", "CBC", "iv");
        }, "加密null数据应该抛出异常");
        
        // 测试无效密钥
        assertThrows(RuntimeException.class, () -> {
            cryptoManager.encryptData("test", "invalid-key", "CBC", "iv");
        }, "使用无效密钥加密应该抛出异常");
        
        // 测试无效密码验证
        assertFalse(cryptoManager.matchesPassword("test", "invalid-encoded-password"));
    }
    
    /**
     * 测试大数据处理
     */
    @Test
    public void testLargeDataHandling() {
        // 生成1MB测试数据
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            largeData.append("Large data test ").append(i).append(" ");
        }
        String largeDataStr = largeData.toString();
        
        // 测试SM3哈希大数
        String hash = cryptoManager.hashData(largeDataStr);
        assertNotNull(hash, "大数据哈希不应该为null");
        assertEquals(64, hash.length(), "大数据哈希长度应该是64个字符");
        
        // 测试SM4加密大数据
        String key = SM4Util.generateKey();
        String encrypted = SM4Util.encryptECB(key, largeDataStr);
        String decrypted = SM4Util.decryptECB(key, encrypted);
        assertEquals(largeDataStr, decrypted, "大数据加密解密应该保持一致");
    }
    
    /**
     * 测试并发安全性
     */
    @Test
    public void testConcurrentSafety() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        
        Thread[] threads = new Thread[threadCount];
        final boolean[] results = new boolean[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        // 密码编码测试
                        String password = "concurrent-test-password-" + threadIndex + "-" + j;
                        String encoded = cryptoManager.encodePassword(password);
                        boolean matches = cryptoManager.matchesPassword(password, encoded);
                        
                        if (!matches) {
                            results[threadIndex] = false;
                            return;
                        }
                        
                        // SM3哈希测试
                        String data = "concurrent-test-data-" + threadIndex + "-" + j;
                        String hash = cryptoManager.hashData(data);
                        if (hash == null || hash.length() != 64) {
                            results[threadIndex] = false;
                            return;
                        }
                    }
                    results[threadIndex] = true;
                } catch (Exception e) {
                    results[threadIndex] = false;
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证所有线程都成功
        for (boolean result : results) {
            assertTrue(result, "所有并发测试都应该成功");
        }
    }
    
    /**
     * 测试特殊字符处理
     */
    @Test
    public void testSpecialCharacterHandling() {
        String[] specialTexts = {
            "中文测试数据",
            "Special chars: !@#$%^&*()_+-=[]{}|;':\"\\,./<>?",
            "Unicode: \u0000\u0001\u0002\u0003",
            "Empty string: ",
            "Newlines:\n\r\t",
            "Mixed: 中文English123!@#"
        };
        
        for (String text : specialTexts) {
            // 测试SM3哈希
            String hash = cryptoManager.hashData(text);
            assertNotNull(hash, "特殊字符哈希不应该为null");
            
            // 测试SM4加密
            String key = SM4Util.generateKey();
            String iv = SM4Util.generateIV();
            String encrypted = SM4Util.encryptCBC(key, iv, text);
            String decrypted = SM4Util.decryptCBC(key, iv, encrypted);
            assertEquals(text, decrypted, "特殊字符加密解密应该保持一致");
        }
    }
}