package com.bankshield.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 水印提取日志实体类
 * 对应数据库表：watermark_extract_log
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("watermark_extract_log")
public class WatermarkExtractLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提取的水印内容
     */
    private String watermarkContent;

    /**
     * 提取时间
     */
    private LocalDateTime extractTime;

    /**
     * 提取来源
     */
    private String extractSource;

    /**
     * 提取结果: SUCCESS/FAIL
     */
    private String extractResult;

    /**
     * 操作人员
     */
    private String operator;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件大小
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 提取耗时（毫秒）
     */
    @TableField("extract_duration")
    private Long extractDuration;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;
}
