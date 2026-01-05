package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合规报告实体
 */
@Data
@TableName("compliance_report")
public class ComplianceReport {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    
    private String reportName;
    
    private String reportType;
    
    private String standard;
    
    private Integer complianceScore;
    
    private Integer totalRules;
    
    private Integer passedRules;
    
    private Integer failedRules;
    
    private Integer criticalRisks;
    
    private Integer highRisks;
    
    private Integer mediumRisks;
    
    private Integer lowRisks;
    
    private String summary;
    
    private String recommendations;
    
    private String filePath;
    
    private String status;
    
    private LocalDateTime generateTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private String createBy;
    
    private String updateBy;
}
