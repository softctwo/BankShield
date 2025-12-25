package com.bankshield.api.service.impl;

import com.bankshield.api.entity.SecurityScanResult;
import com.bankshield.api.entity.SecurityScanTask;
import com.bankshield.api.enums.ScanStatus;
import com.bankshield.api.enums.ScanType;
import com.bankshield.api.mapper.SecurityScanResultMapper;
import com.bankshield.api.mapper.SecurityScanTaskMapper;
import com.bankshield.api.service.SecurityScanEngine;
import com.bankshield.api.service.SecurityScanTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 安全扫描任务服务实现类
 * @author BankShield
 */
@Slf4j
@Service
public class SecurityScanTaskServiceImpl extends ServiceImpl<SecurityScanTaskMapper, SecurityScanTask> implements SecurityScanTaskService {

    @Autowired
    private SecurityScanTaskMapper scanTaskMapper;

    @Autowired
    private SecurityScanResultMapper scanResultMapper;

    @Autowired
    private SecurityScanEngine scanEngine;

    // 任务执行日志存储
    private final Map<Long, List<String>> taskExecutionLogs = new HashMap<>();

    @Override
    @Transactional
    public SecurityScanTask createScanTask(SecurityScanTask task) {
        log.info("创建扫描任务: {}", task.getTaskName());
        
        // 参数验证
        validateScanTask(task);
        
        // 设置默认状态
        task.setStatus(ScanStatus.PENDING.name());
        task.setProgress(0);
        task.setRiskCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        // 保存任务
        scanTaskMapper.insert(task);
        
        log.info("扫描任务创建成功: {}, ID: {}", task.getTaskName(), task.getId());
        return task;
    }

    @Override
    @Async
    public void executeScanTask(Long taskId) {
        log.info("开始执行扫描任务: {}", taskId);
        
        SecurityScanTask task = scanTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("扫描任务不存在: {}", taskId);
            return;
        }
        
        try {
            // 记录执行日志
            addTaskLog(taskId, "开始执行扫描任务: " + task.getTaskName());
            
            // 更新任务状态为运行中
            updateTaskStatus(taskId, ScanStatus.RUNNING.name(), 0, null);
            
            // 记录开始时间
            task.setStartTime(LocalDateTime.now());
            scanTaskMapper.updateById(task);
            
            // 根据扫描类型执行相应的扫描
            List<SecurityScanResult> results = performScanByType(task);
            
            // 保存扫描结果
            if (!results.isEmpty()) {
                saveScanResults(results);
                task.setRiskCount(results.size());
                addTaskLog(taskId, "扫描完成，发现 " + results.size() + " 个风险");
            } else {
                task.setRiskCount(0);
                addTaskLog(taskId, "扫描完成，未发现风险");
            }
            
            // 生成扫描报告
            String reportPath = generateScanReport(taskId);
            task.setReportPath(reportPath);
            
            // 更新任务状态为成功
            task.setEndTime(LocalDateTime.now());
            task.setStatus(ScanStatus.SUCCESS.name());
            task.setProgress(100);
            task.setUpdateTime(LocalDateTime.now());
            scanTaskMapper.updateById(task);
            
            addTaskLog(taskId, "扫描任务执行成功");
            
        } catch (Exception e) {
            log.error("扫描任务执行失败: " + taskId, e);
            
            // 更新任务状态为失败
            task.setEndTime(LocalDateTime.now());
            task.setStatus(ScanStatus.FAILED.name());
            task.setErrorMessage(e.getMessage());
            task.setUpdateTime(LocalDateTime.now());
            scanTaskMapper.updateById(task);
            
            addTaskLog(taskId, "扫描任务执行失败: " + e.getMessage());
        }
    }

    @Override
    public void stopScanTask(Long taskId) {
        log.info("停止扫描任务: {}", taskId);
        
        SecurityScanTask task = scanTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("扫描任务不存在: {}", taskId);
            return;
        }
        
        // 调用扫描引擎停止扫描
        scanEngine.stopScan(task);
        
        // 更新任务状态
        task.setStatus(ScanStatus.PARTIAL.name());
        task.setUpdateTime(LocalDateTime.now());
        scanTaskMapper.updateById(task);
        
        addTaskLog(taskId, "扫描任务已停止");
    }

    @Override
    public int getScanProgress(Long taskId) {
        SecurityScanTask task = scanTaskMapper.selectById(taskId);
        if (task == null) {
            return 0;
        }
        
        // 如果任务已完成，返回100
        if (ScanStatus.SUCCESS.name().equals(task.getStatus()) || 
            ScanStatus.FAILED.name().equals(task.getStatus())) {
            return 100;
        }
        
        // 如果任务正在运行，从扫描引擎获取进度
        if (ScanStatus.RUNNING.name().equals(task.getStatus())) {
            return scanEngine.getScanProgress(task);
        }
        
        return task.getProgress() != null ? task.getProgress() : 0;
    }

    @Override
    public Map<String, Object> getScanTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总任务数
        int totalTasks = (int) (long) scanTaskMapper.selectCount(new QueryWrapper<>());
        statistics.put("totalTasks", totalTasks);
        
        // 不同状态的任务数
        List<SecurityScanTaskMapper.TaskStatusCount> statusCounts = scanTaskMapper.countTasksByStatus();
        Map<String, Long> statusMap = statusCounts.stream()
            .collect(Collectors.toMap(SecurityScanTaskMapper.TaskStatusCount::getStatus, 
                                     SecurityScanTaskMapper.TaskStatusCount::getCount));
        statistics.put("statusCounts", statusMap);
        
        // 今日任务数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        int todayTasks = scanTaskMapper.countTasksByTimeRange(todayStart, LocalDateTime.now());
        statistics.put("todayTasks", todayTasks);
        
        // 本周任务数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        int weekTasks = scanTaskMapper.countTasksByTimeRange(weekStart, LocalDateTime.now());
        statistics.put("weekTasks", weekTasks);
        
        // 本月任务数
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        int monthTasks = scanTaskMapper.countTasksByTimeRange(monthStart, LocalDateTime.now());
        statistics.put("monthTasks", monthTasks);
        
        // 最近7天的任务趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime dateStart = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dateEnd = date.withHour(23).withMinute(59).withSecond(59);
            
            int dayTasks = scanTaskMapper.countTasksByTimeRange(dateStart, dateEnd);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            dayData.put("count", dayTasks);
            trend.add(dayData);
        }
        statistics.put("trend", trend);
        
        return statistics;
    }

    @Override
    public String generateScanReport(Long taskId) {
        log.info("生成扫描报告: {}", taskId);
        
        SecurityScanTask task = scanTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("扫描任务不存在: {}", taskId);
            return null;
        }
        
        try {
            // 获取扫描结果
            List<SecurityScanResult> results = scanResultMapper.selectByTaskId(taskId);
            
            // 生成报告内容
            String reportContent = generateReportContent(task, results);
            
            // 保存报告文件
            String reportPath = saveReportFile(task, reportContent);
            
            // 更新任务报告路径
            task.setReportPath(reportPath);
            task.setUpdateTime(LocalDateTime.now());
            scanTaskMapper.updateById(task);
            
            log.info("扫描报告生成成功: {}", reportPath);
            return reportPath;
            
        } catch (Exception e) {
            log.error("生成扫描报告失败: " + taskId, e);
            throw new RuntimeException("生成报告失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SecurityScanTask> getRecentTasks(int limit) {
        return scanTaskMapper.selectRecentTasks(limit);
    }

    @Override
    public List<SecurityScanTask> getTasksByStatus(String status) {
        return scanTaskMapper.selectByStatus(status);
    }

    @Override
    public void updateTaskStatus(Long taskId, String status, Integer progress, String errorMessage) {
        scanTaskMapper.updateTaskStatus(taskId, status, progress, errorMessage);
    }

    @Override
    public void completeTask(Long taskId, String status, Integer riskCount, String reportPath) {
        scanTaskMapper.updateTaskComplete(taskId, status, LocalDateTime.now(), riskCount, reportPath);
    }

    @Override
    @Transactional
    public boolean deleteScanTask(Long taskId) {
        log.info("删除扫描任务: {}", taskId);
        
        try {
            // 先删除相关的扫描结果
            QueryWrapper<SecurityScanResult> resultWrapper = new QueryWrapper<>();
            resultWrapper.eq("task_id", taskId);
            scanResultMapper.delete(resultWrapper);
            
            // 删除任务
            int result = scanTaskMapper.deleteById(taskId);
            
            // 清理执行日志
            taskExecutionLogs.remove(taskId);
            
            log.info("扫描任务删除成功: {}", taskId);
            return result > 0;
            
        } catch (Exception e) {
            log.error("删除扫描任务失败: " + taskId, e);
            throw new RuntimeException("删除任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean batchDeleteTasks(List<Long> taskIds) {
        log.info("批量删除扫描任务: {}", taskIds);
        
        try {
            for (Long taskId : taskIds) {
                // 删除相关的扫描结果
                QueryWrapper<SecurityScanResult> resultWrapper = new QueryWrapper<>();
                resultWrapper.eq("task_id", taskId);
                scanResultMapper.delete(resultWrapper);
                
                // 清理执行日志
                taskExecutionLogs.remove(taskId);
            }
            
            // 批量删除任务
            int result = scanTaskMapper.deleteBatchIds(taskIds);
            
            log.info("批量删除扫描任务成功，删除数量: {}", result);
            return result > 0;
            
        } catch (Exception e) {
            log.error("批量删除扫描任务失败", e);
            throw new RuntimeException("批量删除任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getTaskExecutionLog(Long taskId) {
        List<String> logs = taskExecutionLogs.get(taskId);
        return logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SecurityScanTask> getScanTasks(
            int page, int size, String taskName, String scanType, String status) {
        com.baomidou.mybatisplus.core.metadata.IPage<SecurityScanTask> pageParam =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SecurityScanTask> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();

        if (taskName != null && !taskName.trim().isEmpty()) {
            wrapper.like("task_name", taskName);
        }
        if (scanType != null && !scanType.trim().isEmpty()) {
            wrapper.eq("scan_type", scanType);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");

        return scanTaskMapper.selectPage(pageParam, wrapper);
    }

    // 私有辅助方法

    private void validateScanTask(SecurityScanTask task) {
        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        
        if (task.getScanType() == null || task.getScanType().trim().isEmpty()) {
            throw new IllegalArgumentException("扫描类型不能为空");
        }
        
        // 验证扫描类型
        try {
            ScanType.valueOf(task.getScanType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的扫描类型: " + task.getScanType());
        }
        
        if (task.getScanTarget() == null || task.getScanTarget().trim().isEmpty()) {
            throw new IllegalArgumentException("扫描目标不能为空");
        }
        
        if (task.getCreatedBy() == null || task.getCreatedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("创建人不能为空");
        }
    }

    private List<SecurityScanResult> performScanByType(SecurityScanTask task) {
        ScanType scanType = ScanType.valueOf(task.getScanType());
        
        switch (scanType) {
            case VULNERABILITY:
                addTaskLog(task.getId(), "开始漏洞扫描");
                return scanEngine.performVulnerabilityScan(task);
                
            case CONFIG:
                addTaskLog(task.getId(), "开始配置检查");
                return scanEngine.performConfigCheck(task);
                
            case WEAK_PASSWORD:
                addTaskLog(task.getId(), "开始弱密码检测");
                return scanEngine.performWeakPasswordCheck(task);
                
            case ANOMALY:
                addTaskLog(task.getId(), "开始异常行为检测");
                return scanEngine.performAnomalyDetection(task);
                
            default:
                throw new IllegalArgumentException("不支持的扫描类型: " + scanType);
        }
    }

    private void saveScanResults(List<SecurityScanResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        
        // 批量插入扫描结果
        scanResultMapper.batchInsert(results);
    }

    private String generateReportContent(SecurityScanTask task, List<SecurityScanResult> results) {
        StringBuilder content = new StringBuilder();
        
        // 报告标题
        content.append("BankShield 安全扫描报告\n");
        content.append("===================\n\n");
        
        // 基本信息
        content.append("任务信息\n");
        content.append("--------\n");
        content.append("任务名称: ").append(task.getTaskName()).append("\n");
        content.append("扫描类型: ").append(task.getScanType()).append("\n");
        content.append("扫描目标: ").append(task.getScanTarget()).append("\n");
        content.append("开始时间: ").append(task.getStartTime()).append("\n");
        content.append("结束时间: ").append(task.getEndTime()).append("\n");
        content.append("发现风险: ").append(task.getRiskCount()).append(" 个\n\n");
        
        // 风险统计
        content.append("风险统计\n");
        content.append("--------\n");
        Map<String, Long> riskLevelCount = results.stream()
            .collect(Collectors.groupingBy(SecurityScanResult::getRiskLevel, Collectors.counting()));
        
        riskLevelCount.forEach((level, count) -> {
            content.append(level).append(": ").append(count).append(" 个\n");
        });
        content.append("\n");
        
        // 风险详情
        content.append("风险详情\n");
        content.append("--------\n");
        
        for (int i = 0; i < results.size(); i++) {
            SecurityScanResult result = results.get(i);
            content.append("风险 ").append(i + 1).append("\n");
            content.append("  风险级别: ").append(result.getRiskLevel()).append("\n");
            content.append("  风险类型: ").append(result.getRiskType()).append("\n");
            content.append("  风险描述: ").append(result.getRiskDescription()).append("\n");
            content.append("  影响范围: ").append(result.getImpactScope()).append("\n");
            content.append("  修复建议: ").append(result.getRemediationAdvice()).append("\n");
            content.append("  发现时间: ").append(result.getDiscoveredTime()).append("\n\n");
        }
        
        return content.toString();
    }

    private String saveReportFile(SecurityScanTask task, String content) throws IOException {
        // 创建报告目录
        String reportDir = "reports/security-scans/" + task.getId();
        File dir = new File(reportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 生成报告文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("security_scan_report_%s_%s.txt", task.getId(), timestamp);
        String filePath = reportDir + "/" + fileName;
        
        // 保存报告文件
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
        
        return filePath;
    }

    private void addTaskLog(Long taskId, String message) {
        taskExecutionLogs.computeIfAbsent(taskId, k -> new ArrayList<>())
            .add(String.format("[%s] %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 
                message));
    }
}