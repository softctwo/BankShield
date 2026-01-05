package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告知同意记录实体
 */
@Data
@TableName("consent_record")
public class ConsentRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String userId;
    
    private String userName;
    
    private String userPhone;
    
    private String userEmail;
    
    private String consentType;
    
    private String consentPurpose;
    
    private String dataCategories;
    
    private String consentMethod;
    
    private String consentStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime consentTime;
    
    private LocalDateTime revokeTime;
    
    private LocalDateTime expireTime;
    
    private String consentContent;
    
    private String consentVersion;
    
    private String ipAddress;
    
    private String deviceInfo;
    
    private String consentEvidence;
    
    private String thirdPartyName;
    
    private String thirdPartyPurpose;
    
    private Integer isSensitive;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
