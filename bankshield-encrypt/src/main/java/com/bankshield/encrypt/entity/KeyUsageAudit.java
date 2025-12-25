package com.bankshield.encrypt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密钥使用审计实体
 */
@Data
@TableName("key_usage_audit")
public class KeyUsageAudit {
    
    /**
     * 审计ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 密钥ID
     */
    private Long keyId;
    
    /**
     * 操作类型：ENCRYPT/DECRYPT/SIGN/VERIFY
     */
    private String operationType;
    
    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
    
    /**
     * 操作人员
     */
    private String operator;
    
    /**
     * 操作结果：SUCCESS/FAILURE
     */
    private String operationResult;
    
    /**
     * 加密数据量（字节）
     */
    private Long dataSize;
    
    /**
     * 数据源ID
     */
    private Long dataSourceId;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}