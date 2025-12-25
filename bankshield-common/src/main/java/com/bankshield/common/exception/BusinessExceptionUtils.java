package com.bankshield.common.exception;

import com.bankshield.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务异常工具类
 * 统一异常处理和错误码管理
 */
@Slf4j
public class BusinessExceptionUtils {

    /**
     * 抛出业务异常
     * @param resultCode 错误码
     * @param message 错误消息
     */
    public static void throwBusiness(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode.getCode(), message);
    }

    /**
     * 抛出业务异常（使用错误码默认消息）
     * @param resultCode 错误码
     */
    public static void throwBusiness(ResultCode resultCode) {
        throw new BusinessException(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 抛出数据操作异常
     * @param operation 操作名称
     * @param cause 原因
     */
    public static void throwDataOperation(String operation, String cause) {
        String message = String.format("%s失败: %s", operation, cause);
        log.error(message);
        throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), message);
    }

    /**
     * 抛出数据操作异常（带异常堆栈）
     * @param operation 操作名称
     * @param cause 原因
     * @param throwable 原始异常
     */
    public static void throwDataOperation(String operation, String cause, Throwable throwable) {
        String message = String.format("%s失败: %s", operation, cause);
        log.error(message, throwable);
        throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), message, throwable);
    }

    /**
     * 抛出加密解密异常
     * @param operation 操作名称
     * @param cause 原因
     */
    public static void throwEncryptOperation(String operation, String cause) {
        String message = String.format("%s失败: %s", operation, cause);
        log.error(message);
        throw new BusinessException(ResultCode.ENCRYPT_ERROR.getCode(), message);
    }

    /**
     * 抛出加密解密异常（带异常堆栈）
     * @param operation 操作名称
     * @param cause 原因
     * @param throwable 原始异常
     */
    public static void throwEncryptOperation(String operation, String cause, Throwable throwable) {
        String message = String.format("%s失败: %s", operation, cause);
        log.error(message, throwable);
        throw new BusinessException(ResultCode.ENCRYPT_ERROR.getCode(), message, throwable);
    }

    /**
     * 抛出业务处理异常
     * @param operation 操作名称
     * @param cause 原因
     */
    public static void throwBusinessOperation(String operation, String cause) {
        String message = String.format("%s失败: %s", operation, cause);
        log.error(message);
        throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    /**
     * 抛出业务处理异常（带异常堆栈）
     * @param operation 操作名称
     * @param cause 原因
     * @param throwable 原始异常
     */
    public static void throwBusinessOperation(String operation, String cause, Throwable throwable) {
        String message = String.format("%s失败: %s", operation, cause);
        log.error(message, throwable);
        throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), message, throwable);
    }

    /**
     * 抛出参数验证异常
     * @param parameter 参数名称
     * @param cause 原因
     */
    public static void throwParameterValidation(String parameter, String cause) {
        String message = String.format("参数验证失败 - %s: %s", parameter, cause);
        log.warn(message);
        throw new BusinessException(ResultCode.PARAMETER_ERROR.getCode(), message);
    }

    /**
     * 抛出网络调用异常
     * @param service 服务名称
     * @param cause 原因
     */
    public static void throwNetworkError(String service, String cause) {
        String message = String.format("远程服务调用失败 - %s: %s", service, cause);
        log.error(message);
        throw new BusinessException(ResultCode.REMOTE_SERVICE_ERROR.getCode(), message);
    }

    /**
     * 抛出网络调用异常（带异常堆栈）
     * @param service 服务名称
     * @param cause 原因
     * @param throwable 原始异常
     */
    public static void throwNetworkError(String service, String cause, Throwable throwable) {
        String message = String.format("远程服务调用失败 - %s: %s", service, cause);
        log.error(message, throwable);
        throw new BusinessException(ResultCode.REMOTE_SERVICE_ERROR.getCode(), message, throwable);
    }

    /**
     * 抛出系统异常
     * @param operation 操作名称
     * @param cause 原因
     */
    public static void throwSystemError(String operation, String cause) {
        String message = String.format("系统错误 - %s: %s", operation, cause);
        log.error(message);
        throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 抛出系统异常（带异常堆栈）
     * @param operation 操作名称
     * @param cause 原因
     * @param throwable 原始异常
     */
    public static void throwSystemError(String operation, String cause, Throwable throwable) {
        String message = String.format("系统错误 - %s: %s", operation, cause);
        log.error(message, throwable);
        throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), message, throwable);
    }
}
