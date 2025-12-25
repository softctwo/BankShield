package com.bankshield.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色互斥注解
 * 用于标记需要进行角色互斥检查的方法
 * 实现等保三级要求的三权分立机制
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleExclusive {
    
    /**
     * 互斥的角色列表
     * 例如：{"SYSTEM_ADMIN", "SECURITY_ADMIN", "AUDIT_ADMIN"}
     * 
     * @return 互斥角色数组
     */
    String[] mutuallyExclusiveRoles() default {};
    
    /**
     * 检查类型
     * ASSIGN: 角色分配检查
     * OPERATION: 操作权限检查
     * 
     * @return 检查类型
     */
    CheckType checkType() default CheckType.ASSIGN;
    
    /**
     * 是否强制检查
     * true: 发现冲突时抛出异常
     * false: 发现冲突时记录违规但不阻止操作
     * 
     * @return 是否强制检查
     */
    boolean forceCheck() default true;
    
    /**
     * 检查类型枚举
     */
    enum CheckType {
        /**
         * 角色分配检查
         */
        ASSIGN,
        
        /**
         * 操作权限检查
         */
        OPERATION
    }
}