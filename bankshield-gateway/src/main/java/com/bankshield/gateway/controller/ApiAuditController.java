package com.bankshield.gateway.controller;

import com.bankshield.common.result.Result;
import com.bankshield.gateway.entity.ApiAccessLog;
import com.bankshield.gateway.service.ApiAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API审计控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/gateway/api")
public class ApiAuditController {
    
    @Autowired
    private ApiAuditService apiAuditService;
    
    /**
     * 获取API访问日志列表
     */
    @GetMapping("/access/logs")
    public Result<Page<ApiAccessLog>> getAccessLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String requestPath,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String accessResult,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("accessTime").descending());
            
            Page<ApiAccessLog> result;
            
            if (requestPath != null && !requestPath.trim().isEmpty()) {
                result = apiAuditService.getAccessLogsByPath(requestPath, pageable);
            } else if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                result = apiAuditService.getAccessLogsByIpAddress(ipAddress, pageable);
            } else if (userId != null) {
                result = apiAuditService.getAccessLogsByUserId(userId, pageable);
            } else if (accessResult != null && !accessResult.trim().isEmpty()) {
                result = apiAuditService.getAccessLogsByResult(accessResult, pageable);
            } else if (startTime != null && endTime != null) {
                result = apiAuditService.getAccessLogsByTimeRange(startTime, endTime, pageable);
            } else {
                result = apiAuditService.getAccessLogs(pageable);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取API访问日志失败", e);
            return Result.error("获取API访问日志失败");
        }
    }
    
    /**
     * 根据ID获取访问日志
     */
    @GetMapping("/access/log/{id}")
    public Result<ApiAccessLog> getAccessLogById(@PathVariable Long id) {
        try {
            java.util.Optional<ApiAccessLog> log = apiAuditService.getAccessLogById(id);
            if (log.isPresent()) {
                return Result.success(log.get());
            } else {
                return Result.error("访问日志不存在");
            }
        } catch (Exception e) {
            log.error("获取访问日志失败", e);
            return Result.error("获取访问日志失败");
        }
    }
    
    /**
     * 获取慢查询日志
     */
    @GetMapping("/slow-queries")
    public Result<Page<ApiAccessLog>> getSlowQueries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1000") Long executeTime) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("executeTime").descending());
            Page<ApiAccessLog> result = apiAuditService.getSlowQueries(executeTime, pageable);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取慢查询日志失败", e);
            return Result.error("获取慢查询日志失败");
        }
    }
    
    /**
     * 获取访问统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getAccessStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            // 默认查询最近7天
            if (startTime == null || endTime == null) {
                endTime = LocalDateTime.now();
                startTime = endTime.minusDays(7);
            }
            
            ApiAuditService.AccessStatistics statistics = apiAuditService.getAccessStatistics(startTime, endTime);
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalAccessCount", statistics.getTotalAccessCount());
            result.put("errorRate", String.format("%.2f%%", statistics.getErrorRate()));
            result.put("averageExecuteTime", String.format("%.2fms", statistics.getAverageExecuteTime()));
            result.put("startTime", startTime);
            result.put("endTime", endTime);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取访问统计信息失败", e);
            return Result.error("获取访问统计信息失败");
        }
    }
    
    /**
     * 获取TOP访问路径
     */
    @GetMapping("/top-paths")
    public Result<List<Object[]>> getTopRequestPaths(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            // 默认查询最近7天
            if (startTime == null || endTime == null) {
                endTime = LocalDateTime.now();
                startTime = endTime.minusDays(7);
            }
            
            List<Object[]> topPaths = apiAuditService.getTopRequestPaths(startTime, endTime, limit);
            return Result.success(topPaths);
        } catch (Exception e) {
            log.error("获取TOP访问路径失败", e);
            return Result.error("获取TOP访问路径失败");
        }
    }
    
    /**
     * 获取错误率统计
     */
    @GetMapping("/error-rate")
    public Result<Map<String, Object>> getErrorRateStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            // 默认查询最近7天
            if (startTime == null || endTime == null) {
                endTime = LocalDateTime.now();
                startTime = endTime.minusDays(7);
            }
            
            ApiAuditService.AccessStatistics statistics = apiAuditService.getAccessStatistics(startTime, endTime);
            
            Map<String, Object> result = new HashMap<>();
            result.put("errorRate", statistics.getErrorRate());
            result.put("totalAccessCount", statistics.getTotalAccessCount());
            result.put("errorCount", (long) (statistics.getTotalAccessCount() * statistics.getErrorRate() / 100));
            result.put("startTime", startTime);
            result.put("endTime", endTime);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取错误率统计失败", e);
            return Result.error("获取错误率统计失败");
        }
    }
    
    /**
     * 删除访问日志
     */
    @DeleteMapping("/access/log/{id}")
    public Result<String> deleteAccessLog(@PathVariable Long id) {
        try {
            java.util.Optional<ApiAccessLog> log = apiAuditService.getAccessLogById(id);
            if (!log.isPresent()) {
                return Result.error("访问日志不存在");
            }
            
            apiAuditService.deleteAccessLog(id);
            log.info("删除访问日志成功：id={}", id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除访问日志失败", e);
            return Result.error("删除访问日志失败");
        }
    }
    
    /**
     * 批量删除访问日志
     */
    @DeleteMapping("/access/logs/batch")
    public Result<String> deleteAccessLogsBatch(@RequestBody Map<String, List<Long>> params) {
        try {
            List<Long> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("日志ID列表不能为空");
            }
            
            apiAuditService.deleteAccessLogs(ids);
            log.info("批量删除访问日志成功：count={}", ids.size());
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除访问日志失败", e);
            return Result.error("批量删除访问日志失败");
        }
    }
    
    /**
     * 清空访问日志（谨慎操作）
     */
    @DeleteMapping("/access/logs/clear")
    public Result<String> clearAccessLogs() {
        try {
            apiAuditService.clearAccessLogs();
            log.warn("清空所有访问日志");
            return Result.success("清空成功");
        } catch (Exception e) {
            log.error("清空访问日志失败", e);
            return Result.error("清空访问日志失败");
        }
    }
    
    /**
     * 获取实时访问统计
     */
    @GetMapping("/realtime-stats")
    public Result<Map<String, Object>> getRealtimeStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            LocalDateTime oneDayAgo = now.minusDays(1);
            
            // 获取最近1小时的统计
            ApiAuditService.AccessStatistics hourStats = apiAuditService.getAccessStatistics(oneHourAgo, now);
            
            // 获取最近1天的统计
            ApiAuditService.AccessStatistics dayStats = apiAuditService.getAccessStatistics(oneDayAgo, now);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hourStats", Map.of(
                "totalAccessCount", hourStats.getTotalAccessCount(),
                "errorRate", hourStats.getErrorRate(),
                "averageExecuteTime", hourStats.getAverageExecuteTime()
            ));
            result.put("dayStats", Map.of(
                "totalAccessCount", dayStats.getTotalAccessCount(),
                "errorRate", dayStats.getErrorRate(),
                "averageExecuteTime", dayStats.getAverageExecuteTime()
            ));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取实时访问统计失败", e);
            return Result.error("获取实时访问统计失败");
        }
    }
}