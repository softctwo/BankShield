package com.bankshield.api.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import com.bankshield.api.service.AccessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 访问控制服务实现
 */
@Slf4j
@Service
public class AccessControlServiceImpl implements AccessControlService {
    
    @Autowired
    private AccessPolicyMapper policyMapper;
    
    @Autowired
    private AccessRuleMapper ruleMapper;
    
    @Autowired
    private AccessLogMapper logMapper;
    
    @Autowired
    private MfaConfigMapper mfaConfigMapper;
    
    @Autowired
    private TemporaryPermissionMapper tempPermissionMapper;
    
    @Autowired
    private IpWhitelistMapper ipWhitelistMapper;
    
    @Autowired
    private IpBlacklistMapper ipBlacklistMapper;
    
    // ==================== 访问策略管理 ====================
    
    @Override
    public IPage<AccessPolicy> pagePolicies(Page<AccessPolicy> page, String policyName, String policyType, String status) {
        LambdaQueryWrapper<AccessPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(policyName), AccessPolicy::getPolicyName, policyName)
               .eq(StringUtils.hasText(policyType), AccessPolicy::getPolicyType, policyType)
               .eq(StringUtils.hasText(status), AccessPolicy::getStatus, status)
               .orderByDesc(AccessPolicy::getPriority)
               .orderByDesc(AccessPolicy::getCreatedTime);
        return policyMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPolicy(AccessPolicy policy) {
        policy.setCreatedTime(LocalDateTime.now());
        policy.setUpdatedTime(LocalDateTime.now());
        policyMapper.insert(policy);
        log.info("创建访问策略: {}", policy.getPolicyCode());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePolicy(AccessPolicy policy) {
        policy.setUpdatedTime(LocalDateTime.now());
        policyMapper.updateById(policy);
        log.info("更新访问策略: {}", policy.getPolicyCode());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePolicy(Long id) {
        policyMapper.deleteById(id);
        log.info("删除访问策略: {}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePolicyStatus(Long id, String status) {
        AccessPolicy policy = policyMapper.selectById(id);
        if (policy != null) {
            policy.setStatus(status);
            policy.setUpdatedTime(LocalDateTime.now());
            policyMapper.updateById(policy);
            log.info("更新策略状态: {} -> {}", id, status);
        }
    }
    
    @Override
    public AccessPolicy getPolicyByCode(String policyCode) {
        return policyMapper.selectByPolicyCode(policyCode);
    }
    
    // ==================== 访问规则管理 ====================
    
    @Override
    public IPage<AccessRule> pageRules(Page<AccessRule> page, Long policyId, String ruleType, String status) {
        LambdaQueryWrapper<AccessRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(policyId != null, AccessRule::getPolicyId, policyId)
               .eq(StringUtils.hasText(ruleType), AccessRule::getRuleType, ruleType)
               .eq(StringUtils.hasText(status), AccessRule::getStatus, status)
               .orderByDesc(AccessRule::getPriority)
               .orderByDesc(AccessRule::getCreatedTime);
        return ruleMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRule(AccessRule rule) {
        rule.setCreatedTime(LocalDateTime.now());
        rule.setUpdatedTime(LocalDateTime.now());
        ruleMapper.insert(rule);
        log.info("创建访问规则: {}", rule.getRuleCode());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(AccessRule rule) {
        rule.setUpdatedTime(LocalDateTime.now());
        ruleMapper.updateById(rule);
        log.info("更新访问规则: {}", rule.getRuleCode());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        ruleMapper.deleteById(id);
        log.info("删除访问规则: {}", id);
    }
    
    @Override
    public List<AccessRule> getRulesByPolicyId(Long policyId) {
        return ruleMapper.selectByPolicyId(policyId);
    }
    
    // ==================== 访问控制核心 ====================
    
    @Override
    public boolean checkAccess(Long userId, String username, String resourceType, 
                               String resourceId, String action, String ipAddress, boolean mfaVerified) {
        long startTime = System.currentTimeMillis();
        boolean allowed = false;
        String policyMatched = null;
        String ruleMatched = null;
        String denyReason = null;
        
        try {
            // 1. 检查IP黑名单
            if (isIpBlacklisted(ipAddress)) {
                denyReason = "IP地址已被封禁";
                return false;
            }
            
            // 2. 获取所有启用的策略（按优先级排序）
            List<AccessPolicy> policies = policyMapper.selectEnabledPolicies();
            
            // 3. 遍历策略，匹配规则
            for (AccessPolicy policy : policies) {
                List<AccessRule> rules = ruleMapper.selectByPolicyId(policy.getId());
                
                for (AccessRule rule : rules) {
                    // 检查规则是否匹配
                    if (matchRule(rule, userId, username, resourceType, resourceId, action, ipAddress, mfaVerified)) {
                        policyMatched = policy.getPolicyCode();
                        ruleMatched = rule.getRuleCode();
                        
                        // 根据策略效果决定是否允许
                        if ("ALLOW".equals(policy.getEffect())) {
                            allowed = true;
                            break;
                        } else {
                            denyReason = "策略拒绝访问";
                            allowed = false;
                            break;
                        }
                    }
                }
                
                if (policyMatched != null) {
                    break;
                }
            }
            
            if (!allowed && denyReason == null) {
                denyReason = "未匹配到任何允许策略";
            }
            
        } catch (Exception e) {
            log.error("访问权限检查失败", e);
            denyReason = "系统错误: " + e.getMessage();
            allowed = false;
        } finally {
            // 记录访问日志
            AccessLog accessLog = new AccessLog();
            accessLog.setUserId(userId);
            accessLog.setUsername(username);
            accessLog.setResourceType(resourceType);
            accessLog.setResourceId(resourceId);
            accessLog.setAction(action);
            accessLog.setAccessResult(allowed ? "ALLOW" : "DENY");
            accessLog.setPolicyMatched(policyMatched);
            accessLog.setRuleMatched(ruleMatched);
            accessLog.setIpAddress(ipAddress);
            accessLog.setMfaVerified(mfaVerified);
            accessLog.setDenyReason(denyReason);
            accessLog.setAccessTime(LocalDateTime.now());
            accessLog.setResponseTime((int)(System.currentTimeMillis() - startTime));
            
            logAccess(accessLog);
        }
        
        return allowed;
    }
    
    /**
     * 匹配规则
     */
    private boolean matchRule(AccessRule rule, Long userId, String username, String resourceType,
                             String resourceId, String action, String ipAddress, boolean mfaVerified) {
        try {
            // 检查MFA要求
            if (rule.getMfaRequired() && !mfaVerified) {
                return false;
            }
            
            // 解析条件
            JSONObject subjectCondition = parseCondition(rule.getSubjectCondition());
            JSONObject resourceCondition = parseCondition(rule.getResourceCondition());
            JSONObject actionCondition = parseCondition(rule.getActionCondition());
            JSONObject environmentCondition = parseCondition(rule.getEnvironmentCondition());
            
            // 匹配主体条件（简化版本，实际应该查询用户角色等）
            if (subjectCondition != null && !matchSubjectCondition(subjectCondition, userId, username)) {
                return false;
            }
            
            // 匹配资源条件
            if (resourceCondition != null && !matchResourceCondition(resourceCondition, resourceType, resourceId)) {
                return false;
            }
            
            // 匹配操作条件
            if (actionCondition != null && !matchActionCondition(actionCondition, action)) {
                return false;
            }
            
            // 匹配环境条件
            if (environmentCondition != null && !matchEnvironmentCondition(environmentCondition, ipAddress)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("规则匹配失败: {}", rule.getRuleCode(), e);
            return false;
        }
    }
    
    private JSONObject parseCondition(String condition) {
        if (!StringUtils.hasText(condition)) {
            return null;
        }
        try {
            return JSON.parseObject(condition);
        } catch (Exception e) {
            log.error("解析条件失败: {}", condition, e);
            return null;
        }
    }
    
    private boolean matchSubjectCondition(JSONObject condition, Long userId, String username) {
        // 简化实现：实际应该检查用户角色、部门等
        return true;
    }
    
    private boolean matchResourceCondition(JSONObject condition, String resourceType, String resourceId) {
        String type = condition.getString("type");
        if (type != null && !"*".equals(type) && !type.equals(resourceType)) {
            return false;
        }
        return true;
    }
    
    private boolean matchActionCondition(JSONObject condition, String action) {
        Object actionObj = condition.get("action");
        if (actionObj == null) {
            return true;
        }
        
        if (actionObj instanceof String) {
            String actionStr = (String) actionObj;
            return "*".equals(actionStr) || actionStr.equals(action);
        }
        
        if (actionObj instanceof List) {
            List<String> actions = (List<String>) actionObj;
            return actions.contains(action);
        }
        
        return false;
    }
    
    private boolean matchEnvironmentCondition(JSONObject condition, String ipAddress) {
        Boolean ipWhitelist = condition.getBoolean("ip_whitelist");
        if (ipWhitelist != null && ipWhitelist) {
            return isIpWhitelisted(ipAddress, null);
        }
        return true;
    }
    
    @Override
    public void logAccess(AccessLog accessLog) {
        try {
            logMapper.insert(accessLog);
        } catch (Exception e) {
            log.error("记录访问日志失败", e);
        }
    }
    
    @Override
    public Map<String, Object> getAccessStatistics(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计访问次数
        List<Map<String, Object>> countByResult = logMapper.countAccessByResult(startTime);
        statistics.put("countByResult", countByResult);
        
        // 统计用户访问次数
        List<Map<String, Object>> countByUser = logMapper.countAccessByUser(startTime, 10);
        statistics.put("topUsers", countByUser);
        
        // 统计拒绝访问的日志
        List<AccessLog> deniedLogs = logMapper.selectDeniedLogs(startTime);
        statistics.put("deniedCount", deniedLogs.size());
        
        return statistics;
    }
    
    // ==================== 临时权限管理 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantTemporaryPermission(TemporaryPermission permission) {
        permission.setStatus("ACTIVE");
        permission.setCreatedTime(LocalDateTime.now());
        tempPermissionMapper.insert(permission);
        log.info("授予临时权限: {} -> {}", permission.getUsername(), permission.getPermissionCode());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeTemporaryPermission(Long id) {
        TemporaryPermission permission = tempPermissionMapper.selectById(id);
        if (permission != null) {
            permission.setStatus("REVOKED");
            tempPermissionMapper.updateById(permission);
            log.info("撤销临时权限: {}", id);
        }
    }
    
    @Override
    public List<TemporaryPermission> getUserActivePermissions(Long userId) {
        return tempPermissionMapper.selectActiveByUserId(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredPermissions() {
        List<TemporaryPermission> expired = tempPermissionMapper.selectExpiredPermissions();
        int count = 0;
        for (TemporaryPermission permission : expired) {
            permission.setStatus("EXPIRED");
            tempPermissionMapper.updateById(permission);
            count++;
        }
        log.info("清理过期临时权限: {} 条", count);
        return count;
    }
    
    // ==================== MFA管理 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configureMfa(MfaConfig config) {
        MfaConfig existing = mfaConfigMapper.selectByUserIdAndType(config.getUserId(), config.getMfaType());
        if (existing != null) {
            config.setId(existing.getId());
            config.setUpdatedTime(LocalDateTime.now());
            mfaConfigMapper.updateById(config);
        } else {
            config.setCreatedTime(LocalDateTime.now());
            config.setUpdatedTime(LocalDateTime.now());
            mfaConfigMapper.insert(config);
        }
        log.info("配置MFA: {} - {}", config.getUsername(), config.getMfaType());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleMfa(Long userId, String mfaType, boolean enabled) {
        MfaConfig config = mfaConfigMapper.selectByUserIdAndType(userId, mfaType);
        if (config != null) {
            config.setMfaEnabled(enabled);
            config.setUpdatedTime(LocalDateTime.now());
            mfaConfigMapper.updateById(config);
            log.info("切换MFA状态: {} - {} -> {}", userId, mfaType, enabled);
        }
    }
    
    @Override
    public boolean verifyMfa(Long userId, String mfaType, String code) {
        MfaConfig config = mfaConfigMapper.selectByUserIdAndType(userId, mfaType);
        if (config == null || !config.getMfaEnabled()) {
            return false;
        }
        
        boolean verified = false;
        
        switch (mfaType) {
            case "TOTP":
                verified = verifyTotp(config.getTotpSecret(), code);
                break;
            case "SMS":
            case "EMAIL":
                // 实际应该从缓存中获取发送的验证码进行比对
                verified = true; // 简化实现
                break;
            default:
                verified = false;
        }
        
        if (verified) {
            config.setLastVerifiedTime(LocalDateTime.now());
            mfaConfigMapper.updateById(config);
        }
        
        return verified;
    }
    
    @Override
    public String generateTotpSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    @Override
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 10; i++) {
            int code = 100000 + random.nextInt(900000);
            codes.add(String.valueOf(code));
        }
        return codes;
    }
    
    /**
     * 验证TOTP
     */
    private boolean verifyTotp(String secret, String code) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            long timeWindow = Instant.now().getEpochSecond() / 30;
            
            for (int i = -1; i <= 1; i++) {
                String generatedCode = generateTotpCode(keyBytes, timeWindow + i);
                if (generatedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("TOTP验证失败", e);
            return false;
        }
    }
    
    private String generateTotpCode(byte[] key, long time) throws Exception {
        byte[] data = ByteBuffer.allocate(8).putLong(time).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);
        
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);
        
        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }
    
    // ==================== IP访问控制 ====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIpWhitelist(IpWhitelist whitelist) {
        whitelist.setCreatedTime(LocalDateTime.now());
        whitelist.setUpdatedTime(LocalDateTime.now());
        ipWhitelistMapper.insert(whitelist);
        log.info("添加IP白名单: {}", whitelist.getIpAddress());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeIpWhitelist(Long id) {
        ipWhitelistMapper.deleteById(id);
        log.info("删除IP白名单: {}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIpBlacklist(IpBlacklist blacklist) {
        blacklist.setStatus("ACTIVE");
        blacklist.setBlockedTime(LocalDateTime.now());
        blacklist.setCreatedTime(LocalDateTime.now());
        ipBlacklistMapper.insert(blacklist);
        log.info("添加IP黑名单: {}", blacklist.getIpAddress());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeIpBlacklist(Long id) {
        IpBlacklist blacklist = ipBlacklistMapper.selectById(id);
        if (blacklist != null) {
            blacklist.setStatus("REMOVED");
            ipBlacklistMapper.updateById(blacklist);
            log.info("删除IP黑名单: {}", id);
        }
    }
    
    @Override
    public boolean isIpWhitelisted(String ipAddress, Long userId) {
        List<IpWhitelist> whitelists = ipWhitelistMapper.selectEnabled();
        for (IpWhitelist whitelist : whitelists) {
            if (matchIp(ipAddress, whitelist.getIpAddress(), whitelist.getIpRange())) {
                if ("ALL".equals(whitelist.getApplyTo())) {
                    return true;
                }
                if ("USER".equals(whitelist.getApplyTo()) && userId != null && userId.equals(whitelist.getTargetId())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean isIpBlacklisted(String ipAddress) {
        List<IpBlacklist> blacklists = ipBlacklistMapper.selectActive();
        for (IpBlacklist blacklist : blacklists) {
            if (matchIp(ipAddress, blacklist.getIpAddress(), blacklist.getIpRange())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredBlacklist() {
        List<IpBlacklist> expired = ipBlacklistMapper.selectExpired();
        int count = 0;
        for (IpBlacklist blacklist : expired) {
            blacklist.setStatus("EXPIRED");
            ipBlacklistMapper.updateById(blacklist);
            count++;
        }
        log.info("清理过期IP黑名单: {} 条", count);
        return count;
    }
    
    /**
     * 匹配IP地址
     */
    private boolean matchIp(String ipAddress, String targetIp, String ipRange) {
        if (ipAddress.equals(targetIp)) {
            return true;
        }
        
        if (StringUtils.hasText(ipRange)) {
            // 简化实现：实际应该使用CIDR匹配
            String prefix = ipRange.split("/")[0];
            return ipAddress.startsWith(prefix.substring(0, prefix.lastIndexOf('.')));
        }
        
        return false;
    }
}
