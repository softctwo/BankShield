package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.KeyStatisticsDTO;
import com.bankshield.api.dto.KeyUsageStatisticsDTO;
import com.bankshield.api.entity.SecurityKey;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 密钥管理服务接口
 */
public interface KeyManagementService {

    /**
     * 分页查询密钥列表
     */
    IPage<SecurityKey> getKeyPage(Page<SecurityKey> page, String keyName, String keyType, 
                                   String keyStatus, String keyUsage);

    /**
     * 根据ID获取密钥
     */
    SecurityKey getKeyById(Long id);

    /**
     * 生成新密钥
     */
    SecurityKey generateKey(String keyName, String keyType, String keyUsage, 
                           Integer keyLength, Integer expireDays, String description, String createdBy);

    /**
     * 轮换密钥
     */
    SecurityKey rotateKey(Long id, String rotationReason, String operator);

    /**
     * 更新密钥状态
     */
    void updateKeyStatus(Long id, Integer status, String operator);

    /**
     * 销毁密钥
     */
    void destroyKey(Long id, String operator);

    /**
     * 导出密钥信息
     */
    void exportKeyInfo(List<Long> keyIds, HttpServletResponse response);

    /**
     * 获取支持的密钥类型
     */
    List<String> getSupportedKeyTypes();

    /**
     * 获取密钥使用统计
     */
    KeyUsageStatisticsDTO getKeyUsageStatistics(Long keyId, String startTime, String endTime);

    /**
     * 获取密钥统计信息
     */
    KeyStatisticsDTO getKeyStatistics();
}
