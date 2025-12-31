package com.bankshield.api.controller;

import com.bankshield.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HealthController {
    
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("application", "BankShield API");
        data.put("version", "1.0.0");
        
        log.info("健康检查请求处理完成");
        return Result.success(data);
    }
    
    @GetMapping("/test")
    public Result<String> test() {
        log.info("测试接口调用成功");
        return Result.success("BankShield API 运行正常！");
    }
}
