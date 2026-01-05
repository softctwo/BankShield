package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合规检查任务实体
 */
@Data
@TableName("compliance_check_task")
public class ComplianceCheckTask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskName;
    
    private String taskType;
    
    private String standard;
    
    private String targetSystem;
    
    private String status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer totalRules;
    
    private Integer passedRules;
    
    private Integer failedRules;
    
    private Integer complianceScore;
    
    private String executor;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
}
