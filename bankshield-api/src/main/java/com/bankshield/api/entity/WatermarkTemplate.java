package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 水印模板实体类
 * 对应数据库表：watermark_template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("watermark_template")
public class WatermarkTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 水印类型: TEXT/IMAGE/DATABASE
     */
    private String watermarkType;

    /**
     * 水印内容
     */
    private String watermarkContent;

    /**
     * 水印位置: TOP_LEFT/TOP_RIGHT/BOTTOM_LEFT/BOTTOM_RIGHT/CENTER/FULLSCREEN
     */
    private String watermarkPosition;

    /**
     * 透明度(0-100)
     */
    private Integer transparency;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 字体颜色
     */
    private String fontColor;

    /**
     * 字体名称
     */
    private String fontFamily;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
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