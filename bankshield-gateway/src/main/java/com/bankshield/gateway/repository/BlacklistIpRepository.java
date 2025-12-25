package com.bankshield.gateway.repository;

import com.bankshield.gateway.entity.BlacklistIp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IP黑名单Repository
 * 
 * @author BankShield
 */
@Repository
public interface BlacklistIpRepository extends JpaRepository<BlacklistIp, Long> {
    
    /**
     * 根据IP地址查询
     */
    Optional<BlacklistIp> findByIpAddress(String ipAddress);
    
    /**
     * 根据IP地址和状态查询
     */
    Optional<BlacklistIp> findByIpAddressAndBlockStatus(String ipAddress, String blockStatus);
    
    /**
     * 查询指定状态的黑名单
     */
    Page<BlacklistIp> findByBlockStatus(String blockStatus, Pageable pageable);
    
    /**
     * 查询所有启用的黑名单（未解封且未过期）
     */
    @Query("SELECT b FROM BlacklistIp b WHERE b.blockStatus = 'BLOCKED' " +
           "AND (b.unblockTime IS NULL OR b.unblockTime > :currentTime)")
    List<BlacklistIp> findActiveBlacklists(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查询已过期的黑名单
     */
    @Query("SELECT b FROM BlacklistIp b WHERE b.blockStatus = 'BLOCKED' " +
           "AND b.unblockTime IS NOT NULL AND b.unblockTime <= :currentTime")
    List<BlacklistIp> findExpiredBlacklists(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 统计指定状态的黑名单数量
     */
    long countByBlockStatus(String blockStatus);
    
    /**
     * 统计活跃的黑名单数量
     */
    @Query("SELECT COUNT(b) FROM BlacklistIp b WHERE b.blockStatus = 'BLOCKED' " +
           "AND (b.unblockTime IS NULL OR b.unblockTime > :currentTime)")
    long countActiveBlacklists(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 检查IP是否在黑名单中（活跃状态）
     */
    @Query("SELECT COUNT(b) > 0 FROM BlacklistIp b WHERE b.ipAddress = :ipAddress " +
           "AND b.blockStatus = 'BLOCKED' AND (b.unblockTime IS NULL OR b.unblockTime > :currentTime)")
    boolean isIpBlacklisted(@Param("ipAddress") String ipAddress, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据IP地址删除
     */
    void deleteByIpAddress(String ipAddress);
    
    /**
     * 批量删除指定IP地址的记录
     */
    void deleteByIpAddressIn(List<String> ipAddresses);
}