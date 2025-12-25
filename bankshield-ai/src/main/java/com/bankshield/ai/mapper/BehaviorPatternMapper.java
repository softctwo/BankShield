package com.bankshield.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.ai.entity.BehaviorPattern;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 行为模式Mapper接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Mapper
public interface BehaviorPatternMapper extends BaseMapper<BehaviorPattern> {

    /**
     * 根据用户ID查询行为模式
     * 
     * @param userId 用户ID
     * @param patternType 模式类型
     * @return 行为模式列表
     */
    List<BehaviorPattern> selectByUserId(@Param("userId") Long userId, 
                                         @Param("patternType") String patternType);

    /**
     * 查询活跃用户的行为模式
     * 
     * @param minConfidence 最小置信度
     * @param limit 限制数量
     * @return 行为模式列表
     */
    List<BehaviorPattern> selectActivePatterns(@Param("minConfidence") Double minConfidence, 
                                               @Param("limit") Integer limit);

    /**
     * 查询相似的行为模式
     * 
     * @param patternData 模式数据
     * @param similarityThreshold 相似度阈值
     * @return 相似的行为模式列表
     */
    List<BehaviorPattern> selectSimilarPatterns(@Param("patternData") String patternData, 
                                                @Param("similarityThreshold") Double similarityThreshold);

    /**
     * 更新行为模式活跃度
     * 
     * @param patternId 模式ID
     * @param isActive 是否活跃
     * @return 更新数量
     */
    Integer updatePatternActivity(@Param("patternId") Long patternId, 
                                  @Param("isActive") Boolean isActive);

    /**
     * 查询过期的行为模式
     * 
     * @param expireTime 过期时间
     * @return 过期的行为模式列表
     */
    List<BehaviorPattern> selectExpiredPatterns(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 统计用户行为模式数量
     * 
     * @param userId 用户ID
     * @param patternType 模式类型
     * @return 行为模式数量
     */
    Integer countByUserId(@Param("userId") Long userId, 
                          @Param("patternType") String patternType);

    /**
     * 查询最新的行为模式
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最新的行为模式列表
     */
    List<BehaviorPattern> selectLatestPatterns(@Param("userId") Long userId, 
                                               @Param("limit") Integer limit);

    /**
     * 批量更新行为模式置信度
     * 
     * @param patterns 行为模式列表
     * @return 更新数量
     */
    Integer batchUpdateConfidence(@Param("patterns") List<BehaviorPattern> patterns);

    /**
     * 删除无效的行为模式
     * 
     * @param maxInactiveTime 最大非活跃时间
     * @return 删除数量
     */
    Integer deleteInactivePatterns(@Param("maxInactiveTime") LocalDateTime maxInactiveTime);
}