package com.bankshield.blockchain.client;

import com.bankshield.blockchain.dto.AnchorData;
import com.bankshield.blockchain.dto.BlockchainTransaction;
import com.bankshield.blockchain.entity.AnchorRecord;

import java.util.List;

/**
 * 区块链客户端接口
 * 支持多种区块链网络的统一接口
 * 
 * @author BankShield
 */
public interface BlockchainClient {

    /**
     * 发送存证交易
     * 
     * @param data 存证数据
     * @return 交易哈希
     */
    String sendTransaction(AnchorData data);

    /**
     * 查询存证记录
     * 
     * @param recordId 记录ID
     * @return 存证记录
     */
    AnchorRecord queryTransaction(String recordId);

    /**
     * 验证数据完整性
     * 
     * @param recordId 记录ID
     * @param expectedHash 期望的哈希值
     * @return 是否通过验证
     */
    boolean verifyDataIntegrity(String recordId, String expectedHash);

    /**
     * 批量查询交易
     * 
     * @param anchorType 存证类型
     * @param businessId 业务ID
     * @return 交易列表
     */
    List<AnchorRecord> queryTransactions(String anchorType, String businessId);

    /**
     * 获取区块链网络状态
     * 
     * @return 网络状态信息
     */
    BlockchainNetworkStatus getNetworkStatus();

    /**
     * 获取交易详情
     * 
     * @param txHash 交易哈希
     * @return 交易详情
     */
    BlockchainTransaction getTransaction(String txHash);

    /**
     * 等待交易确认
     * 
     * @param txHash 交易哈希
     * @param confirmations 需要的确认数
     * @return 是否确认成功
     */
    boolean waitForConfirmation(String txHash, int confirmations);

    /**
     * 获取当前区块高度
     * 
     * @return 区块高度
     */
    long getCurrentBlockHeight();

    /**
     * 估算交易费用
     * 
     * @param data 存证数据
     * @return 估算的费用
     */
    String estimateTransactionFee(AnchorData data);
}