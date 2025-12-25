package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表模板实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("report_template")
public class ReportTemplate {
    
    /**
     * 模板ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 报表类型（等保、PCI-DSS、GDPR、自定义）
     */
    private String reportType;
    
    /**
     * 模板文件路径
     */
    private String templateFilePath;
    
    /**
     * 生成频率（DAILY/WEEKLY/MONTHLY/QUARTERLY）
     */
    private String generationFrequency;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
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
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 模板配置（JSON格式）
     */
    private String templateConfig;
    
    /**
     * 模板参数（JSON格式）
     */
    private String templateParams;
}