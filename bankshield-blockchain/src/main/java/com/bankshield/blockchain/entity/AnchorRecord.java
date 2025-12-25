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
 * 区块链存证记录实体类
 * 对应数据库表：blockchain_anchor_record
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("blockchain_anchor_record")
public class AnchorRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 存证记录ID（业务系统唯一标识）
     */
    @TableField("record_id")
    private String recordId;

    /**
     * 存证类型：AUDIT_LOG-审计日志, KEY_EVENT-密钥事件, COMPLIANCE-合规检查
     */
    @TableField("anchor_type")
    private String anchorType;

    /**
     * 业务数据ID
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 业务数据类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 内容哈希（用于完整性验证）
     */
    @TableField("content_hash")
    private String contentHash;

    /**
     * 默克尔根哈希（批量存证时使用）
     */
    @TableField("merkle_root")
    private String merkleRoot;

    /**
     * 交易哈希（区块链交易ID）
     */
    @TableField("tx_hash")
    private String txHash;

    /**
     * 区块高度
     */
    @TableField("block_number")
    private Long blockNumber;

    /**
     * 区块哈希
     */
    @TableField("block_hash")
    private String blockHash;

    /**
     * 上链时间
     */
    @TableField("anchor_time")
    private LocalDateTime anchorTime;

    /**
     * 上链状态：PENDING-待确认, CONFIRMED-已确认, FAILED-失败
     */
    @TableField("anchor_status")
    private String anchorStatus;

    /**
     * 区块链网络类型：FABRIC-超级账本, ETHEREUM-以太坊, FISCO_BCOS
     */
    @TableField("blockchain_type")
    private String blockchainType;

    /**
     * 智能合约地址
     */
    @TableField("contract_address")
    private String contractAddress;

    /**
     * 通道名称（Fabric）
     */
    @TableField("channel_name")
    private String channelName;

    /**
     * 原始数据内容（JSON格式，可选存储）
     */
    @TableField("original_data")
    private String originalData;

    /**
     * 存证证书编号
     */
    @TableField("certificate_no")
    private String certificateNo;

    /**
     * 验证结果：VERIFIED-已验证, TAMPERED-被篡改
     */
    @TableField("verify_result")
    private String verifyResult;

    /**
     * 验证时间
     */
    @TableField("verify_time")
    private LocalDateTime verifyTime;

    /**
     * 创建者
     */
    @TableField("creator")
    private String creator;

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