package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IP白名单实体
 */
@Data
@TableName("ip_whitelist")
public class IpWhitelist {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String ipAddress;
    
    private String ipRange;
    
    private String description;
    
    private String applyTo;
    
    private Long targetId;
    
    private String status;
    
    private String createdBy;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
