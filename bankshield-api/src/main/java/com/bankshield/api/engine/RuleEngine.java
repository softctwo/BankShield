package com.bankshield.api.engine;

import com.bankshield.api.entity.ComplianceRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则引擎
 * 支持动态规则加载、规则评估和规则链执行
 */
@Slf4j
@Component
public class RuleEngine {

    // 规则缓存
    private final Map<String, ComplianceRule> ruleCache = new ConcurrentHashMap<>();
    
    // 规则链
    private final Map<String, List<ComplianceRule>> ruleChains = new ConcurrentHashMap<>();

    /**
     * 注册规则
     */
    public void registerRule(ComplianceRule rule) {
        if (rule == null || rule.getRuleCode() == null) {
            throw new IllegalArgumentException("规则或规则代码不能为空");
        }
        ruleCache.put(rule.getRuleCode(), rule);
        log.info("规则已注册: {}", rule.getRuleCode());
    }

    /**
     * 批量注册规则
     */
    public void registerRules(List<ComplianceRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return;
        }
        rules.forEach(this::registerRule);
        log.info("批量注册规则完成，共{}条", rules.size());
    }

    /**
     * 移除规则
     */
    public void unregisterRule(String ruleCode) {
        ruleCache.remove(ruleCode);
        log.info("规则已移除: {}", ruleCode);
    }

    /**
     * 获取规则
     */
    public ComplianceRule getRule(String ruleCode) {
        return ruleCache.get(ruleCode);
    }

    /**
     * 获取所有规则
     */
    public Collection<ComplianceRule> getAllRules() {
        return ruleCache.values();
    }

    /**
     * 创建规则链
     */
    public void createRuleChain(String chainName, List<String> ruleCodes) {
        List<ComplianceRule> chain = new ArrayList<>();
        for (String ruleCode : ruleCodes) {
            ComplianceRule rule = ruleCache.get(ruleCode);
            if (rule != null) {
                chain.add(rule);
            } else {
                log.warn("规则不存在，跳过: {}", ruleCode);
            }
        }
        ruleChains.put(chainName, chain);
        log.info("规则链已创建: {}, 包含{}条规则", chainName, chain.size());
    }

    /**
     * 执行规则评估
     */
    public RuleEvaluationResult evaluate(String ruleCode, Map<String, Object> context) {
        ComplianceRule rule = ruleCache.get(ruleCode);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在: " + ruleCode);
        }

        RuleEvaluationResult result = new RuleEvaluationResult();
        result.setRuleCode(ruleCode);
        result.setRuleName(rule.getRuleName());
        result.setStartTime(System.currentTimeMillis());

        try {
            // 执行规则检查逻辑
            boolean passed = executeRuleLogic(rule, context);
            result.setPassed(passed);
            result.setStatus("SUCCESS");
            
            if (!passed) {
                result.setMessage("规则检查未通过: " + rule.getRuleName());
                result.setSeverity(rule.getSeverity());
            } else {
                result.setMessage("规则检查通过");
            }

        } catch (Exception e) {
            log.error("规则执行失败: {}", ruleCode, e);
            result.setPassed(false);
            result.setStatus("ERROR");
            result.setMessage("规则执行异常: " + e.getMessage());
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setExecutionTime(result.getEndTime() - result.getStartTime());
        }

        return result;
    }

    /**
     * 执行规则链
     */
    public List<RuleEvaluationResult> evaluateChain(String chainName, Map<String, Object> context) {
        List<ComplianceRule> chain = ruleChains.get(chainName);
        if (chain == null) {
            throw new IllegalArgumentException("规则链不存在: " + chainName);
        }

        List<RuleEvaluationResult> results = new ArrayList<>();
        for (ComplianceRule rule : chain) {
            RuleEvaluationResult result = evaluate(rule.getRuleCode(), context);
            results.add(result);
            
            // 如果是严重规则且未通过，可以选择中断链执行
            if (!result.isPassed() && "CRITICAL".equals(rule.getSeverity())) {
                log.warn("严重规则未通过，中断规则链执行: {}", rule.getRuleCode());
                break;
            }
        }

        return results;
    }

    /**
     * 批量评估规则
     */
    public List<RuleEvaluationResult> evaluateBatch(List<String> ruleCodes, Map<String, Object> context) {
        List<RuleEvaluationResult> results = new ArrayList<>();
        for (String ruleCode : ruleCodes) {
            try {
                RuleEvaluationResult result = evaluate(ruleCode, context);
                results.add(result);
            } catch (Exception e) {
                log.error("规则评估失败: {}", ruleCode, e);
                RuleEvaluationResult errorResult = new RuleEvaluationResult();
                errorResult.setRuleCode(ruleCode);
                errorResult.setPassed(false);
                errorResult.setStatus("ERROR");
                errorResult.setMessage(e.getMessage());
                results.add(errorResult);
            }
        }
        return results;
    }

    /**
     * 执行规则逻辑（简化版本，实际应该根据规则类型动态执行）
     */
    private boolean executeRuleLogic(ComplianceRule rule, Map<String, Object> context) {
        // 这里是简化的规则执行逻辑
        // 实际应该根据规则的checkType和checkLogic动态执行
        
        String checkType = rule.getCheckType();
        if (checkType == null) {
            return true;
        }

        switch (checkType) {
            case "DATA_ENCRYPTION":
                return checkDataEncryption(context);
            case "ACCESS_CONTROL":
                return checkAccessControl(context);
            case "DATA_RETENTION":
                return checkDataRetention(context);
            case "CONSENT_MANAGEMENT":
                return checkConsentManagement(context);
            case "DATA_BREACH_NOTIFICATION":
                return checkDataBreachNotification(context);
            default:
                // 默认通过
                return true;
        }
    }

    /**
     * 检查数据加密
     */
    private boolean checkDataEncryption(Map<String, Object> context) {
        Object encrypted = context.get("encrypted");
        return encrypted != null && (Boolean) encrypted;
    }

    /**
     * 检查访问控制
     */
    private boolean checkAccessControl(Map<String, Object> context) {
        Object hasPermission = context.get("hasPermission");
        return hasPermission != null && (Boolean) hasPermission;
    }

    /**
     * 检查数据保留
     */
    private boolean checkDataRetention(Map<String, Object> context) {
        Object retentionPeriod = context.get("retentionPeriod");
        return retentionPeriod != null && (Integer) retentionPeriod <= 365;
    }

    /**
     * 检查同意管理
     */
    private boolean checkConsentManagement(Map<String, Object> context) {
        Object hasConsent = context.get("hasConsent");
        return hasConsent != null && (Boolean) hasConsent;
    }

    /**
     * 检查数据泄露通知
     */
    private boolean checkDataBreachNotification(Map<String, Object> context) {
        Object notificationSent = context.get("notificationSent");
        return notificationSent != null && (Boolean) notificationSent;
    }

    /**
     * 获取规则统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", ruleCache.size());
        stats.put("totalChains", ruleChains.size());
        
        Map<String, Long> severityCount = new HashMap<>();
        for (ComplianceRule rule : ruleCache.values()) {
            String severity = rule.getSeverity();
            severityCount.put(severity, severityCount.getOrDefault(severity, 0L) + 1);
        }
        stats.put("severityDistribution", severityCount);
        
        return stats;
    }

    /**
     * 规则评估结果
     */
    public static class RuleEvaluationResult {
        private String ruleCode;
        private String ruleName;
        private boolean passed;
        private String status;
        private String message;
        private String severity;
        private long startTime;
        private long endTime;
        private long executionTime;

        // Getters and Setters
        public String getRuleCode() { return ruleCode; }
        public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
        
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
}
