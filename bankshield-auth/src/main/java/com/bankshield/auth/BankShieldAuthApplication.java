package com.bankshield.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bankshield.auth", "com.bankshield.common"})
@EnableJpaRepositories(basePackages = "com.bankshield.auth.repository")
@EntityScan(basePackages = {"com.bankshield.auth.model", "com.bankshield.common.model"})
public class BankShieldAuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BankShieldAuthApplication.class, args);
    }
}