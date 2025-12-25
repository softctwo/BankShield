package com.bankshield.api.service;

import com.bankshield.api.entity.User;
import com.bankshield.api.mapper.UserMapper;
import com.bankshield.api.service.impl.UserServiceImpl;
import com.bankshield.common.core.Result;
import com.bankshield.common.test.BaseUnitTest;
import com.bankshield.common.test.TestDataFactory;
import com.bankshield.common.utils.EncryptUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 * 使用Mockito进行依赖隔离测试
 * 
 * @author BankShield
 */
@DisplayName("用户管理服务单元测试")
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceUnitTest extends BaseUnitTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private LoginAuditService loginAuditService;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    @Order(1)
    @DisplayName("根据ID查询用户-成功")
    void testGetUserByIdSuccess() {
        // Given
        Long userId = 1L;
        User mockUser = TestDataFactory.createTestUser("testuser");
        mockUser.setId(userId);
        
        when(userService.getById(userId)).thenReturn(mockUser);
        
        // When
        Result<User> result = userService.getUserById(userId);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(userId, result.getData().getId());
        assertEquals("testuser", result.getData().getUsername());
        
        verify(userService, times(1)).getById(userId);
    }
    
    @Test
    @Order(2)
    @DisplayName("根据ID查询用户-用户不存在")
    void testGetUserByIdNotFound() {
        // Given
        Long userId = 999L;
        when(userService.getById(userId)).thenReturn(null);
        
        // When
        Result<User> result = userService.getUserById(userId);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户不存在", result.getMessage());
        
        verify(userService, times(1)).getById(userId);
    }
    
    @Test
    @Order(3)
    @DisplayName("分页查询用户列表-无条件")
    void testGetUserPageNoConditions() {
        // Given
        int page = 1;
        int size = 10;
        Page<User> mockPage = new Page<>(page, size);
        List<User> userList = Arrays.asList(
            TestDataFactory.createTestUser("user1"),
            TestDataFactory.createTestUser("user2")
        );
        mockPage.setRecords(userList);
        mockPage.setTotal(2);
        
        when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);
        
        // When
        Result<Page<User>> result = userService.getUserPage(page, size, null, null);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getTotal());
        assertEquals(2, result.getData().getRecords().size());
        
        verify(userService, times(1)).page(any(Page.class), any(LambdaQueryWrapper.class));
    }
    
    @Test
    @Order(4)
    @DisplayName("分页查询用户列表-按用户名模糊查询")
    void testGetUserPageWithUsername() {
        // Given
        int page = 1;
        int size = 10;
        String username = "test";
        
        Page<User> mockPage = new Page<>(page, size);
        List<User> userList = Arrays.asList(
            TestDataFactory.createTestUser("testuser1"),
            TestDataFactory.createTestUser("testuser2")
        );
        mockPage.setRecords(userList);
        mockPage.setTotal(2);
        
        when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);
        when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(2L);
        
        // When
        Result<Page<User>> result = userService.getUserPage(page, size, username, null);
        
        // Then
        assertResultSuccess(result);
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getTotal());
        
        verify(userService, times(1)).page(any(Page.class), any(LambdaQueryWrapper.class));
    }
    
    @Test
    @Order(5)
    @DisplayName("添加用户-成功")
    void testAddUserSuccess() {
        // Given
        User user = TestDataFactory.createTestUser("newuser");
        user.setPassword("Test@123456");
        
        when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userService.save(any(User.class))).thenReturn(true);
        
        // Mock 静态方法 EncryptUtil.bcryptEncrypt
        try (MockedStatic<EncryptUtil> encryptUtil = mockStatic(EncryptUtil.class)) {
            encryptUtil.when(() -> EncryptUtil.bcryptEncrypt(anyString())).thenReturn("encrypted_password");
            
            // When
            Result<String> result = userService.addUser(user);
            
            // Then
            assertResultSuccess(result);
            assertEquals("添加用户成功", result.getMessage());
            
            verify(userService, times(1)).count(any(LambdaQueryWrapper.class));
            verify(userService, times(1)).save(any(User.class));
            encryptUtil.verify(() -> EncryptUtil.bcryptEncrypt(anyString()), times(1));
        }
    }
    
    @Test
    @Order(6)
    @DisplayName("添加用户-用户名为空")
    void testAddUserUsernameEmpty() {
        // Given
        User user = TestDataFactory.createTestUser("");
        user.setUsername("");
        
        // When
        Result<String> result = userService.addUser(user);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户名不能为空", result.getMessage());
        
        verify(userService, never()).count(any(LambdaQueryWrapper.class));
        verify(userService, never()).save(any(User.class));
    }
    
    @Test
    @Order(7)
    @DisplayName("添加用户-密码为空")
    void testAddUserPasswordEmpty() {
        // Given
        User user = TestDataFactory.createTestUser("testuser");
        user.setPassword("");
        
        // When
        Result<String> result = userService.addUser(user);
        
        // Then
        assertResultError(result, 500);
        assertEquals("密码不能为空", result.getMessage());
        
        verify(userService, never()).count(any(LambdaQueryWrapper.class));
        verify(userService, never()).save(any(User.class));
    }
    
    @Test
    @Order(8)
    @DisplayName("添加用户-用户名已存在")
    void testAddUserUsernameExists() {
        // Given
        User user = TestDataFactory.createTestUser("existinguser");
        user.setPassword("Test@123456");
        
        when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);
        
        // When
        Result<String> result = userService.addUser(user);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户名已存在", result.getMessage());
        
        verify(userService, times(1)).count(any(LambdaQueryWrapper.class));
        verify(userService, never()).save(any(User.class));
    }
    
    @Test
    @Order(9)
    @DisplayName("更新用户-成功")
    void testUpdateUserSuccess() {
        // Given
        Long userId = 1L;
        User existingUser = TestDataFactory.createTestUser("existinguser");
        existingUser.setId(userId);
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setName("更新后的姓名");
        updateUser.setPhone("13900000000");
        
        when(userService.getById(userId)).thenReturn(existingUser);
        when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userService.updateById(any(User.class))).thenReturn(true);
        
        // When
        Result<String> result = userService.updateUser(updateUser);
        
        // Then
        assertResultSuccess(result);
        assertEquals("更新用户成功", result.getMessage());
        
        verify(userService, times(1)).getById(userId);
        verify(userService, times(1)).updateById(any(User.class));
    }
    
    @Test
    @Order(10)
    @DisplayName("更新用户-用户不存在")
    void testUpdateUserNotFound() {
        // Given
        Long userId = 999L;
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setName("更新后的姓名");
        
        when(userService.getById(userId)).thenReturn(null);
        
        // When
        Result<String> result = userService.updateUser(updateUser);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户不存在", result.getMessage());
        
        verify(userService, times(1)).getById(userId);
        verify(userService, never()).updateById(any(User.class));
    }
    
    @Test
    @Order(11)
    @DisplayName("更新用户-用户名已存在")
    void testUpdateUserUsernameExists() {
        // Given
        Long userId = 1L;
        User existingUser = TestDataFactory.createTestUser("existinguser");
        existingUser.setId(userId);
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUsername("newusername");
        
        when(userService.getById(userId)).thenReturn(existingUser);
        when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);
        
        // When
        Result<String> result = userService.updateUser(updateUser);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户名已存在", result.getMessage());
        
        verify(userService, times(1)).getById(userId);
        verify(userService, times(1)).count(any(LambdaQueryWrapper.class));
        verify(userService, never()).updateById(any(User.class));
    }
    
    @Test
    @Order(12)
    @DisplayName("更新用户-更新密码")
    void testUpdateUserWithPassword() {
        // Given
        Long userId = 1L;
        User existingUser = TestDataFactory.createTestUser("existinguser");
        existingUser.setId(userId);
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword("NewPassword123");
        
        when(userService.getById(userId)).thenReturn(existingUser);
        when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userService.updateById(any(User.class))).thenReturn(true);
        
        // Mock 静态方法 EncryptUtil.bcryptEncrypt
        try (MockedStatic<EncryptUtil> encryptUtil = mockStatic(EncryptUtil.class)) {
            encryptUtil.when(() -> EncryptUtil.bcryptEncrypt(anyString())).thenReturn("encrypted_new_password");
            
            // When
            Result<String> result = userService.updateUser(updateUser);
            
            // Then
            assertResultSuccess(result);
            
            verify(userService, times(1)).getById(userId);
            verify(userService, times(1)).updateById(any(User.class));
            encryptUtil.verify(() -> EncryptUtil.bcryptEncrypt(eq("NewPassword123")), times(1));
        }
    }
    
    @Test
    @Order(13)
    @DisplayName("删除用户-成功")
    void testDeleteUserSuccess() {
        // Given
        Long userId = 1L;
        User existingUser = TestDataFactory.createTestUser("testuser");
        existingUser.setId(userId);
        
        when(userService.getById(userId)).thenReturn(existingUser);
        when(userService.removeById(userId)).thenReturn(true);
        
        // When
        Result<String> result = userService.deleteUser(userId);
        
        // Then
        assertResultSuccess(result);
        assertEquals("删除用户成功", result.getMessage());
        
        verify(userService, times(1)).getById(userId);
        verify(userService, times(1)).removeById(userId);
    }
    
    @Test
    @Order(14)
    @DisplayName("删除用户-用户不存在")
    void testDeleteUserNotFound() {
        // Given
        Long userId = 999L;
        when(userService.getById(userId)).thenReturn(null);
        
        // When
        Result<String> result = userService.deleteUser(userId);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户不存在", result.getMessage());
        
        verify(userService, times(1)).getById(userId);
        verify(userService, never()).removeById(anyLong());
    }
    
    @Test
    @Order(15)
    @DisplayName("用户登录-成功")
    void testLoginSuccess() {
        // Given
        String username = "testuser";
        String password = "Test@123456";
        String encryptedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO";
        
        User user = TestDataFactory.createTestUser(username);
        user.setPassword(encryptedPassword);
        user.setStatus(1);
        
        when(userService.getOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        
        // Mock 静态方法 EncryptUtil.bcryptCheck
        try (MockedStatic<EncryptUtil> encryptUtil = mockStatic(EncryptUtil.class)) {
            encryptUtil.when(() -> EncryptUtil.bcryptCheck(eq(password), eq(encryptedPassword))).thenReturn(true);
            
            // When
            Result<String> result = userService.login(username, password);
            
            // Then
            assertResultSuccess(result);
            assertEquals("登录成功", result.getMessage());
            
            verify(userService, times(1)).getOne(any(LambdaQueryWrapper.class));
            verify(loginAuditService, times(1)).recordLoginSuccess(
                anyLong(), eq(username), anyString(), anyString(), anyString(), anyString()
            );
        }
    }
    
    @Test
    @Order(16)
    @DisplayName("用户登录-用户不存在")
    void testLoginUserNotFound() {
        // Given
        String username = "nonexistent";
        String password = "Test@123456";
        
        when(userService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        // When
        Result<String> result = userService.login(username, password);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户名或密码错误", result.getMessage());
        
        verify(userService, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(loginAuditService, times(1)).recordLoginFailure(
            eq(username), anyString(), anyString(), anyString(), anyString(), eq("用户名不存在")
        );
    }
    
    @Test
    @Order(17)
    @DisplayName("用户登录-密码错误")
    void testLoginWrongPassword() {
        // Given
        String username = "testuser";
        String password = "WrongPassword";
        String encryptedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO";
        
        User user = TestDataFactory.createTestUser(username);
        user.setPassword(encryptedPassword);
        user.setStatus(1);
        
        when(userService.getOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        
        // Mock 静态方法 EncryptUtil.bcryptCheck
        try (MockedStatic<EncryptUtil> encryptUtil = mockStatic(EncryptUtil.class)) {
            encryptUtil.when(() -> EncryptUtil.bcryptCheck(eq(password), eq(encryptedPassword))).thenReturn(false);
            
            // When
            Result<String> result = userService.login(username, password);
            
            // Then
            assertResultError(result, 500);
            assertEquals("用户名或密码错误", result.getMessage());
            
            verify(userService, times(1)).getOne(any(LambdaQueryWrapper.class));
            verify(loginAuditService, times(1)).recordLoginFailure(
                eq(username), anyString(), anyString(), anyString(), anyString(), eq("密码错误")
            );
        }
    }
    
    @Test
    @Order(18)
    @DisplayName("用户登录-用户被禁用")
    void testLoginUserDisabled() {
        // Given
        String username = "disableduser";
        String password = "Test@123456";
        String encryptedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO";
        
        User user = TestDataFactory.createTestUser(username);
        user.setPassword(encryptedPassword);
        user.setStatus(0); // 禁用状态
        
        when(userService.getOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        
        // Mock 静态方法 EncryptUtil.bcryptCheck
        try (MockedStatic<EncryptUtil> encryptUtil = mockStatic(EncryptUtil.class)) {
            encryptUtil.when(() -> EncryptUtil.bcryptCheck(eq(password), eq(encryptedPassword))).thenReturn(true);
            
            // When
            Result<String> result = userService.login(username, password);
            
            // Then
            assertResultError(result, 500);
            assertEquals("用户已被禁用", result.getMessage());
            
            verify(userService, times(1)).getOne(any(LambdaQueryWrapper.class));
            verify(loginAuditService, times(1)).recordLoginFailure(
                eq(username), anyString(), anyString(), anyString(), anyString(), eq("用户已被禁用")
            );
        }
    }
    
    @Test
    @Order(19)
    @DisplayName("用户登录-用户名为空")
    void testLoginUsernameEmpty() {
        // Given
        String username = "";
        String password = "Test@123456";
        
        // When
        Result<String> result = userService.login(username, password);
        
        // Then
        assertResultError(result, 500);
        assertEquals("用户名不能为空", result.getMessage());
        
        verify(userService, never()).getOne(any(LambdaQueryWrapper.class));
        verify(loginAuditService, never()).recordLoginSuccess(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(loginAuditService, never()).recordLoginFailure(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    @Order(20)
    @DisplayName("用户登录-密码为空")
    void testLoginPasswordEmpty() {
        // Given
        String username = "testuser";
        String password = "";
        
        // When
        Result<String> result = userService.login(username, password);
        
        // Then
        assertResultError(result, 500);
        assertEquals("密码不能为空", result.getMessage());
        
        verify(userService, never()).getOne(any(LambdaQueryWrapper.class));
        verify(loginAuditService, never()).recordLoginSuccess(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(loginAuditService, never()).recordLoginFailure(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
}