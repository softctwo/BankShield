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
 * 审计日志区块实体类
 * 对应数据库表：audit_block
 * 用于实现审计日志的哈希链完整性校验
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_block")
public class AuditBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区块号（递增）
     */
    @TableField("block_number")
    private Long blockNumber;

    /**
     * 前一个区块的哈希
     */
    @TableField("previous_hash")
    private String previousHash;

    /**
     * 当前区块的哈希
     */
    @TableField("current_hash")
    private String currentHash;

    /**
     * Merkle树根哈希
     */
    @TableField("merkle_root")
    private String merkleRoot;

    /**
     * 区块内审计日志数量
     */
    @TableField("audit_count")
    private Long auditCount;

    /**
     * 区块生成时间
     */
    @TableField("block_time")
    private LocalDateTime blockTime;

    /**
     * 区块创建者
     */
    @TableField("operator")
    private String operator;

    /**
     * 区块元数据（JSON格式）
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 状态：0-验证中, 1-已确认, 2-异常
     */
    @TableField("status")
    private Integer status;

    /**
     * 区块链交易哈希（可选）
     */
    @TableField("blockchain_tx_hash")
    private String blockchainTxHash;

    /**
     * 区块链确认时间
     */
    @TableField("blockchain_confirm_time")
    private LocalDateTime blockchainConfirmTime;
}