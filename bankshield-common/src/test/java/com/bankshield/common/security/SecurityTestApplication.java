package com.bankshield.common.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 安全测试应用
 * 用于测试安全加固模块
 * 
 * @author BankShield
 * @version 1.0.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(basePackages = "com.bankshield.common.security")
public class SecurityTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityTestApplication.class, args);
    }
}