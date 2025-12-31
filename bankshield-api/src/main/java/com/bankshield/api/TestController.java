package com.bankshield.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单测试控制器
 */
@RestController
public class TestController {
    
    @GetMapping("/")
    public String home() {
        return "BankShield API is running!";
    }
    
    @GetMapping("/api/test")
    public String test() {
        return "BankShield API Test Endpoint - OK";
    }
}
