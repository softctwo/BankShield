package com.bankshield.api.aspect;

import com.bankshield.api.annotation.RoleExclusive;
import com.bankshield.api.entity.RoleMutex;
import com.bankshield.api.service.RoleCheckService;
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
            String conflictInfo = conflicts.stream()
                    .map(mutex -> String.format("%s与%s互斥", mutex.getRoleCode(), mutex.getMutexRoleCode()))
                    .collect(java.util.stream.Collectors.joining(", "));
            
            String errorMsg = String.format("角色分配违反三权分立原则：%s", conflictInfo);
            log.warn(errorMsg);
            
            if (roleExclusive.forceCheck()) {
                throw new BusinessException(ResultCode.ROLE_MUTEX_CONFLICT, errorMsg);
            } else {
                // 非强制检查，仅记录违规
                roleCheckService.recordRoleViolation(userId, roleCode, 
                        conflicts.get(0).getMutexRoleCode(), 1, null, "system");
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

        // 检查用户是否具有互斥的角色
        List<String> userRoles = roleCheckService.getAllRoleMutexRules().stream()
                .map(RoleMutex::getRoleCode)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        List<String> conflictingRoles = Arrays.stream(mutuallyExclusiveRoles)
                .filter(userRoles::contains)
                .collect(java.util.stream.Collectors.toList());

        if (conflictingRoles.size() > 1) {
            String errorMsg = String.format("用户同时具有互斥角色：%s，违反三权分立原则", 
                    String.join(", ", conflictingRoles));
            log.warn(errorMsg);
            
            if (roleExclusive.forceCheck()) {
                throw new BusinessException(ResultCode.ROLE_MUTEX_CONFLICT, errorMsg);
            }
        }
    }

    /**
     * 获取当前用户ID
     * 这里需要根据实际的用户认证机制实现
     */
    private Long getCurrentUserId() {
        // TODO: 实现获取当前用户ID的逻辑
        // 可以通过Spring Security Context、Session等方式获取
        return null;
    }
}