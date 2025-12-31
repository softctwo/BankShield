package com.bankshield.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * BankShield API 独立启动类
 * 完全独立，不依赖其他复杂配置
 */
@SpringBootApplication
@RestController
public class BankShieldApiSimpleApplication {
    
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "BankShield API 运行成功！");
        result.put("status", "RUNNING");
        result.put("timestamp", LocalDateTime.now());
        result.put("version", "1.0.0");
        return result;
    }
    
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("application", "BankShield API");
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
    
    public static void main(String[] args) {
        System.setProperty("server.port", "8090");
        System.setProperty("spring.main.lazy-initialization", "true");
        
        try {
            SpringApplication.run(BankShieldApiSimpleApplication.class, args);
        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
