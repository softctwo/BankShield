package com.bankshield.api.service.impl;

import com.bankshield.api.config.WatermarkConfig;
import com.bankshield.api.entity.WatermarkTask;
import com.bankshield.api.entity.WatermarkTemplate;
import com.bankshield.api.enums.TaskStatus;
import com.bankshield.api.mapper.WatermarkTaskMapper;
import com.bankshield.api.mapper.WatermarkTemplateMapper;
import com.bankshield.api.service.WatermarkEmbeddingEngine;
import com.bankshield.api.service.WatermarkTaskService;
import com.bankshield.common.result.Result;
import com.bankshield.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 水印任务服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class WatermarkTaskServiceImpl extends ServiceImpl<WatermarkTaskMapper, WatermarkTask> 
        implements WatermarkTaskService {

    @Autowired
    private WatermarkTaskMapper taskMapper;

    @Autowired
    private WatermarkTemplateMapper templateMapper;

    @Autowired
    private WatermarkEmbeddingEngine embeddingEngine;

    @Autowired
    private WatermarkConfig watermarkConfig;

    // 任务进度跟踪
    private final Map<Long, AtomicInteger> taskProgressMap = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> taskCancelMap = new ConcurrentHashMap<>();

    @Override
    public IPage<WatermarkTask> getTaskPage(Page<WatermarkTask> page, String taskName, String taskType, 
                                           String status, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("分页查询水印任务，页码: {}, 每页大小: {}, 任务名称: {}, 任务类型: {}, 状态: {}, 开始时间: {}, 结束时间: {}", 
                page.getCurrent(), page.getSize(), taskName, taskType, status, startTime, endTime);
        
        try {
            return taskMapper.selectTaskPage(page, taskName, taskType, status, startTime, endTime);
        } catch (Exception e) {
            log.error("分页查询水印任务失败", e);
            throw new BusinessException("查询水印任务失败");
        }
    }

    @Override
    public WatermarkTask getTaskById(Long id) {
        log.info("根据ID获取水印任务，ID: {}", id);
        
        try {
            WatermarkTask task = taskMapper.selectById(id);
            if (task == null) {
                throw new BusinessException("水印任务不存在");
            }
            return task;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取水印任务失败，ID: {}", id, e);
            throw new BusinessException("获取水印任务失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WatermarkTask createTask(WatermarkTask task) {
        log.info("创建水印任务，任务名称: {}", task.getTaskName());
        
        try {
            // 参数验证
            if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
                throw new BusinessException("任务名称不能为空");
            }
            
            if (task.getTaskType() == null || task.getTaskType().trim().isEmpty()) {
                throw new BusinessException("任务类型不能为空");
            }
            
            if (task.getTemplateId() == null) {
                throw new BusinessException("模板ID不能为空");
            }
            
            // 检查模板是否存在
            WatermarkTemplate template = templateMapper.selectById(task.getTemplateId());
            if (template == null) {
                throw new BusinessException("水印模板不存在");
            }
            
            // 检查模板是否启用
            if (template.getEnabled() != 1) {
                throw new BusinessException("水印模板已禁用");
            }
            
            // 设置默认值
            task.setStatus(TaskStatus.PENDING.getCode());
            task.setProcessCount(0L);
            task.setCreateTime(LocalDateTime.now());
            
            taskMapper.insert(task);
            log.info("创建水印任务成功，ID: {}", task.getId());
            return task;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建水印任务失败", e);
            throw new BusinessException("创建水印任务失败");
        }
    }

    @Override
    @Async
    public CompletableFuture<Void> executeTaskAsync(Long taskId) {
        log.info("异步执行水印任务，任务ID: {}", taskId);
        
        try {
            executeTask(taskId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("异步执行水印任务失败，任务ID: {}", taskId, e);
            throw new BusinessException("执行任务失败: " + e.getMessage());
        }
    }

    @Override
    public void executeTask(Long taskId) {
        log.info("执行水印任务，任务ID: {}", taskId);
        
        WatermarkTask task = getTaskById(taskId);
        WatermarkTemplate template = templateMapper.selectById(task.getTemplateId());
        
        if (template == null) {
            throw new BusinessException("水印模板不存在");
        }
        
        // 初始化进度
        AtomicInteger progress = new AtomicInteger(0);
        taskProgressMap.put(taskId, progress);
        taskCancelMap.put(taskId, false);
        
        try {
            // 更新任务状态
            task.setStatus(TaskStatus.RUNNING.getCode());
            task.setStartTime(LocalDateTime.now());
            taskMapper.updateById(task);
            
            // 根据任务类型执行不同的处理
            switch (task.getTaskType()) {
                case "FILE":
                    executeFileTask(task, template, progress);
                    break;
                case "DATABASE":
                    executeDatabaseTask(task, template, progress);
                    break;
                default:
                    throw new BusinessException("不支持的任务类型: " + task.getTaskType());
            }
            
            // 更新任务状态为成功
            task.setStatus(TaskStatus.SUCCESS.getCode());
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
            
            log.info("水印任务执行成功，任务ID: {}", taskId);
            
        } catch (Exception e) {
            log.error("执行水印任务失败，任务ID: {}", taskId, e);
            
            // 更新任务状态为失败
            task.setStatus(TaskStatus.FAILED.getCode());
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
            
            throw new BusinessException("执行任务失败: " + e.getMessage());
        } finally {
            // 清理进度跟踪
            taskProgressMap.remove(taskId);
            taskCancelMap.remove(taskId);
        }
    }

    /**
     * 执行文件类型任务
     */
    private void executeFileTask(WatermarkTask task, WatermarkTemplate template, AtomicInteger progress) {
        log.info("执行文件类型水印任务，任务ID: {}", task.getId());

        // 检查取消标志
        if (taskCancelMap.getOrDefault(task.getId(), false)) {
            throw new BusinessException("任务已被取消");
        }

        progress.set(10); // 开始处理

        // 这里需要实现具体的文件处理逻辑
        // 1. 获取待处理的文件列表
        // 2. 逐个文件添加水印
        // 3. 更新进度和处理数量

        // 模拟文件处理（分步检查取消状态）
        try {
            for (int i = 0; i < 20; i++) {
                // 检查取消标志
                if (taskCancelMap.getOrDefault(task.getId(), false)) {
                    log.info("文件任务检测到取消信号，任务ID: {}", task.getId());
                    throw new BusinessException("任务已被取消");
                }
                Thread.sleep(100); // 模拟处理时间
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("任务被中断");
        }

        // 再次检查取消标志
        if (taskCancelMap.getOrDefault(task.getId(), false)) {
            throw new BusinessException("任务已被取消");
        }

        progress.set(50); // 处理中

        // 设置输出文件路径
        String outputFileName = "watermarked_" + System.currentTimeMillis() + ".pdf";
        String outputFilePath = watermarkConfig.getOutput().getPath() + File.separator + outputFileName;
        task.setOutputFilePath(outputFilePath);

        // TODO: 实际需要生成水印文件
        // 使用 embeddingEngine 对原始文件添加水印并保存到 outputFilePath
        // 目前只设置路径，实际文件生成功能需要实现

        // 更新处理数量
        task.setProcessCount(1L);
        taskMapper.updateById(task);

        progress.set(100); // 完成
    }

    /**
     * 执行数据库类型任务
     */
    private void executeDatabaseTask(WatermarkTask task, WatermarkTemplate template, AtomicInteger progress) {
        log.info("执行数据库类型水印任务，任务ID: {}", task.getId());

        if (task.getDataSourceId() == null) {
            throw new BusinessException("数据库任务需要指定数据源ID");
        }

        // 检查取消标志
        if (taskCancelMap.getOrDefault(task.getId(), false)) {
            throw new BusinessException("任务已被取消");
        }

        progress.set(10); // 开始处理

        // 这里需要实现具体的数据库处理逻辑
        // 1. 连接数据库
        // 2. 获取需要处理的表和记录
        // 3. 添加数据库水印
        // 4. 更新进度和处理数量

        // 模拟数据库处理（分步检查取消状态）
        try {
            for (int i = 0; i < 30; i++) {
                // 检查取消标志
                if (taskCancelMap.getOrDefault(task.getId(), false)) {
                    log.info("数据库任务检测到取消信号，任务ID: {}", task.getId());
                    throw new BusinessException("任务已被取消");
                }
                Thread.sleep(100); // 模拟处理时间
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("任务被中断");
        }

        // 再次检查取消标志
        if (taskCancelMap.getOrDefault(task.getId(), false)) {
            throw new BusinessException("任务已被取消");
        }

        progress.set(70);

        // 更新处理数量（模拟处理了1000条记录）
        task.setProcessCount(1000L);
        taskMapper.updateById(task);

        progress.set(100); // 完成
    }

    @Override
    public int getTaskProgress(Long taskId) {
        AtomicInteger progress = taskProgressMap.get(taskId);
        return progress != null ? progress.get() : 0;
    }

    @Override
    public boolean cancelTask(Long taskId) {
        log.info("取消水印任务，任务ID: {}", taskId);
        
        try {
            WatermarkTask task = getTaskById(taskId);
            
            // 只允许取消运行中的任务
            if (!TaskStatus.RUNNING.getCode().equals(task.getStatus())) {
                throw new BusinessException("只能取消运行中的任务");
            }
            
            // 设置取消标志
            taskCancelMap.put(taskId, true);
            
            // 更新任务状态
            task.setStatus(TaskStatus.FAILED.getCode());
            task.setErrorMessage("任务被取消");
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
            
            log.info("取消水印任务成功，任务ID: {}", taskId);
            return true;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消水印任务失败，任务ID: {}", taskId, e);
            throw new BusinessException("取消任务失败");
        }
    }

    @Override
    public boolean retryTask(Long taskId) {
        log.info("重试水印任务，任务ID: {}", taskId);
        
        try {
            WatermarkTask task = getTaskById(taskId);
            
            // 只允许重试失败的任务
            if (!TaskStatus.FAILED.getCode().equals(task.getStatus())) {
                throw new BusinessException("只能重试失败的任务");
            }
            
            // 重置任务状态
            task.setStatus(TaskStatus.PENDING.getCode());
            task.setErrorMessage(null);
            task.setStartTime(null);
            task.setEndTime(null);
            task.setProcessCount(0L);
            
            taskMapper.updateById(task);
            
            log.info("重试水印任务成功，任务ID: {}", taskId);
            return true;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("重试水印任务失败，任务ID: {}", taskId, e);
            throw new BusinessException("重试任务失败");
        }
    }

    @Override
    public String getTaskResultFile(Long taskId) {
        log.info("获取任务结果文件，任务ID: {}", taskId);
        
        try {
            WatermarkTask task = getTaskById(taskId);
            
            // 只允许获取成功任务的结果文件
            if (!TaskStatus.SUCCESS.getCode().equals(task.getStatus())) {
                throw new BusinessException("只能获取成功任务的结果文件");
            }
            
            if (task.getOutputFilePath() == null || task.getOutputFilePath().trim().isEmpty()) {
                throw new BusinessException("任务结果文件路径为空");
            }
            
            // 检查文件是否存在
            File resultFile = new File(task.getOutputFilePath());
            if (!resultFile.exists()) {
                throw new BusinessException("任务结果文件不存在");
            }
            
            return task.getOutputFilePath();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取任务结果文件失败，任务ID: {}", taskId, e);
            throw new BusinessException("获取结果文件失败");
        }
    }

    /**
     * 获取待处理的任务
     */
    @Override
    public List<WatermarkTask> getPendingTasks(int limit) {
        return taskMapper.selectPendingTasks(limit);
    }

    /**
     * 更新任务（供内部使用）
     */
    @Override
    public boolean updateById(WatermarkTask task) {
        return taskMapper.updateById(task) > 0;
    }
}