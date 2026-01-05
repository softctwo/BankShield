package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IP黑名单实体
 */
@Data
@TableName("ip_blacklist")
public class IpBlacklist {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String ipAddress;
    
    private String ipRange;
    
    private String blockReason;
    
    private String blockType;
    
    private String severity;
    
    private String blockedBy;
    
    private LocalDateTime blockedTime;
    
    private LocalDateTime expireTime;
    
    private String status;
    
    private LocalDateTime createdTime;
}
