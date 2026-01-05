package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临时权限实体
 */
@Data
@TableName("temporary_permission")
public class TemporaryPermission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String permissionCode;
    
    private String permissionName;
    
    private String resourceType;
    
    private String resourceId;
    
    private String grantedBy;
    
    private String grantReason;
    
    private LocalDateTime validFrom;
    
    private LocalDateTime validTo;
    
    private String status;
    
    private LocalDateTime createdTime;
}
