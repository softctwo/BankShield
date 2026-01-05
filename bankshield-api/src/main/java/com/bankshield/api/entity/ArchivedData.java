package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 归档数据实体
 */
@Data
@TableName("archived_data")
public class ArchivedData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String originalTable;
    
    private Long originalId;
    
    private String dataContent;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime archiveTime;
    
    private String archiveStorage;
    
    private Long policyId;
    
    private String assetType;
    
    private String sensitivityLevel;
    
    private LocalDateTime retentionUntil;
    
    private Integer isDestroyed;
    
    private LocalDateTime destroyTime;
    
    private String createBy;
}
