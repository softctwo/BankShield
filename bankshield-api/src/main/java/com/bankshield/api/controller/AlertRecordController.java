package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.common.result.Result;
import com.bankshield.api.entity.AlertRecord;
import com.bankshield.api.mapper.AlertRecordMapper;
import com.bankshield.api.enums.AlertStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警记录管理接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/alert/record")
@Api(tags = "告警记录管理")
public class AlertRecordController {

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @GetMapping("/page")
    @ApiOperation("分页查询告警记录")
    public Result<IPage<AlertRecord>> getAlertRecordPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) LocalDateTime endTime,
            @ApiParam("告警级别") @RequestParam(required = false) String alertLevel,
            @ApiParam("告警状态") @RequestParam(required = false) String alertStatus,
            @ApiParam("关键字") @RequestParam(required = false) String keyword) {
        
        try {
            Page<AlertRecord> pageParam = new Page<>(page, size);
            IPage<AlertRecord> result = alertRecordMapper.selectPage(pageParam, alertLevel, alertStatus, startTime, endTime, keyword);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询告警记录失败", e);
            return Result.error("分页查询告警记录失败");
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取告警详情")
    public Result<AlertRecord> getAlertRecord(@PathVariable Long id) {
        try {
            AlertRecord alertRecord = alertRecordMapper.selectById(id);
            if (alertRecord == null) {
                return Result.error("告警记录不存在");
            }
            return Result.success(alertRecord);
        } catch (Exception e) {
            log.error("获取告警详情失败", e);
            return Result.error("获取告警详情失败");
        }
    }

    @PutMapping("/{id}/resolve")
    @ApiOperation("处理告警")
    public Result<String> resolveAlert(
            @PathVariable Long id,
            @ApiParam("处理人") @RequestParam String handler,
            @ApiParam("处理备注") @RequestParam(required = false) String handleRemark) {
        
        try {
            AlertRecord alertRecord = alertRecordMapper.selectById(id);
            if (alertRecord == null) {
                return Result.error("告警记录不存在");
            }

            if (AlertStatus.RESOLVED.getCode().equals(alertRecord.getAlertStatus())) {
                return Result.error("告警已处理");
            }

            AlertRecord updateRecord = new AlertRecord();
            updateRecord.setId(id);
            updateRecord.setAlertStatus(AlertStatus.RESOLVED.getCode());
            updateRecord.setHandler(handler);
            updateRecord.setHandleTime(LocalDateTime.now());
            updateRecord.setHandleRemark(handleRemark);

            int result = alertRecordMapper.updateById(updateRecord);
            if (result > 0) {
                log.info("处理告警成功: {}", alertRecord.getAlertTitle());
                return Result.success("处理告警成功");
            } else {
                return Result.error("处理告警失败");
            }
        } catch (Exception e) {
            log.error("处理告警失败", e);
            return Result.error("处理告警失败");
        }
    }

    @PutMapping("/{id}/ignore")
    @ApiOperation("忽略告警")
    public Result<String> ignoreAlert(
            @PathVariable Long id,
            @ApiParam("处理人") @RequestParam String handler,
            @ApiParam("忽略原因") @RequestParam(required = false) String handleRemark) {
        
        try {
            AlertRecord alertRecord = alertRecordMapper.selectById(id);
            if (alertRecord == null) {
                return Result.error("告警记录不存在");
            }

            if (AlertStatus.IGNORED.getCode().equals(alertRecord.getAlertStatus())) {
                return Result.error("告警已忽略");
            }

            AlertRecord updateRecord = new AlertRecord();
            updateRecord.setId(id);
            updateRecord.setAlertStatus(AlertStatus.IGNORED.getCode());
            updateRecord.setHandler(handler);
            updateRecord.setHandleTime(LocalDateTime.now());
            updateRecord.setHandleRemark(handleRemark);

            int result = alertRecordMapper.updateById(updateRecord);
            if (result > 0) {
                log.info("忽略告警成功: {}", alertRecord.getAlertTitle());
                return Result.success("忽略告警成功");
            } else {
                return Result.error("忽略告警失败");
            }
        } catch (Exception e) {
            log.error("忽略告警失败", e);
            return Result.error("忽略告警失败");
        }
    }

    @PostMapping("/batch/resolve")
    @ApiOperation("批量处理告警")
    public Result<String> batchResolveAlerts(
            @RequestParam List<Long> ids,
            @ApiParam("处理人") @RequestParam String handler,
            @ApiParam("处理备注") @RequestParam(required = false) String handleRemark) {
        
        try {
            int result = alertRecordMapper.batchUpdateStatus(ids, AlertStatus.RESOLVED.getCode(), handler, handleRemark);
            if (result > 0) {
                log.info("批量处理告警成功: {}条", result);
                return Result.success("批量处理告警成功");
            } else {
                return Result.error("批量处理告警失败");
            }
        } catch (Exception e) {
            log.error("批量处理告警失败", e);
            return Result.error("批量处理告警失败");
        }
    }

    @PostMapping("/batch/ignore")
    @ApiOperation("批量忽略告警")
    public Result<String> batchIgnoreAlerts(
            @RequestParam List<Long> ids,
            @ApiParam("处理人") @RequestParam String handler,
            @ApiParam("忽略原因") @RequestParam(required = false) String handleRemark) {
        
        try {
            int result = alertRecordMapper.batchUpdateStatus(ids, AlertStatus.IGNORED.getCode(), handler, handleRemark);
            if (result > 0) {
                log.info("批量忽略告警成功: {}条", result);
                return Result.success("批量忽略告警成功");
            } else {
                return Result.error("批量忽略告警失败");
            }
        } catch (Exception e) {
            log.error("批量忽略告警失败", e);
            return Result.error("批量忽略告警失败");
        }
    }

    @GetMapping("/unresolved/count")
    @ApiOperation("获取未处理告警数")
    public Result<Map<String, Object>> getUnresolvedAlertCount() {
        try {
            long count = alertRecordMapper.selectPage(
                    new Page<>(1, 1), 
                    null, 
                    "UNRESOLVED", 
                    null, 
                    null, 
                    null
            ).getTotal();
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("count", count);
            result.put("timestamp", LocalDateTime.now());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取未处理告警数失败", e);
            return Result.error("获取未处理告警数失败");
        }
    }

    @GetMapping("/unresolved/recent")
    @ApiOperation("获取最近未处理告警")
    public Result<List<AlertRecord>> getRecentUnresolvedAlerts(
            @ApiParam("数量限制") @RequestParam(defaultValue = "10") int limit) {

        try {
            List<AlertRecord> alerts = alertRecordMapper.selectUnresolvedAlerts(limit);
            return Result.success(alerts);
        } catch (Exception e) {
            log.error("获取最近未处理告警失败", e);
            return Result.error("获取最近未处理告警失败");
        }
    }

    @GetMapping("/stats/today")
    @ApiOperation("获取今日告警统计")
    public Result<Map<String, Object>> getTodayAlertStats() {
        try {
            // 获取今日统计数据
            Map<String, Object> todayStats = alertRecordMapper.countAlerts(
                    LocalDateTime.now().withHour(0).withMinute(0).withSecond(0), 
                    LocalDateTime.now());
            
            // 获取各级别统计
            List<Map<String, Object>> levelStats = alertRecordMapper.countAlertsByLevel(
                    LocalDateTime.now().withHour(0).withMinute(0).withSecond(0), 
                    LocalDateTime.now());
            
            todayStats.put("levelDistribution", levelStats);
            
            return Result.success(todayStats);
        } catch (Exception e) {
            log.error("获取今日告警统计失败", e);
            return Result.error("获取今日告警统计失败");
        }
    }

    @GetMapping("/stats/distribution")
    @ApiOperation("获取告警分布统计")
    public Result<Map<String, Object>> getAlertDistribution() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusDays(7); // 最近7天
            
            // 获取各级别告警数量
            List<Map<String, Object>> levelDistribution = alertRecordMapper.countAlertsByLevel(startTime, now);
            
            // 获取告警趋势
            List<Map<String, Object>> trend = alertRecordMapper.getAlertTrend(startTime, now, "DAY");
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("levelDistribution", levelDistribution);
            result.put("trend", trend);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取告警分布统计失败", e);
            return Result.error("获取告警分布统计失败");
        }
    }
}