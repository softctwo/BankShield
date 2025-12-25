package com.bankshield.lineage.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 影响分析视图对象
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Data
public class ImpactAnalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 资产类型
     */
    private String assetType;

    /**
     * 直接影响统计
     */
    private ImpactStatistics directImpact;

    /**
     * 间接影响统计
     */
    private ImpactStatistics indirectImpact;

    /**
     * 下游表清单
     */
    private List<DownstreamTable> downstreamTables;

    /**
     * 影响路径
     */
    private List<ImpactPath> impactPaths;

    /**
     * 复杂性分析
     */
    private ComplexityAnalysis complexity;

    @Data
    public static class ImpactStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 影响表数量
         */
        private Integer tableCount;

        /**
         * 影响字段数量
         */
        private Integer columnCount;

        /**
         * 影响视图数量
         */
        private Integer viewCount;

        /**
         * 影响报表数量
         */
        private Integer reportCount;

        /**
         * 影响应用数量
         */
        private Integer applicationCount;

        /**
         * 影响ETL任务数量
         */
        private Integer etlCount;

        /**
         * 高风险影响数量
         */
        private Integer highRiskCount;

        /**
         * 中风险影响数量
         */
        private Integer mediumRiskCount;

        /**
         * 低风险影响数量
         */
        private Integer lowRiskCount;
    }

    @Data
    public static class DownstreamTable implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 表ID
         */
        private Long tableId;

        /**
         * 表名称
         */
        private String tableName;

        /**
         * 数据库名称
         */
        private String databaseName;

        /**
         * 影响层级（1表示直接影响）
         */
        private Integer impactLevel;

        /**
         * 血缘路径
         */
        private String lineagePath;

        /**
         * 影响类型
         */
        private String impactType;

        /**
         * 风险等级
         */
        private String riskLevel;

        /**
         * 重要性级别
         */
        private String importanceLevel;
    }

    @Data
    public static class ImpactPath implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 路径ID
         */
        private String pathId;

        /**
         * 路径描述
         */
        private String pathDescription;

        /**
         * 路径长度
         */
        private Integer pathLength;

        /**
         * 路径节点
         */
        private List<PathNode> nodes;
    }

    @Data
    public static class PathNode implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 节点ID
         */
        private Long nodeId;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 节点类型
         */
        private String nodeType;

        /**
         * 关系类型
         */
        private String relationshipType;
    }

    @Data
    public static class ComplexityAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 循环复杂度
         */
        private Integer cyclomaticComplexity;

        /**
         * 依赖数量
         */
        private Integer dependencyCount;

        /**
         * 稳定性评分（0-100）
         */
        private Double stabilityScore;

        /**
         * 维护难度评分（0-100）
        */
        private Double maintenanceScore;

        /**
         * 复杂度等级
         */
        private String complexityLevel;
    }
}