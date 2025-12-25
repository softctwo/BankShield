package com.bankshield.common.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 合规报告生成器测试
 */
@SpringBootTest
@ActiveProfiles("national-crypto")
public class ComplianceReportGenerator {
    
    @Autowired
    private NationalCryptoComplianceReport complianceReport;
    
    @Test
    public void generateAllComplianceReports() {
        // 创建报告目录
        String reportDir = "target/compliance-reports";
        new File(reportDir).mkdirs();
        
        // 生成完整合规报告
        complianceReport.generateCompleteComplianceReport(reportDir);
        
        // 验证报告文件生成
        File mainReport = new File(reportDir + "/national-crypto-compliance-report.txt");
        File usageReport = new File(reportDir + "/algorithm-usage-report.txt");
        File cleanupReport = new File(reportDir + "/international-algorithm-cleanup-report.txt");
        File performanceReport = new File(reportDir + "/performance-test-report.txt");
        
        assertTrue(mainReport.exists(), "主合规报告应该存在");
        assertTrue(usageReport.exists(), "算法使用清单应该存在");
        assertTrue(cleanupReport.exists(), "国际算法清理报告应该存在");
        assertTrue(performanceReport.exists(), "性能测试报告应该存在");
        
        // 验证报告内容不为空
        assertTrue(mainReport.length() > 0, "主合规报告不应该为空");
        assertTrue(usageReport.length() > 0, "算法使用清单不应该为空");
        assertTrue(cleanupReport.length() > 0, "国际算法清理报告不应该为空");
        assertTrue(performanceReport.length() > 0, "性能测试报告不应该为空");
        
        System.out.println("✅ 所有合规报告已生成到: " + reportDir);
    }
    
    @Test
    public void testMainComplianceReport() {
        String report = complianceReport.generateComplianceReport();
        
        // 验证报告包含关键内容
        assertTrue(report.contains("BankShield 国密算法合规报告"), "报告应该包含标题");
        assertTrue(report.contains("算法替换清单"), "报告应该包含算法替换清单");
        assertTrue(report.contains("国密算法实现状态"), "报告应该包含实现状态");
        assertTrue(report.contains("国际算法清理状态"), "报告应该包含清理状态");
        assertTrue(report.contains("性能指标"), "报告应该包含性能指标");
        assertTrue(report.contains("安全性指标"), "报告应该包含安全性指标");
        assertTrue(report.contains("合规性检查"), "报告应该包含合规性检查");
        assertTrue(report.contains("测试覆盖率"), "报告应该包含测试覆盖率");
        assertTrue(report.contains("风险评估"), "报告应该包含风险评估");
        assertTrue(report.contains("结论"), "报告应该包含结论");
        
        // 验证关键算法提及
        assertTrue(report.contains("SM2"), "报告应该提及SM2");
        assertTrue(report.contains("SM3"), "报告应该提及SM3");
        assertTrue(report.contains("SM4"), "报告应该提及SM4");
        assertTrue(report.contains("BCrypt"), "报告应该提及BCrypt替换");
        assertTrue(report.contains("AES"), "报告应该提及AES替换");
        assertTrue(report.contains("RSA"), "报告应该提及RSA替换");
        assertTrue(report.contains("SHA-256"), "报告应该提及SHA-256替换");
        
        System.out.println("✅ 主合规报告内容验证通过");
    }
    
    @Test
    public void testAlgorithmUsageReport() {
        String report = complianceReport.generateAlgorithmUsageReport();
        
        assertTrue(report.contains("国密算法使用清单"), "报告应该包含标题");
        assertTrue(report.contains("SM2算法使用"), "报告应该包含SM2使用");
        assertTrue(report.contains("SM3算法使用"), "报告应该包含SM3使用");
        assertTrue(report.contains("SM4算法使用"), "报告应该包含SM4使用");
        
        System.out.println("✅ 算法使用清单内容验证通过");
    }
    
    @Test
    public void testInternationalAlgorithmCleanupReport() {
        String report = complianceReport.generateInternationalAlgorithmCleanupReport();
        
        assertTrue(report.contains("国际算法清理报告"), "报告应该包含标题");
        assertTrue(report.contains("MD5"), "报告应该提及MD5");
        assertTrue(report.contains("SHA-1"), "报告应该提及SHA-1");
        assertTrue(report.contains("SHA-256"), "报告应该提及SHA-256");
        assertTrue(report.contains("AES"), "报告应该提及AES");
        assertTrue(report.contains("RSA"), "报告应该提及RSA");
        assertTrue(report.contains("BCrypt"), "报告应该提及BCrypt");
        assertTrue(report.contains("清理率: 100%"), "报告应该显示100%清理率");
        
        System.out.println("✅ 国际算法清理报告内容验证通过");
    }
    
    @Test
    public void testPerformanceReport() {
        String report = complianceReport.generatePerformanceReport();
        
        assertTrue(report.contains("性能测试报告"), "报告应该包含标题");
        assertTrue(report.contains("SM2性能"), "报告应该包含SM2性能");
        assertTrue(report.contains("SM3性能"), "报告应该包含SM3性能");
        assertTrue(report.contains("SM4性能"), "报告应该包含SM4性能");
        assertTrue(report.contains("性能指标达标"), "报告应该显示性能达标");
        
        System.out.println("✅ 性能测试报告内容验证通过");
    }
}