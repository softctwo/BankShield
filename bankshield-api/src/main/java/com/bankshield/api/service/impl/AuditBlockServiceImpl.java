package com.bankshield.api.service.impl;

import com.bankshield.api.dto.AuditIntegrityReport;
import com.bankshield.api.dto.AuditIntegrityStatistics;
import com.bankshield.api.entity.AuditBlock;
import com.bankshield.api.entity.AuditOperationBlock;
import com.bankshield.api.entity.OperationAudit;
import com.bankshield.api.mapper.AuditBlockMapper;
import com.bankshield.api.mapper.AuditOperationBlockMapper;
import com.bankshield.api.mapper.OperationAuditMapper;
import com.bankshield.api.service.AuditBlockService;
// import com.bankshield.api.service.AlertService;  // 暂时注释掉，因为AlertService不存在
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计区块服务实现类
 * 实现审计日志完整性校验功能
 * 
 * @author BankShield
 */
@Service
@Slf4j
public class AuditBlockServiceImpl implements AuditBlockService {
    
    @Autowired
    private AuditBlockMapper blockMapper;
    
    @Autowired
    private AuditOperationBlockMapper operationBlockMapper;
    
    @Autowired
    private OperationAuditMapper auditMapper;
    
    // @Autowired
    // private AlertService alertService;  // 暂时注释掉
    
    // 区块大小：每1000条审计日志生成一个区块
    @Value("${audit.block.size:1000}")
    private int blockSize;
    
    // 区块生成间隔：每5分钟检查一次
    @Value("${audit.block.interval-minutes:5}")
    private int blockIntervalMinutes;
    
    /**
     * 生成新的审计区块
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditBlock generateBlock() {
        // 1. 查询未上链的审计日志
        List<OperationAudit> pendingAudits = auditMapper.selectPendingAudits(blockSize);
        
        if (CollectionUtils.isEmpty(pendingAudits)) {
            log.info("没有待处理的审计日志");
            return null;
        }
        
        // 2. 计算Merkle树
        List<String> auditHashes = pendingAudits.stream()
            .map(this::calculateAuditHash)
            .collect(Collectors.toList());
        
        String merkleRoot = calculateMerkleRoot(auditHashes);
        
        // 3. 计算区块哈希
        AuditBlock lastBlock = blockMapper.selectLastBlock();
        String previousHash = lastBlock != null ? lastBlock.getCurrentHash() : "0";
        
        Long blockNumber = lastBlock != null ? lastBlock.getBlockNumber() + 1L : 1L;
        
        AuditBlock block = new AuditBlock();
        block.setBlockNumber(blockNumber);
        block.setPreviousHash(previousHash);
        block.setMerkleRoot(merkleRoot);
        block.setAuditCount((long) pendingAudits.size());
        block.setBlockTime(LocalDateTime.now());
        block.setOperator(getCurrentUsername());
        block.setStatus(1);
        
        // 计算当前区块哈希
        String currentHash = calculateBlockHash(block);
        block.setCurrentHash(currentHash);
        
        // 4. 保存区块
        blockMapper.insert(block);
        
        // 5. 建立审计日志与区块的关联
        for (int i = 0; i < pendingAudits.size(); i++) {
            OperationAudit audit = pendingAudits.get(i);
            
            AuditOperationBlock relation = new AuditOperationBlock();
            relation.setAuditId(audit.getId());
            relation.setBlockId(block.getId());
            relation.setMerklePath(getMerklePath(auditHashes, i));
            relation.setIndexInBlock(i);
            
            operationBlockMapper.insert(relation);
            
            // 更新审计日志的区块ID
            audit.setBlockId(block.getId());
            auditMapper.updateById(audit);
        }
        
        log.info("成功生成审计区块: #{}, 包含 {} 条审计日志", blockNumber, pendingAudits.size());
        
        return block;
    }
    
    /**
     * 验证审计日志完整性
     */
    @Override
    public boolean verifyAuditIntegrity(Long auditId) {
        OperationAudit audit = auditMapper.selectById(auditId);
        if (audit == null || audit.getBlockId() == null) {
            log.warn("审计日志 {} 未上链或不存在", auditId);
            return false;
        }
        
        // 1. 获取关联的区块
        AuditBlock block = blockMapper.selectById(audit.getBlockId());
        if (block == null) {
            log.error("审计日志 {} 关联的区块 {} 不存在", auditId, audit.getBlockId());
            return false;
        }
        
        // 2. 重新计算审计日志哈希
        String calculatedHash = calculateAuditHash(audit);
        
        // 3. 验证Merkle路径
        AuditOperationBlock relation = operationBlockMapper.selectOne(
            new QueryWrapper<AuditOperationBlock>()
                .eq("audit_id", auditId)
                .eq("block_id", block.getId())
        );
        
        if (relation == null) {
            log.error("无法找到审计日志 {} 的区块关联", auditId);
            return false;
        }
        
        // 4. 验证Merkle根
        boolean isValid = verifyMerklePath(
            calculatedHash,
            relation.getMerklePath(),
            relation.getIndexInBlock(),
            block.getMerkleRoot()
        );
        
        if (!isValid) {
            log.error("审计日志 {} 的Merkle验证失败，数据可能被篡改", auditId);
            // 发送告警 - 暂时注释掉，因为AlertService不存在
            // alertService.sendAlert("审计日志篡改检测", "审计日志 " + auditId + " 验证失败");
        }
        
        return isValid;
    }
    
    /**
     * 验证整个审计日志系统的完整性
     */
    @Override
    public AuditIntegrityReport verifySystemIntegrity() {
        AuditIntegrityReport report = new AuditIntegrityReport();
        List<String> issues = new ArrayList<>();
        
        // 获取统计数据
        AuditIntegrityStatistics statistics = getIntegrityStatistics();
        report.setTotalAudits(statistics.getTotalAudits());
        report.setAnchoredAudits(statistics.getAnchoredAudits());
        report.setAnchoringRate(statistics.getAnchoringRate());
        report.setTotalBlocks(statistics.getTotalBlocks());
        
        // 1. 检查上链率
        if (statistics.getAnchoringRate() < 90) {
            issues.add("审计日志上链率低于90%，当前为 " + String.format("%.2f%%", statistics.getAnchoringRate()));
        }
        
        // 2. 检查最新区块的时间
        AuditBlock latestBlock = blockMapper.selectLastBlock();
        if (latestBlock != null) {
            LocalDateTime blockTime = latestBlock.getBlockTime();
            LocalDateTime now = LocalDateTime.now();
            if (blockTime.isBefore(now.minusHours(1))) {
                issues.add("最新区块生成时间超过1小时，可能存在区块生成异常");
            }
        }
        
        // 3. 验证所有已上链的审计日志
        List<OperationAudit> allAudits = auditMapper.selectList(
            new QueryWrapper<OperationAudit>().isNotNull("block_id")
        );
        
        int invalidCount = 0;
        for (OperationAudit audit : allAudits) {
            if (!verifyAuditIntegrity(audit.getId())) {
                invalidCount++;
            }
        }
        
        if (invalidCount > 0) {
            issues.add("发现 " + invalidCount + " 条完整性验证失败的审计日志");
        }
        
        report.setIntegrityIssues(issues);
        report.setVerificationResult(issues.isEmpty() ? 1 : 0);
        
        return report;
    }
    
    /**
     * 分页查询审计区块
     */
    @Override
    public Page<AuditBlock> listBlocks(int page, int size) {
        Page<AuditBlock> pageParam = new Page<>(page, size);
        QueryWrapper<AuditBlock> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("block_number");
        return blockMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取审计完整性统计数据
     */
    @Override
    public AuditIntegrityStatistics getIntegrityStatistics() {
        AuditIntegrityStatistics statistics = new AuditIntegrityStatistics();
        
        // 获取统计数据
        Long totalAudits = blockMapper.countTotalAudits();
        Long anchoredAudits = blockMapper.countAnchoredAudits();
        Long totalBlocks = blockMapper.selectCount(null);
        
        statistics.setTotalAudits(totalAudits);
        statistics.setAnchoredAudits(anchoredAudits);
        statistics.setAnchoringRate(totalAudits > 0 ? (anchoredAudits * 100.0 / totalAudits) : 0);
        statistics.setTotalBlocks(totalBlocks);
        
        // 获取最新区块信息
        AuditBlock latestBlock = blockMapper.selectLastBlock();
        if (latestBlock != null) {
            statistics.setLatestBlockNumber(latestBlock.getBlockNumber());
            statistics.setLatestBlockTime(latestBlock.getBlockTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        return statistics;
    }
    
    /**
     * 计算审计日志哈希（SM3）
     */
    private String calculateAuditHash(OperationAudit audit) {
        String content = audit.getId() + 
                        audit.getUserId() + 
                        audit.getOperationType() + 
                        audit.getRequestUrl() + 
                        audit.getCreateTime();
        return com.bankshield.common.crypto.EncryptUtil.sm3Hash(content);
    }
    
    /**
     * 计算Merkle根哈希
     */
    private String calculateMerkleRoot(List<String> hashes) {
        if (hashes.isEmpty()) {
            return "";
        }
        
        // 简单的Merkle树实现（实际生产环境应该使用更高效的算法）
        while (hashes.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            for (int i = 0; i < hashes.size(); i += 2) {
                if (i + 1 < hashes.size()) {
                    // 合并两个哈希值
                    String combined = hashes.get(i) + hashes.get(i + 1);
                    nextLevel.add(com.bankshield.common.crypto.EncryptUtil.sm3Hash(combined));
                } else {
                    // 奇数个，直接传递
                    nextLevel.add(hashes.get(i));
                }
            }
            hashes = nextLevel;
        }
        
        return hashes.get(0);
    }
    
    /**
     * 计算区块哈希
     */
    private String calculateBlockHash(AuditBlock block) {
        String content = block.getBlockNumber() + 
                        block.getPreviousHash() + 
                        block.getMerkleRoot() + 
                        block.getAuditCount() + 
                        block.getBlockTime();
        return com.bankshield.common.crypto.EncryptUtil.sm3Hash(content);
    }
    
    /**
     * 获取Merkle路径
     */
    private String getMerklePath(List<String> hashes, int index) {
        // 简化的Merkle路径实现
        return "path_" + index + "_" + hashes.get(index);
    }
    
    /**
     * 验证Merkle路径
     */
    private boolean verifyMerklePath(String leafHash, String merklePath, int index, String expectedRoot) {
        // 简化的验证逻辑（实际应该实现完整的Merkle证明）
        String calculatedRoot = com.bankshield.common.crypto.EncryptUtil.sm3Hash(leafHash + merklePath + index);
        return calculatedRoot.equals(expectedRoot);
    }
    
    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        // 从SecurityContext获取当前用户
        try {
            org.springframework.security.core.context.SecurityContext context = 
                org.springframework.security.core.context.SecurityContextHolder.getContext();
            if (context != null && context.getAuthentication() != null) {
                return context.getAuthentication().getName();
            }
        } catch (Exception e) {
            log.warn("获取当前用户名失败", e);
        }
        return "SYSTEM";
    }
    
    /**
     * 定时任务：定期生成审计区块
     */
    @Scheduled(cron = "0 */5 * * * ?")  // 每5分钟执行一次
    public void scheduledBlockGeneration() {
        log.info("开始生成审计区块...");
        
        AuditBlock block = generateBlock();
        
        if (block != null) {
            log.info("成功生成审计区块 #{}", block.getBlockNumber());
        }
    }
}