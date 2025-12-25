package com.bankshield.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI智能安全分析模块启动类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.bankshield")
@ComponentScan(basePackages = "com.bankshield")
@MapperScan("com.bankshield.ai.mapper")
@EnableAsync
@EnableScheduling
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
        System.out.println("BankShield AI智能安全分析模块启动成功！");
    }
}