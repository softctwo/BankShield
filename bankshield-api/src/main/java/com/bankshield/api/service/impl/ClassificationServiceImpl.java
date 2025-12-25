package com.bankshield.api.service.impl;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.ClassificationHistory;
import com.bankshield.api.mapper.DataAssetMapper;
import com.bankshield.api.mapper.ClassificationHistoryMapper;
import com.bankshield.api.service.ClassificationService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据分类分级服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class ClassificationServiceImpl extends ServiceImpl<DataAssetMapper, DataAsset> implements ClassificationService {

    @Autowired
    private DataAssetMapper dataAssetMapper;

    @Autowired
    private ClassificationHistoryMapper classificationHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> manualClassify(Long assetId, Integer manualLevel, Long operatorId) {
        try {
            // 参数校验
            if (assetId == null || manualLevel == null || operatorId == null) {
                return Result.error("参数不能为空");
            }
            
            // 检查资产是否存在
            DataAsset asset = this.getById(assetId);
            if (asset == null) {
                return Result.error("资产不存在");
            }
            
            // 检查等级有效性
            if (manualLevel < 1 || manualLevel > 4) {
                return Result.error("安全等级必须在1-4之间");
            }
            
            // 记录原始等级
            Integer oldLevel = asset.getFinalLevel();
            
            // 更新人工标注等级和最终等级
            asset.setManualLevel(manualLevel);
            asset.setFinalLevel(manualLevel);
            asset.setUpdateTime(LocalDateTime.now());
            
            // C3/C4级需要审核
            if (manualLevel >= 3) {
                asset.setStatus(0); // 待审核
            } else {
                asset.setStatus(1); // 直接生效
            }
            
            boolean result = this.updateById(asset);
            if (!result) {
                return Result.error("更新资产分级失败");
            }
            
            // 记录分类历史
            ClassificationHistory history = ClassificationHistory.builder()
                    .assetId(assetId)
                    .oldLevel(oldLevel)
                    .newLevel(manualLevel)
                    .changeReason("人工标注")
                    .operatorId(operatorId)
                    .operateTime(LocalDateTime.now())
                    .build();
            
            classificationHistoryMapper.insert(history);
            
            return Result.OK("人工标注成功");
        } catch (Exception e) {
            log.error("人工标注失败: {}", e.getMessage());
            return Result.error("人工标注失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> submitReview(Long assetId, Integer finalLevel, String reason) {
        try {
            // 参数校验
            if (assetId == null || finalLevel == null) {
                return Result.error("参数不能为空");
            }
            
            // 检查资产是否存在
            DataAsset asset = this.getById(assetId);
            if (asset == null) {
                return Result.error("资产不存在");
            }
            
            // 检查等级有效性
            if (finalLevel < 1 || finalLevel > 4) {
                return Result.error("安全等级必须在1-4之间");
            }
            
            // 状态变更为待审核
            asset.setFinalLevel(finalLevel);
            asset.setStatus(0); // 待审核
            asset.setClassificationBasis(reason);
            asset.setUpdateTime(LocalDateTime.now());
            
            boolean result = this.updateById(asset);
            if (!result) {
                return Result.error("提交审核失败");
            }
            
            return Result.OK("提交审核成功");
        } catch (Exception e) {
            log.error("提交审核失败: {}", e.getMessage());
            return Result.error("提交审核失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> reviewClassification(Long assetId, Boolean approved, String comment, Long reviewerId) {
        try {
            // 参数校验
            if (assetId == null || approved == null || reviewerId == null) {
                return Result.error("参数不能为空");
            }
            
            // 检查资产是否存在
            DataAsset asset = this.getById(assetId);
            if (asset == null) {
                return Result.error("资产不存在");
            }
            
            // 检查是否处于待审核状态
            if (asset.getStatus() != 0) {
                return Result.error("资产不处于待审核状态");
            }
            
            // 记录原始等级
            Integer oldLevel = asset.getSecurityLevel();
            Integer newLevel = asset.getFinalLevel();
            
            if (approved) {
                // 审核通过
                asset.setStatus(1); // 已生效
                asset.setSecurityLevel(newLevel); // 更新安全等级
                asset.setReviewerId(reviewerId);
                asset.setReviewTime(LocalDateTime.now());
                asset.setReviewComment(comment);
            } else {
                // 审核拒绝
                asset.setStatus(0); // 保持待修改状态
                asset.setFinalLevel(asset.getSecurityLevel()); // 恢复为原等级
                asset.setReviewerId(reviewerId);
                asset.setReviewTime(LocalDateTime.now());
                asset.setReviewComment(comment);
            }
            
            asset.setUpdateTime(LocalDateTime.now());
            
            boolean result = this.updateById(asset);
            if (!result) {
                return Result.error("审核操作失败");
            }
            
            // 记录分类历史
            if (approved && !oldLevel.equals(newLevel)) {
                ClassificationHistory history = ClassificationHistory.builder()
                        .assetId(assetId)
                        .oldLevel(oldLevel)
                        .newLevel(newLevel)
                        .changeReason("审核通过: " + (StringUtils.hasText(comment) ? comment : "无"))
                        .operatorId(reviewerId)
                        .operateTime(LocalDateTime.now())
                        .build();
                
                classificationHistoryMapper.insert(history);
            }
            
            return Result.OK(approved ? "审核通过成功" : "审核拒绝成功");
        } catch (Exception e) {
            log.error("审核分级结果失败: {}", e.getMessage());
            return Result.error("审核分级结果失败");
        }
    }

    @Override
    public Result<Page<DataAsset>> getPendingReviewAssets(int page, int size) {
        try {
            Page<DataAsset> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<DataAsset> queryWrapper = new LambdaQueryWrapper<>();
            
            // 查询待审核的资产
            queryWrapper.eq(DataAsset::getStatus, 0);
            queryWrapper.orderByDesc(DataAsset::getCreateTime);
            
            Page<DataAsset> assetPage = this.page(pageParam, queryWrapper);
            return Result.OK(assetPage);
        } catch (Exception e) {
            log.error("查询待审核资产失败: {}", e.getMessage());
            return Result.error("查询待审核资产失败");
        }
    }

    @Override
    public Result<ClassificationHistory> getClassificationHistory(Long assetId) {
        try {
            if (assetId == null) {
                return Result.error("资产ID不能为空");
            }
            
            LambdaQueryWrapper<ClassificationHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClassificationHistory::getAssetId, assetId);
            queryWrapper.orderByDesc(ClassificationHistory::getOperateTime);
            
            List<ClassificationHistory> historyList = classificationHistoryMapper.selectList(queryWrapper);
            
            if (historyList.isEmpty()) {
                return Result.error("未找到分类历史");
            }
            
            return Result.OK(historyList.get(0));
        } catch (Exception e) {
            log.error("查询分类历史失败: {}", e.getMessage());
            return Result.error("查询分类历史失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchApproveAssets(Long[] assetIds, Long reviewerId) {
        try {
            if (assetIds == null || assetIds.length == 0) {
                return Result.error("资产ID列表不能为空");
            }
            
            int successCount = 0;
            for (Long assetId : assetIds) {
                Result<String> result = reviewClassification(assetId, true, "批量审核通过", reviewerId);
                if (result.isSuccess()) {
                    successCount++;
                }
            }
            
            return Result.OK(String.format("批量审核通过完成，成功 %d/%d 个资产", successCount, assetIds.length));
        } catch (Exception e) {
            log.error("批量审核通过失败: {}", e.getMessage());
            return Result.error("批量审核通过失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchRejectAssets(Long[] assetIds, String comment, Long reviewerId) {
        try {
            if (assetIds == null || assetIds.length == 0) {
                return Result.error("资产ID列表不能为空");
            }
            
            int successCount = 0;
            for (Long assetId : assetIds) {
                Result<String> result = reviewClassification(assetId, false, comment, reviewerId);
                if (result.isSuccess()) {
                    successCount++;
                }
            }
            
            return Result.OK(String.format("批量审核拒绝完成，成功 %d/%d 个资产", successCount, assetIds.length));
        } catch (Exception e) {
            log.error("批量审核拒绝失败: {}", e.getMessage());
            return Result.error("批量审核拒绝失败");
        }
    }
}