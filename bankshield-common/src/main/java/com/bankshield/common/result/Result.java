package com.bankshield.common.result;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = code == 200;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    /**
     * 成功响应（OK别名）
     */
    public static <T> Result<T> OK() {
        return success();
    }
    
    /**
     * 成功响应（OK别名，带数据）
     */
    public static <T> Result<T> OK(T data) {
        return success(data);
    }
    
    /**
     * 成功响应（OK别名，带消息和数据）
     */
    public static <T> Result<T> OK(String message, T data) {
        return success(message, data);
    }
    
    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    /**
     * 成功响应（带数据和消息，参数顺序相反）
     * 注意：当数据类型为String时，请使用success(String message, T data)方法
     */
    public static <T> Result<T> successData(T data, String message) {
        return new Result<>(200, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }
    
    /**
     * 失败响应（带消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    /**
     * 失败响应（带错误码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 失败响应（使用ResultCode）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
    
    /**
     * 失败响应（使用ResultCode和自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }
    
    /**
     * 失败响应（failed别名）
     */
    public static <T> Result<T> failed() {
        return error();
    }
    
    /**
     * 失败响应（failed别名，带消息）
     */
    public static <T> Result<T> failed(String message) {
        return error(message);
    }
    
    /**
     * 失败响应（failed别名，带错误码和消息）
     */
    public static <T> Result<T> failed(Integer code, String message) {
        return error(code, message);
    }
    
    /**
     * 自定义响应
     */
    public static <T> Result<T> build(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    // Getter and Setter
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
        this.success = code == 200;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.success != null && this.success;
    }
}
