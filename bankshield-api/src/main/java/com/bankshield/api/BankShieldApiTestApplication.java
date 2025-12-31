package com.bankshield.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BankShield API 测试启动类
 */
@SpringBootApplication
public class BankShieldApiTestApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "8083");
        SpringApplication.run(BankShieldApiTestApplication.class, args);
    }
}
