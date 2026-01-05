package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问规则实体
 */
@Data
@TableName("access_rule")
public class AccessRule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long policyId;
    
    private String ruleCode;
    
    private String ruleName;
    
    private String ruleType;
    
    private String subjectCondition;
    
    private String resourceCondition;
    
    private String actionCondition;
    
    private String environmentCondition;
    
    private Boolean mfaRequired;
    
    private Integer priority;
    
    private String status;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
