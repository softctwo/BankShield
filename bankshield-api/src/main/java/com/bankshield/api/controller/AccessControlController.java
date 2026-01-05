package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;
import com.bankshield.api.service.AccessControlService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 访问控制管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/access-control")
@RequiredArgsConstructor
@Api(tags = "访问控制管理")
public class AccessControlController {
    
    private final AccessControlService accessControlService;
    
    // ==================== 访问策略管理 ====================
    
    @GetMapping("/policies")
    @ApiOperation("分页查询访问策略")
    @PreAuthorize("hasAuthority('access:policy:query')")
    public Result<IPage<AccessPolicy>> getPolicies(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String policyName,
            @RequestParam(required = false) String policyType,
            @RequestParam(required = false) String status) {
        try {
            Page<AccessPolicy> page = new Page<>(current, size);
            IPage<AccessPolicy> result = accessControlService.pagePolicies(page, policyName, policyType, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询访问策略失败", e);
            return Result.error("查询访问策略失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/policies/{id}")
    @ApiOperation("查询策略详情")
    @PreAuthorize("hasAuthority('access:policy:query')")
    public Result<AccessPolicy> getPolicyById(@PathVariable Long id) {
        try {
            AccessPolicy policy = accessControlService.getPolicyByCode(String.valueOf(id));
            return Result.success(policy);
        } catch (Exception e) {
            log.error("查询策略详情失败", e);
            return Result.error("查询策略详情失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/policies")
    @ApiOperation("新增访问策略")
    @PreAuthorize("hasAuthority('access:policy:add')")
    public Result<Void> createPolicy(@Valid @RequestBody AccessPolicy policy) {
        try {
            accessControlService.createPolicy(policy);
            return Result.success();
        } catch (Exception e) {
            log.error("新增访问策略失败", e);
            return Result.error("新增访问策略失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/policies/{id}")
    @ApiOperation("更新访问策略")
    @PreAuthorize("hasAuthority('access:policy:edit')")
    public Result<Void> updatePolicy(@PathVariable Long id, @Valid @RequestBody AccessPolicy policy) {
        try {
            policy.setId(id);
            accessControlService.updatePolicy(policy);
            return Result.success();
        } catch (Exception e) {
            log.error("更新访问策略失败", e);
            return Result.error("更新访问策略失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/policies/{id}")
    @ApiOperation("删除访问策略")
    @PreAuthorize("hasAuthority('access:policy:delete')")
    public Result<Void> deletePolicy(@PathVariable Long id) {
        try {
            accessControlService.deletePolicy(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除访问策略失败", e);
            return Result.error("删除访问策略失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/policies/{id}/status")
    @ApiOperation("更新策略状态")
    @PreAuthorize("hasAuthority('access:policy:edit')")
    public Result<Void> updatePolicyStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            accessControlService.updatePolicyStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新策略状态失败", e);
            return Result.error("更新策略状态失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/policies/{id}/rules")
    @ApiOperation("查询策略规则")
    @PreAuthorize("hasAuthority('access:policy:query')")
    public Result<List<AccessRule>> getPolicyRules(@PathVariable Long id) {
        try {
            List<AccessRule> rules = accessControlService.getRulesByPolicyId(id);
            return Result.success(rules);
        } catch (Exception e) {
            log.error("查询策略规则失败", e);
            return Result.error("查询策略规则失败: " + e.getMessage());
        }
    }
    
    // ==================== 访问规则管理 ====================
    
    @GetMapping("/rules")
    @ApiOperation("分页查询访问规则")
    @PreAuthorize("hasAuthority('access:rule:query')")
    public Result<IPage<AccessRule>> getRules(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long policyId,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String status) {
        try {
            Page<AccessRule> page = new Page<>(current, size);
            IPage<AccessRule> result = accessControlService.pageRules(page, policyId, ruleType, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询访问规则失败", e);
            return Result.error("查询访问规则失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/rules")
    @ApiOperation("新增访问规则")
    @PreAuthorize("hasAuthority('access:rule:add')")
    public Result<Void> createRule(@Valid @RequestBody AccessRule rule) {
        try {
            accessControlService.createRule(rule);
            return Result.success();
        } catch (Exception e) {
            log.error("新增访问规则失败", e);
            return Result.error("新增访问规则失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/rules/{id}")
    @ApiOperation("更新访问规则")
    @PreAuthorize("hasAuthority('access:rule:edit')")
    public Result<Void> updateRule(@PathVariable Long id, @Valid @RequestBody AccessRule rule) {
        try {
            rule.setId(id);
            accessControlService.updateRule(rule);
            return Result.success();
        } catch (Exception e) {
            log.error("更新访问规则失败", e);
            return Result.error("更新访问规则失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/rules/{id}")
    @ApiOperation("删除访问规则")
    @PreAuthorize("hasAuthority('access:rule:delete')")
    public Result<Void> deleteRule(@PathVariable Long id) {
        try {
            accessControlService.deleteRule(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除访问规则失败", e);
            return Result.error("删除访问规则失败: " + e.getMessage());
        }
    }
    
    // ==================== 访问控制核心 ====================
    
    @PostMapping("/check")
    @ApiOperation("检查访问权限")
    public Result<Boolean> checkAccess(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam String resourceType,
            @RequestParam String resourceId,
            @RequestParam String action,
            @RequestParam(required = false, defaultValue = "false") Boolean mfaVerified,
            HttpServletRequest request) {
        try {
            String ipAddress = getClientIp(request);
            boolean hasAccess = accessControlService.checkAccess(
                userId, username, resourceType, resourceId, action, ipAddress, mfaVerified
            );
            return Result.success(hasAccess);
        } catch (Exception e) {
            log.error("检查访问权限失败", e);
            return Result.error("检查访问权限失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/logs")
    @ApiOperation("查询访问日志")
    @PreAuthorize("hasAuthority('access:log:query')")
    public Result<Map<String, Object>> getAccessLogs(
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            Map<String, Object> statistics = accessControlService.getAccessStatistics(days);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询访问日志失败", e);
            return Result.error("查询访问日志失败: " + e.getMessage());
        }
    }
    
    // ==================== 临时权限管理 ====================
    
    @PostMapping("/temp-permissions")
    @ApiOperation("授予临时权限")
    @PreAuthorize("hasAuthority('access:temp:grant')")
    public Result<Void> grantTemporaryPermission(@Valid @RequestBody TemporaryPermission permission) {
        try {
            accessControlService.grantTemporaryPermission(permission);
            return Result.success();
        } catch (Exception e) {
            log.error("授予临时权限失败", e);
            return Result.error("授予临时权限失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/temp-permissions/{id}")
    @ApiOperation("撤销临时权限")
    @PreAuthorize("hasAuthority('access:temp:revoke')")
    public Result<Void> revokeTemporaryPermission(@PathVariable Long id) {
        try {
            accessControlService.revokeTemporaryPermission(id);
            return Result.success();
        } catch (Exception e) {
            log.error("撤销临时权限失败", e);
            return Result.error("撤销临时权限失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/temp-permissions/user/{userId}")
    @ApiOperation("查询用户临时权限")
    @PreAuthorize("hasAuthority('access:temp:query')")
    public Result<List<TemporaryPermission>> getUserPermissions(@PathVariable Long userId) {
        try {
            List<TemporaryPermission> permissions = accessControlService.getUserActivePermissions(userId);
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("查询用户临时权限失败", e);
            return Result.error("查询用户临时权限失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/temp-permissions/cleanup")
    @ApiOperation("清理过期临时权限")
    @PreAuthorize("hasAuthority('access:temp:manage')")
    public Result<Integer> cleanupExpiredPermissions() {
        try {
            int count = accessControlService.cleanupExpiredPermissions();
            return Result.success(count);
        } catch (Exception e) {
            log.error("清理过期临时权限失败", e);
            return Result.error("清理过期临时权限失败: " + e.getMessage());
        }
    }
    
    // ==================== MFA管理 ====================
    
    @PostMapping("/mfa/config")
    @ApiOperation("配置MFA")
    @PreAuthorize("hasAuthority('access:mfa:config')")
    public Result<Void> configureMfa(@Valid @RequestBody MfaConfig config) {
        try {
            accessControlService.configureMfa(config);
            return Result.success();
        } catch (Exception e) {
            log.error("配置MFA失败", e);
            return Result.error("配置MFA失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/mfa/toggle")
    @ApiOperation("启用/禁用MFA")
    @PreAuthorize("hasAuthority('access:mfa:config')")
    public Result<Void> toggleMfa(
            @RequestParam Long userId,
            @RequestParam String mfaType,
            @RequestParam Boolean enabled) {
        try {
            accessControlService.toggleMfa(userId, mfaType, enabled);
            return Result.success();
        } catch (Exception e) {
            log.error("切换MFA状态失败", e);
            return Result.error("切换MFA状态失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/mfa/verify")
    @ApiOperation("验证MFA")
    public Result<Boolean> verifyMfa(
            @RequestParam Long userId,
            @RequestParam String mfaType,
            @RequestParam String code) {
        try {
            boolean verified = accessControlService.verifyMfa(userId, mfaType, code);
            return Result.success(verified);
        } catch (Exception e) {
            log.error("验证MFA失败", e);
            return Result.error("验证MFA失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/mfa/totp-secret")
    @ApiOperation("生成TOTP密钥")
    @PreAuthorize("hasAuthority('access:mfa:config')")
    public Result<String> generateTotpSecret() {
        try {
            String secret = accessControlService.generateTotpSecret();
            return Result.success(secret);
        } catch (Exception e) {
            log.error("生成TOTP密钥失败", e);
            return Result.error("生成TOTP密钥失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/mfa/backup-codes")
    @ApiOperation("生成备用验证码")
    @PreAuthorize("hasAuthority('access:mfa:config')")
    public Result<List<String>> generateBackupCodes() {
        try {
            List<String> codes = accessControlService.generateBackupCodes();
            return Result.success(codes);
        } catch (Exception e) {
            log.error("生成备用验证码失败", e);
            return Result.error("生成备用验证码失败: " + e.getMessage());
        }
    }
    
    // ==================== IP访问控制 ====================
    
    @PostMapping("/ip/whitelist")
    @ApiOperation("添加IP白名单")
    @PreAuthorize("hasAuthority('access:ip:manage')")
    public Result<Void> addIpWhitelist(@Valid @RequestBody IpWhitelist whitelist) {
        try {
            accessControlService.addIpWhitelist(whitelist);
            return Result.success();
        } catch (Exception e) {
            log.error("添加IP白名单失败", e);
            return Result.error("添加IP白名单失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/ip/whitelist/{id}")
    @ApiOperation("删除IP白名单")
    @PreAuthorize("hasAuthority('access:ip:manage')")
    public Result<Void> removeIpWhitelist(@PathVariable Long id) {
        try {
            accessControlService.removeIpWhitelist(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除IP白名单失败", e);
            return Result.error("删除IP白名单失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/ip/blacklist")
    @ApiOperation("添加IP黑名单")
    @PreAuthorize("hasAuthority('access:ip:manage')")
    public Result<Void> addIpBlacklist(@Valid @RequestBody IpBlacklist blacklist) {
        try {
            accessControlService.addIpBlacklist(blacklist);
            return Result.success();
        } catch (Exception e) {
            log.error("添加IP黑名单失败", e);
            return Result.error("添加IP黑名单失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/ip/blacklist/{id}")
    @ApiOperation("删除IP黑名单")
    @PreAuthorize("hasAuthority('access:ip:manage')")
    public Result<Void> removeIpBlacklist(@PathVariable Long id) {
        try {
            accessControlService.removeIpBlacklist(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除IP黑名单失败", e);
            return Result.error("删除IP黑名单失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/ip/check")
    @ApiOperation("检查IP状态")
    public Result<Map<String, Boolean>> checkIpStatus(
            @RequestParam String ipAddress,
            @RequestParam(required = false) Long userId) {
        try {
            boolean whitelisted = accessControlService.isIpWhitelisted(ipAddress, userId);
            boolean blacklisted = accessControlService.isIpBlacklisted(ipAddress);
            
            Map<String, Boolean> result = Map.of(
                "whitelisted", whitelisted,
                "blacklisted", blacklisted,
                "allowed", whitelisted && !blacklisted
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查IP状态失败", e);
            return Result.error("检查IP状态失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/ip/blacklist/cleanup")
    @ApiOperation("清理过期IP黑名单")
    @PreAuthorize("hasAuthority('access:ip:manage')")
    public Result<Integer> cleanupExpiredBlacklist() {
        try {
            int count = accessControlService.cleanupExpiredBlacklist();
            return Result.success(count);
        } catch (Exception e) {
            log.error("清理过期IP黑名单失败", e);
            return Result.error("清理过期IP黑名单失败: " + e.getMessage());
        }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
