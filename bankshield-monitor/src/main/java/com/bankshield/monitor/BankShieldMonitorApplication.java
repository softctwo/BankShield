package com.bankshield.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BankShield监控服务启动类
 * 
 * @author BankShield Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminServer
@EnableScheduling
public class BankShieldMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankShieldMonitorApplication.class, args);
    }
}