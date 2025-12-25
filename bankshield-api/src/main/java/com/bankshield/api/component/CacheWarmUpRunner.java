package com.bankshield.api.component;

import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import com.bankshield.api.service.PerformanceOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热组件
 * 在应用启动时预热常用数据到缓存
 * 
 * @author BankShield
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmUpRunner implements CommandLineRunner {
    
    private final CacheManager cacheManager;
    private final PerformanceOptimizationService performanceService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final DeptMapper deptMapper;
    private final MenuMapper menuMapper;
    private final SecurityKeyMapper securityKeyMapper;
    private final DataAssetMapper dataAssetMapper;
    private final Executor asyncExecutor;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始缓存预热...");
        long startTime = System.currentTimeMillis();
        
        try {
            // 并行预热不同类型的缓存
            CompletableFuture<Void> userCacheFuture = CompletableFuture.runAsync(() -> warmUpUserCache(), asyncExecutor);
            CompletableFuture<Void> roleCacheFuture = CompletableFuture.runAsync(() -> warmUpRoleCache(), asyncExecutor);
            CompletableFuture<Void> deptCacheFuture = CompletableFuture.runAsync(() -> warmUpDeptCache(), asyncExecutor);
            CompletableFuture<Void> menuCacheFuture = CompletableFuture.runAsync(() -> warmUpMenuCache(), asyncExecutor);
            CompletableFuture<Void> keyCacheFuture = CompletableFuture.runAsync(() -> warmUpKeyCache(), asyncExecutor);
            CompletableFuture<Void> assetCacheFuture = CompletableFuture.runAsync(() -> warmUpAssetCache(), asyncExecutor);
            
            // 等待所有预热任务完成
            CompletableFuture.allOf(userCacheFuture, roleCacheFuture, deptCacheFuture, 
                                   menuCacheFuture, keyCacheFuture, assetCacheFuture)
                           .get(5, TimeUnit.MINUTES);
            
            long endTime = System.currentTimeMillis();
            log.info("缓存预热完成，总耗时: {}ms", endTime - startTime);
            
        } catch (Exception e) {
            log.error("缓存预热失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
    
    /**
     * 预热用户相关缓存
     */
    private void warmUpUserCache() {
        try {
            log.info("预热用户缓存...");
            
            // 预热活跃用户数据
            List<User> activeUsers = userMapper.selectActiveUsers(100); // 查询前100个活跃用户
            
            // 预热用户角色数据
            performanceService.getUsersWithRolesOptimized(1, 20, null, null);
            
            log.info("用户缓存预热完成，预热用户数: {}", activeUsers.size());
            
        } catch (Exception e) {
            log.error("用户缓存预热失败", e);
        }
    }
    
    /**
     * 预热角色相关缓存
     */
    private void warmUpRoleCache() {
        try {
            log.info("预热角色缓存...");
            
            // 预热所有角色数据
            List<Role> allRoles = roleMapper.selectAllRoles();
            
            // 预热常用角色的菜单权限（前10个角色）
            allRoles.stream().limit(10).forEach(role -> {
                try {
                    performanceService.getRoleWithMenusOptimized(role.getId());
                } catch (Exception e) {
                    log.error("预热角色菜单缓存失败: roleId={}", role.getId(), e);
                }
            });
            
            log.info("角色缓存预热完成，预热角色数: {}", allRoles.size());
            
        } catch (Exception e) {
            log.error("角色缓存预热失败", e);
        }
    }
    
    /**
     * 预热部门缓存
     */
    private void warmUpDeptCache() {
        try {
            log.info("预热部门缓存...");
            
            // 预热部门树
            performanceService.getDeptTreeOptimized(1); // 只预热启用的部门
            
            log.info("部门缓存预热完成");
            
        } catch (Exception e) {
            log.error("部门缓存预热失败", e);
        }
    }
    
    /**
     * 预热菜单缓存
     */
    private void warmUpMenuCache() {
        try {
            log.info("预热菜单缓存...");
            
            // 预热所有菜单
            List<Menu> allMenus = menuMapper.selectAllMenus();
            
            log.info("菜单缓存预热完成，预热菜单数: {}", allMenus.size());
            
        } catch (Exception e) {
            log.error("菜单缓存预热失败", e);
        }
    }
    
    /**
     * 预热密钥缓存
     */
    private void warmUpKeyCache() {
        try {
            log.info("预热密钥缓存...");
            
            // 预热密钥统计信息
            performanceService.getKeyStatisticsOptimized(30); // 最近30天的统计
            
            // 预热常用密钥类型
            List<String> commonKeyTypes = securityKeyMapper.selectCommonKeyTypes();
            log.info("密钥缓存预热完成，常用密钥类型数: {}", commonKeyTypes.size());
            
        } catch (Exception e) {
            log.error("密钥缓存预热失败", e);
        }
    }
    
    /**
     * 预热数据资产缓存
     */
    private void warmUpAssetCache() {
        try {
            log.info("预热数据资产缓存...");
            
            // 预热常用的分类分级数据
            List<DataAsset> commonAssets = dataAssetMapper.selectCommonAssets(50); // 前50个常用资产
            
            log.info("数据资产缓存预热完成，预热资产数: {}", commonAssets.size());
            
        } catch (Exception e) {
            log.error("数据资产缓存预热失败", e);
        }
    }
    
    /**
     * 手动触发缓存预热（可用于定时任务）
     */
    public void manualWarmUp() {
        log.info("手动触发缓存预热...");
        try {
            run();
        } catch (Exception e) {
            log.error("手动缓存预热失败", e);
        }
    }
}