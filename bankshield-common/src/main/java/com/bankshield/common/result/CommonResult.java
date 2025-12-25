package com.bankshield.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用响应结果类（别名）
 * 为了兼容性而保留，实际上与 Result 相同
 *
 * @author BankShield
 * @version 1.0.0
 * @deprecated 请使用 {@link Result} 代替
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "通用响应结果")
@Deprecated
public class CommonResult<T> implements Serializable {

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
    public static <T> CommonResult<T> success() {
        return success(null);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> CommonResult<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功响应，带数据和消息
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return CommonResult.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> CommonResult<T> error() {
        return error("操作失败");
    }

    /**
     * 失败响应，带消息
     */
    public static <T> CommonResult<T> error(String message) {
        return error(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 失败响应，带编码和消息
     */
    public static <T> CommonResult<T> error(Integer code, String message) {
        return CommonResult.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应，带结果码
     */
    public static <T> CommonResult<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应 - failed别名
     */
    public static <T> CommonResult<T> failed() {
        return failed("操作失败");
    }

    /**
     * 失败响应，带消息 - failed别名
     */
    public static <T> CommonResult<T> failed(String message) {
        return error(ResultCode.ERROR.getCode(), message);
    }

    /**
     * 从 Result 转换
     */
    public static <T> CommonResult<T> from(Result<T> result) {
        return CommonResult.<T>builder()
                .code(result.getCode())
                .message(result.getMessage())
                .data(result.getData())
                .timestamp(result.getTimestamp())
                .build();
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
