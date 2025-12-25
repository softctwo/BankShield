package com.bankshield.lineage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.lineage.entity.DataLineageNode;
import com.bankshield.lineage.vo.LineageGraph;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据血缘节点Mapper
 *
 * @author BankShield
 * @since 2024-01-24
 */
public interface DataLineageNodeMapper extends BaseMapper<DataLineageNode> {

    /**
     * 根据条件分页查询节点
     */
    IPage<DataLineageNode> selectByCondition(Page<DataLineageNode> page, 
                                              @Param("nodeType") String nodeType,
                                              @Param("nodeName") String nodeName,
                                              @Param("dataSourceId") Long dataSourceId);

    /**
     * 根据节点编码查询节点
     */
    @Select("SELECT * FROM data_lineage_node WHERE node_code = #{nodeCode} AND deleted = 0")
    DataLineageNode selectByNodeCode(@Param("nodeCode") String nodeCode);

    /**
     * 根据表名查询节点
     */
    @Select("SELECT * FROM data_lineage_node WHERE table_name = #{tableName} AND deleted = 0")
    DataLineageNode selectByTableName(@Param("tableName") String tableName);

    /**
     * 查询数据源下的所有节点
     */
    @Select("SELECT * FROM data_lineage_node WHERE data_source_id = #{dataSourceId} AND deleted = 0")
    List<DataLineageNode> selectByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    /**
     * 查询指定类型的节点
     */
    @Select("SELECT * FROM data_lineage_node WHERE node_type = #{nodeType} AND deleted = 0")
    List<DataLineageNode> selectByNodeType(@Param("nodeType") String nodeType);

    /**
     * 查询上游节点
     */
    @Select("SELECT DISTINCT n.* FROM data_lineage_node n " +
            "INNER JOIN data_lineage_edge e ON n.id = e.source_node_id " +
            "WHERE e.target_node_id = #{nodeId} AND e.active = 1 AND n.deleted = 0 AND e.deleted = 0")
    List<DataLineageNode> findUpstreamNodes(@Param("nodeId") Long nodeId);

    /**
     * 查询下游节点
     */
    @Select("SELECT DISTINCT n.* FROM data_lineage_node n " +
            "INNER JOIN data_lineage_edge e ON n.id = e.target_node_id " +
            "WHERE e.source_node_id = #{nodeId} AND e.active = 1 AND n.deleted = 0 AND e.deleted = 0")
    List<DataLineageNode> findDownstreamNodes(@Param("nodeId") Long nodeId);

    /**
     * 查询指定深度的上下游节点
     */
    List<DataLineageNode> findNodesByDepth(@Param("centerNodeId") Long centerNodeId, 
                                          @Param("depth") Integer depth, 
                                          @Param("direction") String direction);

    /**
     * 查询血缘图谱数据
     */
    List<LineageGraph.LineageNode> selectGraphNodes(@Param("centerNodeId") Long centerNodeId, 
                                                     @Param("depth") Integer depth);

    /**
     * 更新节点质量评分
     */
    int updateQualityScore(@Param("nodeId") Long nodeId, 
                          @Param("qualityScore") Double qualityScore);

    /**
     * 批量更新节点状态
     */
    int batchUpdateEnabled(@Param("nodeIds") List<Long> nodeIds, 
                          @Param("enabled") Boolean enabled);
}