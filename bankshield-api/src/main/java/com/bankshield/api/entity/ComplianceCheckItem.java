package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 合规检查项实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("compliance_check_item")
public class ComplianceCheckItem {
    
    /**
     * 检查项ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 检查项名称
     */
    private String checkItemName;
    
    /**
     * 合规标准（等保二级/三级、PCI-DSS、GDPR）
     */
    private String complianceStandard;
    
    /**
     * 检查类型
     */
    private String checkType;
    
    /**
     * 通过状态（PASS/FAIL/UNKNOWN）
     */
    private String passStatus;
    
    /**
     * 检查结果
     */
    private String checkResult;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 下次检查时间
     */
    private LocalDateTime nextCheckTime;
    
    /**
     * 负责人
     */
    private String responsiblePerson;
    
    /**
     * 检查描述
     */
    private String checkDescription;
    
    /**
     * 修复建议
     */
    private String remediation;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}