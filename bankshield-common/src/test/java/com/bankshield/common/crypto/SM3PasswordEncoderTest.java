package com.bankshield.common.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SM3密码编码器测试
 */
@SpringBootTest
public class SM3PasswordEncoderTest {
    
    private SM3PasswordEncoder encoder = new SM3PasswordEncoder();
    
    @Test
    public void testEncode() {
        String password = "MySecurePassword123!";
        
        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);
        
        assertNotNull(encoded1);
        assertNotNull(encoded2);
        assertTrue(encoded1.startsWith("$SM3$"));
        assertTrue(encoded2.startsWith("$SM3$"));
        
        // 相同密码应该产生不同的编码（因为盐不同）
        assertNotEquals(encoded1, encoded2);
    }
    
    @Test
    public void testMatches() {
        String password = "TestPassword456!";
        String wrongPassword = "WrongPassword789!";
        
        String encoded = encoder.encode(password);
        
        // 正确密码应该匹配
        assertTrue(encoder.matches(password, encoded));
        
        // 错误密码不应该匹配
        assertFalse(encoder.matches(wrongPassword, encoded));
    }
    
    @Test
    public void testMatchesWithNullEncoded() {
        String password = "TestPassword";
        
        assertFalse(encoder.matches(password, null));
    }
    
    @Test
    public void testMatchesWithInvalidFormat() {
        String password = "TestPassword";
        
        // 无效格式应该返回false
        assertFalse(encoder.matches(password, "invalid-format"));
        assertFalse(encoder.matches(password, "$SM3$invalid"));
        assertFalse(encoder.matches(password, "$SM3$part1$part2$part3"));
    }
    
    @Test
    public void testGetAlgorithmName() {
        assertEquals("SM3", encoder.getAlgorithmName());
    }
    
    @Test
    public void testUpgradeEncoding() {
        String sm3Encoded = encoder.encode("password");
        String bcryptEncoded = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUOe"; // 示例BCrypt格式
        
        // SM3编码不需要升级
        assertFalse(encoder.upgradeEncoding(sm3Encoded));
        
        // BCrypt编码需要升级
        assertTrue(encoder.upgradeEncoding(bcryptEncoded));
        
        // null需要升级
        assertTrue(encoder.upgradeEncoding(null));
    }
    
    @Test
    public void testEncodeBatch() {
        String password1 = "Password1";
        String password2 = "Password2";
        String password3 = "Password3";
        
        String[] encodedPasswords = encoder.encodeBatch(password1, password2, password3);
        
        assertEquals(3, encodedPasswords.length);
        assertTrue(encodedPasswords[0].startsWith("$SM3$"));
        assertTrue(encodedPasswords[1].startsWith("$SM3$"));
        assertTrue(encodedPasswords[2].startsWith("$SM3$"));
        
        // 验证每个密码
        assertTrue(encoder.matches(password1, encodedPasswords[0]));
        assertTrue(encoder.matches(password2, encodedPasswords[1]));
        assertTrue(encoder.matches(password3, encodedPasswords[2]));
    }
    
    @Test
    public void testMatchesBatch() {
        String password1 = "Password1";
        String password2 = "Password2";
        String password3 = "Password3";
        
        String[] encodedPasswords = {
            encoder.encode(password1),
            encoder.encode(password2),
            encoder.encode(password3)
        };
        
        boolean[] results = encoder.matchesBatch(encodedPasswords, password1, password2, password3);
        
        assertEquals(3, results.length);
        assertTrue(results[0]);
        assertTrue(results[1]);
        assertTrue(results[2]);
    }
    
    @Test
    public void testMatchesBatchWithWrongLength() {
        String[] encodedPasswords = {"encoded1", "encoded2"};
        CharSequence[] rawPasswords = {"password1"};
        
        assertThrows(IllegalArgumentException.class, () -> {
            encoder.matchesBatch(encodedPasswords, rawPasswords);
        });
    }
    
    @Test
    public void testGetEncodingStrength() {
        assertEquals(256, encoder.getEncodingStrength()); // SM3输出256位哈希
    }
    
    @Test
    public void testIsValidEncodedPassword() {
        String validEncoded = encoder.encode("password");
        
        // 有效的编码格式
        assertTrue(encoder.isValidEncodedPassword(validEncoded));
        
        // 无效的编码格式
        assertFalse(encoder.isValidEncodedPassword(null));
        assertFalse(encoder.isValidEncodedPassword("invalid"));
        assertFalse(encoder.isValidEncodedPassword("$SM3$"));
        assertFalse(encoder.isValidEncodedPassword("$SM3$part1$"));
        assertFalse(encoder.isValidEncodedPassword("$SM3$invalid-base64$hash"));
        assertFalse(encoder.isValidEncodedPassword("$SM3$salt$invalid-hash-length"));
    }
    
    @Test
    public void testExtractSalt() {
        String password = "TestPassword";
        String encoded = encoder.encode(password);
        
        String salt = encoder.extractSalt(encoded);
        assertNotNull(salt);
        assertTrue(salt.length() > 0);
        
        // 无效格式应该返回null
        assertNull(encoder.extractSalt("invalid"));
    }
    
    @Test
    public void testExtractHash() {
        String password = "TestPassword";
        String encoded = encoder.encode(password);
        
        String hash = encoder.extractHash(encoded);
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SM3输出64个十六进制字符
        assertTrue(hash.matches("[0-9a-fA-F]{64}"));
        
        // 无效格式应该返回null
        assertNull(encoder.extractHash("invalid"));
    }
    
    @Test
    public void testSpecialCharacters() {
        String password = "Special!@#$%^&*()_+-=[]{}|;':\"\\,./<>?";
        
        String encoded = encoder.encode(password);
        assertTrue(encoder.matches(password, encoded));
    }
    
    @Test
    public void testChineseCharacters() {
        String password = "欢迎使用国密算法SM3进行密码编码！";
        
        String encoded = encoder.encode(password);
        assertTrue(encoder.matches(password, encoded));
    }
    
    @Test
    public void testLongPassword() {
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPassword.append("LongPassword").append(i);
        }
        
        String password = longPassword.toString();
        String encoded = encoder.encode(password);
        assertTrue(encoder.matches(password, encoded));
    }
    
    @Test
    public void testEmptyPassword() {
        String password = "";
        
        String encoded = encoder.encode(password);
        assertTrue(encoder.matches(password, encoded));
    }
    
    @Test
    public void testPerformance() {
        String password = "PerformanceTestPassword123!";
        
        long startTime = System.currentTimeMillis();
        
        // 执行1000次编码和验证
        for (int i = 0; i < 1000; i++) {
            String encoded = encoder.encode(password);
            encoder.matches(password, encoded);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("1000次SM3密码编码和验证耗时: " + duration + "ms");
        assertTrue(duration < 5000); // 应该在5秒内完成
    }
}