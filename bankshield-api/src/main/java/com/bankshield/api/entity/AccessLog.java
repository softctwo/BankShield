package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问日志实体
 */
@Data
@TableName("access_log")
public class AccessLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String userRole;
    
    private String resourceType;
    
    private String resourceId;
    
    private String action;
    
    private String accessResult;
    
    private String policyMatched;
    
    private String ruleMatched;
    
    private String ipAddress;
    
    private String location;
    
    private String userAgent;
    
    private Boolean mfaVerified;
    
    private String denyReason;
    
    private LocalDateTime accessTime;
    
    private Integer responseTime;
}
