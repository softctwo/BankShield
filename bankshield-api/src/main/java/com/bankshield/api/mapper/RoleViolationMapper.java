package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.RoleViolation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色违规记录Mapper接口
 * 提供角色违规记录的数据库操作
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Mapper
public interface RoleViolationMapper extends BaseMapper<RoleViolation> {

    /**
     * 查询未处理的违规记录
     * 
     * @return 违规记录列表
     */
    List<RoleViolation> selectUnhandledViolations();

    /**
     * 查询未发送告警的违规记录
     * 
     * @return 违规记录列表
     */
    List<RoleViolation> selectUnsentAlerts();

    /**
     * 查询指定用户的违规记录
     * 
     * @param userId 用户ID
     * @param status 处理状态（可选）
     * @return 违规记录列表
     */
    List<RoleViolation> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询指定时间范围内的违规记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 处理状态（可选）
     * @return 违规记录列表
     */
    List<RoleViolation> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime, 
                                        @Param("status") Integer status);

    /**
     * 统计违规记录数量
     * 
     * @param status 处理状态（可选）
     * @return 违规记录数量
     */
    int countViolations(@Param("status") Integer status);

    /**
     * 更新告警发送状态
     * 
     * @param id 违规记录ID
     * @param alertSent 告警发送状态
     * @return 更新结果
     */
    int updateAlertStatus(@Param("id") Long id, @Param("alertSent") Integer alertSent);
}