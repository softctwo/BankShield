package com.bankshield.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 脱敏测试请求DTO
 */
@Data
@ApiModel("脱敏测试请求")
public class MaskingTestRequest {

    @ApiModelProperty("测试数据")
    @NotBlank(message = "测试数据不能为空")
    private String testData;

    @ApiModelProperty("敏感数据类型")
    @NotBlank(message = "敏感数据类型不能为空")
    private String sensitiveDataType;

    @ApiModelProperty("脱敏算法")
    @NotBlank(message = "脱敏算法不能为空")
    private String maskingAlgorithm;

    @ApiModelProperty("算法参数（JSON格式）")
    private String algorithmParams;
}