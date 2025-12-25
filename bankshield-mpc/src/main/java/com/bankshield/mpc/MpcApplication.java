package com.bankshield.mpc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 多方安全计算模块启动类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.bankshield"})
@MapperScan("com.bankshield.mpc.mapper")
public class MpcApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MpcApplication.class, args);
    }
}