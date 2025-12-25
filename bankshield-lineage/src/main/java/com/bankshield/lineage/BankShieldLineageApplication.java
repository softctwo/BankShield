package com.bankshield.lineage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据血缘模块启动类
 *
 * @author BankShield
 * @since 2024-01-24
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {"com.bankshield"})
@MapperScan("com.bankshield.lineage.mapper")
public class BankShieldLineageApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankShieldLineageApplication.class, args);
        System.out.println("\n=================================================");
        System.out.println("                                                 ");
        System.out.println("            BankShield 数据血缘模块               ");
        System.out.println("                                                 ");
        System.out.println("            版本：v1.0.0                          ");
        System.out.println("            状态：启动成功                        ");
        System.out.println("                                                 ");
        System.out.println("=================================================\n");
    }
}