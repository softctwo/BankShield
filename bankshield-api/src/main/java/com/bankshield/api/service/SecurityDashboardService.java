package com.bankshield.api.service;

import com.bankshield.api.dto.SecurityDashboardDTO;

/**
 * 安全态势仪表板服务接口
 */
public interface SecurityDashboardService {
    
    /**
     * 获取安全态势仪表板数据
     */
    SecurityDashboardDTO getDashboardData();
    
    /**
     * 计算安全评分
     */
    Integer calculateSecurityScore();
    
    /**
     * 获取安全等级
     */
    String getSecurityLevel();
}
