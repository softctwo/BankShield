package com.bankshield.api.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import com.bankshield.api.service.LifecycleManagementService;
import com.bankshield.api.util.SM3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 生命周期管理服务实现
 */
@Slf4j
@Service
public class LifecycleManagementServiceImpl implements LifecycleManagementService {
    
    @Autowired
    private DataAssetMapper dataAssetMapper;
    
    @Autowired
    private ArchivedDataMapper archivedDataMapper;
    
    @Autowired
    private DestructionRecordMapper destructionRecordMapper;
    
    @Autowired
    private LifecycleExecutionMapper executionMapper;
    
    @Autowired
    private LifecyclePolicyMapper policyMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveAsset(Long assetId, Long policyId, String operator) {
        LifecycleExecution execution = new LifecycleExecution();
        execution.setPolicyId(policyId);
        execution.setAssetId(assetId);
        execution.setExecutionType("ARCHIVE");
        execution.setExecutionStatus("RUNNING");
        execution.setExecutor(operator);
        execution.setExecutionMode("MANUAL");
        execution.setStartTime(LocalDateTime.now());
        
        try {
            DataAsset asset = dataAssetMapper.selectById(assetId);
            if (asset == null) {
                throw new RuntimeException("数据资产不存在");
            }
            
            execution.setAssetName(asset.getAssetName());
            execution.setAssetType(asset.getAssetType());
            
            ArchivedData existingArchive = archivedDataMapper.selectByOriginal("data_asset", assetId);
            if (existingArchive != null) {
                throw new RuntimeException("该资产已归档");
            }
            
            LifecyclePolicy policy = policyMapper.selectById(policyId);
            if (policy == null || !policy.getArchiveEnabled().equals(1)) {
                throw new RuntimeException("归档策略不可用");
            }
            
            ArchivedData archivedData = new ArchivedData();
            archivedData.setOriginalTable("data_asset");
            archivedData.setOriginalId(assetId);
            archivedData.setDataContent(JSON.toJSONString(asset));
            archivedData.setArchiveStorage(policy.getArchiveStorage());
            archivedData.setPolicyId(policyId);
            archivedData.setAssetType(asset.getAssetType());
            archivedData.setSensitivityLevel(asset.getSensitivityLevel());
            archivedData.setRetentionUntil(LocalDateTime.now().plusDays(policy.getRetentionDays()));
            archivedData.setIsDestroyed(0);
            archivedData.setCreateBy(operator);
            
            archivedDataMapper.insert(archivedData);
            
            execution.setExecutionStatus("SUCCESS");
            execution.setEndTime(LocalDateTime.now());
            execution.setDuration(System.currentTimeMillis());
            execution.setAffectedCount(1);
            executionMapper.insert(execution);
            
            log.info("归档数据资产成功: assetId={}, archiveId={}", assetId, archivedData.getId());
            return true;
            
        } catch (Exception e) {
            execution.setExecutionStatus("FAILED");
            execution.setEndTime(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());
            executionMapper.insert(execution);
            
            log.error("归档数据资产失败: assetId={}", assetId, e);
            throw new RuntimeException("归档失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchArchiveAssets(List<Long> assetIds, Long policyId, String operator) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long assetId : assetIds) {
            try {
                archiveAsset(assetId, policyId, operator);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("资产" + assetId + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", assetIds.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        result.put("errors", errors);
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> autoArchive(Long policyId) {
        LifecyclePolicy policy = policyMapper.selectById(policyId);
        if (policy == null || !policy.getArchiveEnabled().equals(1)) {
            throw new RuntimeException("归档策略不可用");
        }
        
        LambdaQueryWrapper<DataAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataAsset::getSensitivityLevel, policy.getSensitivityLevel());
        wrapper.le(DataAsset::getCreateTime, LocalDateTime.now().minusDays(policy.getArchiveDays()));
        
        List<DataAsset> assets = dataAssetMapper.selectList(wrapper);
        
        List<Long> assetIds = new ArrayList<>();
        for (DataAsset asset : assets) {
            ArchivedData existing = archivedDataMapper.selectByOriginal("data_asset", asset.getId());
            if (existing == null) {
                assetIds.add(asset.getId());
            }
        }
        
        return batchArchiveAssets(assetIds, policyId, "system");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean destroyArchivedData(Long archiveId, String reason, String operator) {
        LifecycleExecution execution = new LifecycleExecution();
        execution.setExecutionType("DESTROY");
        execution.setExecutionStatus("RUNNING");
        execution.setExecutor(operator);
        execution.setExecutionMode("MANUAL");
        execution.setStartTime(LocalDateTime.now());
        
        try {
            ArchivedData archivedData = archivedDataMapper.selectById(archiveId);
            if (archivedData == null) {
                throw new RuntimeException("归档数据不存在");
            }
            
            if (archivedData.getIsDestroyed().equals(1)) {
                throw new RuntimeException("该数据已销毁");
            }
            
            execution.setPolicyId(archivedData.getPolicyId());
            execution.setAssetId(archivedData.getOriginalId());
            execution.setAssetName(archivedData.getOriginalTable());
            execution.setAssetType(archivedData.getAssetType());
            
            LifecyclePolicy policy = policyMapper.selectById(archivedData.getPolicyId());
            
            DestructionRecord record = new DestructionRecord();
            record.setAssetId(archivedData.getOriginalId());
            record.setAssetName(archivedData.getOriginalTable());
            record.setAssetType(archivedData.getAssetType());
            record.setDataType("ARCHIVED");
            record.setSensitivityLevel(archivedData.getSensitivityLevel());
            record.setDestructionMethod(policy != null ? policy.getDestroyMethod() : "LOGICAL");
            record.setDestructionReason(reason);
            record.setDataSnapshot(archivedData.getDataContent());
            record.setPolicyId(archivedData.getPolicyId());
            record.setApprovalStatus(policy != null && policy.getApprovalRequired().equals(1) ? "PENDING" : "APPROVED");
            record.setExecutor(operator);
            record.setVerificationHash(SM3Util.hash(archivedData.getDataContent()));
            record.setIsVerified(1);
            
            destructionRecordMapper.insert(record);
            
            if (record.getApprovalStatus().equals("APPROVED")) {
                archivedData.setIsDestroyed(1);
                archivedData.setDestroyTime(LocalDateTime.now());
                archivedDataMapper.updateById(archivedData);
            }
            
            execution.setExecutionStatus("SUCCESS");
            execution.setEndTime(LocalDateTime.now());
            execution.setDuration(System.currentTimeMillis());
            execution.setAffectedCount(1);
            executionMapper.insert(execution);
            
            log.info("销毁归档数据成功: archiveId={}, recordId={}", archiveId, record.getId());
            return true;
            
        } catch (Exception e) {
            execution.setExecutionStatus("FAILED");
            execution.setEndTime(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());
            executionMapper.insert(execution);
            
            log.error("销毁归档数据失败: archiveId={}", archiveId, e);
            throw new RuntimeException("销毁失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchDestroyArchivedData(List<Long> archiveIds, String reason, String operator) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long archiveId : archiveIds) {
            try {
                destroyArchivedData(archiveId, reason, operator);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("归档" + archiveId + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", archiveIds.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        result.put("errors", errors);
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> autoDestroy(Long policyId) {
        LifecyclePolicy policy = policyMapper.selectById(policyId);
        if (policy == null || !policy.getDestroyEnabled().equals(1)) {
            throw new RuntimeException("销毁策略不可用");
        }
        
        List<ArchivedData> pendingList = archivedDataMapper.selectPendingDestruction(LocalDateTime.now());
        
        List<Long> archiveIds = new ArrayList<>();
        for (ArchivedData data : pendingList) {
            if (data.getPolicyId().equals(policyId)) {
                archiveIds.add(data.getId());
            }
        }
        
        return batchDestroyArchivedData(archiveIds, "自动销毁（保留期满）", "system");
    }
    
    @Override
    public List<Map<String, Object>> getPendingArchiveAssets(Long policyId) {
        LifecyclePolicy policy = policyMapper.selectById(policyId);
        if (policy == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<DataAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataAsset::getSensitivityLevel, policy.getSensitivityLevel());
        wrapper.le(DataAsset::getCreateTime, LocalDateTime.now().minusDays(policy.getArchiveDays()));
        
        List<DataAsset> assets = dataAssetMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DataAsset asset : assets) {
            ArchivedData existing = archivedDataMapper.selectByOriginal("data_asset", asset.getId());
            if (existing == null) {
                Map<String, Object> item = new HashMap<>();
                item.put("assetId", asset.getId());
                item.put("assetName", asset.getAssetName());
                item.put("assetType", asset.getAssetType());
                item.put("sensitivityLevel", asset.getSensitivityLevel());
                item.put("createTime", asset.getCreateTime());
                item.put("daysOld", java.time.temporal.ChronoUnit.DAYS.between(asset.getCreateTime(), LocalDateTime.now()));
                result.add(item);
            }
        }
        
        return result;
    }
    
    @Override
    public List<ArchivedData> getPendingDestructionData() {
        return archivedDataMapper.selectPendingDestruction(LocalDateTime.now());
    }
    
    @Override
    public List<LifecycleExecution> getExecutionRecords(Long policyId, Integer limit) {
        if (policyId != null) {
            return executionMapper.selectByPolicyId(policyId);
        } else {
            return executionMapper.selectRecent(limit != null ? limit : 100);
        }
    }
    
    @Override
    public ArchivedData getArchivedDataById(Long id) {
        return archivedDataMapper.selectById(id);
    }
    
    @Override
    public List<DestructionRecord> getDestructionRecords(Long assetId) {
        if (assetId != null) {
            return destructionRecordMapper.selectByAssetId(assetId);
        } else {
            return destructionRecordMapper.selectList(null);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveDestruction(Long recordId, String approver, String comment, boolean approved) {
        try {
            DestructionRecord record = destructionRecordMapper.selectById(recordId);
            if (record == null) {
                throw new RuntimeException("销毁记录不存在");
            }
            
            record.setApprovalStatus(approved ? "APPROVED" : "REJECTED");
            record.setApprover(approver);
            record.setApprovalTime(LocalDateTime.now());
            record.setApprovalComment(comment);
            
            destructionRecordMapper.updateById(record);
            
            if (approved) {
                ArchivedData archivedData = archivedDataMapper.selectByOriginal(
                    record.getAssetName(), record.getAssetId()
                );
                if (archivedData != null) {
                    archivedData.setIsDestroyed(1);
                    archivedData.setDestroyTime(LocalDateTime.now());
                    archivedDataMapper.updateById(archivedData);
                }
            }
            
            log.info("审批销毁申请: recordId={}, approved={}", recordId, approved);
            return true;
            
        } catch (Exception e) {
            log.error("审批销毁申请失败", e);
            throw new RuntimeException("审批失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalPolicies", policyMapper.selectCount(null));
        stats.put("activePolicies", policyMapper.selectActivePolicies().size());
        stats.put("totalArchived", archivedDataMapper.countActive());
        stats.put("totalDestroyed", archivedDataMapper.countDestroyed());
        stats.put("pendingDestruction", archivedDataMapper.selectPendingDestruction(LocalDateTime.now()).size());
        stats.put("pendingApproval", destructionRecordMapper.selectPendingApproval().size());
        stats.put("todayDestructions", destructionRecordMapper.countToday());
        
        List<Map<String, Object>> executionStats = executionMapper.countByStatus();
        stats.put("executionStats", executionStats);
        
        return stats;
    }
    
    @Override
    public boolean verifyArchivedData(Long archiveId) {
        try {
            ArchivedData archivedData = archivedDataMapper.selectById(archiveId);
            if (archivedData == null) {
                return false;
            }
            
            String currentHash = SM3Util.hash(archivedData.getDataContent());
            
            List<DestructionRecord> records = destructionRecordMapper.selectByAssetId(archivedData.getOriginalId());
            if (!records.isEmpty()) {
                DestructionRecord record = records.get(0);
                return currentHash.equals(record.getVerificationHash());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("验证归档数据失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreArchivedData(Long archiveId, String operator) {
        try {
            ArchivedData archivedData = archivedDataMapper.selectById(archiveId);
            if (archivedData == null) {
                throw new RuntimeException("归档数据不存在");
            }
            
            if (archivedData.getIsDestroyed().equals(1)) {
                throw new RuntimeException("该数据已销毁，无法恢复");
            }
            
            DataAsset asset = JSON.parseObject(archivedData.getDataContent(), DataAsset.class);
            
            DataAsset existing = dataAssetMapper.selectById(asset.getId());
            if (existing != null) {
                throw new RuntimeException("原始数据仍存在，无需恢复");
            }
            
            dataAssetMapper.insert(asset);
            
            archivedDataMapper.deleteById(archiveId);
            
            log.info("恢复归档数据成功: archiveId={}, assetId={}", archiveId, asset.getId());
            return true;
            
        } catch (Exception e) {
            log.error("恢复归档数据失败", e);
            throw new RuntimeException("恢复失败: " + e.getMessage());
        }
    }
}
