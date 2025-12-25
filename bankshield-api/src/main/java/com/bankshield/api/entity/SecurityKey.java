package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 安全密钥实体类
 * 对应数据库表：security_key
 *
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("security_key")
public class SecurityKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 密钥名称
     */
    @TableField("key_name")
    private String keyName;

    /**
     * 密钥类型：SM2/SM3/SM4/AES/RSA
     */
    @TableField("key_type")
    private String keyType;

    /**
     * 密钥长度
     */
    @TableField("key_length")
    private Integer keyLength;

    /**
     * 密钥用途：ENCRYPT/DECRYPT/SIGN/VERIFY
     */
    @TableField("key_usage")
    private String keyUsage;

    /**
     * 密钥状态：0-无效, 1-有效, 2-已过期, 3-已吊销
     */
    @TableField("status")
    private Integer status;

    /**
     * 密钥指纹（SHA256）
     */
    @TableField("key_fingerprint")
    private String keyFingerprint;

    /**
     * 密钥材料（加密存储）
     */
    @TableField("key_material")
    private String keyMaterial;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 轮换周期（天）
     */
    @TableField("rotation_cycle")
    private Integer rotationCycle;

    /**
     * 上次轮换时间
     */
    @TableField("last_rotation_time")
    private LocalDateTime lastRotationTime;

    /**
     * 轮换次数
     */
    @TableField("rotation_count")
    private Integer rotationCount;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 关联数据源ID
     */
    @TableField("data_source_id")
    private Long dataSourceId;

    /**
     * 逻辑删除标志
     */
    @TableField("deleted")
    private Integer deleted;
}
