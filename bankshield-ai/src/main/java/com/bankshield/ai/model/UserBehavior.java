package com.bankshield.ai.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为数据传输对象
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@ApiModel(value = "UserBehavior对象", description = "用户行为数据")
public class UserBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "会话ID")
    private String sessionId;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "地理位置")
    private String location;

    @ApiModelProperty(value = "行为类型：login/access/operation/download")
    private String behaviorType;

    @ApiModelProperty(value = "操作类型")
    private String operationType;

    @ApiModelProperty(value = "资源类型")
    private String resourceType;

    @ApiModelProperty(value = "资源ID")
    private Long resourceId;

    @ApiModelProperty(value = "操作时间")
    private LocalDateTime operationTime;

    @ApiModelProperty(value = "响应时间（毫秒）")
    private Long responseTime;

    @ApiModelProperty(value = "数据量（字节）")
    private Long dataSize;

    @ApiModelProperty(value = "操作结果：success/failure")
    private String operationResult;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "用户代理")
    private String userAgent;

    @ApiModelProperty(value = "登录时间（如果是登录行为）")
    private LocalDateTime loginTime;

    @ApiModelProperty(value = "登出时间（如果是登出行为）")
    private LocalDateTime logoutTime;

    @ApiModelProperty(value = "登录时长（分钟）")
    private Long loginDuration;

    @ApiModelProperty(value = "操作频率（次/小时）")
    private Double operationFrequency;

    @ApiModelProperty(value = "权限级别")
    private Integer permissionLevel;

    @ApiModelProperty(value = "敏感度级别")
    private Integer sensitivityLevel;

    @ApiModelProperty(value = "访问来源")
    private String accessSource;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "浏览器类型")
    private String browserType;

    @ApiModelProperty(value = "操作系统")
    private String operatingSystem;

    @ApiModelProperty(value = "额外参数")
    private String extraParams;
}