package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问策略实体
 */
@Data
@TableName("access_policy")
public class AccessPolicy {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String policyCode;
    
    private String policyName;
    
    private String policyType;
    
    private String description;
    
    private Integer priority;
    
    private String effect;
    
    private String status;
    
    private String createdBy;
    
    private LocalDateTime createdTime;
    
    private String updatedBy;
    
    private LocalDateTime updatedTime;
}
