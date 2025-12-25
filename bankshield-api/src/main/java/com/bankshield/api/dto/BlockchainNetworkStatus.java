package com.bankshield.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 网络是否可用
     */
    private boolean available;
    
    /**
     * 网络类型
     */
    private String networkType;
    
    /**
     * 节点数量
     */
    private int nodeCount;
    
    /**
     * 最新区块高度
     */
    private long latestBlockHeight;
    
    /**
     * 网络延迟（毫秒）
     */
    private long networkLatency;
    
    /**
     * 状态描述
     */
    private String statusDescription;
}