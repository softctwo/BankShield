package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.dto.KeyStatisticsDTO;
import com.bankshield.api.dto.KeyUsageStatisticsDTO;
import com.bankshield.api.entity.SecurityKey;
import com.bankshield.api.service.KeyManagementService;
import com.bankshield.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 密钥管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/key")
public class KeyManagementController {

    @Autowired
    private KeyManagementService keyManagementService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER')")
    public Result<IPage<SecurityKey>> getKeyPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyName,
            @RequestParam(required = false) String keyType,
            @RequestParam(required = false) String keyStatus,
            @RequestParam(required = false) String keyUsage) {
        try {
            Page<SecurityKey> page = new Page<>(pageNum, pageSize);
            IPage<SecurityKey> result = keyManagementService.getKeyPage(page, keyName, keyType, keyStatus, keyUsage);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询密钥列表失败", e);
            return Result.error("查询密钥列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER')")
    public Result<SecurityKey> getKeyDetail(@PathVariable Long id) {
        try {
            SecurityKey key = keyManagementService.getKeyById(id);
            if (key == null) {
                return Result.error("密钥不存在");
            }
            return Result.success(key);
        } catch (Exception e) {
            log.error("查询密钥详情失败", e);
            return Result.error("查询密钥详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER')")
    public Result<SecurityKey> generateKey(@RequestBody Map<String, Object> params) {
        try {
            String keyName = (String) params.get("keyName");
            String keyType = (String) params.get("keyType");
            String keyUsage = (String) params.get("keyUsage");
            Integer keyLength = (Integer) params.get("keyLength");
            Integer expireDays = (Integer) params.get("expireDays");
            String description = (String) params.get("description");
            String createdBy = (String) params.get("createdBy");

            SecurityKey key = keyManagementService.generateKey(
                    keyName, keyType, keyUsage, keyLength, expireDays, description, createdBy);
            return Result.success(key);
        } catch (Exception e) {
            log.error("生成密钥失败", e);
            return Result.error("生成密钥失败: " + e.getMessage());
        }
    }

    @PostMapping("/rotate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER')")
    public Result<SecurityKey> rotateKey(
            @PathVariable Long id,
            @RequestParam String rotationReason,
            @RequestParam String operator) {
        try {
            SecurityKey key = keyManagementService.rotateKey(id, rotationReason, operator);
            return Result.success(key);
        } catch (Exception e) {
            log.error("轮换密钥失败", e);
            return Result.error("轮换密钥失败: " + e.getMessage());
        }
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER')")
    public Result<Void> updateKeyStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam String operator) {
        try {
            keyManagementService.updateKeyStatus(id, status, operator);
            return Result.success();
        } catch (Exception e) {
            log.error("更新密钥状态失败", e);
            return Result.error("更新密钥状态失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER')")
    public Result<Void> destroyKey(@PathVariable Long id, @RequestParam String operator) {
        try {
            keyManagementService.destroyKey(id, operator);
            return Result.success();
        } catch (Exception e) {
            log.error("销毁密钥失败", e);
            return Result.error("销毁密钥失败: " + e.getMessage());
        }
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER')")
    public void exportKeyInfo(@RequestBody List<Long> keyIds, HttpServletResponse response) {
        try {
            keyManagementService.exportKeyInfo(keyIds, response);
        } catch (Exception e) {
            log.error("导出密钥信息失败", e);
        }
    }

    @GetMapping("/types")
    public Result<List<String>> getSupportedKeyTypes() {
        try {
            List<String> types = keyManagementService.getSupportedKeyTypes();
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取密钥类型失败", e);
            return Result.error("获取密钥类型失败: " + e.getMessage());
        }
    }

    @GetMapping("/usage/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER', 'AUDITOR')")
    public Result<KeyUsageStatisticsDTO> getKeyUsageStatistics(
            @PathVariable Long id,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            KeyUsageStatisticsDTO statistics = keyManagementService.getKeyUsageStatistics(id, startTime, endTime);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取密钥使用统计失败", e);
            return Result.error("获取密钥使用统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'KEY_MANAGER', 'AUDITOR')")
    public Result<KeyStatisticsDTO> getKeyStatistics() {
        try {
            KeyStatisticsDTO statistics = keyManagementService.getKeyStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取密钥统计信息失败", e);
            return Result.error("获取密钥统计信息失败: " + e.getMessage());
        }
    }
}
