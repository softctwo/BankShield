package com.bankshield.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            // 如果是字符串类型且为数字，则转换为Long
            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            // 如果是自定义的UserDetails对象，需要根据实际情况获取ID
            // 例如：return ((UserDetails) principal).getUserId();

            return null;
        } catch (Exception e) {
            return null;
        }
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
