package com.bankshield.api.service.impl;

import com.bankshield.api.entity.RoleMutex;
import com.bankshield.api.entity.RoleViolation;
import com.bankshield.api.entity.User;
import com.bankshield.api.mapper.RoleMutexMapper;
import com.bankshield.api.mapper.RoleViolationMapper;
import com.bankshield.api.mapper.UserMapper;
import com.bankshield.api.service.RoleCheckService;
import com.bankshield.common.exception.BusinessException;
import com.bankshield.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色互斥检查服务实现类
 * 实现三权分立机制的核心业务逻辑
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCheckServiceImpl implements RoleCheckService {

    private final RoleMutexMapper roleMutexMapper;
    private final RoleViolationMapper roleViolationMapper;
    private final UserMapper userMapper;

    @Value("${role.mutex.enabled:true}")
    private boolean roleMutexEnabled;

    @Value("${role.violation.alert.enabled:true}")
    private boolean violationAlertEnabled;

    /**
     * 三权分立核心角色定义
     */
    private static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    private static final String SECURITY_ADMIN = "SECURITY_ADMIN";
    private static final String AUDIT_ADMIN = "AUDIT_ADMIN";

    @Override
    public boolean checkRoleAssignment(Long userId, String roleCode) {
        if (!roleMutexEnabled) {
            log.info("角色互斥检查已关闭，允许角色分配");
            return true;
        }

        try {
            // 获取用户当前角色
            List<String> userRoles = userMapper.selectRoleCodesByUserId(userId);
            
            // 检查新角色是否与现有角色互斥
            for (String existingRole : userRoles) {
                if (isRoleMutex(existingRole, roleCode)) {
                    log.warn("角色分配冲突：用户ID={}, 现有角色={}, 待分配角色={}", userId, existingRole, roleCode);
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("检查角色分配时发生异常", e);
            throw new BusinessException(ResultCode.ROLE_CHECK_ERROR);
        }
    }

    @Override
    public List<RoleMutex> checkUserRoleConflicts(Long userId) {
        try {
            return roleMutexMapper.selectUserRoleConflicts(userId);
        } catch (Exception e) {
            log.error("检查用户角色冲突时发生异常，用户ID: " + userId, e);
            throw new BusinessException(ResultCode.ROLE_CHECK_ERROR);
        }
    }

    @Override
    public boolean isRoleMutex(String roleCode1, String roleCode2) {
        if (!roleMutexEnabled) {
            return false;
        }

        try {
            // 相同角色不互斥
            if (roleCode1.equals(roleCode2)) {
                return false;
            }

            int count = roleMutexMapper.countMutexRoles(roleCode1, roleCode2);
            return count > 0;
        } catch (Exception e) {
            log.error("检查角色互斥时发生异常，角色1: {}, 角色2: {}", roleCode1, roleCode2, e);
            throw new BusinessException(ResultCode.ROLE_CHECK_ERROR);
        }
    }

    @Override
    @Transactional
    public void recordRoleViolation(Long userId, String roleCode, String mutexRoleCode, 
                                  Integer violationType, Long operatorId, String operatorName) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("用户不存在，ID: {}", userId);
                return;
            }

            // 创建违规记录
            RoleViolation violation = RoleViolation.builder()
                    .userId(userId)
                    .username(user.getUsername())
                    .roleCode(roleCode)
                    .mutexRoleCode(mutexRoleCode)
                    .violationType(violationType)
                    .violationTime(LocalDateTime.now())
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .status(0) // 未处理
                    .alertSent(0) // 未发送告警
                    .build();

            roleViolationMapper.insert(violation);
            log.warn("记录角色违规：用户={}, 违规角色={}, 互斥角色={}, 操作人={}", 
                    user.getUsername(), roleCode, mutexRoleCode, operatorName);

            // 发送违规告警
            if (violationAlertEnabled) {
                sendViolationAlert(violation);
            }
        } catch (Exception e) {
            log.error("记录角色违规时发生异常", e);
            throw new BusinessException(ResultCode.ROLE_VIOLATION_RECORD_ERROR);
        }
    }

    @Override
    public List<RoleMutex> getAllRoleMutexRules() {
        try {
            return roleMutexMapper.selectList(null);
        } catch (Exception e) {
            log.error("获取角色互斥规则时发生异常", e);
            throw new BusinessException(ResultCode.ROLE_MUTEX_QUERY_ERROR);
        }
    }

    @Override
    public List<String> getMutexRoles(String roleCode) {
        try {
            return roleMutexMapper.selectMutexRoleCodesByRoleCode(roleCode);
        } catch (Exception e) {
            log.error("获取互斥角色时发生异常，角色编码: {}", roleCode, e);
            throw new BusinessException(ResultCode.ROLE_MUTEX_QUERY_ERROR);
        }
    }

    @Override
    @Transactional
    public void handleRoleViolation(Long violationId, Integer status, String handleRemark, String handlerName) {
        try {
            RoleViolation violation = roleViolationMapper.selectById(violationId);
            if (violation == null) {
                throw new BusinessException(ResultCode.ROLE_VIOLATION_NOT_FOUND);
            }

            violation.setStatus(status);
            violation.setHandleRemark(handleRemark);
            violation.setHandleTime(LocalDateTime.now());

            roleViolationMapper.updateById(violation);
            
            log.info("处理角色违规记录：ID={}, 状态={}, 处理人={}, 备注={}", 
                    violationId, status, handlerName, handleRemark);
        } catch (Exception e) {
            log.error("处理角色违规记录时发生异常，ID: " + violationId, e);
            throw new BusinessException(ResultCode.ROLE_VIOLATION_HANDLE_ERROR);
        }
    }

    @Override
    public List<RoleViolation> getUnhandledViolations() {
        try {
            return roleViolationMapper.selectUnhandledViolations();
        } catch (Exception e) {
            log.error("获取未处理违规记录时发生异常", e);
            throw new BusinessException(ResultCode.ROLE_VIOLATION_QUERY_ERROR);
        }
    }

    @Override
    public void sendViolationAlert(RoleViolation violation) {
        try {
            // 这里可以实现具体的告警逻辑，如发送邮件、短信、企业微信等
            log.warn("发送角色违规告警：用户={}, 违规角色={}, 互斥角色={}", 
                    violation.getUsername(), violation.getRoleCode(), violation.getMutexRoleCode());

            // 更新告警发送状态
            roleViolationMapper.updateAlertStatus(violation.getId(), 1);
        } catch (Exception e) {
            log.error("发送违规告警时发生异常", e);
        }
    }

    @Override
    public void executeRoleCheckJob() {
        log.info("开始执行角色互斥检查任务");
        
        try {
            // 获取所有用户
            List<User> users = userMapper.selectList(null);
            
            for (User user : users) {
                // 检查用户角色冲突
                List<RoleMutex> conflicts = checkUserRoleConflicts(user.getId());
                
                if (!conflicts.isEmpty()) {
                    log.warn("发现用户角色冲突：用户ID={}, 用户名={}, 冲突数量={}", 
                            user.getId(), user.getUsername(), conflicts.size());
                    
                    // 记录违规
                    for (RoleMutex conflict : conflicts) {
                        // 检查是否已存在相同的未处理违规记录
                        List<RoleViolation> existingViolations = roleViolationMapper.selectByUserId(user.getId(), 0);
                        boolean exists = existingViolations.stream()
                                .anyMatch(v -> v.getRoleCode().equals(conflict.getRoleCode()) 
                                        && v.getMutexRoleCode().equals(conflict.getMutexRoleCode()));
                        
                        if (!exists) {
                            recordRoleViolation(user.getId(), conflict.getRoleCode(), 
                                              conflict.getMutexRoleCode(), 2, null, "system");
                        }
                    }
                }
            }
            
            log.info("角色互斥检查任务执行完成");
        } catch (Exception e) {
            log.error("执行角色互斥检查任务时发生异常", e);
        }
    }

    /**
     * 异步执行角色互斥检查任务
     * 使用线程池管理，防止OOM
     */
    @Override
    @Async("roleCheckExecutor")
    public void executeRoleCheckJobAsync() {
        log.info("开始异步执行角色互斥检查任务");
        
        try {
            executeRoleCheckJob();
            log.info("异步角色互斥检查任务执行完成");
        } catch (Exception e) {
            log.error("异步执行角色互斥检查任务时发生异常", e);
            // 异常信息会被记录，但不会影响调用方
        }
    }

    @Override
    public String getSeparationOfPowersStatus() {
        try {
            // 统计各类违规情况
            int totalViolations = roleViolationMapper.countViolations(null);
            int unhandledViolations = roleViolationMapper.countViolations(0);
            int handledViolations = roleViolationMapper.countViolations(1);
            
            // 获取三权分立角色数量
            long systemAdminCount = userMapper.countUsersByRoleCode(SYSTEM_ADMIN);
            long securityAdminCount = userMapper.countUsersByRoleCode(SECURITY_ADMIN);
            long auditAdminCount = userMapper.countUsersByRoleCode(AUDIT_ADMIN);
            
            return String.format("三权分立状态：系统管理员=%d人，安全策略员=%d人，合规审计员=%d人，" +
                    "总违规记录=%d条，未处理=%d条，已处理=%d条",
                    systemAdminCount, securityAdminCount, auditAdminCount,
                    totalViolations, unhandledViolations, handledViolations);
        } catch (Exception e) {
            log.error("获取三权分立状态时发生异常", e);
            return "获取状态失败";
        }
    }
}