package com.bankshield.api.service;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.ClassificationHistory;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 数据分类分级服务接口
 * 
 * @author BankShield
 */
public interface ClassificationService {

    /**
     * 人工标注数据资产等级
     * 
     * @param assetId 资产ID
     * @param manualLevel 人工标注等级
     * @param operatorId 操作人ID
     * @return 操作结果
     */
    Result<String> manualClassify(Long assetId, Integer manualLevel, Long operatorId);

    /**
     * 提交审核
     * 
     * @param assetId 资产ID
     * @param finalLevel 最终等级
     * @param reason 变更原因
     * @return 操作结果
     */
    Result<String> submitReview(Long assetId, Integer finalLevel, String reason);

    /**
     * 审核分级结果
     * 
     * @param assetId 资产ID
     * @param approved 是否通过
     * @param comment 审核意见
     * @param reviewerId 审核人ID
     * @return 操作结果
     */
    Result<String> reviewClassification(Long assetId, Boolean approved, String comment, Long reviewerId);

    /**
     * 获取待审核的资产列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 待审核资产分页数据
     */
    Result<Page<DataAsset>> getPendingReviewAssets(int page, int size);

    /**
     * 获取资产的分类历史
     * 
     * @param assetId 资产ID
     * @return 分类历史列表
     */
    Result<ClassificationHistory> getClassificationHistory(Long assetId);

    /**
     * 批量审核通过
     * 
     * @param assetIds 资产ID列表
     * @param reviewerId 审核人ID
     * @return 操作结果
     */
    Result<String> batchApproveAssets(Long[] assetIds, Long reviewerId);

    /**
     * 批量审核拒绝
     * 
     * @param assetIds 资产ID列表
     * @param comment 审核意见
     * @param reviewerId 审核人ID
     * @return 操作结果
     */
    Result<String> batchRejectAssets(Long[] assetIds, String comment, Long reviewerId);
}