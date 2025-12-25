package com.bankshield.gateway.controller;

import com.bankshield.common.result.Result;
import com.bankshield.gateway.entity.ApiRouteConfig;
import com.bankshield.gateway.repository.ApiRouteConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 网关配置控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayConfigController {
    
    @Autowired
    private ApiRouteConfigRepository apiRouteConfigRepository;
    
    /**
     * 获取API路由配置列表
     */
    @GetMapping("/api/routes")
    public Result<Page<ApiRouteConfig>> getRoutes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String routeId,
            @RequestParam(required = false) String targetService) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
            
            Page<ApiRouteConfig> result;
            if (routeId != null && !routeId.trim().isEmpty()) {
                result = apiRouteConfigRepository.findByRouteIdContaining(routeId, pageable);
            } else if (targetService != null && !targetService.trim().isEmpty()) {
                result = apiRouteConfigRepository.findByTargetServiceContaining(targetService, pageable);
            } else {
                result = apiRouteConfigRepository.findAll(pageable);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取API路由配置失败", e);
            return Result.error("获取API路由配置失败");
        }
    }
    
    /**
     * 根据ID获取API路由配置
     */
    @GetMapping("/api/route/{id}")
    public Result<ApiRouteConfig> getRouteById(@PathVariable Long id) {
        try {
            Optional<ApiRouteConfig> routeConfig = apiRouteConfigRepository.findById(id);
            if (routeConfig.isPresent()) {
                return Result.success(routeConfig.get());
            } else {
                return Result.error("路由配置不存在");
            }
        } catch (Exception e) {
            log.error("获取API路由配置失败", e);
            return Result.error("获取API路由配置失败");
        }
    }
    
    /**
     * 创建API路由配置
     */
    @PostMapping("/api/route")
    public Result<String> createRoute(@RequestBody ApiRouteConfig routeConfig) {
        try {
            // 验证必填字段
            if (routeConfig.getRouteId() == null || routeConfig.getRouteId().trim().isEmpty()) {
                return Result.error("路由ID不能为空");
            }
            if (routeConfig.getRoutePath() == null || routeConfig.getRoutePath().trim().isEmpty()) {
                return Result.error("路由路径不能为空");
            }
            if (routeConfig.getTargetService() == null || routeConfig.getTargetService().trim().isEmpty()) {
                return Result.error("目标服务不能为空");
            }
            
            // 检查路由ID是否已存在
            if (apiRouteConfigRepository.existsByRouteId(routeConfig.getRouteId())) {
                return Result.error("路由ID已存在");
            }
            
            // 设置默认值
            if (routeConfig.getRequireAuth() == null) {
                routeConfig.setRequireAuth(false);
            }
            if (routeConfig.getEnabled() == null) {
                routeConfig.setEnabled(true);
            }
            if (routeConfig.getSignatureVerificationEnabled() == null) {
                routeConfig.setSignatureVerificationEnabled(false);
            }
            if (routeConfig.getRateLimitThreshold() == null) {
                routeConfig.setRateLimitThreshold(100); // 默认100次/秒
            }
            
            apiRouteConfigRepository.save(routeConfig);
            log.info("创建API路由配置成功：routeId={}", routeConfig.getRouteId());
            return Result.success("创建成功");
        } catch (Exception e) {
            log.error("创建API路由配置失败", e);
            return Result.error("创建API路由配置失败");
        }
    }
    
    /**
     * 更新API路由配置
     */
    @PutMapping("/api/route/{id}")
    public Result<String> updateRoute(@PathVariable Long id, @RequestBody ApiRouteConfig routeConfig) {
        try {
            Optional<ApiRouteConfig> existing = apiRouteConfigRepository.findById(id);
            if (!existing.isPresent()) {
                return Result.error("路由配置不存在");
            }
            
            ApiRouteConfig existingConfig = existing.get();
            
            // 检查路由ID是否重复
            if (!existingConfig.getRouteId().equals(routeConfig.getRouteId()) && 
                apiRouteConfigRepository.existsByRouteId(routeConfig.getRouteId())) {
                return Result.error("路由ID已存在");
            }
            
            // 更新字段
            existingConfig.setRouteId(routeConfig.getRouteId());
            existingConfig.setRoutePath(routeConfig.getRoutePath());
            existingConfig.setTargetService(routeConfig.getTargetService());
            existingConfig.setRequireAuth(routeConfig.getRequireAuth());
            existingConfig.setRequiredRoles(routeConfig.getRequiredRoles());
            existingConfig.setRateLimitThreshold(routeConfig.getRateLimitThreshold());
            existingConfig.setSignatureVerificationEnabled(routeConfig.getSignatureVerificationEnabled());
            existingConfig.setSignatureSecret(routeConfig.getSignatureSecret());
            existingConfig.setEnabled(routeConfig.getEnabled());
            existingConfig.setDescription(routeConfig.getDescription());
            
            apiRouteConfigRepository.save(existingConfig);
            log.info("更新API路由配置成功：routeId={}", routeConfig.getRouteId());
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新API路由配置失败", e);
            return Result.error("更新API路由配置失败");
        }
    }
    
    /**
     * 删除API路由配置
     */
    @DeleteMapping("/api/route/{id}")
    public Result<String> deleteRoute(@PathVariable Long id) {
        try {
            Optional<ApiRouteConfig> existing = apiRouteConfigRepository.findById(id);
            if (!existing.isPresent()) {
                return Result.error("路由配置不存在");
            }
            
            ApiRouteConfig routeConfig = existing.get();
            apiRouteConfigRepository.deleteById(id);
            log.info("删除API路由配置成功：routeId={}", routeConfig.getRouteId());
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除API路由配置失败", e);
            return Result.error("删除API路由配置失败");
        }
    }
    
    /**
     * 启用/禁用API路由配置
     */
    @PutMapping("/api/route/{id}/status")
    public Result<String> updateRouteStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        try {
            Optional<ApiRouteConfig> existing = apiRouteConfigRepository.findById(id);
            if (!existing.isPresent()) {
                return Result.error("路由配置不存在");
            }
            
            ApiRouteConfig routeConfig = existing.get();
            routeConfig.setEnabled(enabled);
            apiRouteConfigRepository.save(routeConfig);
            
            log.info("更新API路由配置状态成功：routeId={}, enabled={}", routeConfig.getRouteId(), enabled);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新API路由配置状态失败", e);
            return Result.error("更新API路由配置状态失败");
        }
    }
}