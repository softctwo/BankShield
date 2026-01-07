package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.AuditLogBlock;
import com.bankshield.api.mapper.AuditLogBlockMapper;
import com.bankshield.api.service.BlockchainService;
import com.bankshield.api.util.SM3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 区块链服务实现
 */
@Slf4j
@Service
public class BlockchainServiceImpl implements BlockchainService {
    
    @Autowired
    private AuditLogBlockMapper blockMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogBlock createBlock(List<Long> logIds) {
        if (logIds == null || logIds.isEmpty()) {
            throw new IllegalArgumentException("日志ID列表不能为空");
        }
        
        // 获取最新区块
        AuditLogBlock latestBlock = getLatestBlock();
        if (latestBlock == null) {
            throw new RuntimeException("创世区块不存在");
        }
        
        // 创建新区块
        AuditLogBlock newBlock = new AuditLogBlock();
        newBlock.setBlockIndex(latestBlock.getBlockIndex() + 1);
        newBlock.setPreviousHash(latestBlock.getBlockHash());
        newBlock.setTimestamp(System.currentTimeMillis());
        newBlock.setLogCount(logIds.size());
        newBlock.setMiner("system");
        newBlock.setNonce(0L);
        
        // 计算Merkle树根（简化处理，实际应该从日志数据计算）
        String[] logHashes = new String[logIds.size()];
        for (int i = 0; i < logIds.size(); i++) {
            logHashes[i] = SM3Util.hash(String.valueOf(logIds.get(i)));
        }
        String merkleRoot = SM3Util.calculateMerkleRoot(logHashes);
        newBlock.setMerkleRoot(merkleRoot);
        
        // 计算区块哈希
        String blockHash = calculateBlockHash(newBlock);
        newBlock.setBlockHash(blockHash);
        
        // 生成签名（简化处理）
        String signature = SM3Util.hash(blockHash + "SIGNATURE_KEY");
        newBlock.setSignature(signature);
        
        newBlock.setIsValid(1);
        newBlock.setCreateTime(LocalDateTime.now());
        
        // 保存区块
        blockMapper.insert(newBlock);
        
        log.info("创建新区块成功，区块索引: {}, 区块哈希: {}", newBlock.getBlockIndex(), newBlock.getBlockHash());
        
        return newBlock;
    }
    
    @Override
    public AuditLogBlock getLatestBlock() {
        return blockMapper.selectLatestBlock();
    }
    
    @Override
    public AuditLogBlock getBlockByIndex(Long blockIndex) {
        return blockMapper.selectByBlockIndex(blockIndex);
    }
    
    @Override
    public boolean verifyBlock(Long blockId) {
        AuditLogBlock block = blockMapper.selectById(blockId);
        if (block == null) {
            log.error("区块不存在: {}", blockId);
            return false;
        }
        
        // 验证区块哈希
        String calculatedHash = calculateBlockHash(block);
        if (!calculatedHash.equals(block.getBlockHash())) {
            log.error("区块哈希验证失败，区块ID: {}", blockId);
            return false;
        }
        
        // 验证前置哈希（跳过创世区块）
        if (block.getBlockIndex() > 0) {
            AuditLogBlock previousBlock = getBlockByIndex(block.getBlockIndex() - 1);
            if (previousBlock == null) {
                log.error("前一个区块不存在，区块索引: {}", block.getBlockIndex() - 1);
                return false;
            }
            
            if (!block.getPreviousHash().equals(previousBlock.getBlockHash())) {
                log.error("前置哈希验证失败，区块ID: {}", blockId);
                return false;
            }
        }
        
        log.info("区块验证通过，区块ID: {}", blockId);
        return true;
    }
    
    @Override
    public Map<String, Object> verifyBlockchain() {
        Map<String, Object> result = new HashMap<>();
        
        long startTime = System.currentTimeMillis();
        
        // 获取所有区块
        List<AuditLogBlock> blocks = blockMapper.selectList(
            new LambdaQueryWrapper<AuditLogBlock>()
                .orderByAsc(AuditLogBlock::getBlockIndex)
        );
        
        if (blocks.isEmpty()) {
            result.put("success", false);
            result.put("message", "区块链为空");
            return result;
        }
        
        int totalBlocks = blocks.size();
        int validBlocks = 0;
        int invalidBlocks = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        
        // 验证每个区块
        for (int i = 0; i < blocks.size(); i++) {
            AuditLogBlock block = blocks.get(i);
            
            // 验证区块哈希
            String calculatedHash = calculateBlockHash(block);
            if (!calculatedHash.equals(block.getBlockHash())) {
                invalidBlocks++;
                Map<String, Object> error = new HashMap<>();
                error.put("blockIndex", block.getBlockIndex());
                error.put("blockId", block.getId());
                error.put("error", "区块哈希不匹配");
                errors.add(error);
                continue;
            }
            
            // 验证前置哈希（跳过创世区块）
            if (i > 0) {
                AuditLogBlock previousBlock = blocks.get(i - 1);
                if (!block.getPreviousHash().equals(previousBlock.getBlockHash())) {
                    invalidBlocks++;
                    Map<String, Object> error = new HashMap<>();
                    error.put("blockIndex", block.getBlockIndex());
                    error.put("blockId", block.getId());
                    error.put("error", "前置哈希不匹配");
                    errors.add(error);
                    continue;
                }
            }
            
            validBlocks++;
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        result.put("success", invalidBlocks == 0);
        result.put("totalBlocks", totalBlocks);
        result.put("validBlocks", validBlocks);
        result.put("invalidBlocks", invalidBlocks);
        result.put("errors", errors);
        result.put("duration", duration);
        result.put("message", invalidBlocks == 0 ? "区块链验证通过" : "区块链验证失败，发现 " + invalidBlocks + " 个无效区块");
        
        log.info("区块链验证完成，总区块数: {}, 有效: {}, 无效: {}, 耗时: {}ms", 
            totalBlocks, validBlocks, invalidBlocks, duration);
        
        return result;
    }
    
    @Override
    public Map<String, Object> verifyBlockchainRange(Long startIndex, Long endIndex) {
        Map<String, Object> result = new HashMap<>();
        
        if (startIndex > endIndex) {
            result.put("success", false);
            result.put("message", "起始索引不能大于结束索引");
            return result;
        }
        
        long startTime = System.currentTimeMillis();
        
        // 获取指定范围的区块
        List<AuditLogBlock> blocks = blockMapper.selectList(
            new LambdaQueryWrapper<AuditLogBlock>()
                .ge(AuditLogBlock::getBlockIndex, startIndex)
                .le(AuditLogBlock::getBlockIndex, endIndex)
                .orderByAsc(AuditLogBlock::getBlockIndex)
        );
        
        int totalBlocks = blocks.size();
        int validBlocks = 0;
        int invalidBlocks = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        
        // 验证每个区块
        for (int i = 0; i < blocks.size(); i++) {
            AuditLogBlock block = blocks.get(i);
            
            // 验证区块哈希
            String calculatedHash = calculateBlockHash(block);
            if (!calculatedHash.equals(block.getBlockHash())) {
                invalidBlocks++;
                Map<String, Object> error = new HashMap<>();
                error.put("blockIndex", block.getBlockIndex());
                error.put("error", "区块哈希不匹配");
                errors.add(error);
                continue;
            }
            
            // 验证前置哈希
            if (i > 0) {
                AuditLogBlock previousBlock = blocks.get(i - 1);
                if (!block.getPreviousHash().equals(previousBlock.getBlockHash())) {
                    invalidBlocks++;
                    Map<String, Object> error = new HashMap<>();
                    error.put("blockIndex", block.getBlockIndex());
                    error.put("error", "前置哈希不匹配");
                    errors.add(error);
                    continue;
                }
            } else if (block.getBlockIndex() > 0) {
                // 如果是范围的第一个区块但不是创世区块，需要验证与前一个区块的连接
                AuditLogBlock previousBlock = getBlockByIndex(block.getBlockIndex() - 1);
                if (previousBlock != null && !block.getPreviousHash().equals(previousBlock.getBlockHash())) {
                    invalidBlocks++;
                    Map<String, Object> error = new HashMap<>();
                    error.put("blockIndex", block.getBlockIndex());
                    error.put("error", "前置哈希不匹配");
                    errors.add(error);
                    continue;
                }
            }
            
            validBlocks++;
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        result.put("success", invalidBlocks == 0);
        result.put("startIndex", startIndex);
        result.put("endIndex", endIndex);
        result.put("totalBlocks", totalBlocks);
        result.put("validBlocks", validBlocks);
        result.put("invalidBlocks", invalidBlocks);
        result.put("errors", errors);
        result.put("duration", duration);
        
        return result;
    }
    
    @Override
    public String calculateBlockHash(AuditLogBlock block) {
        // 计算区块哈希：blockIndex + previousHash + timestamp + merkleRoot + nonce
        String data = String.valueOf(block.getBlockIndex()) +
                     (block.getPreviousHash() != null ? block.getPreviousHash() : "") +
                     String.valueOf(block.getTimestamp()) +
                     (block.getMerkleRoot() != null ? block.getMerkleRoot() : "") +
                     String.valueOf(block.getNonce());
        
        return SM3Util.hash(data);
    }
    
    @Override
    public Map<String, Object> getBlockchainStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取最新区块
        AuditLogBlock latestBlock = getLatestBlock();
        
        // 统计信息
        Long chainHeight = blockMapper.selectChainHeight();
        Long totalBlocks = blockMapper.selectCount(null);
        Long validBlocks = blockMapper.countValidBlocks();
        Long invalidBlocks = blockMapper.countInvalidBlocks();
        
        // 计算总日志数
        Long totalLogs = blockMapper.selectList(null).stream()
            .mapToLong(block -> block.getLogCount() != null ? block.getLogCount() : 0)
            .sum();
        
        stats.put("chainHeight", chainHeight);
        stats.put("totalBlocks", totalBlocks);
        stats.put("validBlocks", validBlocks);
        stats.put("invalidBlocks", invalidBlocks);
        stats.put("totalLogs", totalLogs);
        
        if (latestBlock != null) {
            stats.put("latestBlockIndex", latestBlock.getBlockIndex());
            stats.put("latestBlockHash", latestBlock.getBlockHash());
            stats.put("latestBlockTime", latestBlock.getTimestamp());
        }
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getBlockList(Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        Page<AuditLogBlock> page = new Page<>(pageNum, pageSize);
        Page<AuditLogBlock> blockPage = blockMapper.selectPage(page, 
            new LambdaQueryWrapper<AuditLogBlock>()
                .orderByDesc(AuditLogBlock::getBlockIndex)
        );
        
        result.put("total", blockPage.getTotal());
        result.put("pageNum", blockPage.getCurrent());
        result.put("pageSize", blockPage.getSize());
        result.put("records", blockPage.getRecords());
        
        return result;
    }
    
    @Override
    public Map<String, Object> getBlockDetail(Long blockId) {
        Map<String, Object> detail = new HashMap<>();
        
        AuditLogBlock block = blockMapper.selectById(blockId);
        if (block == null) {
            detail.put("success", false);
            detail.put("message", "区块不存在");
            return detail;
        }
        
        detail.put("success", true);
        detail.put("block", block);
        
        // 验证区块
        boolean isValid = verifyBlock(blockId);
        detail.put("verified", isValid);
        
        // 获取前一个区块
        if (block.getBlockIndex() > 0) {
            AuditLogBlock previousBlock = getBlockByIndex(block.getBlockIndex() - 1);
            detail.put("previousBlock", previousBlock);
        }
        
        // 获取下一个区块
        AuditLogBlock nextBlock = getBlockByIndex(block.getBlockIndex() + 1);
        if (nextBlock != null) {
            detail.put("nextBlock", nextBlock);
        }
        
        return detail;
    }
    
    @Override
    public Map<String, Object> getBlockByTransactionId(String transactionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "功能开发中");
        return result;
    }
    
    @Override
    public Map<String, Object> getBrowserOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("statistics", getBlockchainStatistics());
        overview.put("recentBlocks", getBlockList(1, 10));
        return overview;
    }
    
    @Override
    public Map<String, Object> generateCertificate(String blockId, String transactionId, String certificateType) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "功能开发中");
        return result;
    }
    
    @Override
    public Map<String, Object> verifyCertificate(String certificateCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "功能开发中");
        return result;
    }
    
    @Override
    public Map<String, Object> getBlockchainHealth() {
        Map<String, Object> health = new HashMap<>();
        try {
            Map<String, Object> stats = getBlockchainStatistics();
            health.put("status", "healthy");
            health.put("chainHeight", stats.get("chainHeight"));
            health.put("totalBlocks", stats.get("totalBlocks"));
            health.put("validBlocks", stats.get("validBlocks"));
            health.put("invalidBlocks", stats.get("invalidBlocks"));
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
        }
        return health;
    }
    
    @Override
    public Map<String, Object> searchBlockchain(String keyword, String searchType) {
        Map<String, Object> result = new HashMap<>();
        List<AuditLogBlock> blocks = new ArrayList<>();
        
        try {
            LambdaQueryWrapper<AuditLogBlock> wrapper = new LambdaQueryWrapper<>();
            
            if ("hash".equals(searchType)) {
                wrapper.eq(AuditLogBlock::getBlockHash, keyword);
            } else if ("index".equals(searchType)) {
                wrapper.eq(AuditLogBlock::getBlockIndex, Long.parseLong(keyword));
            } else {
                wrapper.like(AuditLogBlock::getBlockHash, keyword)
                       .or()
                       .like(AuditLogBlock::getMerkleRoot, keyword);
            }
            
            blocks = blockMapper.selectList(wrapper);
            result.put("success", true);
            result.put("data", blocks);
            result.put("total", blocks.size());
        } catch (Exception e) {
            log.error("搜索区块链数据失败", e);
            result.put("success", false);
            result.put("message", "搜索失败: " + e.getMessage());
        }
        
        return result;
    }
}
