package com.bankshield.gateway.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * API路由配置实体类
 * 
 * @author BankShield
 */
@Data
@Entity
@Table(name = "api_route_config")
@EntityListeners(AuditingEntityListener.class)
public class ApiRouteConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 路由ID
     */
    @Column(name = "route_id", nullable = false, unique = true)
    private String routeId;
    
    /**
     * 路由路径
     */
    @Column(name = "route_path", nullable = false)
    private String routePath;
    
    /**
     * 目标服务
     */
    @Column(name = "target_service", nullable = false)
    private String targetService;
    
    /**
     * 是否需要认证
     */
    @Column(name = "require_auth", nullable = false)
    private Boolean requireAuth = false;
    
    /**
     * 所需角色（多个角色用逗号分隔）
     */
    @Column(name = "required_roles")
    private String requiredRoles;
    
    /**
     * 限流阈值（每秒请求数）
     */
    @Column(name = "rate_limit_threshold")
    private Integer rateLimitThreshold;
    
    /**
     * 是否启用签名验证
     */
    @Column(name = "signature_verification_enabled")
    private Boolean signatureVerificationEnabled = false;
    
    /**
     * 签名密钥
     */
    @Column(name = "signature_secret")
    private String signatureSecret;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * 描述
     */
    @Column(name = "description")
    private String description;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * 版本号
     */
    @Version
    private Long version;
}