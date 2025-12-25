package com.bankshield.api.mapper;

import com.bankshield.api.entity.WatermarkTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 水印任务Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface WatermarkTaskMapper extends BaseMapper<WatermarkTask> {

    /**
     * 分页查询水印任务
     * 
     * @param page 分页参数
     * @param taskName 任务名称（模糊查询）
     * @param taskType 任务类型
     * @param status 任务状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<WatermarkTask> selectTaskPage(Page<WatermarkTask> page,
                                      @Param("taskName") String taskName,
                                      @Param("taskType") String taskType,
                                      @Param("status") String status,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待处理的任务
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<WatermarkTask> selectPendingTasks(@Param("limit") int limit);

    /**
     * 统计指定状态的任务数量
     * 
     * @param status 任务状态
     * @return 任务数量
     */
    long countByStatus(@Param("status") String status);

    /**
     * 更新任务状态
     * 
     * @param id 任务ID
     * @param status 新状态
     * @param errorMessage 错误信息
     * @return 更新结果
     */
    int updateTaskStatus(@Param("id") Long id,
                        @Param("status") String status,
                        @Param("errorMessage") String errorMessage);
}