package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据脱敏规则实体类
 * 对应数据库表：masking_rule
 * 用于定义不同类型的敏感数据的脱敏规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("masking_rule")
public class DataMaskingRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 敏感数据类型：PHONE/ID_CARD/BANK_CARD/NAME/EMAIL/ADDRESS
     */
    private String sensitiveDataType;

    /**
     * 脱敏算法：PARTIAL_MASK/FULL_MASK/HASH/SYMMETRIC_ENCRYPT/FORMAT_PRESERVING
     */
    private String maskingAlgorithm;

    /**
     * 算法参数（JSON格式）
     */
    private String algorithmParams;

    /**
     * 适用场景：DISPLAY,EXPORT,QUERY,TRANSFER
     */
    private String applicableScenarios;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 描述
     */
    private String description;
}