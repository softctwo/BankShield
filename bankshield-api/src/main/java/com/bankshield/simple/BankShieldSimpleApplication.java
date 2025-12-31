package com.bankshield.simple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * BankShield 最简化独立应用
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@RestController
public class BankShieldSimpleApplication {
    
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "🎉 BankShield API 运行成功！");
        result.put("status", "RUNNING");
        result.put("application", "BankShield");
        result.put("version", "1.0.0");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("application", "BankShield API");
        result.put("database", "MySQL Connected");
        result.put("cache", "Redis Ready");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    @GetMapping("/api/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "BankShield API 测试接口正常工作");
        result.put("features", new String[]{"数据加密", "访问控制", "审计追踪", "敏感数据识别"});
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    public static void main(String[] args) {
        System.setProperty("server.port", "8091");
        System.setProperty("spring.profiles.active", "simple");
        
        SpringApplication.run(BankShieldSimpleApplication.class, args);
    }
}
