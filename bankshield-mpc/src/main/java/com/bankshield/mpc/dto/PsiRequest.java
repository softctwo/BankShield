package com.bankshield.mpc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 隐私求交请求DTO
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
public class PsiRequest {
    
    /**
     * 参与方ID列表
     */
    @NotEmpty(message = "参与方不能为空")
    private List<String> partyIds;
    
    /**
     * 求交字段
     */
    @NotBlank(message = "求交字段不能为空")
    private String field;
    
    /**
     * 数据类型 (CUSTOMER_ID/ACCOUNT_ID/PHONE)
     */
    private String dataType = "CUSTOMER_ID";
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout = 300;
}