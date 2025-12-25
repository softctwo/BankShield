package com.bankshield.encrypt.service;

import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;
import com.bankshield.encrypt.service.impl.KeyGenerationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密钥生成服务测试类
 */
@SpringBootTest
public class KeyGenerationServiceTest {
    
    @Autowired
    private KeyGenerationService keyGenerationService;
    
    @Test
    public void testGenerateSM4Key() {
        String keyMaterial = keyGenerationService.generateKey(KeyType.SM4, KeyUsage.ENCRYPT, 128);
        assertNotNull(keyMaterial);
        assertFalse(keyMaterial.isEmpty());
        System.out.println("Generated SM4 key: " + keyMaterial);
    }
    
    @Test
    public void testGenerateAESKey() {
        String keyMaterial = keyGenerationService.generateKey(KeyType.AES, KeyUsage.ENCRYPT, 256);
        assertNotNull(keyMaterial);
        assertFalse(keyMaterial.isEmpty());
        System.out.println("Generated AES key: " + keyMaterial);
    }
    
    @Test
    public void testGenerateSM2KeyPair() {
        KeyGenerationService.KeyPair keyPair = keyGenerationService.generateKeyPair(KeyType.SM2, KeyUsage.SIGN, 256);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublicKey());
        assertNotNull(keyPair.getPrivateKey());
        assertFalse(keyPair.getPublicKey().isEmpty());
        assertFalse(keyPair.getPrivateKey().isEmpty());
        System.out.println("Generated SM2 public key: " + keyPair.getPublicKey());
        System.out.println("Generated SM2 private key: " + keyPair.getPrivateKey());
    }
    
    @Test
    public void testGenerateRSAKeyPair() {
        KeyGenerationService.KeyPair keyPair = keyGenerationService.generateKeyPair(KeyType.RSA, KeyUsage.ENCRYPT, 2048);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublicKey());
        assertNotNull(keyPair.getPrivateKey());
        assertFalse(keyPair.getPublicKey().isEmpty());
        assertFalse(keyPair.getPrivateKey().isEmpty());
        System.out.println("Generated RSA public key: " + keyPair.getPublicKey());
        System.out.println("Generated RSA private key: " + keyPair.getPrivateKey());
    }
    
    @Test
    public void testCalculateKeyFingerprint() {
        String keyMaterial = "test-key-material-12345";
        String fingerprint = keyGenerationService.calculateKeyFingerprint(keyMaterial);
        assertNotNull(fingerprint);
        assertFalse(fingerprint.isEmpty());
        System.out.println("Key fingerprint: " + fingerprint);
        
        // 验证相同输入产生相同的指纹
        String fingerprint2 = keyGenerationService.calculateKeyFingerprint(keyMaterial);
        assertEquals(fingerprint, fingerprint2);
    }
    
    @Test
    public void testInvalidKeyType() {
        assertThrows(IllegalArgumentException.class, () -> {
            keyGenerationService.generateKey(KeyType.SM3, KeyUsage.ENCRYPT, null);
        });
    }
    
    @Test
    public void testInvalidKeyUsage() {
        assertThrows(IllegalArgumentException.class, () -> {
            keyGenerationService.generateKey(KeyType.SM2, KeyUsage.ENCRYPT, null);
        });
    }
}