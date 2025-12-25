package com.bankshield.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.ai.entity.AiFeature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI特征Mapper接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Mapper
public interface AiFeatureMapper extends BaseMapper<AiFeature> {

    /**
     * 根据用户ID查询特征数据
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 特征数据列表
     */
    List<AiFeature> selectByUserIdAndTimeRange(@Param("userId") Long userId, 
                                               @Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 根据行为类型查询特征数据
     * 
     * @param behaviorType 行为类型
     * @param limit 限制数量
     * @return 特征数据列表
     */
    List<AiFeature> selectByBehaviorType(@Param("behaviorType") String behaviorType, 
                                         @Param("limit") Integer limit);

    /**
     * 查询异常特征数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param minScore 最小异常分数
     * @return 异常特征数据列表
     */
    List<AiFeature> selectAnomalyFeatures(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime, 
                                          @Param("minScore") Double minScore);

    /**
     * 统计用户异常行为数量
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常行为数量
     */
    Integer countAnomalyByUserId(@Param("userId") Long userId, 
                                 @Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最新特征数据
     * 
     * @param userId 用户ID
     * @param behaviorType 行为类型
     * @param limit 限制数量
     * @return 最新特征数据列表
     */
    List<AiFeature> selectLatestFeatures(@Param("userId") Long userId, 
                                         @Param("behaviorType") String behaviorType, 
                                         @Param("limit") Integer limit);

    /**
     * 批量插入特征数据
     * 
     * @param features 特征数据列表
     * @return 插入数量
     */
    Integer batchInsert(@Param("features") List<AiFeature> features);

    /**
     * 删除过期特征数据
     * 
     * @param expireTime 过期时间
     * @return 删除数量
     */
    Integer deleteExpiredFeatures(@Param("expireTime") LocalDateTime expireTime);
}