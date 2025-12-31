package com.bankshield.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

/**
 * BankShield API 最简化启动类
 * 排除所有可能导致问题的自动配置
 */
@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class,
    RedisAutoConfiguration.class
})
public class BankShieldApiStandaloneApplication {
    
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("server.port", "8088");
        System.setProperty("spring.profiles.active", "standalone");
        System.setProperty("logging.level.root", "INFO");
        System.setProperty("logging.level.com.bankshield", "DEBUG");
        System.setProperty("spring.autoconfigure.exclude", 
            "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
            "com.bankshield.common.security.config.SecurityHardeningAutoConfiguration");
        
        try {
            SpringApplication.run(BankShieldApiStandaloneApplication.class, args);
        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
