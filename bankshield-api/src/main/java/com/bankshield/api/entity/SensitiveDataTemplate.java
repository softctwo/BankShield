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
 * 敏感数据类型模板实体类
 * 对应数据库表：sensitive_data_template
 * 用于定义敏感数据的识别规则和分类标准
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sensitive_data_template")
public class SensitiveDataTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 安全等级
     */
    private Integer securityLevel;

    /**
     * 正则表达式
     */
    private String pattern;

    /**
     * 关键词（JSON数组）
     */
    private String keywords;

    /**
     * 描述
     */
    private String description;

    /**
     * 标准引用: JR/T0197, JR/T0171
     */
    private String standardRef;

    /**
     * 示例数据
     */
    private String examples;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}