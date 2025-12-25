package com.bankshield.mpc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 联合查询请求DTO
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
public class JointQueryRequest {
    
    /**
     * 参与方ID列表
     */
    @NotEmpty(message = "参与方不能为空")
    private List<String> partyIds;
    
    /**
     * 客户ID
     */
    @NotBlank(message = "客户ID不能为空")
    private String customerId;
    
    /**
     * 查询类型 (TOTAL_DEBT/TOTAL_ASSET/CREDIT_SCORE)
     */
    @NotBlank(message = "查询类型不能为空")
    private String queryType;
    
    /**
     * 阈值（最少参与方数量）
     */
    private Integer threshold = 2;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout = 300;
}