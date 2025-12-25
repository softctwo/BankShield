package com.bankshield.api.service.impl;

import com.bankshield.api.entity.SecurityScanResult;
import com.bankshield.api.mapper.SecurityScanResultMapper;
import com.bankshield.api.service.SecurityScanResultService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全扫描结果服务实现类
 * @author BankShield
 */
@Slf4j
@Service
public class SecurityScanResultServiceImpl extends ServiceImpl<SecurityScanResultMapper, SecurityScanResult> 
        implements SecurityScanResultService {

    @Autowired
    private SecurityScanResultMapper scanResultMapper;

    @Override
    public IPage<SecurityScanResult> getScanResults(int page, int size, Long taskId, String riskLevel, String fixStatus) {
        IPage<SecurityScanResult> pageParam = new Page<>(page, size);
        QueryWrapper<SecurityScanResult> wrapper = new QueryWrapper<>();
        
        if (taskId != null) {
            wrapper.eq("task_id", taskId);
        }
        
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            wrapper.eq("risk_level", riskLevel);
        }
        
        if (fixStatus != null && !fixStatus.trim().isEmpty()) {
            wrapper.eq("fix_status", fixStatus);
        }
        
        // 按风险级别降序排列，按发现时间降序排列
        wrapper.orderByDesc("case risk_level " +
            "when 'CRITICAL' then 5 " +
            "when 'HIGH' then 4 " +
            "when 'MEDIUM' then 3 " +
            "when 'LOW' then 2 " +
            "when 'INFO' then 1 " +
            "else 0 end");
        wrapper.orderByDesc("discovered_time");
        
        return scanResultMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional
    public boolean markAsFixed(Long resultId, String fixRemark) {
        try {
            SecurityScanResult result = scanResultMapper.selectById(resultId);
            if (result == null) {
                log.error("扫描结果不存在: {}", resultId);
                return false;
            }
            
            result.setFixStatus("RESOLVED");
            result.setFixTime(LocalDateTime.now());
            result.setFixBy(getCurrentUsername());
            result.setUpdateTime(LocalDateTime.now());
            
            int updateCount = scanResultMapper.updateById(result);
            log.info("标记风险为已修复成功: {}, 修复说明: {}", resultId, fixRemark);
            
            return updateCount > 0;
        } catch (Exception e) {
            log.error("标记风险为已修复失败: " + resultId, e);
            throw new RuntimeException("标记风险为已修复失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean batchMarkAsFixed(List<Long> resultIds, String fixRemark) {
        if (resultIds == null || resultIds.isEmpty()) {
            return true;
        }
        
        try {
            String currentUsername = getCurrentUsername();
            LocalDateTime now = LocalDateTime.now();
            
            for (Long resultId : resultIds) {
                SecurityScanResult result = scanResultMapper.selectById(resultId);
                if (result != null) {
                    result.setFixStatus("RESOLVED");
                    result.setFixTime(now);
                    result.setFixBy(currentUsername);
                    result.setUpdateTime(now);
                    
                    scanResultMapper.updateById(result);
                }
            }
            
            log.info("批量标记风险为已修复成功，数量: {}, 修复说明: {}", resultIds.size(), fixRemark);
            return true;
        } catch (Exception e) {
            log.error("批量标记风险为已修复失败", e);
            throw new RuntimeException("批量标记风险为已修复失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SecurityScanResult> getResultsByTaskId(Long taskId) {
        QueryWrapper<SecurityScanResult> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        wrapper.orderByDesc("risk_level", "discovered_time");
        return scanResultMapper.selectList(wrapper);
    }

    @Override
    public long countByTaskIdAndRiskLevel(Long taskId, String riskLevel) {
        QueryWrapper<SecurityScanResult> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        wrapper.eq("risk_level", riskLevel);
        return scanResultMapper.selectCount(wrapper);
    }

    @Override
    public long countByTaskIdAndFixStatus(Long taskId, String fixStatus) {
        QueryWrapper<SecurityScanResult> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        wrapper.eq("fix_status", fixStatus);
        return scanResultMapper.selectCount(wrapper);
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        // 从SecurityContext获取当前用户
        try {
            return org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}