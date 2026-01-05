package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MFA配置实体
 */
@Data
@TableName("mfa_config")
public class MfaConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String mfaType;
    
    private Boolean mfaEnabled;
    
    private String phone;
    
    private String email;
    
    private String totpSecret;
    
    private String backupCodes;
    
    private LocalDateTime lastVerifiedTime;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
