package com.bankshield.lineage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据血缘节点实体
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("data_lineage_node")
public class DataLineageNode {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 节点类型：TABLE/COLUMN/REPORT/ETL/DATASOURCE
     */
    @TableField("node_type")
    private String nodeType;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 节点编码（唯一标识）
     */
    @TableField("node_code")
    private String nodeCode;

    /**
     * 数据源ID
     */
    @TableField("data_source_id")
    private Long dataSourceId;

    /**
     * 数据库名称
     */
    @TableField("database_name")
    private String databaseName;

    /**
     * Schema名称
     */
    @TableField("schema_name")
    private String schemaName;

    /**
     * 表名称
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 字段名称
     */
    @TableField("column_name")
    private String columnName;

    /**
     * 数据类型
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 扩展属性JSON
     */
    @TableField("properties")
    private String properties;

    /**
     * 质量评分
     */
    @TableField("quality_score")
    private Double qualityScore;

    /**
     * 重要性级别：HIGH/MEDIUM/LOW
     */
    @TableField("importance_level")
    private String importanceLevel;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}