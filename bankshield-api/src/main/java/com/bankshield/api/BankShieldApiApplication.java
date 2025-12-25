package com.bankshield.api;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * BankShield API 启动类
 * 启用三权分立机制的相关配置
 */
@Slf4j
@SpringBootApplication
@EnableEurekaClient
@EnableSwagger2
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
@EnableAsync
@MapperScan("com.bankshield.api.mapper")
public class BankShieldApiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BankShieldApiApplication.class, args);
        log.info("========================================");
        log.info("   BankShield API 启动成功!");
        log.info("   访问地址: http://localhost:8080/api");
        log.info("   Swagger地址: http://localhost:8080/swagger-ui.html");
        log.info("   三权分立机制已启用");
        log.info("========================================");
    }
}