package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 脱敏日志实体
 */
@Data
@TableName("desensitization_log")
public class DesensitizationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String logType;
    
    private Long ruleId;
    
    private String ruleCode;
    
    private Long templateId;
    
    private String templateCode;
    
    private String userId;
    
    private String userName;
    
    private String userRole;
    
    private String targetTable;
    
    private String targetField;
    
    private String originalValueHash;
    
    private String desensitizedValue;
    
    private String algorithmType;
    
    private Integer recordCount;
    
    private Integer executionTime;
    
    private String ipAddress;
    
    private String requestUri;
    
    private String status;
    
    private String errorMessage;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
