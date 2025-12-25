package com.bankshield.common.test;

import com.bankshield.common.core.Result;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Mock配置类
 * 提供测试所需的Mock Bean
 * 
 * @author BankShield
 */
@Configuration
public class MockConfig {
    
    /**
     * Mock RedisTemplate
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(anyString(), any())).thenReturn(true);
        when(valueOperations.set(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L);
        
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.delete(anyList())).thenReturn(1L);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);
        
        return redisTemplate;
    }
    
    /**
     * Mock 文件存储服务
     */
    @Bean
    @Primary
    public FileStorageService fileStorageService() {
        FileStorageService service = mock(FileStorageService.class);
        when(service.uploadFile(any(), anyString())).thenReturn("/test/file/path");
        when(service.downloadFile(anyString())).thenReturn(new byte[1024]);
        when(service.deleteFile(anyString())).thenReturn(true);
        return service;
    }
    
    /**
     * Mock 邮件服务
     */
    @Bean
    @Primary
    public EmailService emailService() {
        EmailService service = mock(EmailService.class);
        when(service.sendEmail(anyString(), anyString(), anyString())).thenReturn(Result.success());
        when(service.sendHtmlEmail(anyString(), anyString(), anyString())).thenReturn(Result.success());
        return service;
    }
    
    /**
     * Mock 短信服务
     */
    @Bean
    @Primary
    public SmsService smsService() {
        SmsService service = mock(SmsService.class);
        when(service.sendSms(anyString(), anyString())).thenReturn(Result.success());
        return service;
    }
    
    /**
     * Mock 文件存储服务接口
     */
    public interface FileStorageService {
        String uploadFile(byte[] fileData, String fileName);
        byte[] downloadFile(String filePath);
        boolean deleteFile(String filePath);
    }
    
    /**
     * Mock 邮件服务接口
     */
    public interface EmailService {
        Result<String> sendEmail(String to, String subject, String content);
        Result<String> sendHtmlEmail(String to, String subject, String htmlContent);
    }
    
    /**
     * Mock 短信服务接口
     */
    public interface SmsService {
        Result<String> sendSms(String phone, String content);
    }
}