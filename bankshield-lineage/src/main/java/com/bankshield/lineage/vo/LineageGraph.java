package com.bankshield.lineage.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 血缘图谱视图对象
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
public class LineageGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图谱ID
     */
    private String graphId;

    /**
     * 中心节点ID
     */
    private Long centerNodeId;

    /**
     * 深度
     */
    private Integer depth;

    /**
     * 节点列表
     */
    private List<LineageNode> nodes;

    /**
     * 边列表
     */
    private List<LineageEdge> links;

    /**
     * 统计信息
     */
    private GraphStatistics statistics;

    @Data
    public static class LineageNode implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 节点ID
         */
        private Long id;

        /**
         * 节点名称
         */
        private String name;

        /**
         * 节点类型
         */
        private String type;

        /**
         * 节点分类（用于可视化）
         */
        private Integer category;

        /**
         * 节点大小
         */
        private Integer symbolSize;

        /**
         * 质量评分
         */
        private Double qualityScore;

        /**
         * 重要性级别
         */
        private String importanceLevel;

        /**
         * 扩展属性
         */
        private Object properties;

        /**
         * 固定位置（用于可视化）
         */
        private Boolean fixed;

        /**
         * X坐标
         */
        private Double x;

        /**
         * Y坐标
         */
        private Double y;
    }

    @Data
    public static class LineageEdge implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 源节点ID
         */
        private Long source;

        /**
         * 目标节点ID
         */
        private Long target;

        /**
         * 关系类型
         */
        private String relationshipType;

        /**
         * 转换逻辑
         */
        private String transformation;

        /**
         * 影响权重
         */
        private Integer impactWeight;

        /**
         * 线宽
         */
        private Integer lineStyle;

        /**
         * 标签
         */
        private String label;
    }

    @Data
    public static class GraphStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 节点总数
         */
        private Integer totalNodes;

        /**
         * 边总数
         */
        private Integer totalEdges;

        /**
         * 表节点数
         */
        private Integer tableNodes;

        /**
         * 字段节点数
         */
        private Integer columnNodes;

        /**
         * 视图节点数
         */
        private Integer viewNodes;

        /**
         * 平均质量评分
         */
        private Double averageQualityScore;

        /**
         * 最高质量评分
         */
        private Double maxQualityScore;

        /**
         * 最低质量评分
         */
        private Double minQualityScore;
    }
}