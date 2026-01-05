package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合规检查结果实体
 */
@Data
@TableName("compliance_check_result")
public class ComplianceCheckResult {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    
    private Long ruleId;
    
    private String checkStatus;
    
    private String riskLevel;
    
    private String findings;
    
    private String evidence;
    
    private String remediationStatus;
    
    private String assignee;
    
    private LocalDateTime remediationDeadline;
    
    private LocalDateTime remediationTime;
    
    private LocalDateTime checkTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
