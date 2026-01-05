package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 个人权利请求实体
 */
@Data
@TableName("personal_rights_request")
public class PersonalRightsRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String requestNo;
    
    private String userId;
    
    private String userName;
    
    private String userPhone;
    
    private String userEmail;
    
    private Integer identityVerified;
    
    private String verificationMethod;
    
    private String requestType;
    
    private String requestReason;
    
    private String requestDetails;
    
    private String requestStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime requestTime;
    
    private LocalDateTime processTime;
    
    private LocalDateTime completeTime;
    
    private String processor;
    
    private String processResult;
    
    private String rejectReason;
    
    private LocalDateTime responseDeadline;
    
    private Integer isOverdue;
    
    private String exportFilePath;
    
    private String exportFormat;
    
    private Integer affectedDataCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
