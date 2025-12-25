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

/**
 * 审计日志与区块关联实体类
 * 对应数据库表：audit_operation_block
 * 用于存储审计日志与区块的关联关系及Merkle路径
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_operation_block")
public class AuditOperationBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审计日志ID
     */
    @TableField("audit_id")
    private Long auditId;

    /**
     * 区块ID
     */
    @TableField("block_id")
    private Long blockId;

    /**
     * Merkle路径（用于验证完整性）
     */
    @TableField("merkle_path")
    private String merklePath;

    /**
     * 在区块中的索引
     */
    @TableField("index_in_block")
    private Integer indexInBlock;
}