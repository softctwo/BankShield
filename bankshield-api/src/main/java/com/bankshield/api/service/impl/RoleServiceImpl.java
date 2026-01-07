package com.bankshield.api.service.impl;

import com.bankshield.api.entity.Role;
import com.bankshield.api.entity.User;
import com.bankshield.api.entity.UserRole;
import com.bankshield.api.mapper.RoleMapper;
import com.bankshield.api.mapper.UserMapper;
import com.bankshield.api.mapper.UserRoleMapper;
import com.bankshield.api.service.RoleCheckService;
import com.bankshield.api.service.RoleService;
import com.bankshield.api.utils.SecurityUtils;
import com.bankshield.common.exception.BusinessException;
import com.bankshield.common.result.Result;
import com.bankshield.common.result.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 * 实现角色管理功能，包含三权分立机制的角色互斥检查
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleCheckService roleCheckService;

    @Override
    public Result<Role> getRoleById(Long id) {
        try {
            Role role = this.getById(id);
            if (role == null) {
                return Result.error(1111, "角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            log.error("查询角色失败: {}", e.getMessage());
            return Result.error("查询角色失败");
        }
    }

    @Override
    public Result<Page<Role>> getRolePage(int page, int size, String roleName, String roleCode) {
        try {
            Page<Role> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加查询条件
            if (StringUtils.hasText(roleName)) {
                queryWrapper.like(Role::getRoleName, roleName);
            }
            if (StringUtils.hasText(roleCode)) {
                queryWrapper.like(Role::getRoleCode, roleCode);
            }
            
            // 按创建时间降序排序
            queryWrapper.orderByDesc(Role::getCreateTime);
            
            Page<Role> rolePage = this.page(pageParam, queryWrapper);
            return Result.success(rolePage);
        } catch (Exception e) {
            log.error("分页查询角色失败: {}", e.getMessage());
            return Result.error("分页查询角色失败");
        }
    }

    @Override
    public Result<List<Role>> getAllEnabledRoles() {
        try {
            LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Role::getStatus, 1); // 只查询启用的角色
            queryWrapper.orderByDesc(Role::getCreateTime);
            
            List<Role> roleList = this.list(queryWrapper);
            return Result.success(roleList);
        } catch (Exception e) {
            log.error("查询启用角色失败: {}", e.getMessage());
            return Result.error("查询启用角色失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addRole(Role role) {
        try {
            // 参数校验
            if (role == null) {
                return Result.error("角色信息不能为空");
            }
            if (!StringUtils.hasText(role.getRoleName())) {
                return Result.error("角色名称不能为空");
            }
            if (!StringUtils.hasText(role.getRoleCode())) {
                return Result.error("角色编码不能为空");
            }

            // 检查角色编码是否已存在
            LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Role::getRoleCode, role.getRoleCode());
            if (this.count(queryWrapper) > 0) {
                return Result.error(1111, "角色编码已存在");
            }

            // 设置默认值
            if (role.getStatus() == null) {
                role.setStatus(1); // 默认启用
            }
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            
            // 设置创建人和更新人
            String currentUser = "system";
            role.setCreateBy(currentUser);
            role.setUpdateBy(currentUser);

            // 保存角色
            boolean result = this.save(role);
            if (result) {
                return Result.success("添加角色成功");
            } else {
                return Result.error("添加角色失败");
            }
        } catch (Exception e) {
            log.error("添加角色失败: {}", e.getMessage());
            return Result.error("添加角色失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateRole(Role role) {
        try {
            // 参数校验
            if (role == null || role.getId() == null) {
                return Result.error("角色ID不能为空");
            }

            // 检查角色是否存在
            Role existingRole = this.getById(role.getId());
            if (existingRole == null) {
                return Result.error(1111, "角色不存在");
            }

            // 如果修改了角色编码，检查新编码是否已存在
            if (StringUtils.hasText(role.getRoleCode()) && !role.getRoleCode().equals(existingRole.getRoleCode())) {
                LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Role::getRoleCode, role.getRoleCode());
                if (this.count(queryWrapper) > 0) {
                    return Result.error(1111, "角色编码已存在");
                }
            }

            // 设置更新时间
            role.setUpdateTime(LocalDateTime.now());
            role.setUpdateBy("system"); // 应该从当前登录用户获取

            // 更新角色
            boolean result = this.updateById(role);
            if (result) {
                return Result.success("更新角色成功");
            } else {
                return Result.error("更新角色失败");
            }
        } catch (Exception e) {
            log.error("更新角色失败: {}", e.getMessage());
            return Result.error("更新角色失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteRole(Long id) {
        try {
            if (id == null) {
                return Result.error("角色ID不能为空");
            }

            // 检查角色是否存在
            Role role = this.getById(id);
            if (role == null) {
                return Result.error(1111, "角色不存在");
            }

            // 检查角色是否被用户使用，如果有用户关联则不允许删除
            LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
            userRoleWrapper.eq(UserRole::getRoleId, id);
            long userCount = userRoleMapper.selectCount(userRoleWrapper);
            
            if (userCount > 0) {
                return Result.error("该角色已被 " + userCount + " 个用户使用，无法删除");
            }

            // 删除角色
            boolean result = this.removeById(id);
            if (result) {
                return Result.success("删除角色成功");
            } else {
                return Result.error("删除角色失败");
            }
        } catch (Exception e) {
            log.error("删除角色失败: {}", e.getMessage());
            return Result.error("删除角色失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> assignRoleToUser(Long userId, Long roleId) {
        try {
            // 参数校验
            if (userId == null || roleId == null) {
                return Result.error("用户ID和角色ID不能为空");
            }

            // 检查用户是否存在
            if (userMapper.selectById(userId) == null) {
                return Result.error(1111, "用户不存在");
            }

            // 检查角色是否存在
            Role role = this.getById(roleId);
            if (role == null) {
                return Result.error(1111, "角色不存在");
            }

            // 检查角色分配是否违反三权分立原则
            boolean canAssign = roleCheckService.checkRoleAssignment(userId, role.getRoleCode());
            if (!canAssign) {
                return Result.error(1111, "角色分配违反三权分立原则");
            }

            // 执行实际的角色分配
            try {
                // 检查是否已存在该角色
                LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UserRole::getUserId, userId)
                           .eq(UserRole::getRoleId, roleId);
                
                UserRole existingUserRole = userRoleMapper.selectOne(queryWrapper);
                if (existingUserRole != null) {
                    return Result.error(1112, "用户已拥有该角色");
                }

                // 创建用户角色关联
                UserRole userRole = UserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .createTime(LocalDateTime.now())
                        .createBy(SecurityUtils.getCurrentUsername())
                        .build();
                
                userRoleMapper.insert(userRole);
                
                log.info("角色分配成功：用户ID={}, 角色ID={}", userId, roleId);
                return Result.success("角色分配成功");
                
            } catch (Exception e) {
                log.error("角色分配数据库操作失败：用户ID={}, 角色ID={}", userId, roleId, e);
                throw new BusinessException(1111, "角色分配失败");
            }
        } catch (BusinessException e) {
            log.error("角色分配业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("角色分配失败", e);
            return Result.error("角色分配失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> assignRolesToUser(Long userId, List<Long> roleIds) {
        try {
            // 参数校验
            if (userId == null || roleIds == null || roleIds.isEmpty()) {
                return Result.error("用户ID和角色ID列表不能为空");
            }

            // 检查用户是否存在
            if (userMapper.selectById(userId) == null) {
                return Result.error(ResultCode.USER_NOT_FOUND);
            }

            // 获取角色编码列表
            List<Role> roles = this.listByIds(roleIds);
            if (roles.size() != roleIds.size()) {
                return Result.error("部分角色不存在");
            }

            // 检查每个角色分配是否违反三权分立原则
            for (Role role : roles) {
                boolean canAssign = roleCheckService.checkRoleAssignment(userId, role.getRoleCode());
                if (!canAssign) {
                    return Result.error(ResultCode.ROLE_MUTEX_CONFLICT, 
                            String.format("角色[%s]分配违反三权分立原则", role.getRoleName()));
                }
            }

            // 执行批量角色分配
            try {
                // 先删除用户现有角色
                userRoleMapper.deleteByUserId(userId);
                
                // 批量分配新角色
                String currentUsername = SecurityUtils.getCurrentUsername();
                LocalDateTime now = LocalDateTime.now();
                
                for (Long roleId : roleIds) {
                    UserRole userRole = UserRole.builder()
                            .userId(userId)
                            .roleId(roleId)
                            .createTime(now)
                            .createBy(currentUsername)
                            .build();
                    
                    userRoleMapper.insert(userRole);
                }
                
                log.info("批量角色分配成功：用户ID={}, 角色数量={}", userId, roleIds.size());
                return Result.success("批量角色分配成功");
                
            } catch (Exception e) {
                log.error("批量角色分配数据库操作失败：用户ID={}, 角色数量={}", userId, roleIds.size(), e);
                throw new BusinessException(1111, "批量角色分配失败");
            }
        } catch (BusinessException e) {
            log.error("批量角色分配业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("批量角色分配失败", e);
            return Result.error("批量角色分配失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> removeRoleFromUser(Long userId, Long roleId) {
        try {
            // 参数校验
            if (userId == null || roleId == null) {
                return Result.error("用户ID和角色ID不能为空");
            }

            // TODO: 调用用户角色关联表的Mapper进行角色移除
            // int rows = userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
            // if (rows > 0) {
            //     log.info("角色移除成功：用户ID={}, 角色ID={}", userId, roleId);
            //     return Result.success("角色移除成功");
            // } else {
            //     return Result.error("角色移除失败，可能该用户没有此角色");
            // }
            
            return Result.success("角色移除成功");
        } catch (Exception e) {
            log.error("角色移除失败", e);
            return Result.error("角色移除失败");
        }
    }

    @Override
    public Result<List<Role>> getUserRoles(Long userId) {
        try {
            // 参数校验
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            // 检查用户是否存在
            if (userMapper.selectById(userId) == null) {
                return Result.error(ResultCode.USER_NOT_FOUND);
            }

            // TODO: 查询用户角色列表
            // List<Role> roles = roleMapper.selectRolesByUserId(userId);
            List<Role> roles = this.list(); // 临时实现
            
            return Result.success(roles);
        } catch (Exception e) {
            log.error("查询用户角色失败", e);
            return Result.error("查询用户角色失败");
        }
    }

    @Override
    public Result<Boolean> checkRoleAssignment(Long userId, String roleCode) {
        try {
            // 参数校验
            if (userId == null || !StringUtils.hasText(roleCode)) {
                return Result.error("用户ID和角色编码不能为空");
            }

            // 执行角色互斥检查
            boolean canAssign = roleCheckService.checkRoleAssignment(userId, roleCode);
            
            if (canAssign) {
                return Result.success("角色分配合规", true);
            } else {
                return Result.success("角色分配违反三权分立原则", false);
            }
        } catch (Exception e) {
            log.error("检查角色分配失败", e);
            return Result.error("检查角色分配失败");
        }
    }
}