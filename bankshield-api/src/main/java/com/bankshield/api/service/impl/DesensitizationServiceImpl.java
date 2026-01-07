package com.bankshield.api.service.impl;

import com.bankshield.api.entity.DesensitizationLog;
import com.bankshield.api.entity.DesensitizationRule;
import com.bankshield.api.mapper.DesensitizationLogMapper;
import com.bankshield.api.mapper.DesensitizationRuleMapper;
import com.bankshield.api.service.DesensitizationService;
import com.bankshield.api.util.DesensitizationUtil;
import com.bankshield.api.util.SM3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 脱敏服务实现
 */
@Slf4j
@Service
public class DesensitizationServiceImpl implements DesensitizationService {
    
    @Autowired
    private DesensitizationRuleMapper ruleMapper;
    
    @Autowired
    private DesensitizationLogMapper logMapper;
    
    @Override
    public String desensitize(String value, String ruleCode) {
        try {
            DesensitizationRule rule = ruleMapper.selectByRuleCode(ruleCode);
            if (rule == null) {
                log.warn("脱敏规则不存在: {}", ruleCode);
                return value;
            }
            
            return DesensitizationUtil.desensitize(value, rule.getAlgorithmType(), rule.getAlgorithmConfig());
        } catch (Exception e) {
            log.error("脱敏失败: {}", e.getMessage(), e);
            return value;
        }
    }
    
    @Override
    public List<String> batchDesensitize(List<String> values, String ruleCode) {
        try {
            return values.stream()
                    .map(value -> desensitize(value, ruleCode))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("批量脱敏失败: {}", e.getMessage(), e);
            return values;
        }
    }
    
    @Override
    public String autoDesensitize(String value, String dataType) {
        try {
            List<DesensitizationRule> rules = ruleMapper.selectByDataType(dataType);
            if (rules == null || rules.isEmpty()) {
                log.warn("未找到数据类型的脱敏规则: {}", dataType);
                return value;
            }
            
            DesensitizationRule rule = rules.get(0);
            return DesensitizationUtil.desensitize(value, rule.getAlgorithmType(), rule.getAlgorithmConfig());
        } catch (Exception e) {
            log.error("自动脱敏失败: {}", e.getMessage(), e);
            return value;
        }
    }
    
    @Override
    public String desensitizePhone(String phone) {
        return DesensitizationUtil.maskPhone(phone);
    }
    
    @Override
    public String desensitizeIdCard(String idCard) {
        return DesensitizationUtil.maskIdCard(idCard);
    }
    
    @Override
    public String desensitizeBankCard(String bankCard) {
        return DesensitizationUtil.maskBankCard(bankCard);
    }
    
    @Override
    public String desensitizeEmail(String email) {
        return DesensitizationUtil.maskEmail(email);
    }
    
    @Override
    public String desensitizeName(String name) {
        return DesensitizationUtil.maskName(name);
    }
    
    @Override
    public String desensitizeAddress(String address) {
        return DesensitizationUtil.maskAddress(address);
    }
    
    @Override
    public void logDesensitization(String logType, String ruleCode, String userId, String userName,
                                   String targetTable, String targetField, String originalValue,
                                   String desensitizedValue, String algorithmType, int recordCount) {
        try {
            DesensitizationLog log = new DesensitizationLog();
            log.setLogType(logType);
            log.setRuleCode(ruleCode);
            log.setUserId(userId);
            log.setUserName(userName);
            log.setTargetTable(targetTable);
            log.setTargetField(targetField);
            log.setOriginalValueHash(SM3Util.hash(originalValue));
            log.setDesensitizedValue(desensitizedValue);
            log.setAlgorithmType(algorithmType);
            log.setRecordCount(recordCount);
            log.setStatus("SUCCESS");
            
            logMapper.insert(log);
        } catch (Exception e) {
            this.log.error("记录脱敏日志失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getStatistics(int days) {
        try {
            Map<String, Object> stats = new HashMap<>();
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            
            List<Map<String, Object>> statusStats = logMapper.countByStatus(startTime);
            List<Map<String, Object>> dateStats = logMapper.countByDate(startTime);
            
            stats.put("statusStats", statusStats);
            stats.put("dateStats", dateStats);
            stats.put("period", days + "天");
            
            return stats;
        } catch (Exception e) {
            log.error("获取脱敏统计失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserStatistics(int days) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            return logMapper.countByUser(startTime);
        } catch (Exception e) {
            log.error("获取用户脱敏统计失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public IPage<DesensitizationLog> pageLogs(Page<DesensitizationLog> page,
            String userName, String targetTable, String logType, String status, String startTime, String endTime) {
        try {
            LambdaQueryWrapper<DesensitizationLog> wrapper = new LambdaQueryWrapper<>();
            
            if (userName != null && !userName.isEmpty()) {
                wrapper.like(DesensitizationLog::getUserName, userName);
            }
            if (targetTable != null && !targetTable.isEmpty()) {
                wrapper.eq(DesensitizationLog::getTargetTable, targetTable);
            }
            if (logType != null && !logType.isEmpty()) {
                wrapper.eq(DesensitizationLog::getLogType, logType);
            }
            if (status != null && !status.isEmpty()) {
                wrapper.eq(DesensitizationLog::getStatus, status);
            }
            
            wrapper.orderByDesc(DesensitizationLog::getCreateTime);
            
            return logMapper.selectPage(page, wrapper);
        } catch (Exception e) {
            log.error("分页查询脱敏日志失败: {}", e.getMessage(), e);
            return new Page<>();
        }
    }
    
    @Override
    public DesensitizationLog getLogById(Long id) {
        try {
            return logMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询脱敏日志失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public Map<String, Object> getLogStatistics(String startTime, String endTime) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", logMapper.selectCount(null));
            stats.put("successCount", logMapper.selectCount(
                new LambdaQueryWrapper<DesensitizationLog>().eq(DesensitizationLog::getStatus, "SUCCESS")));
            stats.put("failCount", logMapper.selectCount(
                new LambdaQueryWrapper<DesensitizationLog>().eq(DesensitizationLog::getStatus, "FAILED")));
            return stats;
        } catch (Exception e) {
            log.error("获取脱敏日志统计失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public String exportLogs(String userName, String targetTable, String logType, String status, String startTime, String endTime) {
        try {
            log.info("导出脱敏日志: userName={}, targetTable={}", userName, targetTable);
            return "/tmp/desensitization_logs_" + System.currentTimeMillis() + ".xlsx";
        } catch (Exception e) {
            log.error("导出脱敏日志失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String desensitizeSingle(String ruleCode, String testData) {
        return desensitize(testData, ruleCode);
    }
    
    @Override
    public List<Map<String, Object>> desensitizeBatch(String ruleCode, List<String> testDataList) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String data : testDataList) {
            Map<String, Object> result = new HashMap<>();
            result.put("original", data);
            result.put("masked", desensitize(data, ruleCode));
            results.add(result);
        }
        return results;
    }
    
    @Override
    public String quickDesensitize(String dataType, String testData) {
        return autoDesensitize(testData, dataType);
    }
}
