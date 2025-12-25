package com.bankshield.api.controller;

import com.bankshield.api.entity.WatermarkTemplate;
import com.bankshield.api.entity.WatermarkTask;
import com.bankshield.api.entity.WatermarkExtractLog;
import com.bankshield.api.service.WatermarkTemplateService;
import com.bankshield.api.service.WatermarkTaskService;
import com.bankshield.api.service.WatermarkExtractService;
import com.bankshield.common.result.Result;
import com.bankshield.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;

/**
 * 水印管理控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/watermark")
public class WatermarkController {

    @Autowired
    private WatermarkTemplateService templateService;

    @Autowired
    private WatermarkTaskService taskService;

    @Autowired
    private WatermarkExtractService extractService;

    /**
     * 分页查询水印模板
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/template/page")
    public Result<IPage<WatermarkTemplate>> getTemplatePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String watermarkType,
            @RequestParam(required = false) Integer enabled) {
        log.info("分页查询水印模板，页码: {}, 每页大小: {}, 模板名称: {}, 水印类型: {}, 是否启用: {}",
                page, size, templateName, watermarkType, enabled);
        Page<WatermarkTemplate> pageParam = new Page<>(page, size);
        return Result.success(templateService.getTemplatePage(pageParam, templateName, watermarkType, enabled));
    }

    /**
     * 创建水印模板
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/template")
    public Result<WatermarkTemplate> createTemplate(@RequestBody WatermarkTemplate template) {
        log.info("创建水印模板，模板名称: {}", template.getTemplateName());
        return Result.success(templateService.createTemplate(template));
    }

    /**
     * 更新水印模板
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/template")
    public Result<WatermarkTemplate> updateTemplate(@RequestBody WatermarkTemplate template) {
        log.info("更新水印模板，ID: {}", template.getId());
        return Result.success(templateService.updateTemplate(template));
    }

    /**
     * 删除水印模板
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/template/{id}")
    public Result<Boolean> deleteTemplate(@PathVariable Long id) {
        log.info("删除水印模板，ID: {}", id);
        return Result.success(templateService.deleteTemplate(id));
    }

    /**
     * 创建水印任务
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/task")
    public Result<WatermarkTask> createTask(@RequestBody WatermarkTask task) {
        log.info("创建水印任务，任务名称: {}", task.getTaskName());
        return Result.success(taskService.createTask(task));
    }

    /**
     * 查询任务状态
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/task/{id}")
    public Result<WatermarkTask> getTaskById(@PathVariable Long id) {
        log.info("查询水印任务状态，ID: {}", id);
        return Result.success(taskService.getTaskById(id));
    }

    /**
     * 获取任务进度
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/task/{id}/progress")
    public Result<Integer> getTaskProgress(@PathVariable Long id) {
        log.info("获取水印任务进度，ID: {}", id);
        return Result.success(taskService.getTaskProgress(id));
    }

    /**
     * 提取水印
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/extract")
    public Result<WatermarkExtractLog> extractWatermark(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("operator") String operator) {
        log.info("提取水印，文件名: {}, 操作人: {}", file.getOriginalFilename(), operator);
        return Result.success(extractService.extractFromFile(file, operator));
    }

    /**
     * 分页查询提取日志
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/extract/log/page")
    public Result<IPage<WatermarkExtractLog>> getExtractLogPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String watermarkContent,
            @RequestParam(required = false) String extractResult,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        log.info("分页查询水印提取日志，页码: {}, 每页大小: {}", page, size);
        Page<WatermarkExtractLog> pageParam = new Page<>(page, size);
        return Result.success(extractService.getExtractLogPage(pageParam, watermarkContent, extractResult,
                operator, startTime, endTime));
    }

    /**
     * 批量提取水印
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/extract/batch")
    public Result<?> batchExtractWatermark(@RequestParam("files") MultipartFile[] files,
                                          @RequestParam("operator") String operator) {
        log.info("批量提取水印，文件数量: {}, 操作人: {}", files.length, operator);
        return Result.success(extractService.batchExtractFromFiles(files, operator));
    }

    /**
     * 验证文件是否包含水印
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/extract/verify")
    public Result<Boolean> verifyWatermark(@RequestParam("file") MultipartFile file) {
        log.info("验证文件是否包含水印，文件名: {}", file.getOriginalFilename());
        return Result.success(extractService.hasWatermark(file));
    }

    /**
     * 获取提取统计信息
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @GetMapping("/extract/statistics")
    public Result<?> getExtractStatistics(@RequestParam(defaultValue = "7") int days) {
        log.info("获取水印提取统计信息，天数: {}", days);
        return Result.success(extractService.getExtractStatistics(days));
    }

    /**
     * 启用水印模板
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/template/{id}/enable")
    public Result<Boolean> enableTemplate(@PathVariable Long id, @RequestParam Integer enabled) {
        log.info("启用水印模板，ID: {}, 启用状态: {}", id, enabled);
        return Result.success(templateService.toggleTemplateStatus(id, enabled));
    }

    /**
     * 取消任务
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/task/{id}/cancel")
    public Result<Boolean> cancelTask(@PathVariable Long id) {
        log.info("取消水印任务，ID: {}", id);
        return Result.success(taskService.cancelTask(id));
    }

    /**
     * 重试任务
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/task/{id}/retry")
    public Result<Boolean> retryTask(@PathVariable Long id) {
        log.info("重试水印任务，ID: {}", id);
        return Result.success(taskService.retryTask(id));
    }

    /**
     * 下载任务结果文件
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/task/{id}/download")
    public void downloadTaskResult(@PathVariable Long id, HttpServletResponse response) {
        log.info("下载水印任务结果文件，ID: {}", id);
        try {
            String filePath = taskService.getTaskResultFile(id);
            File resultFile = new File(filePath);
            
            if (!resultFile.exists()) {
                throw new BusinessException("结果文件不存在");
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + resultFile.getName() + "\"");
            
            // 写入文件内容
            try (InputStream inputStream = new FileInputStream(resultFile);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载任务结果文件失败，任务ID: {}", id, e);
            throw new BusinessException("下载结果文件失败");
        }
    }
}