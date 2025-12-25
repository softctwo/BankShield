package com.bankshield.common.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SM2工具类测试
 */
@SpringBootTest
public class SM2UtilTest {
    
    private KeyPair keyPair;
    private String publicKeyStr;
    private String privateKeyStr;
    
    @BeforeEach
    public void setUp() {
        // 生成密钥对
        keyPair = SM2Util.generateKeyPair();
        publicKeyStr = SM2Util.publicKeyToString(keyPair.getPublic());
        privateKeyStr = SM2Util.privateKeyToString(keyPair.getPrivate());
    }
    
    @Test
    public void testGenerateKeyPair() {
        KeyPair newKeyPair = SM2Util.generateKeyPair();
        assertNotNull(newKeyPair);
        assertNotNull(newKeyPair.getPublic());
        assertNotNull(newKeyPair.getPrivate());
        
        // 验证密钥格式
        String pubKeyStr = SM2Util.publicKeyToString(newKeyPair.getPublic());
        String priKeyStr = SM2Util.privateKeyToString(newKeyPair.getPrivate());
        
        assertNotNull(pubKeyStr);
        assertNotNull(priKeyStr);
        assertTrue(pubKeyStr.length() > 0);
        assertTrue(priKeyStr.length() > 0);
    }
    
    @Test
    public void testEncryptDecrypt() {
        String plainText = "Hello, SM2 Encryption!";
        
        // 公钥加密
        String encrypted = SM2Util.encrypt(publicKeyStr, plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        // 私钥解密
        String decrypted = SM2Util.decrypt(privateKeyStr, encrypted);
        assertEquals(plainText, decrypted);
    }
    
    @Test
    public void testSignVerify() {
        String data = "Test data for SM2 signature";
        
        // 私钥签名
        String signature = SM2Util.sign(privateKeyStr, data);
        assertNotNull(signature);
        assertTrue(signature.length() > 0);
        
        // 公钥验签
        boolean verified = SM2Util.verify(publicKeyStr, data, signature);
        assertTrue(verified);
        
        // 篡改数据后验签失败
        boolean verifiedWithWrongData = SM2Util.verify(publicKeyStr, "Wrong data", signature);
        assertFalse(verifiedWithWrongData);
    }
    
    @Test
    public void testKeyConversion() {
        // 公钥转换
        PublicKey restoredPublicKey = SM2Util.stringToPublicKey(publicKeyStr);
        assertEquals(keyPair.getPublic(), restoredPublicKey);
        
        // 私钥转换
        PrivateKey restoredPrivateKey = SM2Util.stringToPrivateKey(privateKeyStr);
        assertEquals(keyPair.getPrivate(), restoredPrivateKey);
    }
    
    @Test
    public void testEncryptWithChinese() {
        String chineseText = "欢迎使用国密算法SM2！";
        
        String encrypted = SM2Util.encrypt(publicKeyStr, chineseText);
        String decrypted = SM2Util.decrypt(privateKeyStr, encrypted);
        
        assertEquals(chineseText, decrypted);
    }
    
    @Test
    public void testSignWithSpecialCharacters() {
        String specialData = "特殊字符测试！@#$%^&*()_+-=[]{}|;':\",./<>?";
        
        String signature = SM2Util.sign(privateKeyStr, specialData);
        boolean verified = SM2Util.verify(publicKeyStr, specialData, signature);
        
        assertTrue(verified);
    }
    
    @Test
    public void testLargeDataEncryption() {
        // 测试大数据量加密
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("Large data test ").append(i).append(" ");
        }
        
        String plainText = largeData.toString();
        String encrypted = SM2Util.encrypt(publicKeyStr, plainText);
        String decrypted = SM2Util.decrypt(privateKeyStr, encrypted);
        
        assertEquals(plainText, decrypted);
    }
    
    @Test
    public void testInvalidKeyHandling() {
        String invalidKey = "invalid-key";
        String data = "test data";
        
        // 测试无效密钥的处理
        assertThrows(RuntimeException.class, () -> {
            SM2Util.encrypt(invalidKey, data);
        });
        
        assertThrows(RuntimeException.class, () -> {
            SM2Util.decrypt(invalidKey, data);
        });
    }
}