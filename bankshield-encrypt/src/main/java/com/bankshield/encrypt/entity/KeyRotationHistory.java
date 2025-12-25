package com.bankshield.encrypt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密钥轮换历史实体
 */
@Data
@TableName("key_rotation_history")
public class KeyRotationHistory {
    
    /**
     * 轮换ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 旧密钥ID
     */
    private Long oldKeyId;
    
    /**
     * 新密钥ID
     */
    private Long newKeyId;
    
    /**
     * 轮换时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rotationTime;
    
    /**
     * 轮换原因
     */
    private String rotationReason;
    
    /**
     * 操作人员
     */
    private String rotatedBy;
    
    /**
     * 轮换状态：SUCCESS/FAILURE
     */
    private String rotationStatus;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}