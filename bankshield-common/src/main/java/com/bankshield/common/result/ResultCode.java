package com.bankshield.common.result;

/**
 * 结果码枚举
 */
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    ERROR(500, "操作失败"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 参数错误（别名）
     */
    PARAMETER_ERROR(400, "参数错误"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "没有权限访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    
    /**
     * 业务异常
     */
    BUSINESS_ERROR(600, "业务异常"),
    
    /**
     * 数据已存在
     */
    DATA_EXISTS(601, "数据已存在"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(602, "数据不存在"),
    
    /**
     * 数据库异常
     */
    DATABASE_ERROR(700, "数据库异常"),
    
    /**
     * 系统异常
     */
    SYSTEM_ERROR(800, "系统异常"),
    
    /**
     * 角色检查错误
     */
    ROLE_CHECK_ERROR(900, "角色检查错误"),
    
    /**
     * 角色互斥冲突
     */
    ROLE_MUTEX_CONFLICT(901, "角色互斥冲突"),
    
    /**
     * 角色违规记录错误
     */
    ROLE_VIOLATION_RECORD_ERROR(902, "角色违规记录错误"),
    
    /**
     * 角色互斥查询错误
     */
    ROLE_MUTEX_QUERY_ERROR(903, "角色互斥查询错误"),
    
    /**
     * 角色违规记录未找到
     */
    ROLE_VIOLATION_NOT_FOUND(904, "角色违规记录未找到"),
    
    /**
     * 角色违规处理错误
     */
    ROLE_VIOLATION_HANDLE_ERROR(905, "角色违规处理错误"),
    
    /**
     * 角色违规查询错误
     */
    ROLE_VIOLATION_QUERY_ERROR(906, "角色违规查询错误"),
    
    /**
     * 用户未找到
     */
    USER_NOT_FOUND(1001, "用户未找到"),
    
    /**
     * 角色未找到
     */
    ROLE_NOT_FOUND(1002, "角色未找到"),
    
    /**
     * 权限未找到
     */
    PERMISSION_NOT_FOUND(1003, "权限未找到"),
    
    /**
     * 密钥未找到
     */
    KEY_NOT_FOUND(1004, "密钥未找到"),
    
    /**
     * 加密失败
     */
    ENCRYPT_ERROR(1005, "加密失败"),
    
    /**
     * 解密失败
     */
    DECRYPT_ERROR(1006, "解密失败");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
