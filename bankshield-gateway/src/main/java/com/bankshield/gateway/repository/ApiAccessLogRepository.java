package com.bankshield.gateway.repository;

import com.bankshield.gateway.entity.ApiAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API访问日志Repository
 * 
 * @author BankShield
 */
@Repository
public interface ApiAccessLogRepository extends JpaRepository<ApiAccessLog, Long> {
    
    /**
     * 根据请求路径查询
     */
    Page<ApiAccessLog> findByRequestPathContaining(String requestPath, Pageable pageable);
    
    /**
     * 根据用户ID查询
     */
    Page<ApiAccessLog> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据IP地址查询
     */
    Page<ApiAccessLog> findByIpAddress(String ipAddress, Pageable pageable);
    
    /**
     * 根据访问结果查询
     */
    Page<ApiAccessLog> findByAccessResult(String accessResult, Pageable pageable);
    
    /**
     * 根据访问时间范围查询
     */
    @Query("SELECT a FROM ApiAccessLog a WHERE a.accessTime BETWEEN :startTime AND :endTime")
    Page<ApiAccessLog> findByAccessTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    /**
     * 查询慢查询（执行时间大于指定值）
     */
    Page<ApiAccessLog> findByExecuteTimeGreaterThan(Long executeTime, Pageable pageable);
    
    /**
     * 统计指定时间范围内的访问次数
     */
    @Query("SELECT COUNT(a) FROM ApiAccessLog a WHERE a.accessTime BETWEEN :startTime AND :endTime")
    long countByAccessTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定路径的访问次数
     */
    long countByRequestPath(String requestPath);
    
    /**
     * 统计指定用户的访问次数
     */
    long countByUserId(Long userId);
    
    /**
     * 统计指定IP的访问次数
     */
    long countByIpAddress(String ipAddress);
    
    /**
     * 查询指定时间范围内访问次数最多的路径
     */
    @Query("SELECT a.requestPath, COUNT(a) as count FROM ApiAccessLog a " +
           "WHERE a.accessTime BETWEEN :startTime AND :endTime " +
           "GROUP BY a.requestPath ORDER BY count DESC")
    List<Object[]> findTopRequestPaths(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    /**
     * 查询指定时间范围内的错误率
     */
    @Query("SELECT COUNT(CASE WHEN a.accessResult = 'FAIL' THEN 1 END) * 100.0 / COUNT(a) " +
           "FROM ApiAccessLog a WHERE a.accessTime BETWEEN :startTime AND :endTime")
    Double findErrorRateByAccessTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询指定时间范围内的平均执行时间
     */
    @Query("SELECT AVG(a.executeTime) FROM ApiAccessLog a " +
           "WHERE a.accessTime BETWEEN :startTime AND :endTime AND a.executeTime IS NOT NULL")
    Double findAverageExecuteTimeByAccessTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}