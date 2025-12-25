package com.bankshield.encrypt;

import com.bankshield.common.exception.EncryptionException;
import com.bankshield.encrypt.util.EncryptUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

class EncryptUtilTest {
    
    private static final String SM4_KEY = System.getenv("TEST_SM4_KEY") != null ? 
            System.getenv("TEST_SM4_KEY") : "0123456789abcdef"; // 16字节 - 从环境变量获取或使用默认值
    private static final String SM4_KEY_32 = System.getenv("TEST_SM4_KEY_32") != null ? 
            System.getenv("TEST_SM4_KEY_32") : "0123456789abcdef0123456789abcdef"; // 32字节
    private static final String INVALID_KEY = "12345"; // 无效长度
    
    @BeforeEach
    void setUp() {
        // 初始化加密工具类
    }
    
    @Test
    @DisplayName("SM4加密解密成功")
    void testSM4EncryptDecrypt() {
        // Given
        String plainText = "Hello, BankShield!";
        String key = SM4_KEY;
        
        // When
        String encrypted = EncryptUtil.sm4Encrypt(plainText, key);
        String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        
        // Then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }
    
    @Test
    @DisplayName("SM4加密-中文字符串")
    void testSM4EncryptDecryptChinese() {
        // Given
        String plainText = "中国银行数据加密测试";
        String key = SM4_KEY;
        
        // When
        String encrypted = EncryptUtil.sm4Encrypt(plainText, key);
        String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        
        // Then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }
    
    @Test
    @DisplayName("SM4加密-特殊字符")
    void testSM4EncryptDecryptSpecialChars() {
        // Given
        String plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String key = SM4_KEY;
        
        // When
        String encrypted = EncryptUtil.sm4Encrypt(plainText, key);
        String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        
        // Then
        assertThat(encrypted).isNotNull();
        assertThat(decrypted).isEqualTo(plainText);
    }
    
    @Test
    @DisplayName("SM4加密-空字符串")
    void testSM4EncryptDecryptEmptyString() {
        // Given
        String plainText = "";
        String key = SM4_KEY;
        
        // When
        String encrypted = EncryptUtil.sm4Encrypt(plainText, key);
        String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        
        // Then
        assertThat(encrypted).isNotNull();
        assertThat(decrypted).isEqualTo(plainText);
    }
    
    @Test
    @DisplayName("SM4加密-长文本")
    void testSM4EncryptDecryptLongText() {
        // Given
        StringBuilder plainText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            plainText.append("这是一个很长的文本，用于测试SM4加密算法的性能和处理能力。");
        }
        String key = SM4_KEY;
        
        // When
        String encrypted = EncryptUtil.sm4Encrypt(plainText.toString(), key);
        String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        
        // Then
        assertThat(encrypted).isNotNull();
        assertThat(decrypted).isEqualTo(plainText.toString());
    }
    
    @Test
    @DisplayName("SM4加密-无效密钥长度")
    void testSM4EncryptInvalidKeyLength() {
        // Given
        String plainText = "test";
        String invalidKey = INVALID_KEY;
        
        // When & Then
        assertThatThrownBy(() -> EncryptUtil.sm4Encrypt(plainText, invalidKey))
            .isInstanceOf(EncryptionException.class)
            .hasMessageContaining("SM4密钥长度必须是16字节");
    }
    
    @Test
    @DisplayName("SM4解密-无效密文")
    void testSM4DecryptInvalidCipherText() {
        // Given
        String invalidCipherText = "invalid-cipher-text";
        String key = SM4_KEY;
        
        // When & Then
        assertThatThrownBy(() -> EncryptUtil.sm4Decrypt(invalidCipherText, key))
            .isInstanceOf(EncryptionException.class)
            .hasMessageContaining("解密失败");
    }
    
    @ParameterizedTest
    @DisplayName("SM4加密解密-多种密钥")
    @CsvSource({
        "key1234567890123, 测试文本1",
        "abcdef1234567890, 测试文本2",
        "9876543210fedcba, 测试文本3"
    })
    void testSM4EncryptDecryptWithVariousKeys(String key, String plainText) {
        // When
        String encrypted = EncryptUtil.sm4Encrypt(plainText, key);
        String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        
        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }
    
    @Test
    @DisplayName("SM3哈希一致性")
    void testSM3HashConsistency() {
        // Given
        String data = "test data for SM3 hash";
        
        // When
        String hash1 = EncryptUtil.sm3Hash(data);
        String hash2 = EncryptUtil.sm3Hash(data);
        
        // Then
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // 256位 = 64个十六进制字符
        assertThat(hash1).matches("[0-9a-f]{64}");
    }
    
    @Test
    @DisplayName("SM3哈希-不同输入产生不同输出")
    void testSM3HashDifferentInput() {
        // Given
        String data1 = "test data 1";
        String data2 = "test data 2";
        
        // When
        String hash1 = EncryptUtil.sm3Hash(data1);
        String hash2 = EncryptUtil.sm3Hash(data2);
        
        // Then
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(hash1).hasSize(64);
        assertThat(hash2).hasSize(64);
    }
    
    @ParameterizedTest
    @DisplayName("SM3哈希-多种输入")
    @ValueSource(strings = {
        "",
        "a",
        "Hello, World!",
        "中国银行数据安全测试",
        "1234567890abcdefghijklmnopqrstuvwxyz"
    })
    void testSM3HashVariousInputs(String input) {
        // When
        String hash = EncryptUtil.sm3Hash(input);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64);
        assertThat(hash).matches("[0-9a-f]{64}");
    }
    
    @Test
    @DisplayName("SM3哈希-大文件")
    void testSM3HashLargeData() {
        // Given
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeData.append("This is a large file content for SM3 hash testing. ");
        }
        
        // When
        String hash = EncryptUtil.sm3Hash(largeData.toString());
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64);
        assertThat(hash).matches("[0-9a-f]{64}");
    }
    
    @Test
    @DisplayName("SM2签名验证成功")
    void testSM2SignVerify() {
        // Given
        String plainText = "Hello, BankShield SM2!";
        String privateKey = "this-is-a-256-bit-private-key-for-sm2-algorithm";
        String publicKey = "this-is-a-256-bit-public-key-for-sm2-algorithm";
        
        // When
        String signature = EncryptUtil.sm2Sign(plainText, privateKey);
        boolean verified = EncryptUtil.sm2Verify(plainText, signature, publicKey);
        
        // Then
        assertThat(signature).isNotNull();
        assertThat(signature).isNotEmpty();
        assertThat(verified).isTrue();
    }
    
    @Test
    @DisplayName("SM2签名验证-数据被篡改")
    void testSM2SignVerifyTamperedData() {
        // Given
        String originalData = "Original data";
        String tamperedData = "Tampered data";
        String privateKey = "this-is-a-256-bit-private-key-for-sm2-algorithm";
        String publicKey = "this-is-a-256-bit-public-key-for-sm2-algorithm";
        
        // When
        String signature = EncryptUtil.sm2Sign(originalData, privateKey);
        boolean verified = EncryptUtil.sm2Verify(tamperedData, signature, publicKey);
        
        // Then
        assertThat(verified).isFalse();
    }
    
    @Test
    @DisplayName("SM2签名验证-无效签名")
    void testSM2SignVerifyInvalidSignature() {
        // Given
        String plainText = "Test data";
        String invalidSignature = "invalid-signature";
        String publicKey = "this-is-a-256-bit-public-key-for-sm2-algorithm";
        
        // When
        boolean verified = EncryptUtil.sm2Verify(plainText, invalidSignature, publicKey);
        
        // Then
        assertThat(verified).isFalse();
    }
    
    @Test
    @DisplayName("混合加密-对称加密数据，非对称加密密钥")
    void testHybridEncryption() {
        // Given
        String plainText = "This is sensitive data that needs hybrid encryption";
        String sm4Key = SM4_KEY;
        String sm2PublicKey = "this-is-a-256-bit-public-key-for-sm2-algorithm";
        String sm2PrivateKey = "this-is-a-256-bit-private-key-for-sm2-algorithm";
        
        // When - Encrypt
        String encryptedData = EncryptUtil.sm4Encrypt(plainText, sm4Key);
        String encryptedKey = EncryptUtil.sm2Encrypt(sm4Key, sm2PublicKey);
        
        // When - Decrypt
        String decryptedKey = EncryptUtil.sm2Decrypt(encryptedKey, sm2PrivateKey);
        String decryptedData = EncryptUtil.sm4Decrypt(encryptedData, decryptedKey);
        
        // Then
        assertThat(encryptedData).isNotNull();
        assertThat(encryptedKey).isNotNull();
        assertThat(decryptedKey).isEqualTo(sm4Key);
        assertThat(decryptedData).isEqualTo(plainText);
    }
    
    @Test
    @DisplayName("性能测试-SM4加密解密速度")
    void testSM4Performance() {
        // Given
        String plainText = "Performance test data";
        String key = SM4_KEY;
        int iterations = 1000;
        
        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String encrypted = EncryptUtil.sm4Encrypt(plainText, key);
            String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        }
        long endTime = System.currentTimeMillis();
        
        // Then
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        
        System.out.println("SM4加密+解密 " + iterations + " 次，总耗时：" + totalTime + "ms");
        System.out.println("平均每次耗时：" + String.format("%.2f", avgTime) + "ms");
        
        // 性能要求：单次加密+解密 < 10ms
        assertThat(avgTime).isLessThan(10.0);
    }
    
    @Test
    @DisplayName("性能测试-SM3哈希速度")
    void testSM3Performance() {
        // Given
        String data = "Hash performance test data";
        int iterations = 1000;
        
        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String hash = EncryptUtil.sm3Hash(data);
        }
        long endTime = System.currentTimeMillis();
        
        // Then
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        
        System.out.println("SM3哈希 " + iterations + " 次，总耗时：" + totalTime + "ms");
        System.out.println("平均每次耗时：" + String.format("%.2f", avgTime) + "ms");
        
        // 性能要求：单次哈希 < 5ms
        assertThat(avgTime).isLessThan(5.0);
    }
    
    @Test
    @DisplayName("边界测试-最大长度明文")
    void testSM4MaxLengthPlainText() {
        // Given - 构造接近最大长度的明文
        StringBuilder maxText = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            maxText.append("A");
        }
        String key = SM4_KEY;
        
        // When
        String encrypted = EncryptUtil.sm4Encrypt(maxText.toString(), key);
        String decrypted = EncryptUtil.sm4Decrypt(encrypted, key);
        
        // Then
        assertThat(decrypted).isEqualTo(maxText.toString());
    }
    
    @Test
    @DisplayName("异常测试-null输入")
    void testNullInput() {
        // Given
        String key = SM4_KEY;
        
        // When & Then
        assertThatThrownBy(() -> EncryptUtil.sm4Encrypt(null, key))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> EncryptUtil.sm4Decrypt(null, key))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> EncryptUtil.sm3Hash(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("异常测试-null密钥")
    void testNullKey() {
        // Given
        String plainText = "test data";
        
        // When & Then
        assertThatThrownBy(() -> EncryptUtil.sm4Encrypt(plainText, null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> EncryptUtil.sm4Decrypt("encrypted", null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}