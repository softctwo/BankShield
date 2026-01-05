package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生命周期策略实体
 */
@Data
@TableName("lifecycle_policy")
public class LifecyclePolicy implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String policyName;
    
    private String policyCode;
    
    private String dataType;
    
    private String sensitivityLevel;
    
    private Integer retentionDays;
    
    private Integer archiveEnabled;
    
    private Integer archiveDays;
    
    private String archiveStorage;
    
    private Integer destroyEnabled;
    
    private Integer destroyDays;
    
    private String destroyMethod;
    
    private Integer approvalRequired;
    
    private Integer notificationEnabled;
    
    private Integer notificationDays;
    
    private String policyStatus;
    
    private Integer priority;
    
    private String description;
    
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    private String updateBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
