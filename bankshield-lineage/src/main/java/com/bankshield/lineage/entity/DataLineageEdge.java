package com.bankshield.lineage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据血缘边实体（关系）
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("data_lineage_edge")
public class DataLineageEdge {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 源节点ID
     */
    @TableField("source_node_id")
    private Long sourceNodeId;

    /**
     * 目标节点ID
     */
    @TableField("target_node_id")
    private Long targetNodeId;

    /**
     * 关系类型：DIRECT/INDIRECT/VIEW/ETL
     */
    @TableField("relationship_type")
    private String relationshipType;

    /**
     * 转换逻辑描述
     */
    @TableField("transformation")
    private String transformation;

    /**
     * 转换SQL
     */
    @TableField("transformation_sql")
    private String transformationSql;

    /**
     * 影响权重（1-10）
     */
    @TableField("impact_weight")
    private Integer impactWeight;

    /**
     * 血缘路径深度
     */
    @TableField("path_depth")
    private Integer pathDepth;

    /**
     * 是否活跃
     */
    @TableField("active")
    private Boolean active;

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