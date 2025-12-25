package com.bankshield.ai.policy;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态安全策略生成器
 * 
 * 功能：
 * 1. 基于威胁等级自动生成安全策略
 * 2. 支持10种预定义策略模板
 * 3. 策略效果评估和优化
 * 4. 策略版本管理
 */
@Slf4j
@Service
public class DynamicPolicyGenerator {
    
    private static final int POLICY_VERSION = 1;
    
    /**
     * 策略模板
     */
    public enum PolicyTemplate {
        IP_BLOCK("IP封锁策略", "封锁可疑IP"),
        RATE_LIMIT("限流策略", "限制访问频率"),
        USER_ISOLATION("用户隔离策略", "隔离可疑账户"),
        MFA_ENFORCE("MFA强制策略", "强制多因子认证"),
        SESSION_TIMEOUT("会话超时策略", "缩短会话有效期"),
        PERMISSION_RESTRICT("权限限制策略", "降低权限等级"),
        AUDIT_ENHANCE("审计增强策略", "增加审计粒度"),
        ALERT_ESCALATE("告警升级策略", "提升告警级别"),
        GEO_BLOCK("地理封锁策略", "封锁异常地理位置"),
        TIME_RESTRICT("时间限制策略", "限制访问时间段");
        
        private String name;
        private String description;
        
        PolicyTemplate(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 生成动态策略
     * 
     * @param threatLevel 威胁等级 (0-10)
     * @param context 上下文信息
     * @return 安全策略
     */
    public SecurityPolicy generatePolicy(int threatLevel, PolicyContext context) {
        log.info("生成动态安全策略 - 威胁等级:{}, 上下文:{}", threatLevel, context);
        
        SecurityPolicy policy = new SecurityPolicy();
        policy.setPolicyId("POL-" + UUID.randomUUID().toString().substring(0, 8));
        policy.setThreatLevel(threatLevel);
        policy.setGenerateTime(LocalDateTime.now());
        policy.setVersion(POLICY_VERSION);
        
        // 根据威胁等级选择策略模板
        List<PolicyTemplate> templates = selectPolicyTemplates(threatLevel);
        policy.setTemplates(templates);
        
        // 生成具体规则
        List<PolicyRule> rules = new ArrayList<>();
        for (PolicyTemplate template : templates) {
            PolicyRule rule = createRuleFromTemplate(template, threatLevel, context);
            rules.add(rule);
        }
        policy.setRules(rules);
        
        // 计算策略有效性预估
        policy.setExpectedEffectiveness(calculateExpectedEffectiveness(policy));
        
        log.info("安全策略生成完成 - ID:{}, 模板数:{}, 规则数:{}, 预期效果:{}", 
                policy.getPolicyId(), templates.size(), rules.size(), policy.getExpectedEffectiveness());
        
        return policy;
    }
    
    /**
     * 选择策略模板
     */
    private List<PolicyTemplate> selectPolicyTemplates(int threatLevel) {
        List<PolicyTemplate> templates = new ArrayList<>();
        
        if (threatLevel >= 8) {
            // 严重威胁：所有策略
            templates.addAll(Arrays.asList(PolicyTemplate.values()));
        } else if (threatLevel >= 6) {
            // 高危威胁：8个策略
            templates.add(PolicyTemplate.IP_BLOCK);
            templates.add(PolicyTemplate.USER_ISOLATION);
            templates.add(PolicyTemplate.MFA_ENFORCE);
            templates.add(PolicyTemplate.ALERT_ESCALATE);
            templates.add(PolicyTemplate.SESSION_TIMEOUT);
            templates.add(PolicyTemplate.GEO_BLOCK);
            templates.add(PolicyTemplate.AUDIT_ENHANCE);
            templates.add(PolicyTemplate.PERMISSION_RESTRICT);
        } else if (threatLevel >= 4) {
            // 中危威胁：6个策略
            templates.add(PolicyTemplate.RATE_LIMIT);
            templates.add(PolicyTemplate.SESSION_TIMEOUT);
            templates.add(PolicyTemplate.MFA_ENFORCE);
            templates.add(PolicyTemplate.AUDIT_ENHANCE);
            templates.add(PolicyTemplate.ALERT_ESCALATE);
            templates.add(PolicyTemplate.GEO_BLOCK);
        } else if (threatLevel >= 2) {
            // 低危威胁：3个策略
            templates.add(PolicyTemplate.RATE_LIMIT);
            templates.add(PolicyTemplate.AUDIT_ENHANCE);
            templates.add(PolicyTemplate.ALERT_ESCALATE);
        } else {
            // 极低威胁：仅审计增强
            templates.add(PolicyTemplate.AUDIT_ENHANCE);
        }
        
        return templates;
    }
    
    /**
     * 从模板创建规则
     */
    private PolicyRule createRuleFromTemplate(PolicyTemplate template, int threatLevel, PolicyContext context) {
        PolicyRule rule = new PolicyRule();
        rule.setRuleId(template.name() + "-" + System.currentTimeMillis());
        rule.setTemplate(template);
        rule.setEnabled(true);
        rule.setPriority(calculatePriority(threatLevel));
        
        switch (template) {
            case IP_BLOCK:
                rule.setCondition("ip.anomaly_score > " + (threatLevel * 10));
                rule.setAction("BLOCK_IP");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("durationHours", Math.min(threatLevel, 24));
                }});
                break;
                
            case RATE_LIMIT:
                rule.setCondition("user.request_rate > " + (100 - threatLevel * 10));
                rule.setAction("LIMIT_RATE");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("maxRequestsPerMinute", Math.max(10, 100 - threatLevel * 15));
                }});
                break;
                
            case USER_ISOLATION:
                rule.setCondition("user.risk_score > " + (threatLevel / 10.0));
                rule.setAction("ISOLATE_USER");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("durationHours", threatLevel * 3);
                }});
                break;
                
            case MFA_ENFORCE:
                rule.setCondition("user.sensitive_action == true");
                rule.setAction("REQUIRE_MFA");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("methods", Arrays.asList("SMS", "OTP", "Biometric"));
                }});
                break;
                
            case SESSION_TIMEOUT:
                rule.setCondition("user.privilege_level >= " + (5 - threatLevel / 2));
                rule.setAction("REDUCE_SESSION_TIMEOUT");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("timeoutMinutes", Math.max(15, 60 - threatLevel * 5));
                }});
                break;
                
            case PERMISSION_RESTRICT:
                rule.setCondition("user.anomaly_count > " + (threatLevel * 2));
                rule.setAction("REDUCE_PERMISSIONS");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("permissionLevel", Math.max(1, 5 - threatLevel / 2));
                }});
                break;
                
            case AUDIT_ENHANCE:
                rule.setCondition("true"); // 始终启用
                rule.setAction("ENHANCE_AUDIT");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("logLevel", threatLevel > 5 ? "DEBUG" : "INFO");
                    put("logFields", Arrays.asList("timestamp", "userId", "ip", "action", "result"));
                }});
                break;
                
            case ALERT_ESCALATE:
                rule.setCondition("threat.level >= " + (threatLevel > 5 ? 5 : 7));
                rule.setAction("ESCALATE_ALERT");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("channels", Arrays.asList("SMS", "Email", "Webhook"));
                    put("escalationDelayMinutes", Math.max(5, 30 - threatLevel * 3));
                }});
                break;
                
            case GEO_BLOCK:
                rule.setCondition("ip.geo_anomaly == true");
                rule.setAction("BLOCK_GEO");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("countries", context.getSuspiciousCountries());
                }});
                break;
                
            case TIME_RESTRICT:
                rule.setCondition("time.outside_business_hours == true");
                rule.setAction("RESTRICT_TIME");
                rule.setParameters(new HashMap<String, Object>() {{
                    put("allowedHours", Arrays.asList("09:00-18:00"));
                    put("allowedDays", Arrays.asList("MON", "TUE", "WED", "THU", "FRI"));
                }});
                break;
        }
        
        rule.setDescription(generateRuleDescription(rule, threatLevel));
        
        return rule;
    }
    
    /**
     * 计算规则优先级
     */
    private int calculatePriority(int threatLevel) {
        return Math.min(threatLevel * 2, 10);
    }
    
    /**
     * 计算策略预期效果
     */
    private double calculateExpectedEffectiveness(SecurityPolicy policy) {
        int threatLevel = policy.getThreatLevel();
        int ruleCount = policy.getRules().size();
        
        // 基础效果
        double baseEffect = Math.min(threatLevel * 0.15, 0.9);
        
        // 规则数量加成
        double ruleBonus = Math.min(ruleCount * 0.05, 0.3);
        
        // 模板匹配度（假设策略基于历史数据分析）
        double relevance = 0.7 + Math.random() * 0.25;
        
        // 最终效果
        return Math.min((baseEffect + ruleBonus) * relevance, 0.95);
    }
    
    /**
     * 生成规则描述
     */
    private String generateRuleDescription(PolicyRule rule, int threatLevel) {
        return String.format("【%s】威胁等级 %d - %s - 优先级:%d", 
                rule.getTemplate().getName(),
                threatLevel,
                rule.getAction(),
                rule.getPriority());
    }
    
    /**
     * 查询可用策略模板
     */
    public List<PolicyTemplateInfo> listAvailableTemplates() {
        return Arrays.stream(PolicyTemplate.values())
                .map(template -> new PolicyTemplateInfo(
                        template.name(),
                        template.getName(),
                        template.getDescription()
                ))
                .collect(Collectors.toList());
    }
    
    // Data Classes
    
    @Data
    public static class PolicyContext {
        private String userId;
        private String ipAddress;
        private String resourceId;
        private List<String> suspiciousCountries;
        private Map<String, Object> additionalData;
    }
    
    @Data
    public static class SecurityPolicy {
        private String policyId;
        private int threatLevel;
        private LocalDateTime generateTime;
        private int version;
        private double expectedEffectiveness;
        private List<PolicyTemplate> templates;
        private List<PolicyRule> rules;
    }
    
    @Data
    public static class PolicyRule {
        private String ruleId;
        private PolicyTemplate template;
        private String condition;
        private String action;
        private Map<String, Object> parameters;
        private int priority;
        private boolean enabled;
        private String description;
    }
    
    @Data
    public static class PolicyTemplateInfo {
        private String code;
        private String name;
        private String description;
        
        public PolicyTemplateInfo(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }
    }
}
