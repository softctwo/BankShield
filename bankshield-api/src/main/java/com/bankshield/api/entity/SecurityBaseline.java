package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 安全基线配置实体类
 * @author BankShield
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("security_baseline")
public class SecurityBaseline implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 检查项名称
     */
    private String checkItemName;

    /**
     * 合规标准：等保三级/PCI-DSS/OWASP_TOP10/自定义
     */
    private String complianceStandard;

    /**
     * 检查类型：AUTH/SESSION/ENCRYPTION/PASSWORD/ACCESS_CONTROL
     */
    private String checkType;

    /**
     * 风险级别：CRITICAL/HIGH/MEDIUM/LOW/INFO
     */
    private String riskLevel;

    /**
     * 检查说明
     */
    private String description;

    /**
     * 修复建议
     */
    private String remedyAdvice;

    /**
     * 通过状态：PASS/FAIL/UNKNOWN/NOT_APPLICABLE
     */
    private String passStatus;

    /**
     * 检查结果
     */
    private String checkResult;

    /**
     * 检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    /**
     * 下次检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextCheckTime;

    /**
     * 负责人
     */
    private String responsiblePerson;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 内置标识
     */
    private Boolean builtin;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}