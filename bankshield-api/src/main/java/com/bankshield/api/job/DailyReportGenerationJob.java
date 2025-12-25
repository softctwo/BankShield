package com.bankshield.api.job;

import com.bankshield.api.entity.ReportTemplate;
import com.bankshield.api.entity.ReportGenerationTask;
import com.bankshield.api.mapper.ReportTemplateMapper;
import com.bankshield.api.mapper.ReportGenerationTaskMapper;
import com.bankshield.api.service.ReportGenerationTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 每日报表生成定时任务
 * 每天早上8点自动生成本日的合规报表
 */
@Slf4j
@Component
public class DailyReportGenerationJob {
    
    @Autowired
    private ReportTemplateMapper templateMapper;
    
    @Autowired
    private ReportGenerationTaskMapper taskMapper;
    
    @Autowired
    private ReportGenerationTaskService taskService;
    
    /**
     * 每天早上8点执行日报生成任务
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailyReports() {
        log.info("开始执行每日报表生成任务");
        
        try {
            // 查询所有已启用的日报模板
            List<ReportTemplate> templates = templateMapper.selectEnabledByFrequency("DAILY");
            log.info("找到 {} 个日报模板", templates.size());
            
            for (ReportTemplate template : templates) {
                try {
                    // 创建生成任务
                    ReportGenerationTask task = new ReportGenerationTask();
                    task.setTemplateId(template.getId());
                    task.setStatus("PENDING");
                    task.setCreatedBy("SYSTEM_JOB");
                    task.setReportPeriod(getCurrentDate());
                    task.setCreateTime(LocalDateTime.now());
                    taskMapper.insert(task);
                    
                    // 异步执行生成
                    CompletableFuture.runAsync(() -> {
                        try {
                            taskService.generateReport(task.getId());
                        } catch (Exception e) {
                            log.error("异步生成报表失败，任务ID: {}", task.getId(), e);
                        }
                    });
                    
                    log.info("创建日报生成任务成功，模板ID: {}, 任务ID: {}", template.getId(), task.getId());
                    
                } catch (Exception e) {
                    log.error("创建日报生成任务失败，模板ID: {}", template.getId(), e);
                }
            }
            
            log.info("每日报表生成任务执行完成，共创建 {} 个任务", templates.size());
            
        } catch (Exception e) {
            log.error("每日报表生成任务执行失败", e);
        }
    }
    
    /**
     * 每周一早上8点执行周报生成任务
     */
    @Scheduled(cron = "0 0 8 ? * MON")
    public void generateWeeklyReports() {
        log.info("开始执行每周报表生成任务");
        
        try {
            // 查询所有已启用的周报模板
            List<ReportTemplate> templates = templateMapper.selectEnabledByFrequency("WEEKLY");
            log.info("找到 {} 个周报模板", templates.size());
            
            for (ReportTemplate template : templates) {
                try {
                    // 创建生成任务
                    ReportGenerationTask task = new ReportGenerationTask();
                    task.setTemplateId(template.getId());
                    task.setStatus("PENDING");
                    task.setCreatedBy("SYSTEM_JOB");
                    task.setReportPeriod(getCurrentWeek());
                    task.setCreateTime(LocalDateTime.now());
                    taskMapper.insert(task);
                    
                    // 异步执行生成
                    CompletableFuture.runAsync(() -> {
                        try {
                            taskService.generateReport(task.getId());
                        } catch (Exception e) {
                            log.error("异步生成报表失败，任务ID: {}", task.getId(), e);
                        }
                    });
                    
                    log.info("创建周报生成任务成功，模板ID: {}, 任务ID: {}", template.getId(), task.getId());
                    
                } catch (Exception e) {
                    log.error("创建周报生成任务失败，模板ID: {}", template.getId(), e);
                }
            }
            
            log.info("每周报表生成任务执行完成，共创建 {} 个任务", templates.size());
            
        } catch (Exception e) {
            log.error("每周报表生成任务执行失败", e);
        }
    }
    
    /**
     * 每月1号早上8点执行月报生成任务
     */
    @Scheduled(cron = "0 0 8 1 * ?")
    public void generateMonthlyReports() {
        log.info("开始执行每月报表生成任务");
        
        try {
            // 查询所有已启用的月报模板
            List<ReportTemplate> templates = templateMapper.selectEnabledByFrequency("MONTHLY");
            log.info("找到 {} 个月报模板", templates.size());
            
            for (ReportTemplate template : templates) {
                try {
                    // 创建生成任务
                    ReportGenerationTask task = new ReportGenerationTask();
                    task.setTemplateId(template.getId());
                    task.setStatus("PENDING");
                    task.setCreatedBy("SYSTEM_JOB");
                    task.setReportPeriod(getCurrentMonth());
                    task.setCreateTime(LocalDateTime.now());
                    taskMapper.insert(task);
                    
                    // 异步执行生成
                    CompletableFuture.runAsync(() -> {
                        try {
                            taskService.generateReport(task.getId());
                        } catch (Exception e) {
                            log.error("异步生成报表失败，任务ID: {}", task.getId(), e);
                        }
                    });
                    
                    log.info("创建月报生成任务成功，模板ID: {}, 任务ID: {}", template.getId(), task.getId());
                    
                } catch (Exception e) {
                    log.error("创建月报生成任务失败，模板ID: {}", template.getId(), e);
                }
            }
            
            log.info("每月报表生成任务执行完成，共创建 {} 个任务", templates.size());
            
        } catch (Exception e) {
            log.error("每月报表生成任务执行失败", e);
        }
    }
    
    /**
     * 每季度第一个月1号早上8点执行季报生成任务
     */
    @Scheduled(cron = "0 0 8 1 1,4,7,10 ?")
    public void generateQuarterlyReports() {
        log.info("开始执行每季度报表生成任务");
        
        try {
            // 查询所有已启用的季报模板
            List<ReportTemplate> templates = templateMapper.selectEnabledByFrequency("QUARTERLY");
            log.info("找到 {} 个季报模板", templates.size());
            
            for (ReportTemplate template : templates) {
                try {
                    // 创建生成任务
                    ReportGenerationTask task = new ReportGenerationTask();
                    task.setTemplateId(template.getId());
                    task.setStatus("PENDING");
                    task.setCreatedBy("SYSTEM_JOB");
                    task.setReportPeriod(getCurrentQuarter());
                    task.setCreateTime(LocalDateTime.now());
                    taskMapper.insert(task);
                    
                    // 异步执行生成
                    CompletableFuture.runAsync(() -> {
                        try {
                            taskService.generateReport(task.getId());
                        } catch (Exception e) {
                            log.error("异步生成报表失败，任务ID: {}", task.getId(), e);
                        }
                    });
                    
                    log.info("创建季报生成任务成功，模板ID: {}, 任务ID: {}", template.getId(), task.getId());
                    
                } catch (Exception e) {
                    log.error("创建季报生成任务失败，模板ID: {}", template.getId(), e);
                }
            }
            
            log.info("每季度报表生成任务执行完成，共创建 {} 个任务", templates.size());
            
        } catch (Exception e) {
            log.error("每季度报表生成任务执行失败", e);
        }
    }
    
    // 辅助方法
    private String getCurrentDate() {
        return LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE);
    }
    
    private String getCurrentWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY);
        LocalDateTime endOfWeek = now.with(java.time.DayOfWeek.SUNDAY);
        return startOfWeek.format(java.time.format.DateTimeFormatter.ISO_DATE) + " 至 " + 
               endOfWeek.format(java.time.format.DateTimeFormatter.ISO_DATE);
    }
    
    private String getCurrentMonth() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
    }
    
    private String getCurrentQuarter() {
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        return now.getYear() + " Q" + quarter;
    }
}