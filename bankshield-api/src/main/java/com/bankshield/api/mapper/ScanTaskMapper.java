package com.bankshield.api.mapper;

import com.bankshield.api.entity.ScanTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产扫描任务Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface ScanTaskMapper extends BaseMapper<ScanTask> {

    /**
     * 按数据源ID查询扫描任务
     * 
     * @param dataSourceId 数据源ID
     * @return 扫描任务列表
     */
    List<ScanTask> selectByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    /**
     * 按任务状态查询扫描任务
     * 
     * @param taskStatus 任务状态
     * @return 扫描任务列表
     */
    List<ScanTask> selectByTaskStatus(@Param("taskStatus") Integer taskStatus);

    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param progressPercent 进度百分比
     * @param taskStatus 任务状态
     * @return 更新结果
     */
    int updateTaskProgress(@Param("taskId") Long taskId, 
                          @Param("progressPercent") Integer progressPercent,
                          @Param("taskStatus") Integer taskStatus);

    /**
     * 查询正在执行的任务
     * 
     * @return 正在执行的任务列表
     */
    List<ScanTask> selectRunningTasks();
}