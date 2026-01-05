package com.bankshield.api.scanner;

import com.bankshield.api.entity.ScanRule;
import com.bankshield.api.entity.SecurityScanTask;
import com.bankshield.api.entity.VulnerabilityRecord;
import com.bankshield.api.mapper.ScanRuleMapper;
import com.bankshield.api.mapper.SecurityScanTaskMapper;
import com.bankshield.api.mapper.VulnerabilityRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScanEngine {

    @Autowired
    private SqlInjectionScanner sqlScanner;

    @Autowired
    private XssScanner xssScanner;

    @Autowired
    private DependencyScanner dependencyScanner;

    @Autowired
    private CodeSecurityScanner codeScanner;

    @Autowired
    private ScanRuleMapper scanRuleMapper;

    @Autowired
    private SecurityScanTaskMapper taskMapper;

    @Autowired
    private VulnerabilityRecordMapper vulnerabilityMapper;

    @Async
    public void executeScan(SecurityScanTask task) {
        log.info("开始执行扫描任务: {} (ID: {})", task.getTaskName(), task.getId());
        
        try {
            // 更新任务状态为运行中
            task.setStatus("RUNNING");
            task.setStartTime(LocalDateTime.now());
            task.setProgress(0);
            taskMapper.updateById(task);
            
            List<VulnerabilityRecord> allVulnerabilities = new ArrayList<>();
            
            // 根据扫描类型执行相应的扫描
            String scanType = task.getScanType();
            if (scanType == null) {
                scanType = "FULL_SCAN";
            }
            
            switch (scanType) {
                case "SQL_INJECTION":
                    allVulnerabilities.addAll(executeSqlInjectionScan(task));
                    break;
                    
                case "XSS":
                    allVulnerabilities.addAll(executeXssScan(task));
                    break;
                    
                case "DEPENDENCY":
                    allVulnerabilities.addAll(executeDependencyScan(task));
                    break;
                    
                case "CODE_SCAN":
                    allVulnerabilities.addAll(executeCodeScan(task));
                    break;
                    
                case "FULL_SCAN":
                    task.setProgress(10);
                    taskMapper.updateById(task);
                    allVulnerabilities.addAll(executeSqlInjectionScan(task));
                    
                    task.setProgress(30);
                    taskMapper.updateById(task);
                    allVulnerabilities.addAll(executeXssScan(task));
                    
                    task.setProgress(50);
                    taskMapper.updateById(task);
                    allVulnerabilities.addAll(executeDependencyScan(task));
                    
                    task.setProgress(70);
                    taskMapper.updateById(task);
                    allVulnerabilities.addAll(executeCodeScan(task));
                    break;
                    
                default:
                    log.warn("未知的扫描类型: {}", scanType);
            }
            
            // 保存漏洞记录
            for (VulnerabilityRecord vuln : allVulnerabilities) {
                vulnerabilityMapper.insert(vuln);
            }
            
            // 统计漏洞数量
            int highRiskCount = (int) allVulnerabilities.stream()
                    .filter(v -> "CRITICAL".equals(v.getSeverity()) || "HIGH".equals(v.getSeverity()))
                    .count();
            int mediumRiskCount = (int) allVulnerabilities.stream()
                    .filter(v -> "MEDIUM".equals(v.getSeverity()))
                    .count();
            int lowRiskCount = (int) allVulnerabilities.stream()
                    .filter(v -> "LOW".equals(v.getSeverity()))
                    .count();
            
            // 更新任务完成状态
            task.setStatus("SUCCESS");
            task.setEndTime(LocalDateTime.now());
            task.setProgress(100);
            task.setRiskCount(allVulnerabilities.size());
            
            // 计算扫描时长
            if (task.getStartTime() != null && task.getEndTime() != null) {
                Duration duration = Duration.between(task.getStartTime(), task.getEndTime());
                log.info("扫描耗时: {} 秒", duration.getSeconds());
            }
            
            taskMapper.updateById(task);
            
            log.info("扫描任务完成: {} - 发现 {} 个漏洞 (高危: {}, 中危: {}, 低危: {})", 
                    task.getTaskName(), allVulnerabilities.size(), highRiskCount, mediumRiskCount, lowRiskCount);
            
        } catch (Exception e) {
            log.error("扫描任务执行失败: {}", e.getMessage(), e);
            task.setStatus("FAILED");
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
            taskMapper.updateById(task);
        }
    }

    private List<VulnerabilityRecord> executeSqlInjectionScan(SecurityScanTask task) {
        log.info("执行SQL注入扫描...");
        List<ScanRule> rules = scanRuleMapper.selectEnabledByType("SQL_INJECTION");
        return sqlScanner.scan(task.getId(), task.getScanTarget(), rules);
    }

    private List<VulnerabilityRecord> executeXssScan(SecurityScanTask task) {
        log.info("执行XSS扫描...");
        List<ScanRule> rules = scanRuleMapper.selectEnabledByType("XSS");
        return xssScanner.scan(task.getId(), task.getScanTarget(), rules);
    }

    private List<VulnerabilityRecord> executeDependencyScan(SecurityScanTask task) {
        log.info("执行依赖漏洞扫描...");
        List<VulnerabilityRecord> vulnerabilities = new ArrayList<>();
        
        String target = task.getScanTarget();
        if (target != null) {
            if (target.contains("pom.xml")) {
                vulnerabilities.addAll(dependencyScanner.scanMavenDependencies(task.getId(), target));
            }
            if (target.contains("package.json")) {
                vulnerabilities.addAll(dependencyScanner.scanNpmDependencies(task.getId(), target));
            }
        }
        
        return vulnerabilities;
    }

    private List<VulnerabilityRecord> executeCodeScan(SecurityScanTask task) {
        log.info("执行代码安全扫描...");
        List<ScanRule> rules = scanRuleMapper.selectEnabledByType("SECURITY_MISCONFIGURATION");
        return codeScanner.scanJavaCode(task.getId(), task.getScanTarget(), rules);
    }
}
