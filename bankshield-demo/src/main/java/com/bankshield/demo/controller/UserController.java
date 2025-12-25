package com.bankshield.demo.controller;

import com.bankshield.demo.entity.User;
import com.bankshield.demo.response.ApiResponse;
import com.bankshield.demo.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("创建用户")
    @PostMapping("/create")
    public ApiResponse<User> createUser(
            @ApiParam("用户信息") @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ApiResponse.success(createdUser, "用户创建成功");
        } catch (Exception e) {
            return ApiResponse.error("用户创建失败: " + e.getMessage());
        }
    }

    @ApiOperation("根据ID获取用户")
    @GetMapping("/findById/{id}")
    public ApiResponse<User> findById(
            @ApiParam("用户ID") @PathVariable Long id) {
        try {
            Optional<User> userOptional = userService.findById(id);
            if (userOptional.isPresent()) {
                return ApiResponse.success(userOptional.get(), "用户查询成功");
            } else {
                return ApiResponse.error("用户不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("用户查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("根据用户名获取用户")
    @GetMapping("/findByUsername/{username}")
    public ApiResponse<User> findByUsername(
            @ApiParam("用户名") @PathVariable String username) {
        try {
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isPresent()) {
                return ApiResponse.success(userOptional.get(), "用户查询成功");
            } else {
                return ApiResponse.error("用户不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("用户查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("用户登录验证")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(
            @ApiParam("用户名") @RequestParam String username,
            @ApiParam("密码") @RequestParam String password) {
        try {
            boolean isValid = userService.validateUser(username, password);
            Map<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("valid", isValid);
            
            if (isValid) {
                return ApiResponse.success(result, "登录验证成功");
            } else {
                return ApiResponse.error("用户名或密码错误");
            }
        } catch (Exception e) {
            return ApiResponse.error("登录验证失败: " + e.getMessage());
        }
    }

    @ApiOperation("更新用户")
    @PutMapping("/update/{id}")
    public ApiResponse<User> updateUser(
            @ApiParam("用户ID") @PathVariable Long id,
            @ApiParam("用户信息") @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ApiResponse.success(updatedUser, "用户更新成功");
        } catch (Exception e) {
            return ApiResponse.error("用户更新失败: " + e.getMessage());
        }
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteUser(
            @ApiParam("用户ID") @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功", "用户删除成功");
        } catch (Exception e) {
            return ApiResponse.error("用户删除失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取所有用户")
    @GetMapping("/findAll")
    public ApiResponse<List<User>> findAll() {
        try {
            List<User> users = userService.findAll();
            return ApiResponse.success(users, "用户列表查询成功");
        } catch (Exception e) {
            return ApiResponse.error("用户列表查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取活跃用户")
    @GetMapping("/findActiveUsers")
    public ApiResponse<List<User>> findActiveUsers() {
        try {
            List<User> users = userService.findActiveUsers();
            return ApiResponse.success(users, "活跃用户查询成功");
        } catch (Exception e) {
            return ApiResponse.error("活跃用户查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取系统统计信息")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getSystemStats() {
        try {
            Object stats = userService.getSystemStats();
            Map<String, Object> result = new HashMap<>();
            
            // 手动获取统计信息
            long totalUsers = userService.findAll().size();
            long activeUsers = userService.countActiveUsers();
            long inactiveUsers = totalUsers - activeUsers;
            
            result.put("totalUsers", totalUsers);
            result.put("activeUsers", activeUsers);
            result.put("inactiveUsers", inactiveUsers);
            result.put("activeUserPercentage", totalUsers > 0 ? (activeUsers * 100.0 / totalUsers) : 0);
            
            return ApiResponse.success(result, "系统统计查询成功");
        } catch (Exception e) {
            return ApiResponse.error("系统统计查询失败: " + e.getMessage());
        }
    }

    @ApiOperation("测试数据库连接")
    @GetMapping("/testDatabase")
    public ApiResponse<Map<String, Object>> testDatabase() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 创建一个测试用户
            User testUser = new User();
            testUser.setUsername("test_user_" + System.currentTimeMillis());
            testUser.setEmail("test@example.com");
            testUser.setRealName("测试用户");
            testUser.setStatus(1);
            testUser.setPassword("test123");
            
            // 创建用户
            User createdUser = userService.createUser(testUser);
            
            // 立即查找
            Optional<User> foundUser = userService.findByUsername(createdUser.getUsername());
            
            result.put("userCreated", createdUser.getId() != null);
            result.put("userFound", foundUser.isPresent());
            result.put("databaseWorking", foundUser.isPresent() && 
                       foundUser.get().getUsername().equals(createdUser.getUsername()));
            
            return ApiResponse.success(result, "数据库连接测试完成");
        } catch (Exception e) {
            return ApiResponse.error("数据库连接测试失败: " + e.getMessage());
        }
    }
}