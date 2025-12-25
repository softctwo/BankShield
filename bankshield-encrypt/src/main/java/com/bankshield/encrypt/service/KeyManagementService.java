package com.bankshield.encrypt.service;

import com.bankshield.common.result.PageResult;
import com.bankshield.common.result.Result;
import com.bankshield.encrypt.entity.EncryptionKey;
import com.bankshield.encrypt.enums.KeyStatus;
import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 密钥管理服务接口
 */
public interface KeyManagementService {
    
    /**
     * 生成新密钥
     * 
     * @param keyName 密钥名称
     * @param keyType 密钥类型
     * @param keyUsage 密钥用途
     * @param keyLength 密钥长度
     * @param expireDays 过期天数
     * @param rotationCycle 轮换周期（天）
     * @param description 描述
     * @param createdBy 创建人
     * @return 生成的密钥
     */
    Result<EncryptionKey> generateKey(String keyName, KeyType keyType, KeyUsage keyUsage, 
                                     Integer keyLength, Integer expireDays, Integer rotationCycle,
                                     String description, String createdBy);
    
    /**
     * 分页查询密钥
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyName 密钥名称（模糊查询）
     * @param keyType 密钥类型
     * @param keyStatus 密钥状态
     * @param keyUsage 密钥用途
     * @return 密钥分页结果
     */
    PageResult<EncryptionKey> getKeyList(Integer pageNum, Integer pageSize, String keyName,
                                        String keyType, String keyStatus, String keyUsage);
    
    /**
     * 获取密钥详情
     * 
     * @param keyId 密钥ID
     * @return 密钥详情
     */
    Result<EncryptionKey> getKeyDetail(Long keyId);
    
    /**
     * 更新密钥状态
     * 
     * @param keyId 密钥ID
     * @param status 新状态
     * @param operator 操作员
     * @return 更新结果
     */
    Result<Void> updateKeyStatus(Long keyId, KeyStatus status, String operator);
    
    /**
     * 手动轮换密钥
     * 
     * @param keyId 密钥ID
     * @param rotationReason 轮换原因
     * @param operator 操作员
     * @return 新密钥
     */
    Result<EncryptionKey> rotateKey(Long keyId, String rotationReason, String operator);
    
    /**
     * 销毁密钥
     * 
     * @param keyId 密钥ID
     * @param operator 操作员
     * @return 销毁结果
     */
    Result<Void> destroyKey(Long keyId, String operator);
    
    /**
     * 获取密钥统计信息
     * 
     * @return 统计信息
     */
    Result<Map<String, Object>> getKeyStatistics();
    
    /**
     * 获取支持的密钥类型
     * 
     * @return 密钥类型列表
     */
    Result<List<Map<String, String>>> getSupportedKeyTypes();
    
    /**
     * 获取密钥使用统计
     * 
     * @param keyId 密钥ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用统计
     */
    Result<Map<String, Object>> getKeyUsageStatistics(Long keyId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 导出密钥信息
     * 
     * @param keyIds 密钥ID列表（为null时导出所有）
     * @return 导出文件路径
     */
    Result<String> exportKeyInfo(List<Long> keyIds);
}