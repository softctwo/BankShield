package com.bankshield.gateway.repository;

import com.bankshield.gateway.entity.RateLimitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 限流规则Repository
 * 
 * @author BankShield
 */
@Repository
public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, Long> {
    
    /**
     * 根据规则名称查询
     */
    Optional<RateLimitRule> findByRuleName(String ruleName);
    
    /**
     * 查询所有启用的规则
     */
    List<RateLimitRule> findByEnabledTrue();
    
    /**
     * 根据限流维度查询启用的规则
     */
    List<RateLimitRule> findByLimitDimensionAndEnabledTrue(String limitDimension);
    
    /**
     * 统计启用的规则数量
     */
    long countByEnabledTrue();
    
    /**
     * 根据规则名称查询（不区分大小写）
     */
    Optional<RateLimitRule> findByRuleNameIgnoreCase(String ruleName);
    
    /**
     * 检查规则名称是否已存在
     */
    boolean existsByRuleName(String ruleName);
    
    /**
     * 检查规则名称是否已存在（排除指定ID）
     */
    boolean existsByRuleNameAndIdNot(String ruleName, Long id);
}