package com.bankshield.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功响应
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    UNPROCESSABLE_ENTITY(422, "请求参数验证失败"),
    
    // 服务器错误 5xx
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误码 1000-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    OLD_PASSWORD_ERROR(1004, "原密码错误"),
    USERNAME_EXIST(1005, "用户名已存在"),
    PHONE_EXIST(1006, "手机号已存在"),
    EMAIL_EXIST(1007, "邮箱已存在"),
    
    // 角色相关错误 1100-1199
    ROLE_NOT_FOUND(1101, "角色不存在"),
    ROLE_EXIST(1102, "角色已存在"),
    ROLE_DISABLED(1103, "角色已被禁用"),
    ROLE_MUTEX_CONFLICT(1104, "角色互斥冲突"),
    ROLE_CHECK_ERROR(1105, "角色检查失败"),
    ROLE_VIOLATION_NOT_FOUND(1106, "违规记录不存在"),
    ROLE_VIOLATION_RECORD_ERROR(1107, "违规记录失败"),
    ROLE_VIOLATION_HANDLE_ERROR(1108, "违规处理失败"),
    ROLE_VIOLATION_QUERY_ERROR(1109, "违规查询失败"),
    ROLE_MUTEX_QUERY_ERROR(1110, "互斥规则查询失败"),
    ROLE_ASSIGN_ERROR(1111, "角色分配失败"),
    ROLE_ALREADY_ASSIGNED(1112, "用户已拥有该角色"),
    
    // 部门相关错误 1200-1299
    DEPT_NOT_FOUND(1201, "部门不存在"),
    DEPT_EXIST(1202, "部门已存在"),
    DEPT_HAS_CHILD(1203, "部门存在下级部门"),
    DEPT_HAS_USER(1204, "部门存在用户"),
    
    // 菜单相关错误 1300-1399
    MENU_NOT_FOUND(1301, "菜单不存在"),
    MENU_EXIST(1302, "菜单已存在"),
    MENU_HAS_CHILD(1303, "菜单存在下级菜单"),
    
    // 数据安全相关错误 1400-1499
    ENCRYPT_ERROR(1401, "加密失败"),
    DECRYPT_ERROR(1402, "解密失败"),
    KEY_NOT_FOUND(1403, "密钥不存在"),
    KEY_EXPIRED(1404, "密钥已过期"),
    DATA_TYPE_NOT_FOUND(1405, "数据类型不存在"),
    MASK_RULE_ERROR(1406, "脱敏规则错误"),
    
    // 审计相关错误 1500-1599
    AUDIT_LOG_ERROR(1501, "审计日志记录失败"),
    AUDIT_REPORT_ERROR(1502, "审计报表生成失败"),
    
    // 系统配置相关错误 1600-1699
    CONFIG_NOT_FOUND(1601, "配置项不存在"),
    CONFIG_ERROR(1602, "配置错误"),
    
    // 文件相关错误 1700-1799
    FILE_NOT_FOUND(1701, "文件不存在"),
    FILE_UPLOAD_ERROR(1702, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(1703, "文件下载失败"),
    
    // 网络相关错误 1800-1899
    NETWORK_ERROR(1801, "网络错误"),
    REMOTE_SERVICE_ERROR(1802, "远程服务调用失败"),
    
    // 数据库相关错误 1900-1999
    DATABASE_ERROR(1901, "数据库错误"),
    DATA_INTEGRITY_ERROR(1902, "数据完整性错误"),
    
    // 其他错误
    PARAMETER_ERROR(2001, "参数错误"),
    VALIDATION_ERROR(2002, "验证失败"),
    BUSINESS_ERROR(2003, "业务处理失败"),
    SYSTEM_ERROR(2004, "系统错误"),
    UNKNOWN_ERROR(9999, "未知错误");

    private final Integer code;
    private final String message;
}