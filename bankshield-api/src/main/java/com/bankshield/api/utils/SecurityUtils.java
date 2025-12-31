package com.bankshield.api.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 安全工具类
 * 提供当前用户信息获取等功能
 * 
 * @author BankShield
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public static String getCurrentUsername() {
        // TODO: 实现从Spring Security上下文获取用户信息
        return "admin";
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        // TODO: 实现从Spring Security上下文获取用户ID
        return 1L;
    }

    /**
     * 获取当前用户部门ID
     * @return 部门ID
     */
    public static Long getCurrentDeptId() {
        // TODO: 实现从Spring Security上下文获取部门信息
        return 1L;
    }

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        // TODO: 实现管理员权限检查
        return true;
    }
}
