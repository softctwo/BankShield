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
    SYSTEM_ERROR(800, "系统异常");
    
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
