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
public class XssScanner {

    public List<VulnerabilityRecord> scan(Long taskId, String target, List<ScanRule> rules) {
        List<VulnerabilityRecord> vulnerabilities = new ArrayList<>();
        
        log.info("开始XSS扫描，目标: {}", target);
        
        for (ScanRule rule : rules) {
            try {
                if (detectVulnerability(target, rule)) {
                    VulnerabilityRecord vuln = createVulnerability(taskId, target, rule);
                    vulnerabilities.add(vuln);
                    log.warn("发现XSS漏洞: {} - {}", rule.getRuleCode(), rule.getRuleName());
                }
            } catch (Exception e) {
                log.error("XSS检测失败: {}", e.getMessage(), e);
            }
        }
        
        log.info("XSS扫描完成，发现 {} 个漏洞", vulnerabilities.size());
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
        vuln.setVulnType("XSS");
        vuln.setSeverity(rule.getSeverity());
        vuln.setCvssScore(calculateCvssScore(rule.getSeverity()));
        vuln.setDescription(rule.getDescription());
        vuln.setLocation(location);
        vuln.setProofOfConcept("检测到XSS特征: " + rule.getDetectionPattern());
        vuln.setImpact("可能导致会话劫持、钓鱼攻击、恶意代码执行或用户信息窃取");
        vuln.setRecommendation("1. 对所有用户输入进行HTML编码\n2. 使用CSP(Content Security Policy)\n3. 设置HttpOnly和Secure标志\n4. 使用安全的模板引擎");
        vuln.setReferenceLinks("https://owasp.org/www-community/attacks/xss/");
        vuln.setStatus("OPEN");
        vuln.setCreatedTime(LocalDateTime.now());
        vuln.setUpdatedTime(LocalDateTime.now());
        return vuln;
    }

    private BigDecimal calculateCvssScore(String severity) {
        switch (severity) {
            case "CRITICAL": return new BigDecimal("9.0");
            case "HIGH": return new BigDecimal("7.5");
            case "MEDIUM": return new BigDecimal("5.0");
            case "LOW": return new BigDecimal("3.0");
            default: return new BigDecimal("0.0");
        }
    }
}
