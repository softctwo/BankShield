package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 脱敏模板实体
 */
@Data
@TableName("desensitization_template")
public class DesensitizationTemplate implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String templateName;
    
    private String templateCode;
    
    private String templateType;
    
    private String targetTable;
    
    private String targetFields;
    
    private String roleFilter;
    
    private String conditionFilter;
    
    private String status;
    
    private String description;
    
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
