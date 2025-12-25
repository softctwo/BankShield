package com.bankshield.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为模式实体类
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("behavior_pattern")
@ApiModel(value = "BehaviorPattern对象", description = "用户行为模式表")
public class BehaviorPattern implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "模式类型：登录时间/操作频率/数据访问")
    @TableField("pattern_type")
    private String patternType;

    @ApiModelProperty(value = "模式数据JSON")
    @TableField("pattern_data")
    private String patternData;

    @ApiModelProperty(value = "模式置信度")
    @TableField("confidence")
    private Double confidence;

    @ApiModelProperty(value = "模式强度")
    @TableField("strength")
    private Double strength;

    @ApiModelProperty(value = "样本数量")
    @TableField("sample_count")
    private Integer sampleCount;

    @ApiModelProperty(value = "首次出现时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("first_seen_time")
    private LocalDateTime firstSeenTime;

    @ApiModelProperty(value = "最后出现时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_seen_time")
    private LocalDateTime lastSeenTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否有效")
    @TableField("is_active")
    private Boolean isActive;

    @ApiModelProperty(value = "是否删除")
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}