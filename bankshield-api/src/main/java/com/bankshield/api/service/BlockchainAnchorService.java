package com.bankshield.api.service;

import com.bankshield.api.dto.BlockchainNetworkStatus;
import com.bankshield.api.entity.AuditBlock;

/**
 * 区块链锚定服务接口
 * 提供将审计区块上链的功能
 * 
 * @author BankShield
 */
public interface BlockchainAnchorService {
    
    /**
     * 将审计区块上链
     * 
     * @param block 审计区块
     * @return 区块链交易哈希
     */
    String anchorToBlockchain(AuditBlock block);
    
    /**
     * 验证区块链上的锚定数据
     * 
     * @param block 审计区块
     * @return 验证结果
     */
    boolean verifyBlockchainAnchor(AuditBlock block);
    
    /**
     * 获取区块链网络状态
     * 
     * @return 网络状态
     */
    BlockchainNetworkStatus getNetworkStatus();
}