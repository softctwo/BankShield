package com.bankshield.common.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SM4工具类测试
 */
@SpringBootTest
public class SM4UtilTest {
    
    @Test
    public void testGenerateKey() {
        String key1 = SM4Util.generateKey();
        String key2 = SM4Util.generateKey();
        
        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2);
        
        // Base64编码的密钥长度应该是24（16字节密钥 -> 24字符Base64）
        assertEquals(24, key1.length());
        assertEquals(24, key2.length());
    }
    
    @Test
    public void testGenerateIV() {
        String iv1 = SM4Util.generateIV();
        String iv2 = SM4Util.generateIV();
        
        assertNotNull(iv1);
        assertNotNull(iv2);
        assertNotEquals(iv1, iv2);
        
        // Base64编码的IV长度应该是24（16字节IV -> 24字符Base64）
        assertEquals(24, iv1.length());
        assertEquals(24, iv2.length());
    }
    
    @Test
    public void testECBEncryptionDecryption() {
        String key = SM4Util.generateKey();
        String plainText = "Hello, SM4 ECB Mode!";
        
        // ECB模式加密
        String encrypted = SM4Util.encryptECB(key, plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        // ECB模式解密
        String decrypted = SM4Util.decryptECB(key, encrypted);
        assertEquals(plainText, decrypted);
    }
    
    @Test
    public void testCBCEncryptionDecryption() {
        String key = SM4Util.generateKey();
        String iv = SM4Util.generateIV();
        String plainText = "Hello, SM4 CBC Mode!";
        
        // CBC模式加密
        String encrypted = SM4Util.encryptCBC(key, iv, plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        // CBC模式解密
        String decrypted = SM4Util.decryptCBC(key, iv, encrypted);
        assertEquals(plainText, decrypted);
    }
    
    @Test
    public void testCTREncryptionDecryption() {
        String key = SM4Util.generateKey();
        String iv = SM4Util.generateIV();
        String plainText = "Hello, SM4 CTR Mode!";
        
        // CTR模式加密
        String encrypted = SM4Util.encryptCTR(key, iv, plainText);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        // CTR模式解密
        String decrypted = SM4Util.decryptCTR(key, iv, encrypted);
        assertEquals(plainText, decrypted);
    }
    
    @Test
    public void testGenericEncryption() {
        String key = SM4Util.generateKey();
        String iv = SM4Util.generateIV();
        String plainText = "Generic encryption test";
        
        // 测试ECB模式
        String encryptedECB = SM4Util.encrypt(key, plainText, "ECB", null);
        String decryptedECB = SM4Util.decrypt(key, encryptedECB, "ECB", null);
        assertEquals(plainText, decryptedECB);
        
        // 测试CBC模式
        String encryptedCBC = SM4Util.encrypt(key, plainText, "CBC", iv);
        String decryptedCBC = SM4Util.decrypt(key, encryptedCBC, "CBC", iv);
        assertEquals(plainText, decryptedCBC);
        
        // 测试CTR模式
        String encryptedCTR = SM4Util.encrypt(key, plainText, "CTR", iv);
        String decryptedCTR = SM4Util.decrypt(key, encryptedCTR, "CTR", iv);
        assertEquals(plainText, decryptedCTR);
    }
    
    @Test
    public void testBytesEncryptionDecryption() {
        String key = SM4Util.generateKey();
        byte[] plainData = "SM4 bytes encryption test".getBytes();
        
        // 字节数组加密
        byte[] encryptedData = SM4Util.encryptBytes(key, plainData);
        assertNotNull(encryptedData);
        assertNotEquals(plainData.length, encryptedData.length); // 加密后长度会变化（PKCS5Padding）
        
        // 字节数组解密
        byte[] decryptedData = SM4Util.decryptBytes(key, encryptedData);
        assertArrayEquals(plainData, decryptedData);
    }
    
    @Test
    public void testGenerateKeyIV() {
        SM4Util.KeyIV keyIV = SM4Util.generateKeyIV();
        
        assertNotNull(keyIV);
        assertNotNull(keyIV.getKey());
        assertNotNull(keyIV.getIv());
        assertEquals(24, keyIV.getKey().length());
        assertEquals(24, keyIV.getIv().length());
    }
    
    @Test
    public void testChineseCharacters() {
        String key = SM4Util.generateKey();
        String iv = SM4Util.generateIV();
        String chineseText = "欢迎使用国密算法SM4进行加密！";
        
        // CBC模式测试中文
        String encrypted = SM4Util.encryptCBC(key, iv, chineseText);
        String decrypted = SM4Util.decryptCBC(key, iv, encrypted);
        assertEquals(chineseText, decrypted);
    }
    
    @Test
    public void testSpecialCharacters() {
        String key = SM4Util.generateKey();
        String specialText = "Special chars: !@#$%^&*()_+-=[]{}|;':\"\\,./<>?";
        
        String encrypted = SM4Util.encryptECB(key, specialText);
        String decrypted = SM4Util.decryptECB(key, encrypted);
        assertEquals(specialText, decrypted);
    }
    
    @Test
    public void testLargeData() {
        String key = SM4Util.generateKey();
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("Large data test ").append(i).append(" ");
        }
        
        String plainText = largeData.toString();
        String encrypted = SM4Util.encryptECB(key, plainText);
        String decrypted = SM4Util.decryptECB(key, encrypted);
        assertEquals(plainText, decrypted);
    }
    
    @Test
    public void testInvalidMode() {
        String key = SM4Util.generateKey();
        String plainText = "Test data";
        
        // 测试不支持的加密模式
        assertThrows(IllegalArgumentException.class, () -> {
            SM4Util.encrypt(key, plainText, "INVALID", null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            SM4Util.decrypt(key, "encrypted", "INVALID", null);
        });
    }
    
    @Test
    public void testCBCWithoutIV() {
        String key = SM4Util.generateKey();
        String plainText = "Test data";
        
        // CBC模式没有IV应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            SM4Util.encrypt(key, plainText, "CBC", null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            SM4Util.decrypt(key, "encrypted", "CBC", null);
        });
    }
    
    @Test
    public void testCTRWithoutIV() {
        String key = SM4Util.generateKey();
        String plainText = "Test data";
        
        // CTR模式没有IV应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            SM4Util.encrypt(key, plainText, "CTR", null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            SM4Util.decrypt(key, "encrypted", "CTR", null);
        });
    }
    
    @Test
    public void testWrongKey() {
        String key1 = SM4Util.generateKey();
        String key2 = SM4Util.generateKey();
        String plainText = "Test data";
        
        String encrypted = SM4Util.encryptECB(key1, plainText);
        
        // 使用错误的密钥解密应该失败
        assertThrows(RuntimeException.class, () -> {
            SM4Util.decryptECB(key2, encrypted);
        });
    }
    
    @Test
    public void testWrongIV() {
        String key = SM4Util.generateKey();
        String iv1 = SM4Util.generateIV();
        String iv2 = SM4Util.generateIV();
        String plainText = "Test data";
        
        String encrypted = SM4Util.encryptCBC(key, iv1, plainText);
        
        // 使用错误的IV解密应该失败
        String decrypted = SM4Util.decryptCBC(key, iv2, encrypted);
        assertNotEquals(plainText, decrypted);
    }
    
    @Test
    public void testEmptyData() {
        String key = SM4Util.generateKey();
        String plainText = "";
        
        String encrypted = SM4Util.encryptECB(key, plainText);
        String decrypted = SM4Util.decryptECB(key, encrypted);
        assertEquals(plainText, decrypted);
    }
}