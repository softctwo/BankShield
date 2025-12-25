package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.SecurityEvent;
import com.bankshield.api.entity.ThreatIntelligence;
import com.bankshield.api.service.SecurityPostureService;
import com.bankshield.api.service.ThreatIntelligenceService;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 安全态势感知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Api(tags = "安全态势感知")
public class SecurityPostureController {

    private final SecurityPostureService securityPostureService;
    private final ThreatIntelligenceService threatIntelligenceService;

    /**
     * 获取实时安全态势数据
     */
    @GetMapping("/posture/realtime")
    @ApiOperation("获取实时安全态势数据")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_ADMIN')")
    public Result<Map<String, Object>> getRealTimeSecurityPosture() {
        try {
            Map<String, Object> postureData = securityPostureService.getRealTimeSecurityPosture();
            return Result.success(postureData);
        } catch (Exception e) {
            log.error("获取实时安全态势数据失败", e);
            return Result.error("获取实时安全态势数据失败");
        }
    }

    /**
     * 分页查询安全事件
     */
    @GetMapping("/events/page")
    @ApiOperation("分页查询安全事件")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_ADMIN')")
    public Result<IPage<SecurityEvent>> getSecurityEventPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("事件类型") @RequestParam(required = false) String eventType,
            @ApiParam("风险级别") @RequestParam(required = false) String riskLevel,
            @ApiParam("处理状态") @RequestParam(required = false) String processStatus,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            Page<SecurityEvent> pageParam = new Page<>(page, size);
            IPage<SecurityEvent> result = securityPostureService.getSecurityEventPage(
                pageParam, eventType, riskLevel, processStatus, startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询安全事件失败", e);
            return Result.error("分页查询安全事件失败");
        }
    }

    /**
     * 获取威胁情报列表
     */
    @GetMapping("/threats")
    @ApiOperation("获取威胁情报列表")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_ADMIN')")
    public Result<IPage<ThreatIntelligence>> getThreatIntelligencePage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("威胁类型") @RequestParam(required = false) String threatType,
            @ApiParam("威胁等级") @RequestParam(required = false) String threatLevel,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            Page<ThreatIntelligence> pageParam = new Page<>(page, size);
            IPage<ThreatIntelligence> result = threatIntelligenceService.getThreatIntelligencePage(
                pageParam, threatType, threatLevel, status, startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取威胁情报列表失败", e);
            return Result.error("获取威胁情报列表失败");
        }
    }

    /**
     * 获取活跃威胁情报
     */
    @GetMapping("/threats/active")
    @ApiOperation("获取活跃威胁情报")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_ADMIN')")
    public Result<?> getActiveThreats(
            @ApiParam("数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        
        try {
            List<ThreatIntelligence> threats = threatIntelligenceService.getActiveThreats(limit);
            return Result.success(threats);
        } catch (Exception e) {
            log.error("获取活跃威胁情报失败", e);
            return Result.error("获取活跃威胁情报失败");
        }
    }

    /**
     * 获取威胁情报统计
     */
    @GetMapping("/threats/statistics")
    @ApiOperation("获取威胁情报统计")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_ADMIN')")
    public Result<Map<String, Object>> getThreatStatistics() {
        try {
            Map<String, Object> statistics = threatIntelligenceService.getThreatStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取威胁情报统计失败", e);
            return Result.error("获取威胁情报统计失败");
        }
    }

    /**
     * 根据ID获取威胁情报详情
     */
    @GetMapping("/threats/{id}")
    @ApiOperation("根据ID获取威胁情报详情")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_ADMIN')")
    public Result<ThreatIntelligence> getThreatById(@PathVariable Long id) {
        try {
            ThreatIntelligence threat = threatIntelligenceService.getThreatById(id);
            if (threat != null) {
                return Result.success(threat);
            } else {
                return Result.error("威胁情报不存在");
            }
        } catch (Exception e) {
            log.error("获取威胁情报详情失败", e);
            return Result.error("获取威胁情报详情失败");
        }
    }

    /**
     * 保存威胁情报
     */
    @PostMapping("/threats")
    @ApiOperation("保存威胁情报")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<?> saveThreatIntelligence(@RequestBody ThreatIntelligence threat) {
        try {
            boolean success = threatIntelligenceService.saveThreatIntelligence(threat);
            if (success) {
                return Result.success("威胁情报保存成功");
            } else {
                return Result.error("威胁情报保存失败");
            }
        } catch (Exception e) {
            log.error("保存威胁情报失败", e);
            return Result.error("保存威胁情报失败");
        }
    }

    /**
     * 更新威胁情报状态
     */
    @PutMapping("/threats/{id}/status")
    @ApiOperation("更新威胁情报状态")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<?> updateThreatStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            boolean success = threatIntelligenceService.updateThreatStatus(id, status);
            if (success) {
                return Result.success("威胁情报状态更新成功");
            } else {
                return Result.error("威胁情报状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新威胁情报状态失败", e);
            return Result.error("更新威胁情报状态失败");
        }
    }

    /**
     * 删除威胁情报
     */
    @DeleteMapping("/threats/{id}")
    @ApiOperation("删除威胁情报")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<?> deleteThreatIntelligence(@PathVariable Long id) {
        try {
            boolean success = threatIntelligenceService.deleteThreatIntelligence(id);
            if (success) {
                return Result.success("威胁情报删除成功");
            } else {
                return Result.error("威胁情报删除失败");
            }
        } catch (Exception e) {
            log.error("删除威胁情报失败", e);
            return Result.error("删除威胁情报失败");
        }
    }

    /**
     * 获取安全评分
     */
    @GetMapping("/score")
    @ApiOperation("获取安全评分")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','AUDIT_ADMIN')")
    public Result<Map<String, Object>> getSecurityScore() {
        try {
            // 获取完整的安全态势数据
            Map<String, Object> postureData = securityPostureService.getRealTimeSecurityPosture();
            
            // 提取评分相关数据
            Map<String, Object> scoreData = new java.util.HashMap<>();
            scoreData.put("securityScore", postureData.get("securityScore"));
            scoreData.put("unprocessedAlertCount", postureData.get("unprocessedAlertCount"));
            scoreData.put("unprocessedEventCount", postureData.get("unprocessedEventCount"));
            scoreData.put("activeThreatCount", postureData.get("activeThreatCount"));
            scoreData.put("systemHealth", postureData.get("systemHealth"));
            
            return Result.success(scoreData);
        } catch (Exception e) {
            log.error("获取安全评分失败", e);
            return Result.error("获取安全评分失败");
        }
    }
}