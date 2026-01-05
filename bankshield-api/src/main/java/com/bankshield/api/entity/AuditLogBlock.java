package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志区块实体
 */
@Data
@TableName("audit_log_block")
public class AuditLogBlock implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long blockIndex;
    
    private String blockHash;
    
    private String previousHash;
    
    private Long timestamp;
    
    private Long nonce;
    
    private String merkleRoot;
    
    private Integer logCount;
    
    private Integer blockSize;
    
    private String miner;
    
    private String signature;
    
    private Integer isValid;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
