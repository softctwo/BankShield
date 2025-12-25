package com.bankshield.lineage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.lineage.entity.DataLineageEdge;
import com.bankshield.lineage.vo.LineageGraph;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据血缘边Mapper
 *
 * @author BankShield
 * @since 2024-01-24
 */
public interface DataLineageEdgeMapper extends BaseMapper<DataLineageEdge> {

    /**
     * 查询指定节点的直接关系
     */
    @Select("SELECT * FROM data_lineage_edge WHERE (source_node_id = #{nodeId} OR target_node_id = #{nodeId}) " +
            "AND active = 1 AND deleted = 0")
    List<DataLineageEdge> findDirectEdges(@Param("nodeId") Long nodeId);

    /**
     * 查询上游边
     */
    @Select("SELECT * FROM data_lineage_edge WHERE target_node_id = #{nodeId} AND active = 1 AND deleted = 0")
    List<DataLineageEdge> findUpstreamEdges(@Param("nodeId") Long nodeId);

    /**
     * 查询下游边
     */
    @Select("SELECT * FROM data_lineage_edge WHERE source_node_id = #{nodeId} AND active = 1 AND deleted = 0")
    List<DataLineageEdge> findDownstreamEdges(@Param("nodeId") Long nodeId);

    /**
     * 查询指定节点列表的边
     */
    List<DataLineageEdge> findEdgesByNodes(@Param("nodeIds") List<Long> nodeIds);

    /**
     * 查询血缘图谱边数据
     */
    List<LineageGraph.LineageEdge> selectGraphEdges(@Param("nodeIds") List<Long> nodeIds);

    /**
     * 查询影响路径
     */
    List<DataLineageEdge> findImpactPath(@Param("sourceNodeId") Long sourceNodeId, 
                                        @Param("targetNodeId") Long targetNodeId, 
                                        @Param("maxDepth") Integer maxDepth);

    /**
     * 批量插入边
     */
    int batchInsert(@Param("edges") List<DataLineageEdge> edges);

    /**
     * 更新边状态
     */
    int updateActiveStatus(@Param("edgeIds") List<Long> edgeIds, 
                          @Param("active") Boolean active);

    /**
     * 删除指定节点的所有边
     */
    int deleteByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 统计节点关系数量
     */
    @Select("SELECT COUNT(*) FROM data_lineage_edge WHERE source_node_id = #{nodeId} AND active = 1 AND deleted = 0")
    int countDownstreamRelations(@Param("nodeId") Long nodeId);

    /**
     * 统计节点被引用数量
     */
    @Select("SELECT COUNT(*) FROM data_lineage_edge WHERE target_node_id = #{nodeId} AND active = 1 AND deleted = 0")
    int countUpstreamRelations(@Param("nodeId") Long nodeId);
}