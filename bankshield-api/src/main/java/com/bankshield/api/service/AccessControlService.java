package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 访问控制服务接口
 */
public interface AccessControlService {
    
    // ==================== 访问策略管理 ====================
    
    /**
     * 分页查询访问策略
     */
    IPage<AccessPolicy> pagePolicies(Page<AccessPolicy> page, String policyName, String policyType, String status);
    
    /**
     * 创建访问策略
     */
    void createPolicy(AccessPolicy policy);
    
    /**
     * 更新访问策略
     */
    void updatePolicy(AccessPolicy policy);
    
    /**
     * 删除访问策略
     */
    void deletePolicy(Long id);
    
    /**
     * 更新策略状态
     */
    void updatePolicyStatus(Long id, String status);
    
    /**
     * 根据策略编码查询
     */
    AccessPolicy getPolicyByCode(String policyCode);
    
    // ==================== 访问规则管理 ====================
    
    /**
     * 分页查询访问规则
     */
    IPage<AccessRule> pageRules(Page<AccessRule> page, Long policyId, String ruleType, String status);
    
    /**
     * 创建访问规则
     */
    void createRule(AccessRule rule);
    
    /**
     * 更新访问规则
     */
    void updateRule(AccessRule rule);
    
    /**
     * 删除访问规则
     */
    void deleteRule(Long id);
    
    /**
     * 根据策略ID查询规则
     */
    List<AccessRule> getRulesByPolicyId(Long policyId);
    
    // ==================== 访问控制核心 ====================
    
    /**
     * 检查访问权限
     */
    boolean checkAccess(Long userId, String username, String resourceType, 
                       String resourceId, String action, String ipAddress, boolean mfaVerified);
    
    /**
     * 记录访问日志
     */
    void logAccess(AccessLog accessLog);
    
    /**
     * 获取访问统计
     */
    Map<String, Object> getAccessStatistics(int days);
    
    // ==================== 临时权限管理 ====================
    
    /**
     * 授予临时权限
     */
    void grantTemporaryPermission(TemporaryPermission permission);
    
    /**
     * 撤销临时权限
     */
    void revokeTemporaryPermission(Long id);
    
    /**
     * 查询用户的有效临时权限
     */
    List<TemporaryPermission> getUserActivePermissions(Long userId);
    
    /**
     * 清理过期临时权限
     */
    int cleanupExpiredPermissions();
    
    // ==================== MFA管理 ====================
    
    /**
     * 配置MFA
     */
    void configureMfa(MfaConfig config);
    
    /**
     * 启用/禁用MFA
     */
    void toggleMfa(Long userId, String mfaType, boolean enabled);
    
    /**
     * 验证MFA
     */
    boolean verifyMfa(Long userId, String mfaType, String code);
    
    /**
     * 生成TOTP密钥
     */
    String generateTotpSecret();
    
    /**
     * 生成备用验证码
     */
    List<String> generateBackupCodes();
    
    // ==================== IP访问控制 ====================
    
    /**
     * 添加IP白名单
     */
    void addIpWhitelist(IpWhitelist whitelist);
    
    /**
     * 删除IP白名单
     */
    void removeIpWhitelist(Long id);
    
    /**
     * 添加IP黑名单
     */
    void addIpBlacklist(IpBlacklist blacklist);
    
    /**
     * 删除IP黑名单
     */
    void removeIpBlacklist(Long id);
    
    /**
     * 检查IP是否在白名单
     */
    boolean isIpWhitelisted(String ipAddress, Long userId);
    
    /**
     * 检查IP是否在黑名单
     */
    boolean isIpBlacklisted(String ipAddress);
    
    /**
     * 清理过期的IP黑名单
     */
    int cleanupExpiredBlacklist();
}
