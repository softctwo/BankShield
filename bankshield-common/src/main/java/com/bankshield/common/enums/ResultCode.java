package com.bankshield.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "成功"),
    
    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不被允许"),
    
    // 服务端错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误
    BUSINESS_ERROR(1001, "业务处理失败"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_EXIST(1003, "数据已存在"),
    PARAMETER_ERROR(1004, "参数校验失败"),
    OPERATION_FAILED(1005, "操作失败"),
    
    // 认证授权错误
    LOGIN_FAILED(2001, "登录失败"),
    TOKEN_INVALID(2002, "Token无效"),
    TOKEN_EXPIRED(2003, "Token已过期"),
    ACCESS_DENIED(2004, "访问被拒绝"),
    
    // 加密相关错误
    ENCRYPT_ERROR(3001, "加密失败"),
    DECRYPT_ERROR(3002, "解密失败"),
    SIGNATURE_ERROR(3003, "签名验证失败"),
    
    // 数据脱敏错误
    DESENSITIZE_ERROR(4001, "数据脱敏失败");
    
    private final Integer code;
    private final String message;
}