package com.bankshield.common.test;

import com.bankshield.common.core.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试基类
 * 提供通用的测试工具和方法
 * 
 * @author BankShield
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public abstract class BaseUnitTest {
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    /**
     * 验证Result成功
     */
    protected <T> void assertResultSuccess(Result<T> result) {
        assertNotNull(result, "Result不应为null");
        assertEquals(200, result.getCode(), "状态码应为200");
        assertTrue(result.isSuccess(), "应标记为成功");
    }
    
    /**
     * 验证Result失败
     */
    protected <T> void assertResultError(Result<T> result, int expectedCode) {
        assertNotNull(result, "Result不应为null");
        assertEquals(expectedCode, result.getCode(), "状态码不匹配");
        assertFalse(result.isSuccess(), "应标记为失败");
    }
    
    /**
     * 验证Result失败并包含特定消息
     */
    protected <T> void assertResultErrorWithMessage(Result<T> result, int expectedCode, String expectedMessage) {
        assertResultError(result, expectedCode);
        assertTrue(result.getMessage().contains(expectedMessage), 
            "错误消息应包含: " + expectedMessage + ", 实际: " + result.getMessage());
    }
    
    /**
     * 生成随机字符串
     */
    protected String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
    
    /**
     * 生成随机数字
     */
    protected long randomLong() {
        return ThreadLocalRandom.current().nextLong(1, 1000000);
    }
    
    /**
     * 生成随机整数
     */
    protected int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
    
    /**
     * 生成随机邮箱
     */
    protected String randomEmail() {
        return randomString(8) + "@test.com";
    }
    
    /**
     * 生成随机手机号
     */
    protected String randomPhone() {
        return "1" + (ThreadLocalRandom.current().nextLong(3000000000L, 3999999999L));
    }
    
    /**
     * 等待指定时间（毫秒）
     */
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }
    
    /**
     * 将JSON字符串转换为对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }
}