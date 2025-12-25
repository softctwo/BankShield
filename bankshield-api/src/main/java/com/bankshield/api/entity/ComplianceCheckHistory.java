package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 合规检查历史实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("compliance_check_history")
public class ComplianceCheckHistory {
    
    /**
     * 历史记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 合规标准（等保二级/三级、PCI-DSS、GDPR）
     */
    private String complianceStandard;
    
    /**
     * 检查结果（JSON格式，包含所有检查项结果）
     */
    private String checkResult;
    
    /**
     * 合规评分（0-100）
     */
    private Integer complianceScore;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 检查人
     */
    private String checker;
    
    /**
     * 检查报告路径
     */
    private String reportPath;
    
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