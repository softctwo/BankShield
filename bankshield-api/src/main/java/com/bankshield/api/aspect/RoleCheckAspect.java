package com.bankshield.api.aspect;

import com.bankshield.api.annotation.RoleExclusive;
import com.bankshield.api.entity.RoleMutex;
import com.bankshield.api.service.RoleCheckService;
import com.bankshield.api.service.RoleService;
import com.bankshield.common.exception.BusinessException;
import com.bankshield.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 角色互斥检查切面
 * 通过AOP实现三权分立机制的角色互斥检查
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class RoleCheckAspect {

    private final RoleCheckService roleCheckService;
    private final RoleService roleService;

    /**
     * 定义切入点：所有标记了@RoleExclusive注解的方法
     */
    @Pointcut("@annotation(com.bankshield.api.annotation.RoleExclusive)")
    public void roleCheckPointcut() {}

    /**
     * 角色互斥检查前置通知
     */
    @Before("roleCheckPointcut()")
    public void doRoleCheck(JoinPoint joinPoint) {
        try {
            // 获取方法签名和注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            RoleExclusive roleExclusive = method.getAnnotation(RoleExclusive.class);
            
            if (roleExclusive == null) {
                return;
            }

            log.info("执行角色互斥检查：方法={}.{}", 
                    method.getDeclaringClass().getSimpleName(), method.getName());

            // 获取方法参数
            Object[] args = joinPoint.getArgs();
            
            // 根据检查类型执行不同的检查逻辑
            switch (roleExclusive.checkType()) {
                case ASSIGN:
                    handleRoleAssignmentCheck(args, roleExclusive);
                    break;
                case OPERATION:
                    handleOperationPermissionCheck(args, roleExclusive);
                    break;
                default:
                    log.warn("未知的检查类型：{}", roleExclusive.checkType());
            }

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("角色互斥检查发生异常", e);
            throw new BusinessException(ResultCode.ROLE_CHECK_ERROR);
        }
    }

    /**
     * 处理角色分配检查
     */
    private void handleRoleAssignmentCheck(Object[] args, RoleExclusive roleExclusive) {
        // 解析参数，获取用户ID和角色编码
        Long userId = null;
        String roleCode = null;
        
        for (Object arg : args) {
            if (arg instanceof Long) {
                userId = (Long) arg;
            } else if (arg instanceof String) {
                roleCode = (String) arg;
            } else if (arg instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> paramMap = (java.util.Map<String, Object>) arg;
                if (paramMap.containsKey("userId")) {
                    userId = Long.valueOf(paramMap.get("userId").toString());
                }
                if (paramMap.containsKey("roleCode")) {
                    roleCode = (String) paramMap.get("roleCode");
                }
            }
        }

        if (userId == null || roleCode == null) {
            log.warn("无法从方法参数中获取用户ID和角色编码，跳过角色互斥检查");
            return;
        }

        // 执行角色互斥检查
        boolean hasConflict = !roleCheckService.checkRoleAssignment(userId, roleCode);
        
        if (hasConflict) {
            List<RoleMutex> conflicts = roleCheckService.checkUserRoleConflicts(userId);
            if (conflicts.isEmpty()) {
                log.warn("检测到角色冲突但冲突列表为空，跳过记录");
                return;
            }
            
            String conflictInfo = conflicts.stream()
                    .map(mutex -> String.format("%s与%s互斥", mutex.getRoleCode(), mutex.getMutexRoleCode()))
                    .collect(java.util.stream.Collectors.joining(", "));
            
            String errorMsg = String.format("角色分配违反三权分立原则：%s", conflictInfo);
            log.warn(errorMsg);
            
            if (roleExclusive.forceCheck()) {
                throw new BusinessException(ResultCode.ROLE_MUTEX_CONFLICT, errorMsg);
            } else {
                // 非强制检查，仅记录违规 - 安全地获取第一个冲突角色
                if (!conflicts.isEmpty()) {
                    RoleMutex firstConflict = conflicts.get(0);
                    roleCheckService.recordRoleViolation(userId, roleCode, 
                            firstConflict.getMutexRoleCode(), 1, null, "system");
                } else {
                    log.warn("检测到角色冲突但冲突列表为空，无法记录违规信息");
                }
            }
        }
    }

    /**
     * 处理操作权限检查
     */
    private void handleOperationPermissionCheck(Object[] args, RoleExclusive roleExclusive) {
        // 这里可以实现操作权限的检查逻辑
        // 例如：检查当前用户是否同时具有互斥的操作权限
        
        String[] mutuallyExclusiveRoles = roleExclusive.mutuallyExclusiveRoles();
        if (mutuallyExclusiveRoles.length == 0) {
            log.debug("未指定互斥角色，跳过操作权限检查");
            return;
        }

        // 获取当前用户（这里需要根据实际的用户上下文获取）
        // 可以通过Spring Security Context或其他方式获取
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            log.warn("无法获取当前用户ID，跳过操作权限检查");
            return;
        }

        // 获取用户实际拥有的角色编码列表 - 修复：只检查用户实际拥有的角色
        List<String> userRoleCodes = null;
        try {
            // 使用RoleService获取用户角色，这是最直接和准确的方式
            com.bankshield.common.result.Result<java.util.List<com.bankshield.api.entity.Role>> roleResult = roleService.getUserRoles(currentUserId);
            if (roleResult.isSuccess() && roleResult.getData() != null) {
                userRoleCodes = roleResult.getData().stream()
                        .map(com.bankshield.api.entity.Role::getRoleCode)
                        .filter(code -> code != null && !code.trim().isEmpty())
                        .collect(java.util.stream.Collectors.toList());
                log.debug("成功获取用户角色列表，用户ID={}, 角色数量={}", currentUserId, userRoleCodes.size());
            } else {
                log.warn("获取用户角色失败，用户ID={}, 错误信息={}", currentUserId, roleResult.getMessage());
            }
        } catch (Exception e) {
            log.error("获取用户角色时发生异常，用户ID=" + currentUserId, e);
        }

        // 如果无法从RoleService获取角色，尝试其他方式
        if (userRoleCodes == null || userRoleCodes.isEmpty()) {
            log.warn("无法从RoleService获取用户角色，尝试从角色互斥检查服务获取");
            try {
                // 从checkUserRoleConflicts中获取用户相关的角色
                List<RoleMutex> conflicts = roleCheckService.checkUserRoleConflicts(currentUserId);
                userRoleCodes = new java.util.ArrayList<>();
                for (RoleMutex conflict : conflicts) {
                    if (conflict.getRoleCode() != null) {
                        userRoleCodes.add(conflict.getRoleCode());
                    }
                }
                // 去重
                userRoleCodes = userRoleCodes.stream().distinct().collect(java.util.stream.Collectors.toList());
                log.debug("从角色冲突检查中获取用户角色列表，用户ID={}, 角色数量={}", currentUserId, userRoleCodes.size());
            } catch (Exception e) {
                log.error("从角色冲突检查获取用户角色时发生异常，用户ID=" + currentUserId, e);
            }
        }

        // 如果还是无法获取用户角色，记录警告并返回
        if (userRoleCodes == null || userRoleCodes.isEmpty()) {
            log.warn("无法获取用户实际拥有的角色，跳过互斥检查，用户ID={}", currentUserId);
            return;
        }

        // 只检查用户实际拥有的角色中是否存在互斥
        List<String> conflictingRoles = Arrays.stream(mutuallyExclusiveRoles)
                .filter(role -> role != null && !role.trim().isEmpty())
                .filter(userRoleCodes::contains)
                .collect(java.util.stream.Collectors.toList());

        if (conflictingRoles.size() > 1) {
            String errorMsg = String.format("用户同时具有互斥角色：%s，违反三权分立原则", 
                    String.join(", ", conflictingRoles));
            log.warn(errorMsg);
            
            if (roleExclusive.forceCheck()) {
                throw new BusinessException(ResultCode.ROLE_MUTEX_CONFLICT, errorMsg);
            }
        } else {
            log.debug("用户角色互斥检查通过，用户ID={}, 检查的角色={}, 用户实际角色={}", 
                    currentUserId, Arrays.toString(mutuallyExclusiveRoles), userRoleCodes);
        }
    }

    /**
     * 获取当前用户ID
     * 从Spring Security Context中获取当前认证的用户信息
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("用户未认证");
                return null;
            }

            // 假设认证对象中包含了用户ID（可以是Principal、credentials或其他属性）
            Object principal = authentication.getPrincipal();

            // 如果是字符串类型且为数字，则转换为Long
            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("无法将用户标识符转换为用户ID: {}", principal);
                }
            }

            // 如果是自定义的UserDetails对象，需要根据实际情况获取ID
            // 例如：return ((UserDetails) principal).getUserId();

            log.warn("无法从认证信息中获取用户ID");
            return null;
        } catch (Exception e) {
            log.error("获取当前用户ID时发生错误: {}", e.getMessage());
            return null;
        }
    }
}