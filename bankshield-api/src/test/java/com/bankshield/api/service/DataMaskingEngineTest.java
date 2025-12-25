package com.bankshield.api.service;

import com.bankshield.api.dto.MaskingAlgorithmParams;
import com.bankshield.api.entity.DataMaskingRule;
import com.bankshield.api.enums.MaskingAlgorithm;
import com.bankshield.api.enums.MaskingScenario;
import com.bankshield.api.enums.SensitiveDataType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 数据脱敏引擎测试类
 */
@ExtendWith(MockitoExtension.class)
class DataMaskingEngineTest {

    @Mock
    private DataMaskingRuleService maskingRuleService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataMaskingEngine maskingEngine;

    private DataMaskingRule phoneRule;
    private DataMaskingRule idCardRule;

    @BeforeEach
    void setUp() {
        // 初始化手机号脱敏规则
        phoneRule = DataMaskingRule.builder()
                .id(1L)
                .ruleName("手机号脱敏")
                .sensitiveDataType(SensitiveDataType.PHONE.getCode())
                .maskingAlgorithm(MaskingAlgorithm.PARTIAL_MASK.getCode())
                .algorithmParams("{\"keepPrefix\": 3, \"keepSuffix\": 4, \"maskChar\": \"*\"}")
                .enabled(true)
                .build();

        // 初始化身份证号脱敏规则
        idCardRule = DataMaskingRule.builder()
                .id(2L)
                .ruleName("身份证号脱敏")
                .sensitiveDataType(SensitiveDataType.ID_CARD.getCode())
                .maskingAlgorithm(MaskingAlgorithm.PARTIAL_MASK.getCode())
                .algorithmParams("{\"keepPrefix\": 6, \"keepSuffix\": 4, \"maskChar\": \"*\"}")
                .enabled(true)
                .build();
    }

    @Test
    void testMaskData_WithPhoneRule() throws Exception {
        // 准备数据
        String originalData = "13812345678";
        String expectedMaskedData = "138****5678";

        // 模拟ObjectMapper行为
        MaskingAlgorithmParams params = MaskingAlgorithmParams.builder()
                .keepPrefix(3)
                .keepSuffix(4)
                .maskChar("*")
                .build();
        when(objectMapper.readValue(phoneRule.getAlgorithmParams(), MaskingAlgorithmParams.class))
                .thenReturn(params);

        // 执行测试
        String result = maskingEngine.maskData(originalData, phoneRule);

        // 验证结果
        assertEquals(expectedMaskedData, result);
    }

    @Test
    void testMaskData_WithIdCardRule() throws Exception {
        // 准备数据
        String originalData = "110101199003074567";
        String expectedMaskedData = "110101********4567";

        // 模拟ObjectMapper行为
        MaskingAlgorithmParams params = MaskingAlgorithmParams.builder()
                .keepPrefix(6)
                .keepSuffix(4)
                .maskChar("*")
                .build();
        when(objectMapper.readValue(idCardRule.getAlgorithmParams(), MaskingAlgorithmParams.class))
                .thenReturn(params);

        // 执行测试
        String result = maskingEngine.maskData(originalData, idCardRule);

        // 验证结果
        assertEquals(expectedMaskedData, result);
    }

    @Test
    void testMaskData_WithNullData() {
        // 执行测试
        String result = maskingEngine.maskData(null, phoneRule);

        // 验证结果
        assertNull(result);
    }

    @Test
    void testMaskData_WithEmptyData() throws Exception {
        // 准备数据
        String originalData = "";

        // 模拟ObjectMapper行为
        MaskingAlgorithmParams params = MaskingAlgorithmParams.builder()
                .keepPrefix(3)
                .keepSuffix(4)
                .maskChar("*")
                .build();
        when(objectMapper.readValue(phoneRule.getAlgorithmParams(), MaskingAlgorithmParams.class))
                .thenReturn(params);

        // 执行测试
        String result = maskingEngine.maskData(originalData, phoneRule);

        // 验证结果
        assertEquals("", result);
    }

    @Test
    void testMaskData_WithDisabledRule() {
        // 准备数据
        String originalData = "13812345678";
        phoneRule.setEnabled(false);

        // 执行测试
        String result = maskingEngine.maskData(originalData, phoneRule);

        // 验证结果
        assertEquals(originalData, result);
    }

    @Test
    void testMaskByType_WithExistingRule() {
        // 准备数据
        String originalData = "13812345678";
        String expectedMaskedData = "138****5678";

        // 模拟Service行为
        when(maskingRuleService.getRulesBySensitiveType(
                SensitiveDataType.PHONE.getCode(),
                MaskingScenario.DISPLAY.getCode()))
                .thenReturn(Arrays.asList(phoneRule));

        // 执行测试
        String result = maskingEngine.maskByType(originalData, SensitiveDataType.PHONE.getCode(), MaskingScenario.DISPLAY.getCode());

        // 验证结果
        assertEquals(expectedMaskedData, result);
    }

    @Test
    void testMaskByType_WithNoRule() {
        // 准备数据
        String originalData = "13812345678";

        // 模拟Service行为：没有找到规则
        when(maskingRuleService.getRulesBySensitiveType(anyString(), anyString()))
                .thenReturn(Arrays.asList());

        // 执行测试
        String result = maskingEngine.maskByType(originalData, SensitiveDataType.PHONE.getCode(), MaskingScenario.DISPLAY.getCode());

        // 验证结果：应该使用默认的脱敏规则
        assertEquals("138****5678", result);
    }

    @Test
    void testMaskByType_WithNullData() {
        // 执行测试
        String result = maskingEngine.maskByType(null, SensitiveDataType.PHONE.getCode(), MaskingScenario.DISPLAY.getCode());

        // 验证结果
        assertNull(result);
    }

    @Test
    void testMaskDataList() throws Exception {
        // 准备数据
        List<String> originalDataList = Arrays.asList("13812345678", "13987654321", "13711111111");
        List<String> expectedMaskedDataList = Arrays.asList("138****5678", "139****4321", "137****1111");

        // 模拟ObjectMapper行为
        MaskingAlgorithmParams params = MaskingAlgorithmParams.builder()
                .keepPrefix(3)
                .keepSuffix(4)
                .maskChar("*")
                .build();
        when(objectMapper.readValue(phoneRule.getAlgorithmParams(), MaskingAlgorithmParams.class))
                .thenReturn(params);

        // 执行测试
        List<String> result = maskingEngine.maskDataList(originalDataList, phoneRule);

        // 验证结果
        assertEquals(expectedMaskedDataList, result);
    }

    @Test
    void testMaskMapField() {
        // 准备数据
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", "张三");
        dataMap.put("phone", "13812345678");
        dataMap.put("email", "zhangsan@example.com");

        // 执行测试
        Map<String, Object> result = maskingEngine.maskMapField(
                dataMap, "phone", SensitiveDataType.PHONE.getCode(), MaskingScenario.DISPLAY.getCode());

        // 验证结果
        assertEquals("138****5678", result.get("phone"));
        assertEquals("张三", result.get("name")); // 未脱敏字段保持不变
        assertEquals("zhangsan@example.com", result.get("email")); // 未脱敏字段保持不变
    }
}