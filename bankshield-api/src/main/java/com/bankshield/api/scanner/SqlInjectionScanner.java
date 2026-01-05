package com.bankshield.api.scanner;

import com.bankshield.api.entity.ScanRule;
import com.bankshield.api.entity.VulnerabilityRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SqlInjectionScanner {

    public List<VulnerabilityRecord> scan(Long taskId, String target, List<ScanRule> rules) {
        List<VulnerabilityRecord> vulnerabilities = new ArrayList<>();
        
        log.info("开始SQL注入扫描，目标: {}", target);
        
        for (ScanRule rule : rules) {
            try {
                if (detectVulnerability(target, rule)) {
                    VulnerabilityRecord vuln = createVulnerability(taskId, target, rule);
                    vulnerabilities.add(vuln);
                    log.warn("发现SQL注入漏洞: {} - {}", rule.getRuleCode(), rule.getRuleName());
                }
            } catch (Exception e) {
                log.error("SQL注入检测失败: {}", e.getMessage(), e);
            }
        }
        
        log.info("SQL注入扫描完成，发现 {} 个漏洞", vulnerabilities.size());
        return vulnerabilities;
    }

    private boolean detectVulnerability(String target, ScanRule rule) {
        String pattern = rule.getDetectionPattern();
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }
        
        try {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(target).find();
        } catch (Exception e) {
            log.error("正则表达式匹配失败: {}", e.getMessage());
            return false;
        }
    }

    private VulnerabilityRecord createVulnerability(Long taskId, String location, ScanRule rule) {
        VulnerabilityRecord vuln = new VulnerabilityRecord();
        vuln.setTaskId(taskId);
        vuln.setVulnCode(rule.getRuleCode());
        vuln.setVulnName(rule.getRuleName());
        vuln.setVulnType("SQL_INJECTION");
        vuln.setSeverity(rule.getSeverity());
        vuln.setCvssScore(calculateCvssScore(rule.getSeverity()));
        vuln.setDescription(rule.getDescription());
        vuln.setLocation(location);
        vuln.setProofOfConcept("检测到SQL注入特征: " + rule.getDetectionPattern());
        vuln.setImpact("可能导致数据库信息泄露、数据篡改或系统被完全控制");
        vuln.setRecommendation("1. 使用参数化查询或预编译语句\n2. 对用户输入进行严格验证和转义\n3. 使用ORM框架\n4. 实施最小权限原则");
        vuln.setReferenceLinks("https://owasp.org/www-community/attacks/SQL_Injection");
        vuln.setStatus("OPEN");
        vuln.setCreatedTime(LocalDateTime.now());
        vuln.setUpdatedTime(LocalDateTime.now());
        return vuln;
    }

    private BigDecimal calculateCvssScore(String severity) {
        switch (severity) {
            case "CRITICAL": return new BigDecimal("9.5");
            case "HIGH": return new BigDecimal("8.0");
            case "MEDIUM": return new BigDecimal("5.5");
            case "LOW": return new BigDecimal("3.0");
            default: return new BigDecimal("0.0");
        }
    }
}
