package com.bankshield.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 脱敏测试响应DTO
 */
@Data
@ApiModel("脱敏测试响应")
public class MaskingTestResponse {

    @ApiModelProperty("原始数据")
    private String originalData;

    @ApiModelProperty("脱敏后数据")
    private String maskedData;

    @ApiModelProperty("敏感数据类型")
    private String sensitiveDataType;

    @ApiModelProperty("脱敏算法")
    private String maskingAlgorithm;

    @ApiModelProperty("算法参数")
    private String algorithmParams;
}