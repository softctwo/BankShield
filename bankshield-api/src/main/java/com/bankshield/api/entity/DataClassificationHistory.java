package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据分级历史实体
 */
@Data
@TableName("data_classification_history")
public class DataClassificationHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long assetId;
    
    private String assetName;
    
    private String oldLevel;
    
    private String newLevel;
    
    private String classificationMethod;
    
    private String reason;
    
    private String operator;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime classifyTime;
}
