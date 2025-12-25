package com.bankshield.common;

import com.bankshield.common.security.filter.SecureHeadersFilter;
import com.bankshield.common.security.filter.WafFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 单元测试基类
 * 提供通用的测试工具和方法
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public abstract class BaseUnitTest {
    
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    private WebApplicationContext webAppContext;
    
    @BeforeAll
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .addFilter(new WafFilter())
                .addFilter(new SecureHeadersFilter())
                .build();
    }
    
    /**
     * 生成测试UUID
     */
    protected String generateTestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成测试时间
     */
    protected LocalDateTime generateTestTime() {
        return LocalDateTime.now();
    }
    
    /**
     * 生成随机字符串
     */
    protected String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    /**
     * 生成随机数字
     */
    protected int generateRandomNumber(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }
    
    /**
     * 验证Result结构
     */
    protected void assertResultSuccess(com.bankshield.common.result.Result<?> result) {
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(200, result.getCode());
        org.junit.jupiter.api.Assertions.assertTrue(result.isSuccess());
    }
    
    /**
     * 验证错误Result结构
     */
    protected void assertResultError(com.bankshield.common.result.Result<?> result, int expectedCode) {
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(expectedCode, result.getCode());
        org.junit.jupiter.api.Assertions.assertFalse(result.isSuccess());
    }
    
    /**
     * 等待一段时间（用于异步操作测试）
     */
    protected void waitForAsync(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}