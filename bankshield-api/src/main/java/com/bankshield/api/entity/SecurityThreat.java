package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全威胁实体
 */
@Data
@TableName("security_threat")
public class SecurityThreat {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String threatType;
    
    private String severity;
    
    private String sourceIp;
    
    private String sourceCountry;
    
    private String targetIp;
    
    private String targetSystem;
    
    private String status;
    
    private String description;
    
    private LocalDateTime detectTime;
    
    private LocalDateTime handleTime;
    
    private String handler;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
