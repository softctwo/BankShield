package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 脱敏规则实体
 */
@Data
@TableName("desensitization_rule")
public class DesensitizationRule implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String ruleName;
    
    private String ruleCode;
    
    private String dataType;
    
    private String algorithmType;
    
    private String algorithmConfig;
    
    private String sensitivityLevel;
    
    private String applyScope;
    
    private Integer priority;
    
    private String status;
    
    private String description;
    
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
