package com.bankshield.common.exception;

import com.bankshield.common.result.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BankShieldException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String message;
    
    public BankShieldException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BankShieldException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
    
    public BankShieldException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
}