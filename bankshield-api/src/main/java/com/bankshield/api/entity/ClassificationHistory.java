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
 * 分类分级历史实体类
 * 对应数据库表：classification_history
 * 用于记录数据资产分类分级的变更历史
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("classification_history")
public class ClassificationHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资产ID
     */
    @TableField("asset_id")
    private Long assetId;

    /**
     * 原等级
     */
    @TableField("old_level")
    private Integer oldLevel;

    /**
     * 新等级
     */
    @TableField("new_level")
    private Integer newLevel;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 操作人
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private LocalDateTime operateTime;
}