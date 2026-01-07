package com.bankshield.api.controller;

import com.bankshield.api.entity.AlertRecord;
import com.bankshield.api.mapper.AlertRecordMapper;
import com.bankshield.api.service.NotificationService;
import com.bankshield.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlertRecordControllerTest {

    @Mock
    private AlertRecordMapper alertRecordMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlertRecordController alertRecordController;

    private AlertRecord testRecord;

    @BeforeEach
    public void setUp() {
        testRecord = new AlertRecord();
        testRecord.setId(1L);
        testRecord.setRuleId(1L);
        testRecord.setRuleName("CPU使用率告警");
        testRecord.setAlertLevel("HIGH");
        testRecord.setAlertMessage("CPU使用率超过阈值");
        testRecord.setAlertStatus("PENDING");
        testRecord.setAlertTime(LocalDateTime.now());
    }

    @Test
    public void testGetAlertRecord_Success() {
        when(alertRecordMapper.selectById(1L)).thenReturn(testRecord);

        Result<AlertRecord> result = alertRecordController.getAlertRecord(1L);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("CPU使用率告警", result.getData().getRuleName());
    }

    @Test
    public void testGetAlertRecord_NotFound() {
        when(alertRecordMapper.selectById(999L)).thenReturn(null);

        Result<AlertRecord> result = alertRecordController.getAlertRecord(999L);

        assertFalse(result.isSuccess());
    }

    @Test
    public void testAcknowledgeAlert_Success() {
        when(alertRecordMapper.selectById(1L)).thenReturn(testRecord);
        when(alertRecordMapper.updateById(any(AlertRecord.class))).thenReturn(1);

        Result<String> result = alertRecordController.acknowledgeAlert(1L, "已处理");

        assertTrue(result.isSuccess());
        verify(alertRecordMapper, times(1)).updateById(any(AlertRecord.class));
    }

    @Test
    public void testBatchAcknowledgeAlerts_Success() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(alertRecordMapper.batchUpdateStatus(anyList(), anyString(), anyString())).thenReturn(3);

        Result<String> result = alertRecordController.batchAcknowledgeAlerts(ids, "批量处理");

        assertTrue(result.isSuccess());
        verify(alertRecordMapper, times(1)).batchUpdateStatus(anyList(), anyString(), anyString());
    }

    @Test
    public void testGetPendingAlertsCount_Success() {
        when(alertRecordMapper.countByStatus("PENDING")).thenReturn(5);

        Result<Integer> result = alertRecordController.getPendingAlertsCount();

        assertTrue(result.isSuccess());
        assertEquals(5, result.getData());
    }
}
