package com.bankshield.encrypt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 密钥管理模块启动类
 */
@SpringBootApplication(scanBasePackages = "com.bankshield")
@MapperScan("com.bankshield.encrypt.mapper")
@EnableScheduling
public class BankShieldEncryptApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BankShieldEncryptApplication.class, args);
    }
}