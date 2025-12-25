package com.bankshield.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存证数据传输对象
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存证记录ID
     */
    private String recordId;

    /**
     * 存证类型：AUDIT_LOG-审计日志, KEY_EVENT-密钥事件, COMPLIANCE-合规检查
     */
    private String anchorType;

    /**
     * 业务数据ID
     */
    private String businessId;

    /**
     * 业务数据类型
     */
    private String businessType;

    /**
     * 内容哈希（用于完整性验证）
     */
    private String contentHash;

    /**
     * 原始数据内容（JSON格式）
     */
    private String originalData;

    /**
     * 元数据（可选）
     */
    private String metadata;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 区块链网络类型：FABRIC, ETHEREUM, FISCO_BCOS
     */
    private String blockchainType;

    /**
     * 智能合约地址
     */
    private String contractAddress;

    /**
     * 通道名称（Fabric）
     */
    private String channelName;
}