package com.bankshield.api.service;

import com.bankshield.api.entity.ArchivedData;
import com.bankshield.api.entity.DestructionRecord;
import com.bankshield.api.entity.LifecycleExecution;

import java.util.List;
import java.util.Map;

/**
 * 生命周期管理服务接口
 */
public interface LifecycleManagementService {
    
    /**
     * 归档单个数据资产
     */
    boolean archiveAsset(Long assetId, Long policyId, String operator);
    
    /**
     * 批量归档数据资产
     */
    Map<String, Object> batchArchiveAssets(List<Long> assetIds, Long policyId, String operator);
    
    /**
     * 自动归档（根据策略）
     */
    Map<String, Object> autoArchive(Long policyId);
    
    /**
     * 销毁单个归档数据
     */
    boolean destroyArchivedData(Long archiveId, String reason, String operator);
    
    /**
     * 批量销毁归档数据
     */
    Map<String, Object> batchDestroyArchivedData(List<Long> archiveIds, String reason, String operator);
    
    /**
     * 自动销毁（根据策略）
     */
    Map<String, Object> autoDestroy(Long policyId);
    
    /**
     * 获取待归档的数据资产列表
     */
    List<Map<String, Object>> getPendingArchiveAssets(Long policyId);
    
    /**
     * 获取待销毁的归档数据列表
     */
    List<ArchivedData> getPendingDestructionData();
    
    /**
     * 获取执行记录
     */
    List<LifecycleExecution> getExecutionRecords(Long policyId, Integer limit);
    
    /**
     * 获取归档数据详情
     */
    ArchivedData getArchivedDataById(Long id);
    
    /**
     * 获取销毁记录
     */
    List<DestructionRecord> getDestructionRecords(Long assetId);
    
    /**
     * 审批销毁申请
     */
    boolean approveDestruction(Long recordId, String approver, String comment, boolean approved);
    
    /**
     * 获取生命周期统计信息
     */
    Map<String, Object> getStatistics();
    
    /**
     * 验证归档数据完整性
     */
    boolean verifyArchivedData(Long archiveId);
    
    /**
     * 恢复归档数据
     */
    boolean restoreArchivedData(Long archiveId, String operator);
}
