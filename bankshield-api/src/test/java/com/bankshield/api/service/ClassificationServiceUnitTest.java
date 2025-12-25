package com.bankshield.api.service;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.ClassificationHistory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 数据分类服务单元测试
 *
 * @author BankShield
 */
@ExtendWith(MockitoExtension.class)
public class ClassificationServiceTest {

    @Mock
    private com.bankshield.api.mapper.DataAssetMapper dataAssetMapper;

    @Mock
    private com.bankshield.api.mapper.ClassificationHistoryMapper classificationHistoryMapper;

    @InjectMocks
    private ClassificationServiceImpl classificationService;

    @Test
    void testManualClassify_Success() {
        // Arrange
        Long assetId = 1L;
        Integer manualLevel = 3;
        Long operatorId = 1L;

        DataAsset mockAsset = new DataAsset();
        mockAsset.setId(assetId);
        mockAsset.setAssetName("测试资产");
        when(dataAssetMapper.selectById(assetId)).thenReturn(mockAsset);

        // Act
        ClassificationService.ClassificationRequest request = new ClassificationService.ClassificationRequest();
        request.setAssetId(assetId);
        request.setManualLevel(manualLevel);
        request.setOperatorId(operatorId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(dataAssetMapper, times(1)).selectById(assetId);
        verify(dataAssetMapper, times(1)).updateById(any(DataAsset.class));
    }

    @Test
    void testManualClassify_AssetNotFound() {
        // Arrange
        Long assetId = 99999L;
        when(dataAssetMapper.selectById(assetId)).thenReturn(null);

        // Act
        ClassificationService.ClassificationRequest request = new ClassificationService.ClassificationRequest();
        request.setAssetId(assetId);
        request.setManualLevel(3);
        request.setOperatorId(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("资产不存在", result.getMessage());
        verify(dataAssetMapper, times(1)).selectById(assetId);
        verify(dataAssetMapper, never()).updateById(any());
    }

    @Test
    void testGetClassificationHistory_Success() {
        // Arrange
        Long assetId = 1L;

        ClassificationHistory mockHistory = new ClassificationHistory();
        mockHistory.setId(1L);
        mockHistory.setAssetId(assetId);
        when(classificationHistoryMapper.selectById(assetId)).thenReturn(mockHistory);

        // Act
        Result<ClassificationHistory> result = classificationService.getClassificationHistory(assetId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mockHistory, result.getData());
    }

    @Test
    void testReviewClassification_Approve() {
        // Arrange
        Long historyId = 1L;
        Boolean approved = true;
        String comment = "审核通过";
        Long reviewerId = 2L;

        ClassificationHistory mockHistory = new ClassificationHistory();
        mockHistory.setId(historyId);
        mockHistory.setStatus(0); // 待审核状态
        when(classificationHistoryMapper.selectById(historyId)).thenReturn(mockHistory);

        // Act
        Result<String> result = classificationService.reviewClassification(historyId, approved, comment, reviewerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(classificationHistoryMapper, times(1)).updateById(any(ClassificationHistory.class));
    }

    @Test
    void testReviewClassification_Reject() {
        // Arrange
        Long historyId = 1L;
        Boolean approved = false;
        String comment = "数据不符合规范";
        Long reviewerId = 2L;

        ClassificationHistory mockHistory = new ClassificationHistory();
        mockHistory.setId(historyId);
        mockHistory.setStatus(0);
        when(classificationHistoryMapper.selectById(historyId)).thenReturn(mockHistory);

        // Act
        Result<String> result = classificationService.reviewClassification(historyId, approved, comment, reviewerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(classificationHistoryMapper, times(1)).updateById(any(ClassificationHistory.class));
    }

    @Test
    void testGetPendingReviewAssets_Success() {
        // Arrange
        Page<ClassificationHistory> mockPage = new Page<>();
        mockPage.setRecords(Arrays.asList(new ClassificationHistory(), new ClassificationHistory()));
        mockPage.setTotal(2);

        when(classificationHistoryMapper.selectPendingReviewPage(any(Page.class))).thenReturn(mockPage);

        // Act
        Result<Page<DataAsset>> result = classificationService.getPendingReviewAssets(1, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getTotal());
    }

    @Test
    void testBatchApproveAssets_Success() {
        // Arrange
        Long[] assetIds = {1L, 2L, 3L};
        Long reviewerId = 2L;

        when(dataAssetMapper.selectBatchIds(any())).thenReturn(Arrays.asList(
                new DataAsset(), new DataAsset(), new DataAsset()));

        // Act
        Result<String> result = classificationService.batchApproveAssets(assetIds, reviewerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(dataAssetMapper, times(1)).selectBatchIds(assetIds);
        verify(dataAssetMapper, times(assetIds.length)).updateById(any(DataAsset.class));
    }

    @Test
    void testBatchRejectAssets_Success() {
        // Arrange
        Long[] assetIds = {1L, 2L, 3L};
        String comment = "批量审核拒绝";
        Long reviewerId = 2L;

        // Act
        Result<String> result = classificationService.batchRejectAssets(assetIds, comment, reviewerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(classificationHistoryMapper, times(1)).insertBatch(any());
    }
}
