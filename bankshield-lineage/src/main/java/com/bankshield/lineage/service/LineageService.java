package com.bankshield.lineage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.lineage.entity.DataLineageNode;
import com.bankshield.lineage.vo.ImpactAnalysis;
import com.bankshield.lineage.vo.LineageGraph;
import com.bankshield.lineage.vo.LineageInfo;

import java.util.List;

/**
 * 数据血缘服务接口
 *
 * @author BankShield
 * @since 2024-01-24
 */
public interface LineageService {

    /**
     * 构建血缘图谱
     *
     * @param centerNodeId 中心节点ID
     * @param depth 深度
     * @return 血缘图谱
     */
    LineageGraph buildGraph(Long centerNodeId, Integer depth);

    /**
     * 分析影响
     *
     * @param assetId 资产ID
     * @return 影响分析
     */
    ImpactAnalysis analyzeImpact(Long assetId);

    /**
     * 分析字段影响
     *
     * @param fieldId 字段ID
     * @return 影响分析
     */
    ImpactAnalysis analyzeFieldImpact(Long fieldId);

    /**
     * 分析表影响
     *
     * @param tableId 表ID
     * @return 影响分析
     */
    ImpactAnalysis analyzeTableImpact(Long tableId);

    /**
     * 从SQL日志自动发现血缘
     */
    void discoverFromSqlLogs();

    /**
     * 提取SQL血缘
     *
     * @param sql SQL语句
     * @param dbType 数据库类型
     * @return 血缘信息
     */
    LineageInfo extractSqlLineage(String sql, String dbType);

    /**
     * 保存血缘信息
     *
     * @param lineageInfo 血缘信息
     * @return 是否成功
     */
    boolean saveLineageInfo(LineageInfo lineageInfo);

    /**
     * 查询节点
     *
     * @param nodeId 节点ID
     * @return 节点信息
     */
    DataLineageNode getNodeById(Long nodeId);

    /**
     * 分页查询节点
     *
     * @param page 分页参数
     * @param nodeType 节点类型
     * @param nodeName 节点名称
     * @param dataSourceId 数据源ID
     * @return 节点分页
     */
    IPage<DataLineageNode> getNodes(Page<DataLineageNode> page, String nodeType, String nodeName, Long dataSourceId);

    /**
     * 查询上游节点
     *
     * @param nodeId 节点ID
     * @return 上游节点列表
     */
    List<DataLineageNode> getUpstreamNodes(Long nodeId);

    /**
     * 查询下游节点
     *
     * @param nodeId 节点ID
     * @return 下游节点列表
     */
    List<DataLineageNode> getDownstreamNodes(Long nodeId);

    /**
     * 查询完整血缘路径
     *
     * @param sourceNodeId 源节点ID
     * @param targetNodeId 目标节点ID
     * @param maxDepth 最大深度
     * @return 血缘路径
     */
    List<LineageGraph.LineageEdge> getLineagePath(Long sourceNodeId, Long targetNodeId, Integer maxDepth);

    /**
     * 计算节点复杂性
     *
     * @param nodeId 节点ID
     * @return 复杂性评分
     */
    Double calculateComplexity(Long nodeId);

    /**
     * 验证血缘关系
     *
     * @param sourceNodeId 源节点ID
     * @param targetNodeId 目标节点ID
     * @return 是否存在血缘关系
     */
    boolean validateLineage(Long sourceNodeId, Long targetNodeId);

    /**
     * 批量导入血缘数据
     *
     * @param lineageData 血缘数据
     * @return 导入结果
     */
    boolean importLineageData(String lineageData);

    /**
     * 导出血缘数据
     *
     * @param nodeId 节点ID
     * @param depth 深度
     * @return 导出数据
     */
    String exportLineageData(Long nodeId, Integer depth);
}