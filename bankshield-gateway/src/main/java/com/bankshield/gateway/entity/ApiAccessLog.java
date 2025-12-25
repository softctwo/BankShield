package com.bankshield.gateway.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * API访问日志实体类
 * 
 * @author BankShield
 */
@Data
@Entity
@Table(name = "api_access_log", indexes = {
    @Index(name = "idx_request_path", columnList = "request_path"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_access_time", columnList = "access_time"),
    @Index(name = "idx_ip_address", columnList = "ip_address")
})
@EntityListeners(AuditingEntityListener.class)
public class ApiAccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 请求路径
     */
    @Column(name = "request_path", nullable = false, length = 500)
    private String requestPath;
    
    /**
     * 请求方法
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    /**
     * 请求参数
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;
    
    /**
     * 请求头
     */
    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;
    
    /**
     * 响应状态码
     */
    @Column(name = "response_status")
    private Integer responseStatus;
    
    /**
     * 响应内容
     */
    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;
    
    /**
     * 执行时间（毫秒）
     */
    @Column(name = "execute_time")
    private Long executeTime;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 访问时间
     */
    @CreatedDate
    @Column(name = "access_time", nullable = false, updatable = false)
    private LocalDateTime accessTime;
    
    /**
     * 访问结果
     */
    @Column(name = "access_result", length = 20)
    private String accessResult;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 请求体大小
     */
    @Column(name = "request_body_size")
    private Long requestBodySize;
    
    /**
     * 响应体大小
     */
    @Column(name = "response_body_size")
    private Long responseBodySize;
}