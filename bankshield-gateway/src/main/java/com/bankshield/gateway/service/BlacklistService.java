package com.bankshield.gateway.service;

import com.bankshield.gateway.entity.BlacklistIp;
import com.bankshield.gateway.repository.BlacklistIpRepository;
import com.bankshield.gateway.repository.BlacklistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 黑名单服务
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class BlacklistService {
    
    @Autowired
    private BlacklistIpRepository blacklistIpRepository;
    
    @Autowired
    private BlacklistRepository blacklistRepository;
    
    /**
     * 添加IP到黑名单
     * 
     * @param ipAddress IP地址
     * @param blockReason 封禁原因
     * @param blockDuration 封禁时长（秒）
     * @param operator 操作人员
     * @return 黑名单记录
     */
    @Transactional
    public BlacklistIp addToBlacklist(String ipAddress, String blockReason, Long blockDuration, String operator) {
        // 检查是否已存在
        Optional<BlacklistIp> existing = blacklistIpRepository.findByIpAddress(ipAddress);
        if (existing.isPresent() && existing.get().getBlockStatus().equals(BlacklistIp.BlockStatus.BLOCKED.getValue())) {
            log.warn("IP已在黑名单中: {}", ipAddress);
            return existing.get();
        }
        
        BlacklistIp blacklistIp = new BlacklistIp();
        blacklistIp.setIpAddress(ipAddress);
        blacklistIp.setBlockReason(blockReason);
        blacklistIp.setBlockStatus(BlacklistIp.BlockStatus.BLOCKED.getValue());
        blacklistIp.setOperator(operator);
        
        if (blockDuration != null && blockDuration > 0) {
            blacklistIp.setUnblockTime(LocalDateTime.now().plusSeconds(blockDuration));
        }
        
        BlacklistIp saved = blacklistIpRepository.save(blacklistIp);
        
        // 同步到Redis
        blacklistRepository.addToBlacklist(ipAddress, blockReason, blockDuration != null ? blockDuration : 0);
        
        log.info("IP已添加到黑名单: {}, 原因: {}, 操作人员: {}", ipAddress, blockReason, operator);
        return saved;
    }
    
    /**
     * 从黑名单中移除IP
     * 
     * @param id 黑名单ID
     * @param operator 操作人员
     * @return 黑名单记录
     */
    @Transactional
    public BlacklistIp removeFromBlacklist(Long id, String operator) {
        Optional<BlacklistIp> optional = blacklistIpRepository.findById(id);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("黑名单记录不存在: " + id);
        }
        
        BlacklistIp blacklistIp = optional.get();
        blacklistIp.setBlockStatus(BlacklistIp.BlockStatus.UNBLOCKED.getValue());
        blacklistIp.setUnblockTime(LocalDateTime.now());
        blacklistIp.setOperator(operator);
        
        BlacklistIp saved = blacklistIpRepository.save(blacklistIp);
        
        // 从Redis中移除
        blacklistRepository.removeFromBlacklist(blacklistIp.getIpAddress());
        
        log.info("IP已从黑名单中移除: {}, 操作人员: {}", blacklistIp.getIpAddress(), operator);
        return saved;
    }
    
    /**
     * 检查IP是否在黑名单中
     * 
     * @param ipAddress IP地址
     * @return true: 在黑名单中, false: 不在黑名单中
     */
    public boolean isBlacklisted(String ipAddress) {
        return blacklistRepository.isBlacklisted(ipAddress);
    }
    
    /**
     * 获取IP的封禁信息
     * 
     * @param ipAddress IP地址
     * @return 黑名单记录
     */
    public Optional<BlacklistIp> getBlacklistInfo(String ipAddress) {
        return blacklistIpRepository.findByIpAddress(ipAddress);
    }
    
    /**
     * 获取活跃的黑名单
     * 
     * @return 活跃黑名单列表
     */
    public List<BlacklistIp> getActiveBlacklists() {
        return blacklistIpRepository.findActiveBlacklists(LocalDateTime.now());
    }
    
    /**
     * 获取黑名单分页列表
     * 
     * @param pageable 分页信息
     * @return 黑名单分页结果
     */
    public Page<BlacklistIp> getBlacklists(Pageable pageable) {
        return blacklistIpRepository.findAll(pageable);
    }
    
    /**
     * 根据状态获取黑名单分页列表
     * 
     * @param blockStatus 封禁状态
     * @param pageable 分页信息
     * @return 黑名单分页结果
     */
    public Page<BlacklistIp> getBlacklistsByStatus(String blockStatus, Pageable pageable) {
        return blacklistIpRepository.findByBlockStatus(blockStatus, pageable);
    }
    
    /**
     * 自动处理过期的黑名单
     */
    @Transactional
    public void processExpiredBlacklists() {
        LocalDateTime now = LocalDateTime.now();
        List<BlacklistIp> expiredBlacklists = blacklistIpRepository.findExpiredBlacklists(now);
        
        for (BlacklistIp blacklistIp : expiredBlacklists) {
            blacklistIp.setBlockStatus(BlacklistIp.BlockStatus.EXPIRED.getValue());
            blacklistIp.setUnblockTime(now);
            
            // 从Redis中移除
            blacklistRepository.removeFromBlacklist(blacklistIp.getIpAddress());
            
            log.info("IP黑名单已过期自动解封: {}", blacklistIp.getIpAddress());
        }
        
        if (!expiredBlacklists.isEmpty()) {
            blacklistIpRepository.saveAll(expiredBlacklists);
            log.info("处理了 {} 个过期的黑名单记录", expiredBlacklists.size());
        }
    }
    
    /**
     * 批量添加IP到黑名单
     * 
     * @param ipAddresses IP地址列表
     * @param blockReason 封禁原因
     * @param blockDuration 封禁时长（秒）
     * @param operator 操作人员
     */
    @Transactional
    public void addToBlacklistBatch(List<String> ipAddresses, String blockReason, Long blockDuration, String operator) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime unblockTime = null;
        
        if (blockDuration != null && blockDuration > 0) {
            unblockTime = now.plusSeconds(blockDuration);
        }
        
        for (String ipAddress : ipAddresses) {
            try {
                // 检查是否已存在
                Optional<BlacklistIp> existing = blacklistIpRepository.findByIpAddress(ipAddress);
                if (existing.isPresent() && existing.get().getBlockStatus().equals(BlacklistIp.BlockStatus.BLOCKED.getValue())) {
                    log.warn("IP已在黑名单中: {}", ipAddress);
                    continue;
                }
                
                BlacklistIp blacklistIp = new BlacklistIp();
                blacklistIp.setIpAddress(ipAddress);
                blacklistIp.setBlockReason(blockReason);
                blacklistIp.setBlockStatus(BlacklistIp.BlockStatus.BLOCKED.getValue());
                blacklistIp.setBlockTime(now);
                blacklistIp.setUnblockTime(unblockTime);
                blacklistIp.setOperator(operator);
                
                blacklistIpRepository.save(blacklistIp);
                
                // 同步到Redis
                blacklistRepository.addToBlacklist(ipAddress, blockReason, blockDuration != null ? blockDuration : 0);
                
                log.info("IP已添加到黑名单: {}, 原因: {}, 操作人员: {}", ipAddress, blockReason, operator);
            } catch (Exception e) {
                log.error("添加IP到黑名单失败: {}", ipAddress, e);
            }
        }
    }
    
    /**
     * 批量从黑名单中移除IP
     * 
     * @param ids 黑名单ID列表
     * @param operator 操作人员
     */
    @Transactional
    public void removeFromBlacklistBatch(List<Long> ids, String operator) {
        List<BlacklistIp> blacklists = blacklistIpRepository.findAllById(ids);
        LocalDateTime now = LocalDateTime.now();
        
        for (BlacklistIp blacklistIp : blacklists) {
            blacklistIp.setBlockStatus(BlacklistIp.BlockStatus.UNBLOCKED.getValue());
            blacklistIp.setUnblockTime(now);
            blacklistIp.setOperator(operator);
            
            // 从Redis中移除
            blacklistRepository.removeFromBlacklist(blacklistIp.getIpAddress());
            
            log.info("IP已从黑名单中移除: {}, 操作人员: {}", blacklistIp.getIpAddress(), operator);
        }
        
        blacklistIpRepository.saveAll(blacklists);
    }
    
    /**
     * 获取黑名单统计
     * 
     * @return 黑名单统计
     */
    public BlacklistStatistics getStatistics() {
        LocalDateTime now = LocalDateTime.now();
        
        long totalCount = blacklistIpRepository.count();
        long activeCount = blacklistIpRepository.countActiveBlacklists(now);
        long blockedCount = blacklistIpRepository.countByBlockStatus(BlacklistIp.BlockStatus.BLOCKED.getValue());
        long unblockedCount = blacklistIpRepository.countByBlockStatus(BlacklistIp.BlockStatus.UNBLOCKED.getValue());
        long expiredCount = blacklistIpRepository.countByBlockStatus(BlacklistIp.BlockStatus.EXPIRED.getValue());
        
        BlacklistStatistics statistics = new BlacklistStatistics();
        statistics.setTotalCount(totalCount);
        statistics.setActiveCount(activeCount);
        statistics.setBlockedCount(blockedCount);
        statistics.setUnblockedCount(unblockedCount);
        statistics.setExpiredCount(expiredCount);
        
        return statistics;
    }
    
    /**
     * 黑名单统计类
     */
    public static class BlacklistStatistics {
        private long totalCount;
        private long activeCount;
        private long blockedCount;
        private long unblockedCount;
        private long expiredCount;
        
        // Getters and Setters
        public long getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }
        
        public long getActiveCount() {
            return activeCount;
        }
        
        public void setActiveCount(long activeCount) {
            this.activeCount = activeCount;
        }
        
        public long getBlockedCount() {
            return blockedCount;
        }
        
        public void setBlockedCount(long blockedCount) {
            this.blockedCount = blockedCount;
        }
        
        public long getUnblockedCount() {
            return unblockedCount;
        }
        
        public void setUnblockedCount(long unblockedCount) {
            this.unblockedCount = unblockedCount;
        }
        
        public long getExpiredCount() {
            return expiredCount;
        }
        
        public void setExpiredCount(long expiredCount) {
            this.expiredCount = expiredCount;
        }
    }
}