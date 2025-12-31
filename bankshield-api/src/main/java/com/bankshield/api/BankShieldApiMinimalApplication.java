package com.bankshield.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

/**
 * BankShield API 最简化启动类
 */
@SpringBootApplication(exclude = {
    SecurityFilterAutoConfiguration.class,
    RedisAutoConfiguration.class
})
public class BankShieldApiMinimalApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "8082");
        System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(BankShieldApiMinimalApplication.class, args);
    }
}
