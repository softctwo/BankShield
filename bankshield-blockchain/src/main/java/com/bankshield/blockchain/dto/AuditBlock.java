package com.bankshield.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计区块数据传输对象
 *
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区块ID
     */
    private String blockId;

    /**
     * 区块哈希
     */
    private String blockHash;

    /**
     * 区块高度
     */
    private Long blockNumber;

    /**
     * 交易数量
     */
    private Integer transactionCount;

    /**
     * Merkle根
     */
    private String merkleRoot;

    /**
     * 区块创建时间
     */
    private LocalDateTime createTime;

    /**
     * 区块确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 前一个区块哈希
     */
    private String previousHash;

    /**
     * 区块状态：CREATED-已创建, CONFIRMED-已确认, FINALIZED-已最终确认
     */
    private String status;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 验证者列表
     */
    private String validators;

    /**
     * 区块数据哈希
     */
    private String dataHash;

    /**
     * 区块大小（字节）
     */
    private Long blockSize;

    /**
     * 额外参数
     */
    private Map<String, Object> extraData;

    /**
     * 区块版本
     */
    private String version;

    /**
     * 共识算法
     */
    private String consensusAlgorithm;

    /**
     * 区块奖励
     */
    private String blockReward;

    /**
     * Gas使用量
     */
    private Long gasUsed;

    /**
     * Gas限制
     */
    private Long gasLimit;
}
