package com.bankshield.common.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SM3工具类测试
 */
@SpringBootTest
public class SM3UtilTest {
    
    @Test
    public void testHash() {
        String data = "Hello, SM3 Hash!";
        String hash = SM3Util.hash(data);
        
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SM3输出256位，64个十六进制字符
        assertTrue(hash.matches("[0-9a-fA-F]{64}"));
        
        // 相同数据应该产生相同的哈希
        String hash2 = SM3Util.hash(data);
        assertEquals(hash, hash2);
        
        // 不同数据应该产生不同的哈希
        String differentHash = SM3Util.hash("Different data");
        assertNotEquals(hash, differentHash);
    }
    
    @Test
    public void testHashWithBytes() {
        byte[] data = "Test data for byte array".getBytes();
        byte[] hash = SM3Util.hash(data);
        
        assertNotNull(hash);
        assertEquals(32, hash.length); // SM3输出256位，32字节
        
        // 相同数据应该产生相同的哈希
        byte[] hash2 = SM3Util.hash(data);
        assertArrayEquals(hash, hash2);
    }
    
    @Test
    public void testHashWithSalt() {
        byte[] data = "Password123".getBytes();
        byte[] salt = SM3Util.generateSalt(16);
        
        String hash1 = SM3Util.hashWithSalt(data, salt);
        String hash2 = SM3Util.hashWithSalt(data, salt);
        
        // 相同数据和盐应该产生相同的哈希
        assertEquals(hash1, hash2);
        
        // 不同的盐应该产生不同的哈希
        byte[] differentSalt = SM3Util.generateSalt(16);
        String hash3 = SM3Util.hashWithSalt(data, differentSalt);
        assertNotEquals(hash1, hash3);
    }
    
    @Test
    public void testGenerateSalt() {
        byte[] salt1 = SM3Util.generateSalt(16);
        byte[] salt2 = SM3Util.generateSalt(16);
        
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(16, salt1.length);
        assertEquals(16, salt2.length);
        
        // 生成的盐应该是随机的
        assertFalse(java.util.Arrays.equals(salt1, salt2));
    }
    
    @Test
    public void testHMAC() {
        String key = "secret-key";
        String data = "Message to authenticate";
        
        String hmac1 = SM3Util.hmac(key, data);
        String hmac2 = SM3Util.hmac(key, data);
        
        assertNotNull(hmac1);
        assertEquals(hmac1, hmac2);
        
        // 不同的密钥应该产生不同的HMAC
        String hmac3 = SM3Util.hmac("different-key", data);
        assertNotEquals(hmac1, hmac3);
        
        // 不同的数据应该产生不同的HMAC
        String hmac4 = SM3Util.hmac(key, "different-data");
        assertNotEquals(hmac1, hmac4);
    }
    
    @Test
    public void testHMACWithBytes() {
        byte[] key = "secret-key".getBytes();
        byte[] data = "Message to authenticate".getBytes();
        
        byte[] hmac1 = SM3Util.hmac(key, data);
        byte[] hmac2 = SM3Util.hmac(key, data);
        
        assertNotNull(hmac1);
        assertArrayEquals(hmac1, hmac2);
    }
    
    @Test
    public void testGeneratePasswordHash() {
        String password = "MySecurePassword123!";
        
        String hash1 = SM3Util.generatePasswordHash(password);
        String hash2 = SM3Util.generatePasswordHash(password);
        
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertTrue(hash1.startsWith("$SM3$"));
        assertTrue(hash2.startsWith("$SM3$"));
        
        // 相同密码应该产生不同的哈希（因为盐不同）
        assertNotEquals(hash1, hash2);
    }
    
    @Test
    public void testVerifyPasswordHash() {
        String password = "TestPassword456!";
        String wrongPassword = "WrongPassword789!";
        
        String hash = SM3Util.generatePasswordHash(password);
        
        // 正确密码应该验证通过
        assertTrue(SM3Util.verifyPasswordHash(password, hash));
        
        // 错误密码应该验证失败
        assertFalse(SM3Util.verifyPasswordHash(wrongPassword, hash));
        
        // 无效格式的哈希应该验证失败
        assertFalse(SM3Util.verifyPasswordHash(password, "invalid-hash"));
        assertFalse(SM3Util.verifyPasswordHash(password, "$SM3$invalid"));
    }
    
    @Test
    public void testFileHash() {
        byte[] fileData = "This is simulated file content for testing SM3 hash.".getBytes();
        
        String hash1 = SM3Util.fileHash(fileData);
        String hash2 = SM3Util.fileHash(fileData);
        
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
        
        // 不同的文件内容应该产生不同的哈希
        byte[] differentFileData = "Different file content.".getBytes();
        String differentHash = SM3Util.fileHash(differentFileData);
        assertNotEquals(hash1, differentHash);
    }
    
    @Test
    public void testGenerateMAC() {
        String key = "mac-key";
        String message = "Message to protect";
        
        String mac = SM3Util.generateMAC(key, message);
        assertNotNull(mac);
        assertEquals(64, mac.length());
    }
    
    @Test
    public void testVerifyMAC() {
        String key = "verification-key";
        String message = "Message to verify";
        
        String mac = SM3Util.generateMAC(key, message);
        
        // 正确的MAC应该验证通过
        assertTrue(SM3Util.verifyMAC(key, message, mac));
        
        // 错误的MAC应该验证失败
        assertFalse(SM3Util.verifyMAC(key, message, "wrong-mac"));
        
        // 不同的消息应该验证失败
        assertFalse(SM3Util.verifyMAC(key, "different message", mac));
        
        // 不同的密钥应该验证失败
        assertFalse(SM3Util.verifyMAC("wrong-key", message, mac));
    }
    
    @Test
    public void testChineseCharacters() {
        String chineseText = "欢迎使用国密算法SM3进行哈希计算！";
        
        String hash = SM3Util.hash(chineseText);
        assertNotNull(hash);
        assertEquals(64, hash.length());
        
        // 相同的中文内容应该产生相同的哈希
        String hash2 = SM3Util.hash(chineseText);
        assertEquals(hash, hash2);
    }
    
    @Test
    public void testEmptyData() {
        String hash = SM3Util.hash("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
        
        String hmac = SM3Util.hmac("key", "");
        assertNotNull(hmac);
        assertEquals(64, hmac.length());
    }
    
    @Test
    public void testLargeData() {
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeData.append("Large data test ").append(i).append(" ");
        }
        
        String hash = SM3Util.hash(largeData.toString());
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}