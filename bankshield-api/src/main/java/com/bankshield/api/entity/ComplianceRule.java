package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合规规则实体
 */
@Data
@TableName("compliance_rule")
public class ComplianceRule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String ruleCode;
    
    private String ruleName;
    
    private String category;
    
    private String standard;
    
    private String description;
    
    private String checkType;
    
    private String checkScript;
    
    private String severity;
    
    private Integer weight;
    
    private String status;
    
    private String remediationGuide;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
}
