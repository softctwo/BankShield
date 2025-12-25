package com.bankshield.gateway.filter;

import com.bankshield.common.utils.SignatureUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 增强版签名验证过滤器测试
 * 
 * @author BankShield
 */
public class EnhancedSignatureVerificationFilterTest {
    
    @Test
    public void testSignatureGenerationAndVerification() {
        // 测试参数
        String method = "POST";
        String path = "/api/transfer";
        String queryString = "from=123&to=456&amount=1000";
        String body = "{\"currency\":\"CNY\",\"remark\":\"test transfer\"}";
        String appId = "test_app_001";
        String secretKey = "test_secret_key_12345";
        
        // 生成签名
        Map<String, String> headers = new HashMap<>();
        String signature = SignatureUtil.generateSignature(method, path, queryString, headers, body, appId, secretKey);
        
        // 验证生成的签名Headers
        assertNotNull(headers.get("X-Signature"));
        assertNotNull(headers.get("X-Timestamp"));
        assertNotNull(headers.get("X-Nonce"));
        assertNotNull(headers.get("X-App-Id"));
        assertNotNull(headers.get("Content-MD5"));
        
        System.out.println("Generated Headers:");
        headers.forEach((key, value) -> System.out.println(key + ": " + value));
        
        // 验证签名
        boolean isValid = SignatureUtil.verifySignature(
            headers.get("X-Signature"), 
            method, path, queryString, headers, body, appId, secretKey
        );
        
        assertTrue(isValid, "签名验证应该通过");
    }
    
    @Test
    public void testSignatureWithDifferentBody() {
        // 测试参数
        String method = "POST";
        String path = "/api/transfer";
        String queryString = "from=123&to=456&amount=1000";
        String originalBody = "{\"currency\":\"CNY\",\"remark\":\"test transfer\"}";
        String tamperedBody = "{\"currency\":\"USD\",\"remark\":\"hacked transfer\"}";
        String appId = "test_app_001";
        String secretKey = "test_secret_key_12345";
        
        // 生成原始签名
        Map<String, String> headers = new HashMap<>();
        String originalSignature = SignatureUtil.generateSignature(method, path, queryString, headers, originalBody, appId, secretKey);
        
        // 验证原始签名
        boolean originalValid = SignatureUtil.verifySignature(
            originalSignature, method, path, queryString, headers, originalBody, appId, secretKey
        );
        assertTrue(originalValid, "原始签名应该有效");
        
        // 使用篡改后的body验证签名
        boolean tamperedValid = SignatureUtil.verifySignature(
            originalSignature, method, path, queryString, headers, tamperedBody, appId, secretKey
        );
        assertFalse(tamperedValid, "篡改后的签名应该无效");
    }
    
    @Test
    public void testSignatureWithHeaders() {
        // 测试参数
        String method = "POST";
        String path = "/api/transfer";
        String queryString = "";
        String body = "{\"amount\":1000}";
        String appId = "test_app_001";
        String secretKey = "test_secret_key_12345";
        String contentType = "application/json";
        
        // 生成签名
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        
        String signature = SignatureUtil.generateSignature(method, path, queryString, headers, body, appId, secretKey);
        
        // 验证签名
        boolean isValid = SignatureUtil.verifySignature(
            signature, method, path, queryString, headers, body, appId, secretKey
        );
        
        assertTrue(isValid, "包含Header的签名应该有效");
        
        // 修改Header后验证
        headers.put("Content-Type", "text/plain");
        boolean invalidAfterHeaderChange = SignatureUtil.verifySignature(
            signature, method, path, queryString, headers, body, appId, secretKey
        );
        
        assertFalse(invalidAfterHeaderChange, "Header修改后的签名应该无效");
    }
    
    @Test
    public void testNonceGeneration() {
        // 测试随机数生成
        Map<String, String> headers1 = new HashMap<>();
        Map<String, String> headers2 = new HashMap<>();
        
        String signature1 = SignatureUtil.generateSignature("GET", "/api/test", "", headers1, "", "app1", "key1");
        String signature2 = SignatureUtil.generateSignature("GET", "/api/test", "", headers2, "", "app1", "key1");
        
        String nonce1 = headers1.get("X-Nonce");
        String nonce2 = headers2.get("X-Nonce");
        
        assertNotNull(nonce1);
        assertNotNull(nonce2);
        assertNotEquals(nonce1, nonce2, "两次生成的随机数应该不同");
        assertEquals(32, nonce1.length(), "随机数长度应该为32位");
        assertEquals(32, nonce2.length(), "随机数长度应该为32位");
    }
    
    @Test
    public void testSignStringFormat() {
        // 测试签名字符串格式
        String method = "POST";
        String path = "/api/test";
        String queryString = "param=value";
        String timestamp = "1234567890123";
        String nonce = "abcdef1234567890abcdef1234567890";
        String appId = "test_app";
        String secretKey = "test_key";
        String body = "test body";
        String contentType = "application/json";
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("X-Timestamp", timestamp);
        headers.put("X-Nonce", nonce);
        headers.put("X-App-Id", appId);
        
        String signature = SignatureUtil.generateSignature(method, path, queryString, headers, body, appId, secretKey);
        
        // 验证生成的签名格式
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        
        // Base64编码的签名应该只包含Base64字符
        assertTrue(signature.matches("^[A-Za-z0-9+/=]+$"), "签名应该是有效的Base64格式");
    }
}