package com.bankshield.gateway.service;

import com.bankshield.gateway.entity.ApiAccessLog;
import com.bankshield.gateway.repository.ApiAccessLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * API审计服务
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class ApiAuditService {
    
    @Autowired
    private ApiAccessLogRepository apiAccessLogRepository;
    
    /**
     * 异步记录API访问日志
     * 
     * @param accessLog 访问日志
     */
    @Async
    public void logApiAccess(ApiAccessLog accessLog) {
        try {
            apiAccessLogRepository.save(accessLog);
            log.debug("API访问日志已记录: {}", accessLog.getRequestPath());
        } catch (Exception e) {
            log.error("记录API访问日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 同步记录API访问日志
     * 
     * @param accessLog 访问日志
     */
    public void logApiAccessSync(ApiAccessLog accessLog) {
        try {
            apiAccessLogRepository.save(accessLog);
            log.debug("API访问日志已记录: {}", accessLog.getRequestPath());
        } catch (Exception e) {
            log.error("记录API访问日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 查询API访问日志
     * 
     * @param pageable 分页信息
     * @return 访问日志分页结果
     */
    public Page<ApiAccessLog> getAccessLogs(Pageable pageable) {
        return apiAccessLogRepository.findAll(pageable);
    }
    
    /**
     * 根据请求路径查询访问日志
     * 
     * @param requestPath 请求路径
     * @param pageable 分页信息
     * @return 访问日志分页结果
     */
    public Page<ApiAccessLog> getAccessLogsByPath(String requestPath, Pageable pageable) {
        return apiAccessLogRepository.findByRequestPathContaining(requestPath, pageable);
    }
    
    /**
     * 根据用户ID查询访问日志
     * 
     * @param userId 用户ID
     * @param pageable 分页信息
     * @return 访问日志分页结果
     */
    public Page<ApiAccessLog> getAccessLogsByUserId(Long userId, Pageable pageable) {
        return apiAccessLogRepository.findByUserId(userId, pageable);
    }
    
    /**
     * 根据IP地址查询访问日志
     * 
     * @param ipAddress IP地址
     * @param pageable 分页信息
     * @return 访问日志分页结果
     */
    public Page<ApiAccessLog> getAccessLogsByIpAddress(String ipAddress, Pageable pageable) {
        return apiAccessLogRepository.findByIpAddress(ipAddress, pageable);
    }
    
    /**
     * 根据访问结果查询访问日志
     * 
     * @param accessResult 访问结果
     * @param pageable 分页信息
     * @return 访问日志分页结果
     */
    public Page<ApiAccessLog> getAccessLogsByResult(String accessResult, Pageable pageable) {
        return apiAccessLogRepository.findByAccessResult(accessResult, pageable);
    }
    
    /**
     * 根据时间范围查询访问日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页信息
     * @return 访问日志分页结果
     */
    public Page<ApiAccessLog> getAccessLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return apiAccessLogRepository.findByAccessTimeBetween(startTime, endTime, pageable);
    }
    
    /**
     * 查询慢查询访问日志
     * 
     * @param executeTime 执行时间阈值（毫秒）
     * @param pageable 分页信息
     * @return 慢查询访问日志分页结果
     */
    public Page<ApiAccessLog> getSlowQueries(Long executeTime, Pageable pageable) {
        return apiAccessLogRepository.findByExecuteTimeGreaterThan(executeTime, pageable);
    }
    
    /**
     * 获取访问统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 访问统计
     */
    public AccessStatistics getAccessStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        long totalAccessCount = apiAccessLogRepository.countByAccessTimeBetween(startTime, endTime);
        Double errorRate = apiAccessLogRepository.findErrorRateByAccessTimeBetween(startTime, endTime);
        Double averageExecuteTime = apiAccessLogRepository.findAverageExecuteTimeByAccessTimeBetween(startTime, endTime);
        
        AccessStatistics statistics = new AccessStatistics();
        statistics.setTotalAccessCount(totalAccessCount);
        statistics.setErrorRate(errorRate != null ? errorRate : 0.0);
        statistics.setAverageExecuteTime(averageExecuteTime != null ? averageExecuteTime : 0.0);
        
        return statistics;
    }
    
    /**
     * 获取TOP访问路径
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return TOP访问路径列表
     */
    public List<Object[]> getTopRequestPaths(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        return apiAccessLogRepository.findTopRequestPaths(startTime, endTime, org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    /**
     * 根据ID获取访问日志
     * 
     * @param id 日志ID
     * @return 访问日志
     */
    public Optional<ApiAccessLog> getAccessLogById(Long id) {
        return apiAccessLogRepository.findById(id);
    }
    
    /**
     * 删除访问日志
     * 
     * @param id 日志ID
     */
    public void deleteAccessLog(Long id) {
        apiAccessLogRepository.deleteById(id);
    }
    
    /**
     * 批量删除访问日志
     * 
     * @param ids 日志ID列表
     */
    public void deleteAccessLogs(List<Long> ids) {
        apiAccessLogRepository.deleteAllById(ids);
    }
    
    /**
     * 清空访问日志
     */
    public void clearAccessLogs() {
        apiAccessLogRepository.deleteAll();
    }
    
    /**
     * 访问统计类
     */
    public static class AccessStatistics {
        private long totalAccessCount;
        private double errorRate;
        private double averageExecuteTime;
        
        // Getters and Setters
        public long getTotalAccessCount() {
            return totalAccessCount;
        }
        
        public void setTotalAccessCount(long totalAccessCount) {
            this.totalAccessCount = totalAccessCount;
        }
        
        public double getErrorRate() {
            return errorRate;
        }
        
        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }
        
        public double getAverageExecuteTime() {
            return averageExecuteTime;
        }
        
        public void setAverageExecuteTime(double averageExecuteTime) {
            this.averageExecuteTime = averageExecuteTime;
        }
    }
}