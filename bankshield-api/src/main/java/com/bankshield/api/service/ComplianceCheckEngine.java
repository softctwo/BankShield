package com.bankshield.api.service;

import com.bankshield.api.entity.ComplianceCheckItem;

import java.util.List;
import java.util.Map;

/**
 * 合规检查引擎接口
 */
public interface ComplianceCheckEngine {
    
    /**
     * 执行等保检查
     */
    List<ComplianceCheckItem> performDengBaoCheck();
    
    /**
     * 执行PCI-DSS检查
     */
    List<ComplianceCheckItem> performPciDssCheck();
    
    /**
     * 执行GDPR检查
     */
    List<ComplianceCheckItem> performGdprCheck();
    
    /**
     * 执行所有合规检查
     */
    Map<String, List<ComplianceCheckItem>> performAllChecks();
    
    /**
     * 检查访问控制
     */
    ComplianceCheckItem checkAccessControl();
    
    /**
     * 检查身份鉴别
     */
    ComplianceCheckItem checkIdentityAuthentication();
    
    /**
     * 检查安全审计
     */
    ComplianceCheckItem checkSecurityAudit();
    
    /**
     * 检查数据完整性
     */
    ComplianceCheckItem checkDataIntegrity();
    
    /**
     * 检查防火墙配置
     */
    ComplianceCheckItem checkFirewallConfiguration();
    
    /**
     * 检查加密传输
     */
    ComplianceCheckItem checkEncryptedTransmission();
    
    /**
     * 检查数据主体权利
     */
    ComplianceCheckItem checkDataSubjectRights();
    
    /**
     * 计算合规评分
     */
    double calculateComplianceScore(String complianceStandard);
    
    /**
     * 获取不合规项
     */
    List<ComplianceCheckItem> getNonCompliantItems(String complianceStandard);

    /**
     * 执行特定检查项
     *
     * @param checkType 检查类型
     * @return 检查结果
     */
    ComplianceCheckItem performCheck(String checkType);
}