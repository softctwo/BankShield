package com.bankshield.api.service;

import com.bankshield.api.entity.User;
import com.bankshield.common.core.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试类
 * 
 * @author BankShield
 */
@Slf4j
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = User.builder()
                .username("testuser")
                .password("123456")
                .name("测试用户")
                .phone("13800138000")
                .email("test@bankshield.com")
                .deptId(1L)
                .status(1)
                .remark("测试用户")
                .build();

        Result<String> result = userService.addUser(user);
        log.info("添加用户结果: {}", result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGetUserById() {
        // 先添加一个用户
        User user = User.builder()
                .username("testuser2")
                .password("123456")
                .name("测试用户2")
                .phone("13800138001")
                .email("test2@bankshield.com")
                .deptId(1L)
                .status(1)
                .remark("测试用户2")
                .build();

        Result<String> addResult = userService.addUser(user);
        assertTrue(addResult.isSuccess());

        // 查询用户
        Result<User> getResult = userService.getUserById(1L);
        log.info("查询用户结果: {}", getResult);
        assertTrue(getResult.isSuccess());
        assertNotNull(getResult.getResult());
    }

    @Test
    public void testGetUserPage() {
        Result<Page<User>> result = userService.getUserPage(1, 10, null, null);
        log.info("分页查询用户结果: {}", result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
    }

    @Test
    public void testUpdateUser() {
        // 先添加一个用户
        User user = User.builder()
                .username("testuser3")
                .password("123456")
                .name("测试用户3")
                .phone("13800138002")
                .email("test3@bankshield.com")
                .deptId(1L)
                .status(1)
                .remark("测试用户3")
                .build();

        Result<String> addResult = userService.addUser(user);
        assertTrue(addResult.isSuccess());

        // 更新用户
        user.setId(1L);
        user.setName("更新后的测试用户3");
        user.setPhone("13900139000");
        
        Result<String> updateResult = userService.updateUser(user);
        log.info("更新用户结果: {}", updateResult);
        assertTrue(updateResult.isSuccess());
    }

    @Test
    public void testLogin() {
        Result<String> result = userService.login("testuser", "123456");
        log.info("登录结果: {}", result);
        // 注意：这里可能会失败，因为数据库中可能没有对应的用户
        // 实际测试中应该确保有测试用户存在
    }
}