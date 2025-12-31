package com.bankshield.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BankShield API 极简启动类
 * 只包含最基本的功能
 */
@SpringBootApplication
public class BankShieldApiMinimalApplicationNew2 {
    
    public static void main(String[] args) {
        System.setProperty("server.port", "8089");
        System.setProperty("spring.main.lazy-initialization", "true");
        
        try {
            SpringApplication.run(BankShieldApiMinimalApplicationNew2.class, args);
        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
