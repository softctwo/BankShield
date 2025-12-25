package com.bankshield.encrypt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 加密密钥实体
 */
@Data
@Getter
@Setter
@TableName("encrypt_key")
public class EncryptionKey {
    
    /**
     * 密钥ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 密钥名称
     */
    private String keyName;
    
    /**
     * 密钥类型：SM2/SM3/SM4/AES/RSA
     */
    private String keyType;
    
    /**
     * 密钥长度
     */
    private Integer keyLength;
    
    /**
     * 密钥用途：ENCRYPT/DECRYPT/SIGN/VERIFY
     */
    private String keyUsage;
    
    /**
     * 密钥状态：ACTIVE/INACTIVE/EXPIRED/REVOKED/DESTROYED
     */
    private String keyStatus;
    
    /**
     * 密钥指纹（SHA256）
     */
    private String keyFingerprint;
    
    /**
     * 密钥材料（加密存储）
     */
    private String keyMaterial;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    /**
     * 轮换周期（天）
     */
    private Integer rotationCycle;
    
    /**
     * 上次轮换时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRotationTime;
    
    /**
     * 轮换次数
     */
    private Integer rotationCount;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 关联数据源ID
     */
    private Long dataSourceId;
    
    /**
     * 轮换状态
     */
    private String rotationStatus;
    
    /**
     * 轮换开始时间（新密钥生成时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rotationStartTime;
    
    /**
     * 前向加密密钥ID（用于解密旧数据）
     */
    private Long prevKeyId;
    
    /**
     * 下一密钥ID（已轮换的新密钥）
     */
    private Long nextKeyId;
    
    /**
     * 预计轮换完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rotationCompleteTime;
    
    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}