package com.bankshield.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * BankShield网关启动类
 * 
 * @author BankShield
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.bankshield")
@EnableJpaAuditing
@EnableAsync
@ComponentScan(basePackages = "com.bankshield")
public class BankShieldGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BankShieldGatewayApplication.class, args);
    }
}