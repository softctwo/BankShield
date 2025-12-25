package com.bankshield.blockchain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 密钥生命周期事件实体类
 * 对应数据库表：blockchain_key_event
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blockchain_key_event")
public class KeyChainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 密钥ID
     */
    @TableField("key_id")
    private Long keyId;

    /**
     * 密钥名称
     */
    @TableField("key_name")
    private String keyName;

    /**
     * 密钥类型：SM2, SM4, AES, RSA
     */
    @TableField("key_type")
    private String keyType;

    /**
     * 事件类型：GENERATE-生成, ROTATE-轮换, DISABLE-禁用, DESTROY-销毁
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 事件时间
     */
    @TableField("event_time")
    private LocalDateTime eventTime;

    /**
     * 操作者
     */
    @TableField("operator")
    private String operator;

    /**
     * 操作者ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 事件描述
     */
    @TableField("event_description")
    private String eventDescription;

    /**
     * 密钥指纹（哈希值）
     */
    @TableField("key_fingerprint")
    private String keyFingerprint;

    /**
     * 密钥状态变更前
     */
    @TableField("status_before")
    private String statusBefore;

    /**
     * 密钥状态变更后
     */
    @TableField("status_after")
    private String statusAfter;

    /**
     * 关联的存证记录ID
     */
    @TableField("anchor_record_id")
    private String anchorRecordId;

    /**
     * 区块链交易哈希
     */
    @TableField("blockchain_tx_hash")
    private String blockchainTxHash;

    /**
     * 事件哈希（用于完整性验证）
     */
    @TableField("event_hash")
    private String eventHash;

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
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}