package com.bankshield.common.security;

/**
 * 安全工具类
 *
 * @author BankShield
 */
public class SecurityUtils {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    /**
     * 获取当前用户名
     *
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        String username = CURRENT_USER.get();
        return username != null ? username : "system";
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     */
    public static Long getCurrentUserId() {
        // TODO: 实现从 Spring Security Context 获取当前用户ID
        return 1L;
    }

    /**
     * 设置当前用户
     *
     * @param username 用户名
     */
    public static void setCurrentUser(String username) {
        CURRENT_USER.set(username);
    }

    /**
     * 清除当前用户
     */
    public static void clearCurrentUser() {
        CURRENT_USER.remove();
    }
}
