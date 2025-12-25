package com.bankshield.mpc.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 安全求和请求DTO
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
public class SecureSumRequest {
    
    /**
     * 参与方ID列表
     */
    @NotEmpty(message = "参与方不能为空")
    private List<String> partyIds;
    
    /**
     * 求和字段
     */
    private String field;
    
    /**
     * Paillier公钥（可选，系统可自动生成）
     */
    private String publicKey;
    
    /**
     * Paillier私钥（可选，系统可自动生成）
     */
    private String privateKey;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout = 300;
}