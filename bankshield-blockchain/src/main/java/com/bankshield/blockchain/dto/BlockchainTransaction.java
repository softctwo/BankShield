package com.bankshield.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 区块链交易数据传输对象
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 区块哈希
     */
    private String blockHash;

    /**
     * 区块高度
     */
    private Long blockNumber;

    /**
     * 交易发送方地址
     */
    private String fromAddress;

    /**
     * 交易接收方地址
     */
    private String toAddress;

    /**
     * 交易金额（以太坊等需要）
     */
    private String value;

    /**
     * Gas价格（以太坊）
     */
    private String gasPrice;

    /**
     * Gas限制（以太坊）
     */
    private String gasLimit;

    /**
     * 交易数据
     */
    private String data;

    /**
     * 交易签名
     */
    private String signature;

    /**
     * 交易状态：PENDING-待确认, CONFIRMED-已确认, FAILED-失败
     */
    private String status;

    /**
     * 交易时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 确认数
     */
    private Integer confirmations;

    /**
     * 额外参数
     */
    private Map<String, Object> extraData;

    /**
     * 智能合约方法名
     */
    private String contractMethod;

    /**
     * 智能合约参数
     */
    private String contractArgs;
}