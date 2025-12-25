package com.bankshield.api.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;
import com.bankshield.api.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 性能优化服务类
 * 提供优化的查询方法，避免N+1查询问题
 * 
 * @author BankShield
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceOptimizationService {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final DeptMapper deptMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final AuditOperationMapper auditOperationMapper;
    private final SecurityKeyMapper securityKeyMapper;
    private final DataAssetMapper dataAssetMapper;
    
    /**
     * 优化的用户角色查询（避免N+1问题）
     */
    @Cacheable(value = "userCache", key = "'users_with_roles_'.concat(#pageNum).concat('_').concat(#pageSize)")
    @Transactional(readOnly = true)
    public Map<String, Object> getUsersWithRolesOptimized(int pageNum, int pageSize, String username, Long deptId) {
        log.info("执行优化的用户角色查询: pageNum={}, pageSize={}", pageNum, pageSize);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 分页查询用户（单次查询）
            Page<User> page = new Page<>(pageNum, pageSize);
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            
            if (username != null && !username.trim().isEmpty()) {
                userWrapper.like("username", username);
            }
            if (deptId != null) {
                userWrapper.eq("dept_id", deptId);
            }
            
            userWrapper.orderByDesc("create_time");
            Page<User> userPage = userMapper.selectPage(page, userWrapper);
            List<User> users = userPage.getRecords();
            
            if (users.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", Collections.emptyList());
                result.put("total", 0L);
                result.put("pages", 0L);
                return result;
            }
            
            // 2. 一次性获取所有用户ID
            List<Long> userIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            // 3. 一次性查询所有用户角色关系
            QueryWrapper<UserRole> userRoleWrapper = new QueryWrapper<>();
            userRoleWrapper.in("user_id", userIds);
            List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
            
            // 4. 一次性查询所有角色
            Set<Long> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toSet());

            // 5. 构建角色映射
            final Map<Long, Role> roleMap;
            if (!roleIds.isEmpty()) {
                QueryWrapper<Role> roleWrapper = new QueryWrapper<>();
                roleWrapper.in("id", roleIds);
                List<Role> roles = roleMapper.selectList(roleWrapper);
                roleMap = roles.stream().collect(Collectors.toMap(Role::getId, r -> r));
            } else {
                roleMap = Collections.emptyMap();
            }

            // 5. 构建用户-角色映射
            Map<Long, List<Role>> userRoleMap = userRoles.stream()
                    .collect(Collectors.groupingBy(
                            UserRole::getUserId,
                            Collectors.mapping(
                                    ur -> roleMap.get(ur.getRoleId()),
                                    Collectors.filtering(Objects::nonNull, Collectors.toList())
                            )
                    ));
            
            // 6. 组装最终数据
            List<Map<String, Object>> userRoleList = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("user", user);
                userMap.put("roles", userRoleMap.getOrDefault(user.getId(), Collections.emptyList()));
                return userMap;
            }).collect(Collectors.toList());
            
            // 7. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", userRoleList);
            result.put("total", userPage.getTotal());
            result.put("pages", userPage.getPages());
            result.put("current", userPage.getCurrent());
            result.put("size", userPage.getSize());
            
            long endTime = System.currentTimeMillis();
            log.info("优化的用户角色查询完成，耗时: {}ms, 用户数: {}", endTime - startTime, users.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("优化的用户角色查询失败", e);
            throw new RuntimeException("查询用户角色信息失败", e);
        }
    }
    
    /**
     * 优化的角色菜单查询（避免N+1问题）
     */
    @Cacheable(value = "roleCache", key = "'role_menus_'.concat(#roleId)")
    @Transactional(readOnly = true)
    public Map<String, Object> getRoleWithMenusOptimized(Long roleId) {
        log.info("执行优化的角色菜单查询: roleId={}", roleId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 查询角色信息
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                return Collections.emptyMap();
            }
            
            // 2. 查询角色菜单关系
            QueryWrapper<RoleMenu> roleMenuWrapper = new QueryWrapper<>();
            roleMenuWrapper.eq("role_id", roleId);
            List<RoleMenu> roleMenus = roleMenuMapper.selectList(roleMenuWrapper);
            
            if (roleMenus.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("role", role);
                result.put("menus", Collections.emptyList());
                return result;
            }
            
            // 3. 一次性查询所有菜单
            Set<Long> menuIds = roleMenus.stream()
                    .map(RoleMenu::getMenuId)
                    .collect(Collectors.toSet());
            
            QueryWrapper<Menu> menuWrapper = new QueryWrapper<>();
            menuWrapper.in("id", menuIds);
            menuWrapper.orderByAsc("sort_order");
            List<Menu> menus = menuMapper.selectList(menuWrapper);
            
            // 4. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("role", role);
            result.put("menus", menus);
            result.put("menuCount", menus.size());
            
            long endTime = System.currentTimeMillis();
            log.info("优化的角色菜单查询完成，耗时: {}ms, 菜单数: {}", endTime - startTime, menus.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("优化的角色菜单查询失败", e);
            throw new RuntimeException("查询角色菜单信息失败", e);
        }
    }
    
    /**
     * 优化的部门树查询（一次性查询所有部门）
     */
    @Cacheable(value = "deptCache", key = "'dept_tree_'.concat(#status)")
    @Transactional(readOnly = true)
    public List<Dept> getDeptTreeOptimized(Integer status) {
        log.info("执行优化的部门树查询: status={}", status);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 一次性查询所有部门
            QueryWrapper<Dept> deptWrapper = new QueryWrapper<>();
            if (status != null) {
                deptWrapper.eq("status", status);
            }
            deptWrapper.orderByAsc("sort_order");
            List<Dept> allDepts = deptMapper.selectList(deptWrapper);
            
            // 2. 构建部门树（内存中处理）
            Map<Long, Dept> deptMap = allDepts.stream()
                    .collect(Collectors.toMap(Dept::getId, d -> d));
            
            // 3. 构建父子关系
            List<Dept> rootDepts = new ArrayList<>();
            for (Dept dept : allDepts) {
                if (dept.getParentId() == 0) {
                    rootDepts.add(dept);
                } else {
                    Dept parent = deptMap.get(dept.getParentId());
                    if (parent != null) {
                        if (parent.getChildren() == null) {
                            parent.setChildren(new ArrayList<>());
                        }
                        parent.getChildren().add(dept);
                    }
                }
            }
            
            long endTime = System.currentTimeMillis();
            log.info("优化的部门树查询完成，耗时: {}ms, 部门数: {}", endTime - startTime, allDepts.size());
            
            return rootDepts;
            
        } catch (Exception e) {
            log.error("优化的部门树查询失败", e);
            throw new RuntimeException("查询部门树失败", e);
        }
    }
    
    /**
     * 优化的审计统计查询（带缓存）
     */
    @Cacheable(value = "auditCache", key = "'audit_stats_'.concat(#days).concat('_').concat(#operationType)")
    @Transactional(readOnly = true)
    public Map<String, Object> getAuditStatisticsOptimized(int days, String operationType) {
        log.info("执行优化的审计统计查询: days={}, operationType={}", days, operationType);
        
        long startTime = System.currentTimeMillis();
        
        try {
            LocalDateTime startTimeParam = LocalDateTime.now().minusDays(days);
            
            QueryWrapper<AuditOperation> wrapper = new QueryWrapper<>();
            wrapper.ge("create_time", startTimeParam);
            
            if (operationType != null && !operationType.trim().isEmpty()) {
                wrapper.eq("operation_type", operationType);
            }
            
            // 1. 查询总记录数
            Long totalCount = auditOperationMapper.selectCount(wrapper);
            
            // 2. 查询成功/失败统计
            wrapper.select("operation_result", "count(*) as count");
            wrapper.groupBy("operation_result");
            List<Map<String, Object>> resultStats = auditOperationMapper.selectMaps(wrapper);
            
            // 3. 查询操作类型统计
            QueryWrapper<AuditOperation> typeWrapper = new QueryWrapper<>();
            typeWrapper.ge("create_time", startTimeParam);
            typeWrapper.select("operation_type", "count(*) as count", "avg(execution_time) as avg_time");
            typeWrapper.groupBy("operation_type");
            List<Map<String, Object>> typeStats = auditOperationMapper.selectMaps(typeWrapper);
            
            // 4. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("resultStats", resultStats);
            result.put("typeStats", typeStats);
            result.put("days", days);
            result.put("queryTime", startTimeParam);
            
            long endTime = System.currentTimeMillis();
            log.info("优化的审计统计查询完成，耗时: {}ms, 总记录数: {}", endTime - startTime, totalCount);
            
            return result;
            
        } catch (Exception e) {
            log.error("优化的审计统计查询失败", e);
            throw new RuntimeException("查询审计统计失败", e);
        }
    }
    
    /**
     * 优化的密钥状态统计查询
     */
    @Cacheable(value = "keyCache", key = "'key_stats_'.concat(#days)")
    @Transactional(readOnly = true)
    public Map<String, Object> getKeyStatisticsOptimized(int days) {
        log.info("执行优化的密钥统计查询: days={}", days);
        
        long startTime = System.currentTimeMillis();
        
        try {
            LocalDateTime startTimeParam = LocalDateTime.now().minusDays(days);
            
            // 1. 查询密钥统计
            QueryWrapper<SecurityKey> wrapper = new QueryWrapper<>();
            wrapper.ge("create_time", startTimeParam);
            wrapper.select("key_type", "status", "count(*) as count",
                         "sum(case when expire_time < now() then 1 else 0 end) as expired_count");
            wrapper.groupBy("key_type", "status");
            List<Map<String, Object>> keyStats = securityKeyMapper.selectMaps(wrapper);
            
            // 2. 查询即将过期的密钥
            LocalDateTime expireSoonTime = LocalDateTime.now().plusDays(7);
            QueryWrapper<SecurityKey> expireWrapper = new QueryWrapper<>();
            expireWrapper.between("expire_time", LocalDateTime.now(), expireSoonTime);
            expireWrapper.eq("status", 1); // 只查询有效的密钥
            Long expireSoonCount = securityKeyMapper.selectCount(expireWrapper);
            
            // 3. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("keyStats", keyStats);
            result.put("expireSoonCount", expireSoonCount);
            result.put("days", days);
            result.put("queryTime", startTimeParam);
            
            long endTime = System.currentTimeMillis();
            log.info("优化的密钥统计查询完成，耗时: {}ms", endTime - startTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("优化的密钥统计查询失败", e);
            throw new RuntimeException("查询密钥统计失败", e);
        }
    }
    
    /**
     * 清理缓存（用于数据更新时）
     */
    public void clearCache(String cacheName) {
        log.info("清理缓存: {}", cacheName);
        // 这里可以集成实际的缓存清理逻辑
        // 例如使用CacheManager来清理特定的缓存
    }
}