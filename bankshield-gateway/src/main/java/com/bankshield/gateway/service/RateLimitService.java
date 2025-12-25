package com.bankshield.gateway.service;

import com.bankshield.gateway.entity.RateLimitRule;
import com.bankshield.gateway.repository.RateLimitRepository;
import com.bankshield.gateway.repository.RateLimitRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 限流服务
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class RateLimitService {
    
    @Autowired
    private RateLimitRuleRepository rateLimitRuleRepository;
    
    @Autowired
    private RateLimitRepository rateLimitRepository;
    
    /**
     * 检查是否允许访问
     * 
     * @param dimension 限流维度
     * @param key 限流键
     * @param ruleName 规则名称
     * @return true: 允许访问, false: 拒绝访问
     */
    public boolean isAllowed(String dimension, String key, String ruleName) {
        Optional<RateLimitRule> ruleOptional = rateLimitRuleRepository.findByRuleName(ruleName);
        if (!ruleOptional.isPresent() || !ruleOptional.get().getEnabled()) {
            log.debug("限流规则不存在或未启用: {}", ruleName);
            return true;
        }
        
        RateLimitRule rule = ruleOptional.get();
        if (!rule.getLimitDimension().equals(dimension)) {
            log.warn("限流维度不匹配，规则: {}, 请求维度: {}", rule.getLimitDimension(), dimension);
            return true;
        }
        
        String rateLimitKey = buildRateLimitKey(dimension, key, ruleName);
        boolean allowed = rateLimitRepository.isAllowed(rateLimitKey, rule.getLimitThreshold(), rule.getLimitWindow());
        
        if (!allowed) {
            log.warn("限流拒绝访问，维度: {}, 键: {}, 规则: {}", dimension, key, ruleName);
        }
        
        return allowed;
    }
    
    /**
     * 检查是否允许访问（使用默认规则）
     * 
     * @param dimension 限流维度
     * @param key 限流键
     * @return true: 允许访问, false: 拒绝访问
     */
    public boolean isAllowed(String dimension, String key) {
        List<RateLimitRule> rules = rateLimitRuleRepository.findByLimitDimensionAndEnabledTrue(dimension);
        if (rules.isEmpty()) {
            log.debug("未找到启用的限流规则，维度: {}", dimension);
            return true;
        }
        
        // 检查所有适用的规则
        for (RateLimitRule rule : rules) {
            String rateLimitKey = buildRateLimitKey(dimension, key, rule.getRuleName());
            boolean allowed = rateLimitRepository.isAllowed(rateLimitKey, rule.getLimitThreshold(), rule.getLimitWindow());
            
            if (!allowed) {
                log.warn("限流拒绝访问，维度: {}, 键: {}, 规则: {}", dimension, key, rule.getRuleName());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取当前计数
     * 
     * @param dimension 限流维度
     * @param key 限流键
     * @param ruleName 规则名称
     * @return 当前计数
     */
    public long getCurrentCount(String dimension, String key, String ruleName) {
        String rateLimitKey = buildRateLimitKey(dimension, key, ruleName);
        return rateLimitRepository.getCurrentCount(rateLimitKey);
    }
    
    /**
     * 获取剩余时间
     * 
     * @param dimension 限流维度
     * @param key 限流键
     * @param ruleName 规则名称
     * @return 剩余时间（秒）
     */
    public long getRemainingTime(String dimension, String key, String ruleName) {
        Optional<RateLimitRule> ruleOptional = rateLimitRuleRepository.findByRuleName(ruleName);
        if (!ruleOptional.isPresent()) {
            return 0;
        }
        
        String rateLimitKey = buildRateLimitKey(dimension, key, ruleName);
        return rateLimitRepository.getExpireTime(rateLimitKey);
    }
    
    /**
     * 创建限流规则
     * 
     * @param rule 限流规则
     * @return 创建的规则
     */
    public RateLimitRule createRule(RateLimitRule rule) {
        if (rateLimitRuleRepository.existsByRuleName(rule.getRuleName())) {
            throw new IllegalArgumentException("规则名称已存在: " + rule.getRuleName());
        }
        
        return rateLimitRuleRepository.save(rule);
    }
    
    /**
     * 更新限流规则
     * 
     * @param rule 限流规则
     * @return 更新的规则
     */
    public RateLimitRule updateRule(RateLimitRule rule) {
        if (rateLimitRuleRepository.existsByRuleNameAndIdNot(rule.getRuleName(), rule.getId())) {
            throw new IllegalArgumentException("规则名称已存在: " + rule.getRuleName());
        }
        
        return rateLimitRuleRepository.save(rule);
    }
    
    /**
     * 删除限流规则
     * 
     * @param id 规则ID
     */
    public void deleteRule(Long id) {
        rateLimitRuleRepository.deleteById(id);
    }
    
    /**
     * 获取所有限流规则
     * 
     * @return 限流规则列表
     */
    public List<RateLimitRule> getAllRules() {
        return rateLimitRuleRepository.findAll();
    }
    
    /**
     * 获取启用的限流规则
     * 
     * @return 启用的限流规则列表
     */
    public List<RateLimitRule> getEnabledRules() {
        return rateLimitRuleRepository.findByEnabledTrue();
    }
    
    /**
     * 根据ID获取限流规则
     * 
     * @param id 规则ID
     * @return 限流规则
     */
    public Optional<RateLimitRule> getRuleById(Long id) {
        return rateLimitRuleRepository.findById(id);
    }
    
    /**
     * 根据规则名称获取限流规则
     * 
     * @param ruleName 规则名称
     * @return 限流规则
     */
    public Optional<RateLimitRule> getRuleByName(String ruleName) {
        return rateLimitRuleRepository.findByRuleName(ruleName);
    }
    
    /**
     * 构建限流键
     * 
     * @param dimension 限流维度
     * @param key 限流键
     * @param ruleName 规则名称
     * @return 限流键
     */
    private String buildRateLimitKey(String dimension, String key, String ruleName) {
        return String.format("rate_limit:%s:%s:%s", dimension, key, ruleName);
    }
}