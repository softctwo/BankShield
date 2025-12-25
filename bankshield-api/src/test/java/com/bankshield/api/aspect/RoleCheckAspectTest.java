package com.bankshield.api.aspect;

import com.bankshield.api.annotation.RoleExclusive;
import com.bankshield.api.entity.RoleMutex;
import com.bankshield.api.service.RoleCheckService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * RoleCheckAspect 单元测试
 * 测试空指针异常修复
 */
public class RoleCheckAspectTest {

    @Mock
    private RoleCheckService roleCheckService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private RoleCheckAspect roleCheckAspect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleCheckAspect = new RoleCheckAspect(roleCheckService);
    }

    @Test
    void testHandleRoleAssignmentCheck_WithEmptyConflicts_ShouldNotThrowException() throws Exception {
        // 准备测试数据
        Long userId = 1L;
        String roleCode = "ADMIN";
        
        // 模拟方法参数
        Object[] args = new Object[]{userId, roleCode};
        
        // 模拟方法签名
        Method method = TestClass.class.getMethod("testMethod", Long.class, String.class);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(args);
        
        // 模拟角色互斥检查返回false（有冲突）
        when(roleCheckService.checkRoleAssignment(userId, roleCode)).thenReturn(false);
        
        // 模拟返回空的冲突列表（这是关键测试点）
        when(roleCheckService.checkUserRoleConflicts(userId)).thenReturn(new ArrayList<>());
        
        // 创建注解实例
        RoleExclusive roleExclusive = mock(RoleExclusive.class);
        when(roleExclusive.checkType()).thenReturn(RoleExclusive.CheckType.ASSIGN);
        when(roleExclusive.forceCheck()).thenReturn(false);
        
        // 执行测试 - 应该不抛出异常
        assertDoesNotThrow(() -> roleCheckAspect.doRoleCheck(joinPoint));
    }

    @Test
    void testHandleRoleAssignmentCheck_WithConflicts_ShouldHandleProperly() throws Exception {
        // 准备测试数据
        Long userId = 1L;
        String roleCode = "ADMIN";
        
        // 模拟方法参数
        Object[] args = new Object[]{userId, roleCode};
        
        // 模拟方法签名
        Method method = TestClass.class.getMethod("testMethod", Long.class, String.class);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(args);
        
        // 模拟角色互斥检查返回false（有冲突）
        when(roleCheckService.checkRoleAssignment(userId, roleCode)).thenReturn(false);
        
        // 模拟返回有内容的冲突列表
        List<RoleMutex> conflicts = new ArrayList<>();
        RoleMutex mutex = new RoleMutex();
        mutex.setRoleCode("ADMIN");
        mutex.setMutexRoleCode("AUDITOR");
        conflicts.add(mutex);
        
        when(roleCheckService.checkUserRoleConflicts(userId)).thenReturn(conflicts);
        
        // 创建注解实例
        RoleExclusive roleExclusive = mock(RoleExclusive.class);
        when(roleExclusive.checkType()).thenReturn(RoleExclusive.CheckType.ASSIGN);
        when(roleExclusive.forceCheck()).thenReturn(false);
        
        // 执行测试 - 应该不抛出异常
        assertDoesNotThrow(() -> roleCheckAspect.doRoleCheck(joinPoint));
        
        // 验证记录违规方法被调用
        verify(roleCheckService).recordRoleViolation(eq(userId), eq(roleCode), 
                eq("AUDITOR"), eq(1), isNull(), eq("system"));
    }

    // 测试用的辅助类
    public static class TestClass {
        public void testMethod(Long userId, String roleCode) {
            // 测试方法
        }
    }
}