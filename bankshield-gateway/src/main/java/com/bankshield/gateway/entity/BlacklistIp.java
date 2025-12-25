package com.bankshield.gateway.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * IP黑名单实体类
 * 
 * @author BankShield
 */
@Data
@Entity
@Table(name = "blacklist_ip", indexes = {
    @Index(name = "idx_ip_address", columnList = "ip_address"),
    @Index(name = "idx_block_status", columnList = "block_status"),
    @Index(name = "idx_unblock_time", columnList = "unblock_time")
})
@EntityListeners(AuditingEntityListener.class)
public class BlacklistIp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;
    
    /**
     * 封禁原因
     */
    @Column(name = "block_reason", columnDefinition = "TEXT")
    private String blockReason;
    
    /**
     * 封禁时间
     */
    @CreatedDate
    @Column(name = "block_time", nullable = false, updatable = false)
    private LocalDateTime blockTime;
    
    /**
     * 解封时间
     */
    @Column(name = "unblock_time")
    private LocalDateTime unblockTime;
    
    /**
     * 封禁状态
     */
    @Column(name = "block_status", nullable = false, length = 20)
    private String blockStatus = BlockStatus.BLOCKED.getValue();
    
    /**
     * 操作人员
     */
    @Column(name = "operator", length = 50)
    private String operator;
    
    /**
     * 版本号
     */
    @Version
    private Long version;
    
    /**
     * 封禁状态枚举
     */
    public enum BlockStatus {
        BLOCKED("BLOCKED"),
        UNBLOCKED("UNBLOCKED"),
        EXPIRED("EXPIRED");
        
        private final String value;
        
        BlockStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}