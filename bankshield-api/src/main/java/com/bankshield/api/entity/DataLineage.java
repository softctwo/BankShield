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
 * 数据血缘关系实体类
 * 对应数据库表：data_lineage
 * 用于记录数据资产之间的血缘关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_lineage")
public class DataLineage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 源资产ID
     */
    @TableField("source_asset_id")
    private Long sourceAssetId;

    /**
     * 目标资产ID
     */
    @TableField("target_asset_id")
    private Long targetAssetId;

    /**
     * 源字段
     */
    private String sourceField;

    /**
     * 目标字段
     */
    private String targetField;

    /**
     * 转换逻辑
     */
    private String transformation;

    /**
     * 血缘类型: DIRECT, TRANSFORMED
     */
    private String lineageType;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}