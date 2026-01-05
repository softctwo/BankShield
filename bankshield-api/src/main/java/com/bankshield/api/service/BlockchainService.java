package com.bankshield.api.service;

import com.bankshield.api.entity.AuditLogBlock;
import java.util.List;
import java.util.Map;

/**
 * 区块链服务接口
 */
public interface BlockchainService {
    
    /**
     * 创建新区块
     * @param logIds 审计日志ID列表
     * @return 新创建的区块
     */
    AuditLogBlock createBlock(List<Long> logIds);
    
    /**
     * 获取最新区块
     * @return 最新区块
     */
    AuditLogBlock getLatestBlock();
    
    /**
     * 根据区块索引获取区块
     * @param blockIndex 区块索引
     * @return 区块
     */
    AuditLogBlock getBlockByIndex(Long blockIndex);
    
    /**
     * 验证单个区块
     * @param blockId 区块ID
     * @return 验证结果
     */
    boolean verifyBlock(Long blockId);
    
    /**
     * 验证整条区块链
     * @return 验证结果
     */
    Map<String, Object> verifyBlockchain();
    
    /**
     * 验证区块范围
     * @param startIndex 起始区块索引
     * @param endIndex 结束区块索引
     * @return 验证结果
     */
    Map<String, Object> verifyBlockchainRange(Long startIndex, Long endIndex);
    
    /**
     * 计算区块哈希
     * @param block 区块
     * @return 哈希值
     */
    String calculateBlockHash(AuditLogBlock block);
    
    /**
     * 获取区块链统计信息
     * @return 统计信息
     */
    Map<String, Object> getBlockchainStatistics();
    
    /**
     * 获取区块列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 区块列表
     */
    Map<String, Object> getBlockList(Integer pageNum, Integer pageSize);
    
    /**
     * 获取区块详情（包含日志数据）
     * @param blockId 区块ID
     * @return 区块详情
     */
    Map<String, Object> getBlockDetail(Long blockId);
}
