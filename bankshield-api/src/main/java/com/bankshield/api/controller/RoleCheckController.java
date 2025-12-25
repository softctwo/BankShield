package com.bankshield.api.controller;

import com.bankshield.api.annotation.RoleExclusive;
import com.bankshield.api.entity.RoleMutex;
import com.bankshield.api.entity.RoleViolation;
import com.bankshield.api.service.RoleCheckService;
import com.bankshield.common.result.PageResult;
import com.bankshield.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色互斥检查控制器
 * 提供三权分立机制相关的API接口
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
@Api(tags = "角色互斥管理")
public class RoleCheckController {

    private final RoleCheckService roleCheckService;

    /**
     * 分配角色（带互斥检查）
     */
    @PostMapping("/assign")
    @ApiOperation(value = "分配角色", notes = "分配角色时进行三权分立互斥检查")
    @RoleExclusive(checkType = RoleExclusive.CheckType.ASSIGN, forceCheck = true)
    public Result<Void> assignRole(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "角色编码", required = true) @RequestParam String roleCode) {
        
        try {
            log.info("分配角色：用户ID={}, 角色编码={}", userId, roleCode);
            
            // 检查角色分配
            boolean canAssign = roleCheckService.checkRoleAssignment(userId, roleCode);
            
            if (!canAssign) {
                return Result.error("角色分配违反三权分立原则");
            }
            
            // TODO: 调用实际的分配角色服务
            // roleService.assignRole(userId, roleCode);
            
            return Result.success();
        } catch (Exception e) {
            log.error("分配角色失败", e);
            return Result.error("分配角色失败");
        }
    }

    /**
     * 检查用户角色合规性
     */
    @GetMapping("/check/{userId}")
    @ApiOperation(value = "检查用户角色合规性", notes = "检查指定用户是否存在角色互斥违规")
    public Result<List<RoleMutex>> checkUserRoleCompliance(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        
        try {
            log.info("检查用户角色合规性：用户ID={}", userId);
            
            List<RoleMutex> conflicts = roleCheckService.checkUserRoleConflicts(userId);
            
            if (conflicts.isEmpty()) {
                return Result.success(conflicts, "用户角色配置合规");
            } else {
                return Result.success(conflicts, String.format("发现%d条角色互斥违规", conflicts.size()));
            }
        } catch (Exception e) {
            log.error("检查用户角色合规性失败", e);
            return Result.error("检查失败");
        }
    }

    /**
     * 查询违规记录
     */
    @GetMapping("/violations")
    @ApiOperation(value = "查询违规记录", notes = "分页查询角色违规记录")
    public Result<PageResult<RoleViolation>> getRoleViolations(
            @ApiParam(value = "用户ID") @RequestParam(required = false) Long userId,
            @ApiParam(value = "处理状态：0-未处理，1-已处理，2-已忽略") @RequestParam(required = false) Integer status,
            @ApiParam(value = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam(value = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") int pageSize) {
        
        try {
            log.info("查询违规记录：用户ID={}, 状态={}, 时间范围={}-{}, 页码={}, 每页数量={}", 
                    userId, status, startTime, endTime, pageNum, pageSize);
            
            List<RoleViolation> violations;
            if (userId != null) {
                violations = roleCheckService.getUnhandledViolations(); // 简化实现
            } else if (startTime != null && endTime != null) {
                violations = roleCheckService.getUnhandledViolations(); // 简化实现
            } else {
                violations = roleCheckService.getUnhandledViolations();
            }
            
            // 手动分页
            int total = violations.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            
            List<RoleViolation> pageList = violations.subList(start, end);
            PageResult<RoleViolation> pageResult = new PageResult<>(pageList, total, pageNum, pageSize);
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询违规记录失败", e);
            return Result.error("查询失败");
        }
    }

    /**
     * 处理违规记录
     */
    @PutMapping("/violation/handle/{id}")
    @ApiOperation(value = "处理违规记录", notes = "处理角色违规记录")
    public Result<Void> handleRoleViolation(
            @ApiParam(value = "违规记录ID", required = true) @PathVariable Long id,
            @ApiParam(value = "处理状态：1-已处理，2-已忽略", required = true) @RequestParam Integer status,
            @ApiParam(value = "处理备注") @RequestParam(required = false) String handleRemark,
            @ApiParam(value = "处理人姓名") @RequestParam String handlerName) {
        
        try {
            log.info("处理违规记录：ID={}, 状态={}, 处理人={}, 备注={}", id, status, handlerName, handleRemark);
            
            roleCheckService.handleRoleViolation(id, status, handleRemark, handlerName);
            
            return Result.success();
        } catch (Exception e) {
            log.error("处理违规记录失败", e);
            return Result.error("处理失败");
        }
    }

    /**
     * 获取所有角色互斥规则
     */
    @GetMapping("/mutex-rules")
    @ApiOperation(value = "获取角色互斥规则", notes = "获取所有角色互斥规则配置")
    public Result<List<RoleMutex>> getRoleMutexRules() {
        
        try {
            log.info("获取角色互斥规则");
            
            List<RoleMutex> rules = roleCheckService.getAllRoleMutexRules();
            
            return Result.success(rules);
        } catch (Exception e) {
            log.error("获取角色互斥规则失败", e);
            return Result.error("获取失败");
        }
    }

    /**
     * 获取指定角色的互斥角色
     */
    @GetMapping("/mutex-roles/{roleCode}")
    @ApiOperation(value = "获取互斥角色", notes = "获取指定角色的互斥角色列表")
    public Result<List<String>> getMutexRoles(
            @ApiParam(value = "角色编码", required = true) @PathVariable String roleCode) {
        
        try {
            log.info("获取互斥角色：角色编码={}", roleCode);
            
            List<String> mutexRoles = roleCheckService.getMutexRoles(roleCode);
            
            return Result.success(mutexRoles);
        } catch (Exception e) {
            log.error("获取互斥角色失败", e);
            return Result.error("获取失败");
        }
    }

    /**
     * 获取三权分立状态
     */
    @GetMapping("/separation-status")
    @ApiOperation(value = "获取三权分立状态", notes = "获取系统三权分立机制的运行状态")
    public Result<String> getSeparationOfPowersStatus() {
        
        try {
            log.info("获取三权分立状态");
            
            String status = roleCheckService.getSeparationOfPowersStatus();
            
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取三权分立状态失败", e);
            return Result.error("获取失败");
        }
    }

    /**
     * 手动触发角色检查
     */
    @PostMapping("/check/trigger")
    @ApiOperation(value = "手动触发角色检查", notes = "手动触发角色互斥检查任务")
    public Result<String> triggerRoleCheck() {

        try {
            log.info("手动触发角色检查");

            // 异步执行检查任务
            new Thread(() -> roleCheckService.executeRoleCheckJob()).start();

            return Result.success("角色检查任务已触发，正在后台执行");
        } catch (Exception e) {
            log.error("手动触发角色检查失败", e);
            return Result.error("触发失败");
        }
    }
}