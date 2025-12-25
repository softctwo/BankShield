package com.bankshield.api.service;

import com.bankshield.api.entity.WatermarkTask;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 水印任务服务接口
 * 
 * @author BankShield
 */
public interface WatermarkTaskService {

    /**
     * 分页查询水印任务
     * 
     * @param page 分页参数
     * @param taskName 任务名称
     * @param taskType 任务类型
     * @param status 任务状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<WatermarkTask> getTaskPage(Page<WatermarkTask> page, String taskName, String taskType, 
                                   String status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID获取任务
     * 
     * @param id 任务ID
     * @return 任务信息
     */
    WatermarkTask getTaskById(Long id);

    /**
     * 创建水印任务
     * 
     * @param task 任务信息
     * @return 创建的任务
     */
    WatermarkTask createTask(WatermarkTask task);

    /**
     * 异步执行水印嵌入任务
     * 
     * @param taskId 任务ID
     * @return 异步结果
     */
    CompletableFuture<Void> executeTaskAsync(Long taskId);

    /**
     * 执行任务
     * 
     * @param taskId 任务ID
     */
    void executeTask(Long taskId);

    /**
     * 获取任务执行进度
     * 
     * @param taskId 任务ID
     * @return 进度百分比
     */
    int getTaskProgress(Long taskId);

    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelTask(Long taskId);

    /**
     * 重新执行任务
     * 
     * @param taskId 任务ID
     * @return 是否重新执行成功
     */
    boolean retryTask(Long taskId);

    /**
     * 获取任务结果文件
     *
     * @param taskId 任务ID
     * @return 文件路径
     */
    String getTaskResultFile(Long taskId);

    /**
     * 获取待处理任务列表
     *
     * @param limit 限制数量
     * @return 待处理任务列表
     */
    List<WatermarkTask> getPendingTasks(int limit);

    /**
     * 更新任务
     *
     * @param task 任务信息
     * @return 是否更新成功
     */
    boolean updateById(WatermarkTask task);
}