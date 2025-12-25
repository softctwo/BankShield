package com.bankshield.encrypt.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密钥存储服务测试类
 */
@SpringBootTest
public class KeyStorageServiceTest {
    
    @Autowired
    private KeyStorageService keyStorageService;
    
    @Test
    public void testEncryptDecryptKeyMaterial() {
        String originalKeyMaterial = "test-key-material-12345-abcdef";
        
        // 加密
        String encrypted = keyStorageService.encryptKeyMaterial(originalKeyMaterial);
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        assertNotEquals(originalKeyMaterial, encrypted);
        System.out.println("Original: " + originalKeyMaterial);
        System.out.println("Encrypted: " + encrypted);
        
        // 解密
        String decrypted = keyStorageService.decryptKeyMaterial(encrypted);
        assertNotNull(decrypted);
        assertEquals(originalKeyMaterial, decrypted);
        System.out.println("Decrypted: " + decrypted);
    }
    
    @Test
    public void testEncryptEmptyKeyMaterial() {
        assertThrows(IllegalArgumentException.class, () -> {
            keyStorageService.encryptKeyMaterial("");
        });
    }
    
    @Test
    public void testDecryptEmptyKeyMaterial() {
        assertThrows(IllegalArgumentException.class, () -> {
            keyStorageService.decryptKeyMaterial("");
        });
    }
    
    @Test
    public void testSecureDeleteKeyMaterial() {
        String keyMaterial = "sensitive-key-material";
        // 安全删除不应该抛出异常
        assertDoesNotThrow(() -> {
            keyStorageService.secureDeleteKeyMaterial(keyMaterial);
        });
    }
    
    @Test
    public void testValidateStorageConfiguration() {
        boolean isValid = keyStorageService.validateStorageConfiguration();
        assertTrue(isValid);
    }
    
    @Test
    public void testGetStorageType() {
        String storageType = keyStorageService.getStorageType();
        assertNotNull(storageType);
        assertFalse(storageType.isEmpty());
        System.out.println("Storage type: " + storageType);
    }
}