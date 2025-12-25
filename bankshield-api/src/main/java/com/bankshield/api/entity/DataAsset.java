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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据资产实体类
 * 对应数据库表：data_asset
 * 用于存储数据资产的分类分级信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_asset")
public class DataAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 资产类型: DATABASE, FILE, BIGDATA
     */
    private String assetType;

    /**
     * 存储位置
     */
    private String storageLocation;

    /**
     * 数据源ID
     */
    @TableField("data_source_id")
    private Long dataSourceId;

    /**
     * 安全等级: 1:C1, 2:C2, 3:C3, 4:C4
     */
    private Integer securityLevel;

    /**
     * 分类依据
     */
    private String classificationBasis;

    /**
     * 自动识别等级
     */
    @TableField("automatic_level")
    private Integer automaticLevel;

    /**
     * 人工标注等级
     */
    @TableField("manual_level")
    private Integer manualLevel;

    /**
     * 最终等级
     */
    @TableField("final_level")
    private Integer finalLevel;

    /**
     * 识别方法: REGEX, KEYWORD, ML
     */
    private String recognizeMethod;

    /**
     * 识别置信度
     */
    private BigDecimal recognizeConfidence;

    /**
     * 匹配的规则
     */
    private String patternMatched;

    /**
     * 业务负责人
     */
    private String businessOwner;

    /**
     * 技术负责人
     */
    private String technicalOwner;

    /**
     * 业务条线
     */
    private String businessLine;

    /**
     * 状态: 0:待审核, 1:已生效, 2:已下线
     */
    private Integer status;

    /**
     * 审核人ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField("review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewComment;

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
}