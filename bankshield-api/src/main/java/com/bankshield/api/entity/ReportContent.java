package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表内容实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("report_content")
public class ReportContent {
    
    /**
     * 内容ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 章节名称
     */
    private String chapterName;
    
    /**
     * 章节数据（JSON格式）
     */
    private String chapterData;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 生成时间
     */
    private LocalDateTime generationTime;
    
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