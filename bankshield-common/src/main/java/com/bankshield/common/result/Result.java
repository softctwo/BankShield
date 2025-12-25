package com.bankshield.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "响应码")
    private Integer code;

    @ApiModelProperty(value = "响应消息")
    private String message;

    @ApiModelProperty(value = "响应数据")
    private T data;

    @ApiModelProperty(value = "时间戳")
    private Long timestamp;

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功响应，带数据和消息
     */
    public static <T> Result<T> success(T data, String message) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * OK响应 - success的别名
     */
    public static <T> Result<T> OK() {
        return OK(null);
    }

    /**
     * OK响应 - success的别名，带数据
     */
    public static <T> Result<T> OK(T data) {
        return OK(data, "操作成功");
    }

    /**
     * OK响应 - success的别名，带数据和消息
     */
    public static <T> Result<T> OK(T data, String message) {
        return success(data, message);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    /**
     * 失败响应，带消息
     */
    public static <T> Result<T> error(String message) {
        return error(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 失败响应，带编码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应，带结果码
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应，带结果码和自定义消息
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return error(resultCode.getCode(), message);
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}