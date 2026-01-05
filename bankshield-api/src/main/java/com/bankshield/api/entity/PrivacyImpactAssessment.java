package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人信息影响评估实体
 */
@Data
@TableName("privacy_impact_assessment")
public class PrivacyImpactAssessment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String assessmentNo;
    
    private String assessmentName;
    
    private String projectName;
    
    private String projectType;
    
    private String assessmentType;
    
    private String assessmentStatus;
    
    private String riskLevel;
    
    private String dataCategories;
    
    private Long dataVolume;
    
    private String dataSubjects;
    
    private String processingPurpose;
    
    private String processingMethod;
    
    private Integer dataRetentionPeriod;
    
    private Integer thirdPartyInvolved;
    
    private String thirdPartyList;
    
    private Integer crossBorderTransfer;
    
    private String transferCountries;
    
    private String identifiedRisks;
    
    private String riskMitigationMeasures;
    
    private String residualRiskLevel;
    
    private String legalBasis;
    
    private String complianceRequirements;
    
    private String assessor;
    
    private LocalDate assessmentDate;
    
    private String reviewer;
    
    private LocalDate reviewDate;
    
    private String reviewComment;
    
    private LocalDate nextAssessmentDate;
    
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
