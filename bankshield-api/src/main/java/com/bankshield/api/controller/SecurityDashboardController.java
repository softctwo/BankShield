package com.bankshield.api.controller;

import com.bankshield.api.dto.SecurityDashboardDTO;
import com.bankshield.api.service.SecurityDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全态势仪表板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/security/dashboard")
public class SecurityDashboardController {
    
    @Autowired
    private SecurityDashboardService dashboardService;
    
    /**
     * 获取安全态势仪表板数据
     */
    @GetMapping("/data")
    public Map<String, Object> getDashboardData() {
        try {
            SecurityDashboardDTO data = dashboardService.getDashboardData();
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", data);
            result.put("message", "success");
            return result;
        } catch (Exception e) {
            log.error("获取安全态势数据失败: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取安全态势数据失败");
            return result;
        }
    }
    
    /**
     * 获取安全评分
     */
    @GetMapping("/score")
    public Map<String, Object> getSecurityScore() {
        try {
            Integer score = dashboardService.calculateSecurityScore();
            String level = dashboardService.getSecurityLevel();
            
            Map<String, Object> data = new HashMap<>();
            data.put("score", score);
            data.put("level", level);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", data);
            result.put("message", "success");
            return result;
        } catch (Exception e) {
            log.error("获取安全评分失败: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取安全评分失败");
            return result;
        }
    }
}
