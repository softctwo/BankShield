package com.bankshield.encrypt.controller;

import com.bankshield.common.result.PageResult;
import com.bankshield.common.result.Result;
import com.bankshield.encrypt.dto.GenerateKeyRequest;
import com.bankshield.encrypt.dto.RotateKeyRequest;
import com.bankshield.encrypt.entity.EncryptionKey;
import com.bankshield.encrypt.enums.KeyStatus;
import com.bankshield.encrypt.enums.KeyType;
import com.bankshield.encrypt.enums.KeyUsage;
import com.bankshield.encrypt.service.KeyManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 密钥管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/key")
@Api(tags = "密钥管理")
@PreAuthorize("hasRole('SECURITY_ADMIN')")
public class KeyManagementController {
    
    @Autowired
    private KeyManagementService keyManagementService;
    
    /**
     * 分页查询密钥
     */
    @GetMapping("/page")
    @ApiOperation("分页查询密钥")
    public PageResult<EncryptionKey> getKeyPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("密钥名称") @RequestParam(required = false) String keyName,
            @ApiParam("密钥类型") @RequestParam(required = false) String keyType,
            @ApiParam("密钥状态") @RequestParam(required = false) String keyStatus,
            @ApiParam("密钥用途") @RequestParam(required = false) String keyUsage) {
        
        log.info("分页查询密钥，页码：{}，每页大小：{}，查询条件：name={}, type={}, status={}, usage={}", 
                pageNum, pageSize, keyName, keyType, keyStatus, keyUsage);
        
        return keyManagementService.getKeyList(pageNum, pageSize, keyName, keyType, keyStatus, keyUsage);
    }
    
    /**
     * 获取密钥详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取密钥详情")
    public Result<EncryptionKey> getKeyDetail(
            @ApiParam("密钥ID") @PathVariable @NotNull Long id) {
        
        log.info("获取密钥详情，ID：{}", id);
        return keyManagementService.getKeyDetail(id);
    }
    
    /**
     * 生成新密钥
     */
    @PostMapping("/generate")
    @ApiOperation("生成新密钥")
    public Result<EncryptionKey> generateKey(@Valid @RequestBody GenerateKeyRequest request) {
        
        log.info("生成新密钥，请求：{}", request);
        
        return keyManagementService.generateKey(
            request.getKeyName(),
            KeyType.fromCode(request.getKeyType()),
            request.getKeyUsage() != null ? KeyUsage.fromCode(request.getKeyUsage()) : null,
            request.getKeyLength(),
            request.getExpireDays(),
            request.getRotationCycle(),
            request.getDescription(),
            request.getCreatedBy()
        );
    }
    
    /**
     * 手动轮换密钥
     */
    @PostMapping("/rotate/{id}")
    @ApiOperation("手动轮换密钥")
    public Result<EncryptionKey> rotateKey(
            @ApiParam("密钥ID") @PathVariable @NotNull Long id,
            @ApiParam("轮换原因") @RequestParam @NotBlank String rotationReason,
            @ApiParam("操作员") @RequestParam @NotBlank String operator) {
        
        log.info("手动轮换密钥，ID：{}，原因：{}，操作员：{}", id, rotationReason, operator);
        return keyManagementService.rotateKey(id, rotationReason, operator);
    }
    
    /**
     * 更新密钥状态
     */
    @PutMapping("/status/{id}")
    @ApiOperation("更新密钥状态")
    public Result<Void> updateKeyStatus(
            @ApiParam("密钥ID") @PathVariable @NotNull Long id,
            @ApiParam("新状态") @RequestParam @NotBlank String status,
            @ApiParam("操作员") @RequestParam @NotBlank String operator) {
        
        log.info("更新密钥状态，ID：{}，新状态：{}，操作员：{}", id, status, operator);
        
        try {
            KeyStatus keyStatus = KeyStatus.fromCode(status);
            return keyManagementService.updateKeyStatus(id, keyStatus, operator);
        } catch (IllegalArgumentException e) {
            return Result.error("无效的状态值：" + status);
        }
    }
    
    /**
     * 销毁密钥
     */
    @DeleteMapping("/{id}")
    @ApiOperation("销毁密钥")
    public Result<Void> destroyKey(
            @ApiParam("密钥ID") @PathVariable @NotNull Long id,
            @ApiParam("操作员") @RequestParam @NotBlank String operator) {
        
        log.info("销毁密钥，ID：{}，操作员：{}", id, operator);
        return keyManagementService.destroyKey(id, operator);
    }
    
    /**
     * 导出密钥信息
     */
    @PostMapping("/export")
    @ApiOperation("导出密钥信息")
    public Result<String> exportKeyInfo(
            @ApiParam("密钥ID列表（为空时导出所有）") @RequestBody(required = false) List<Long> keyIds) {
        
        log.info("导出密钥信息，密钥ID列表：{}", keyIds);
        return keyManagementService.exportKeyInfo(keyIds);
    }
    
    /**
     * 获取支持的密钥类型列表
     */
    @GetMapping("/types")
    @ApiOperation("获取支持的密钥类型列表")
    public Result<List<Map<String, String>>> getSupportedKeyTypes() {
        log.info("获取支持的密钥类型列表");
        return keyManagementService.getSupportedKeyTypes();
    }
    
    /**
     * 查询密钥使用统计
     */
    @GetMapping("/usage/{id}")
    @ApiOperation("查询密钥使用统计")
    public Result<Map<String, Object>> getKeyUsageStatistics(
            @ApiParam("密钥ID") @PathVariable @NotNull Long id,
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("查询密钥使用统计，密钥ID：{}，时间范围：{} - {}", id, startTime, endTime);
        return keyManagementService.getKeyUsageStatistics(id, startTime, endTime);
    }
    
    /**
     * 获取密钥统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取密钥统计信息")
    public Result<Map<String, Object>> getKeyStatistics() {
        log.info("获取密钥统计信息");
        return keyManagementService.getKeyStatistics();
    }
    
    /**
     * 生成密钥请求DTO
     */
    public static class GenerateKeyRequest {
        @NotBlank(message = "密钥名称不能为空")
        private String keyName;
        
        @NotNull(message = "密钥类型不能为空")
        private String keyType;
        
        @NotNull(message = "密钥用途不能为空")
        private String keyUsage;
        
        private Integer keyLength;
        private Integer expireDays;
        private Integer rotationCycle;
        private String description;
        private String createdBy;
        
        // Getters and Setters
        public String getKeyName() { return keyName; }
        public void setKeyName(String keyName) { this.keyName = keyName; }
        
        public String getKeyType() { return keyType; }
        public void setKeyType(String keyType) { this.keyType = keyType; }
        
        public String getKeyUsage() { return keyUsage; }
        public void setKeyUsage(String keyUsage) { this.keyUsage = keyUsage; }
        
        public Integer getKeyLength() { return keyLength; }
        public void setKeyLength(Integer keyLength) { this.keyLength = keyLength; }
        
        public Integer getExpireDays() { return expireDays; }
        public void setExpireDays(Integer expireDays) { this.expireDays = expireDays; }
        
        public Integer getRotationCycle() { return rotationCycle; }
        public void setRotationCycle(Integer rotationCycle) { this.rotationCycle = rotationCycle; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        
        @Override
        public String toString() {
            return "GenerateKeyRequest{" +
                    "keyName='" + keyName + '\'' +
                    ", keyType='" + keyType + '\'' +
                    ", keyUsage='" + keyUsage + '\'' +
                    ", keyLength=" + keyLength +
                    ", expireDays=" + expireDays +
                    ", rotationCycle=" + rotationCycle +
                    ", description='" + description + '\'' +
                    ", createdBy='" + createdBy + '\'' +
                    '}';
        }
    }
}