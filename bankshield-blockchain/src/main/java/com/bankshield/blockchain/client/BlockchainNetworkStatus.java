package com.bankshield.blockchain.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 区块链网络状态
 * 
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainNetworkStatus {

    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 连接状态
     */
    private boolean connected;

    /**
     * 当前区块高度
     */
    private long blockHeight;

    /**
     * 最新区块哈希
     */
    private String latestBlockHash;

    /**
     * 最新区块时间
     */
    private LocalDateTime latestBlockTime;

    /**
     * 节点数量
     */
    private int peerCount;

    /**
     * 待确认交易数
     */
    private int pendingTransactions;

    /**
     * 网络延迟（毫秒）
     */
    private long networkDelay;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 状态检查时间
     */
    private LocalDateTime checkTime;
}