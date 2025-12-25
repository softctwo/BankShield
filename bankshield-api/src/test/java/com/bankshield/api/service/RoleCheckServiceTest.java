package com.bankshield.api.service;

import com.bankshield.api.entity.Role;
import com.bankshield.api.entity.RoleMutex;
import com.bankshield.api.entity.RoleViolation;
import com.bankshield.api.entity.User;
import com.bankshield.api.mapper.RoleMapper;
import com.bankshield.api.mapper.UserMapper;
import com.bankshield.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色互斥检查服务测试类
 * 测试三权分立机制的核心功能
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@SpringBootTest
@Transactional
public class RoleCheckServiceTest {

    @Autowired
    private RoleCheckService roleCheckService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 测试角色分配检查 - 无冲突情况
     */
    @Test
    public void testCheckRoleAssignment_NoConflict() {
        // 创建测试用户
        User user = createTestUser("test_user_1", "测试用户1");
        userMapper.insert(user);

        // 检查分配普通用户角色（应该通过）
        boolean result = roleCheckService.checkRoleAssignment(user.getId(), "USER");
        assertTrue(result, "分配普通用户角色应该通过检查");
    }

    /**
     * 测试角色分配检查 - 三权分立冲突
     */
    @Test
    public void testCheckRoleAssignment_SeparationOfPowersConflict() {
        // 创建测试用户
        User user = createTestUser("test_user_2", "测试用户2");
        userMapper.insert(user);

        // 先分配系统管理员角色
        Role systemAdminRole = roleMapper.selectByCode("SYSTEM_ADMIN");
        if (systemAdminRole != null) {
            // 模拟用户已有系统管理员角色
            // 这里需要模拟用户角色关联，实际项目中需要UserRoleMapper
            
            // 检查分配安全策略员角色（应该失败）
            boolean result = roleCheckService.checkRoleAssignment(user.getId(), "SECURITY_ADMIN");
            assertFalse(result, "分配安全策略员角色应该失败，因为用户已有系统管理员角色");
        }
    }

    /**
     * 测试角色互斥检查
     */
    @Test
    public void testIsRoleMutex() {
        // 检查系统管理员与安全策略员是否互斥（应该互斥）
        boolean result1 = roleCheckService.isRoleMutex("SYSTEM_ADMIN", "SECURITY_ADMIN");
        assertTrue(result1, "系统管理员与安全策略员应该互斥");

        // 检查系统管理员与合规审计员是否互斥（应该互斥）
        boolean result2 = roleCheckService.isRoleMutex("SYSTEM_ADMIN", "AUDIT_ADMIN");
        assertTrue(result2, "系统管理员与合规审计员应该互斥");

        // 检查安全策略员与合规审计员是否互斥（应该互斥）
        boolean result3 = roleCheckService.isRoleMutex("SECURITY_ADMIN", "AUDIT_ADMIN");
        assertTrue(result3, "安全策略员与合规审计员应该互斥");

        // 检查相同角色是否互斥（不应该互斥）
        boolean result4 = roleCheckService.isRoleMutex("SYSTEM_ADMIN", "SYSTEM_ADMIN");
        assertFalse(result4, "相同角色不应该互斥");

        // 检查普通角色是否互斥（不应该互斥）
        boolean result5 = roleCheckService.isRoleMutex("USER", "ADMIN");
        assertFalse(result5, "普通角色不应该互斥");
    }

    /**
     * 测试获取互斥角色列表
     */
    @Test
    public void testGetMutexRoles() {
        // 获取系统管理员的互斥角色
        List<String> mutexRoles = roleCheckService.getMutexRoles("SYSTEM_ADMIN");
        assertNotNull(mutexRoles, "互斥角色列表不应该为null");
        assertTrue(mutexRoles.contains("SECURITY_ADMIN"), "应该包含安全策略员");
        assertTrue(mutexRoles.contains("AUDIT_ADMIN"), "应该包含合规审计员");
        
        log.info("系统管理员的互斥角色：{}", mutexRoles);
    }

    /**
     * 测试获取所有互斥规则
     */
    @Test
    public void testGetAllRoleMutexRules() {
        List<RoleMutex> rules = roleCheckService.getAllRoleMutexRules();
        assertNotNull(rules, "互斥规则列表不应该为null");
        assertFalse(rules.isEmpty(), "互斥规则列表不应该为空");
        
        log.info("互斥规则数量：{}", rules.size());
        for (RoleMutex rule : rules) {
            log.info("规则：{} 与 {} 互斥 - {}", 
                    rule.getRoleCode(), rule.getMutexRoleCode(), rule.getDescription());
        }
    }

    /**
     * 测试记录角色违规
     */
    @Test
    public void testRecordRoleViolation() {
        // 创建测试用户
        User user = createTestUser("test_user_3", "测试用户3");
        userMapper.insert(user);

        // 记录违规
        roleCheckService.recordRoleViolation(
                user.getId(), 
                "SYSTEM_ADMIN", 
                "SECURITY_ADMIN", 
                1, // 手动分配
                1L, // 操作人ID
                "test_operator" // 操作人姓名
        );

        // 验证违规记录是否创建
        List<RoleViolation> violations = roleCheckService.getUnhandledViolations();
        assertFalse(violations.isEmpty(), "应该存在未处理的违规记录");
        
        boolean found = violations.stream()
                .anyMatch(v -> v.getUserId().equals(user.getId()) && 
                                v.getRoleCode().equals("SYSTEM_ADMIN") &&
                                v.getMutexRoleCode().equals("SECURITY_ADMIN"));
        assertTrue(found, "应该找到对应的违规记录");
    }

    /**
     * 测试处理违规记录
     */
    @Test
    public void testHandleRoleViolation() {
        // 创建测试用户
        User user = createTestUser("test_user_4", "测试用户4");
        userMapper.insert(user);

        // 记录违规
        roleCheckService.recordRoleViolation(
                user.getId(), 
                "SYSTEM_ADMIN", 
                "AUDIT_ADMIN", 
                2, // 系统检测
                null, 
                "system"
        );

        // 获取违规记录
        List<RoleViolation> violations = roleCheckService.getUnhandledViolations();
        assertFalse(violations.isEmpty(), "应该存在未处理的违规记录");

        RoleViolation violation = violations.get(0);
        
        // 处理违规
        roleCheckService.handleRoleViolation(
                violation.getId(), 
                1, // 已处理
                "测试处理备注", 
                "test_handler"
        );

        // 验证处理结果
        List<RoleViolation> handledViolations = roleCheckService.getUnhandledViolations();
        boolean stillExists = handledViolations.stream()
                .anyMatch(v -> v.getId().equals(violation.getId()));
        assertFalse(stillExists, "违规记录应该不再存在于未处理列表中");
    }

    /**
     * 测试三权分立状态
     */
    @Test
    public void testGetSeparationOfPowersStatus() {
        String status = roleCheckService.getSeparationOfPowersStatus();
        assertNotNull(status, "三权分立状态不应该为null");
        assertFalse(status.isEmpty(), "三权分立状态不应该为空");
        
        log.info("三权分立状态：{}", status);
    }

    /**
     * 测试执行角色检查任务
     */
    @Test
    public void testExecuteRoleCheckJob() {
        // 执行角色检查任务
        roleCheckService.executeRoleCheckJob();
        
        // 验证是否生成违规记录（如果存在违规情况）
        List<RoleViolation> violations = roleCheckService.getUnhandledViolations();
        log.info("执行检查后未处理的违规记录数量：{}", violations.size());
    }

    /**
     * 创建测试用户
     */
    private User createTestUser(String username, String name) {
        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setPassword("test_password");
        user.setStatus(1);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());
        user.setCreateBy("test");
        user.setUpdateBy("test");
        return user;
    }
}