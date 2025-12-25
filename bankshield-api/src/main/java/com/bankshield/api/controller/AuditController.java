package com.bankshield.api.controller;

import com.bankshield.api.entity.LoginAudit;
import com.bankshield.api.entity.OperationAudit;
import com.bankshield.api.service.LoginAuditService;
import com.bankshield.api.service.OperationAuditService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
 * 审计管理控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private OperationAuditService operationAuditService;

    @Autowired
    private LoginAuditService loginAuditService;

    /**
     * 分页查询操作审计列表
     */
    @GetMapping("/operation/page")
    public Result<Page<OperationAudit>> getOperationAuditPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("分页查询操作审计列表，页码: {}, 每页大小: {}, 用户名: {}, 操作类型: {}, 模块: {}, 状态: {}, 开始时间: {}, 结束时间: {}", 
                page, size, username, operationType, operationModule, status, startTime, endTime);
        
        return operationAuditService.getOperationAuditPage(page, size, username, operationType, 
                operationModule, status, startTime, endTime);
    }

    /**
     * 根据ID获取操作审计详情
     */
    @GetMapping("/operation/{id}")
    public Result<OperationAudit> getOperationAuditById(@PathVariable Long id) {
        log.info("查询操作审计详情，ID: {}", id);
        return operationAuditService.getOperationAuditById(id);
    }

    /**
     * 分页查询登录审计列表
     */
    @GetMapping("/login/page")
    public Result<Page<LoginAudit>> getLoginAuditPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("分页查询登录审计列表，页码: {}, 每页大小: {}, 用户名: {}, 状态: {}, 开始时间: {}, 结束时间: {}", 
                page, size, username, status, startTime, endTime);
        
        return loginAuditService.getLoginAuditPage(page, size, username, status, startTime, endTime);
    }

    /**
     * 根据ID获取登录审计详情
     */
    @GetMapping("/login/{id}")
    public Result<LoginAudit> getLoginAuditById(@PathVariable Long id) {
        log.info("查询登录审计详情，ID: {}", id);
        return loginAuditService.getLoginAuditById(id);
    }

    /**
     * 导出操作审计Excel
     */
    @PostMapping("/export/operation")
    public void exportOperationAudit(
            HttpServletResponse response,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("导出操作审计Excel，用户名: {}, 操作类型: {}, 模块: {}, 状态: {}, 开始时间: {}, 结束时间: {}", 
                username, operationType, operationModule, status, startTime, endTime);
        
        operationAuditService.exportOperationAudit(response, username, operationType, operationModule, 
                status, startTime, endTime);
    }

    /**
     * 导出登录审计Excel
     */
    @PostMapping("/export/login")
    public void exportLoginAudit(
            HttpServletResponse response,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("导出登录审计Excel，用户名: {}, 状态: {}, 开始时间: {}, 结束时间: {}", 
                username, status, startTime, endTime);
        
        loginAuditService.exportLoginAudit(response, username, status, startTime, endTime);
    }
}