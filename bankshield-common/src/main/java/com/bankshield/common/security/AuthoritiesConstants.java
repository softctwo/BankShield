package com.bankshield.common.security;

/**
 * 权限常量定义
 * 定义系统中使用的角色和权限常量
 */
public final class AuthoritiesConstants {
    
    // 角色定义
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SECURITY_ADMIN = "ROLE_SECURITY_ADMIN";
    public static final String ROLE_AUDIT_ADMIN = "ROLE_AUDIT_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    
    // 权限定义
    public static final String AUTHORITY_DECRYPT = "DECRYPT";
    public static final String AUTHORITY_ENCRYPT = "ENCRYPT";
    public static final String AUTHORITY_KEY_MANAGE = "KEY_MANAGE";
    public static final String AUTHORITY_VAULT_ACCESS = "VAULT_ACCESS";
    public static final String AUTHORITY_SECURITY_SCAN = "SECURITY_SCAN";
    
    private AuthoritiesConstants() {
        // 私有构造函数防止实例化
    }
}