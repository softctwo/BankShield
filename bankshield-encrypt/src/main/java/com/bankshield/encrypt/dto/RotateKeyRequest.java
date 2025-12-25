package com.bankshield.encrypt.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 密钥轮换请求DTO
 */
@Data
public class RotateKeyRequest {

    @NotNull(message = "密钥ID不能为空")
    private Long keyId;

    @NotBlank(message = "轮换原因不能为空")
    private String rotationReason;

    private String operatedBy;
}