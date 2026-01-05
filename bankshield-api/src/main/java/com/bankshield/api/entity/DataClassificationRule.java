package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据分级规则实体
 */
@Data
@TableName("data_classification_rule")
public class DataClassificationRule implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String ruleName;
    
    private String ruleCode;
    
    private String dataType;
    
    private String fieldPattern;
    
    private String contentPattern;
    
    private String sensitivityLevel;
    
    private Integer priority;
    
    private String ruleStatus;
    
    private String description;
    
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    private String updateBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
