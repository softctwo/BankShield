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
 * 数据地图实体类
 * 对应数据库表：data_map
 * 用于记录数据地图的元信息和配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_map")
public class DataMap implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地图名称
     */
    private String mapName;

    /**
     * 地图类型: GLOBAL, BUSINESS_DOMAIN, DATA_SOURCE, CUSTOM
     */
    private String mapType;

    /**
     * 地图范围（JSON格式）
     */
    private String scope;

    /**
     * 地图配置（JSON格式）
     */
    private String config;

    /**
     * 节点数量
     */
    @TableField("node_count")
    private Integer nodeCount;

    /**
     * 关系数量
     */
    @TableField("relationship_count")
    private Integer relationshipCount;

    /**
     * 地图数据（JSON格式，存储完整的地图数据）
     */
    @TableField("map_data")
    private String mapData;

    /**
     * 缩略图路径
     */
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 是否为默认地图
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 状态: ACTIVE, INACTIVE, DRAFT
     */
    private String status;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}