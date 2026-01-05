package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生命周期执行记录实体
 */
@Data
@TableName("lifecycle_execution")
public class LifecycleExecution implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long policyId;
    
    private String executionType;
    
    private Long assetId;
    
    private String assetName;
    
    private String assetType;
    
    private String executionStatus;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long duration;
    
    private Integer affectedCount;
    
    private String errorMessage;
    
    private String executor;
    
    private String executionMode;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
