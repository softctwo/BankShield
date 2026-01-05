package com.bankshield.api.scanner;

import com.bankshield.api.entity.ScanRule;
import com.bankshield.api.entity.VulnerabilityRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CodeSecurityScanner {

    public List<VulnerabilityRecord> scanJavaCode(Long taskId, String sourcePath, List<ScanRule> rules) {
        List<VulnerabilityRecord> vulnerabilities = new ArrayList<>();
        
        log.info("开始代码安全扫描，路径: {}", sourcePath);
        
        File sourceDir = new File(sourcePath);
        if (!sourceDir.exists()) {
            log.warn("源代码路径不存在: {}", sourcePath);
            return vulnerabilities;
        }
        
        scanDirectory(taskId, sourceDir, rules, vulnerabilities);
        
        log.info("代码安全扫描完成，发现 {} 个漏洞", vulnerabilities.size());
        return vulnerabilities;
    }

    private void scanDirectory(Long taskId, File dir, List<ScanRule> rules, List<VulnerabilityRecord> vulnerabilities) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(taskId, file, rules, vulnerabilities);
            } else if (file.getName().endsWith(".java")) {
                scanFile(taskId, file, rules, vulnerabilities);
            }
        }
    }

    private void scanFile(Long taskId, File file, List<ScanRule> rules, List<VulnerabilityRecord> vulnerabilities) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                for (ScanRule rule : rules) {
                    if (detectVulnerability(line, rule)) {
                        VulnerabilityRecord vuln = createVulnerability(taskId, file, lineNumber, line, rule);
                        vulnerabilities.add(vuln);
                        log.warn("发现代码安全问题: {} - {} ({}:{})", 
                                rule.getRuleCode(), rule.getRuleName(), file.getName(), lineNumber);
                    }
                }
            }
        } catch (Exception e) {
            log.error("扫描文件失败: {} - {}", file.getAbsolutePath(), e.getMessage());
        }
    }

    private boolean detectVulnerability(String code, ScanRule rule) {
        String pattern = rule.getDetectionPattern();
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }
        
        try {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(code).find();
        } catch (Exception e) {
            log.error("正则表达式匹配失败: {}", e.getMessage());
            return false;
        }
    }

    private VulnerabilityRecord createVulnerability(Long taskId, File file, int lineNumber, 
                                                    String code, ScanRule rule) {
        VulnerabilityRecord vuln = new VulnerabilityRecord();
        vuln.setTaskId(taskId);
        vuln.setVulnCode(rule.getRuleCode());
        vuln.setVulnName(rule.getRuleName());
        vuln.setVulnType("SECURITY_MISCONFIGURATION");
        vuln.setSeverity(rule.getSeverity());
        vuln.setCvssScore(calculateCvssScore(rule.getSeverity()));
        vuln.setDescription(rule.getDescription());
        vuln.setLocation(file.getAbsolutePath() + ":" + lineNumber);
        vuln.setProofOfConcept("代码行: " + code.trim());
        vuln.setImpact("可能导致敏感信息泄露或系统安全风险");
        vuln.setRecommendation("1. 使用安全的编码实践\n2. 避免硬编码敏感信息\n3. 使用配置文件或环境变量\n4. 定期进行代码审查");
        vuln.setReferenceLinks(rule.getReferenceLinks());
        vuln.setStatus("OPEN");
        vuln.setCreatedTime(LocalDateTime.now());
        vuln.setUpdatedTime(LocalDateTime.now());
        return vuln;
    }

    private BigDecimal calculateCvssScore(String severity) {
        switch (severity) {
            case "CRITICAL": return new BigDecimal("9.5");
            case "HIGH": return new BigDecimal("7.5");
            case "MEDIUM": return new BigDecimal("5.0");
            case "LOW": return new BigDecimal("2.5");
            default: return new BigDecimal("0.0");
        }
    }
}
