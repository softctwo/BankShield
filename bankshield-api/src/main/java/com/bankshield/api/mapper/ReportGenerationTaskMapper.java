package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ReportGenerationTask;
import org.apache.ibatis.annotations.Param;

/**
 * 报表生成任务Mapper接口
 */
public interface ReportGenerationTaskMapper extends BaseMapper<ReportGenerationTask> {
    
    /**
     * 分页查询报表生成任务
     */
    IPage<ReportGenerationTask> selectTaskPage(Page<ReportGenerationTask> page,
                                               @Param("templateId") Long templateId,
                                               @Param("status") String status,
                                               @Param("createdBy") String createdBy);
    
    /**
     * 查询正在运行的任务数量
     */
    int countRunningTasks();
    
    /**
     * 查询最近完成的任务
     */
    ReportGenerationTask selectLatestCompletedTask(@Param("templateId") Long templateId);
    
    /**
     * 按周期统计任务数量
     */
    default int countByPeriod(String period) {
        return (int) (long) selectCount(new QueryWrapper<ReportGenerationTask>()
                .like("report_period", period));
    }
}