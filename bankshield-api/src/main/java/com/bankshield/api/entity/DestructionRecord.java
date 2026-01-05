package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 销毁记录实体
 */
@Data
@TableName("destruction_record")
public class DestructionRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long assetId;
    
    private String assetName;
    
    private String assetType;
    
    private String dataType;
    
    private String sensitivityLevel;
    
    private String destructionMethod;
    
    private String destructionReason;
    
    private String dataSnapshot;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime destructionTime;
    
    private Long policyId;
    
    private String approvalStatus;
    
    private String approver;
    
    private LocalDateTime approvalTime;
    
    private String approvalComment;
    
    private String executor;
    
    private String verificationHash;
    
    private Integer isVerified;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
