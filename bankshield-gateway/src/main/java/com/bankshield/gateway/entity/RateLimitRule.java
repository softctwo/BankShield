package com.bankshield.gateway.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 限流规则实体类
 * 
 * @author BankShield
 */
@Data
@Entity
@Table(name = "rate_limit_rule", uniqueConstraints = {
    @UniqueConstraint(name = "uk_rule_name", columnNames = {"rule_name"})
})
@EntityListeners(AuditingEntityListener.class)
public class RateLimitRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 规则名称
     */
    @Column(name = "rule_name", nullable = false, unique = true, length = 100)
    private String ruleName;
    
    /**
     * 限流维度
     */
    @Column(name = "limit_dimension", nullable = false, length = 20)
    private String limitDimension;
    
    /**
     * 限流阈值（每秒请求数）
     */
    @Column(name = "limit_threshold", nullable = false)
    private Integer limitThreshold;
    
    /**
     * 限流窗口（秒）
     */
    @Column(name = "limit_window", nullable = false)
    private Integer limitWindow;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
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
    
    /**
     * 限流维度枚举
     */
    public enum LimitDimension {
        IP("IP"),
        USER("USER"),
        API("API"),
        GLOBAL("GLOBAL");
        
        private final String value;
        
        LimitDimension(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}