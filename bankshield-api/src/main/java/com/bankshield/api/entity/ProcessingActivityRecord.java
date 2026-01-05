package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人信息处理活动记录实体
 */
@Data
@TableName("processing_activity_record")
public class ProcessingActivityRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String activityName;
    
    private String activityType;
    
    private String businessPurpose;
    
    private String legalBasis;
    
    private String dataCategories;
    
    private String dataSubjects;
    
    private String dataSources;
    
    private String dataRecipients;
    
    private Integer retentionPeriod;
    
    private String securityMeasures;
    
    private Integer crossBorder;
    
    private String transferMechanism;
    
    private String responsiblePerson;
    
    private String department;
    
    private String systemName;
    
    private String activityStatus;
    
    private Integer piaRequired;
    
    private Long piaId;
    
    private LocalDate lastReviewDate;
    
    private LocalDate nextReviewDate;
    
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
