package com.bankshield.encrypt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 生成密钥请求DTO
 */
@Data
public class GenerateKeyRequest {

    @NotBlank(message = "密钥名称不能为空")
    private String keyName;

    @NotBlank(message = "密钥类型不能为空")
    private String keyType;

    private String keyUsage;

    @Min(value = 128, message = "密钥长度不能小于128位")
    @Max(value = 4096, message = "密钥长度不能大于4096位")
    private Integer keyLength;

    @Min(value = 1, message = "过期天数不能小于1天")
    private Integer expireDays;

    @Min(value = 30, message = "轮换周期不能小于30天")
    private Integer rotationCycle;

    private String description;

    private String createdBy;
}