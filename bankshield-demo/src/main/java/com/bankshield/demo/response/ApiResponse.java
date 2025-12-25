package com.bankshield.demo.response;

/**
 * 统一API响应类
 */
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, String message, T data, Long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<T>(200, "操作成功", null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(200, "操作成功", data, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<T>(200, message, data, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error() {
        return new ApiResponse<T>(500, "操作失败", null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<T>(500, message, null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<T>(code, message, null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<T>(400, message, null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<T>(401, message, null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<T>(403, message, null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<T>(404, message, null, System.currentTimeMillis());
    }
}