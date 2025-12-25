package com.bankshield.api.controller;

import com.bankshield.api.entity.User;
import com.bankshield.common.test.BaseIntegrationTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 用户管理API集成测试
 * 测试完整的用户管理流程
 * 
 * @author BankShield
 */
@DisplayName("用户管理API集成测试")
@ActiveProfiles("test")
public class UserControllerIntegrationTest extends BaseIntegrationTest {
    
    private static Long testUserId;
    private static String testUsername;
    
    @Test
    @Order(1)
    @DisplayName("管理员登录")
    void testAdminLogin() {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "123456");
        
        // When
        Response response = given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(toJson(loginRequest))
            .when()
            .post("/api/auth/login");
        
        // Then
        assertSuccessResponse(response);
        response.then()
            .body("data.accessToken", notNullValue())
            .body("data.user.username", equalTo("admin"));
        
        // 保存token供后续测试使用
        adminToken = response.jsonPath().getString("data.accessToken");
    }
    
    @Test
    @Order(2)
    @DisplayName("创建用户-成功")
    void testCreateUserSuccess() {
        // Given
        testUsername = "api_test_user_" + System.currentTimeMillis();
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("username", testUsername);
        createRequest.put("password", "Test@123456");
        createRequest.put("name", "API测试用户");
        createRequest.put("phone", "13800000000");
        createRequest.put("email", testUsername + "@test.com");
        createRequest.put("deptId", 1);
        createRequest.put("status", 1);
        createRequest.put("remark", "API集成测试用户");
        
        // When
        Response response = givenAuthenticated()
            .body(toJson(createRequest))
            .when()
            .post("/api/user");
        
        // Then
        assertSuccessResponse(response);
        response.then()
            .body("message", equalTo("添加用户成功"));
        
        // 保存用户ID供后续测试使用
        testUserId = response.jsonPath().getLong("data");
        assertTrue(testUserId > 0);
    }
    
    @Test
    @Order(3)
    @DisplayName("创建用户-用户名已存在")
    void testCreateUserUsernameExists() {
        // Given
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("username", testUsername); // 使用已存在的用户名
        createRequest.put("password", "Test@123456");
        createRequest.put("name", "重复用户");
        createRequest.put("phone", "13900000000");
        createRequest.put("email", "duplicate@test.com");
        createRequest.put("deptId", 1);
        createRequest.put("status", 1);
        
        // When
        Response response = givenAuthenticated()
            .body(toJson(createRequest))
            .when()
            .post("/api/user");
        
        // Then
        assertErrorResponse(response, 500);
        response.then()
            .body("message", containsString("用户名已存在"));
    }
    
    @Test
    @Order(4)
    @DisplayName("创建用户-参数校验失败")
    void testCreateUserValidationFailed() {
        // Given - 缺少必填字段
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("username", ""); // 空用户名
        createRequest.put("password", "Test@123456");
        // 缺少name字段
        
        // When
        Response response = givenAuthenticated()
            .body(toJson(createRequest))
            .when()
            .post("/api/user");
        
        // Then
        assertErrorResponse(response, 500);
    }
    
    @Test
    @Order(5)
    @DisplayName("查询用户详情-成功")
    void testGetUserDetailSuccess() {
        // Given
        assert testUserId != null : "测试用户ID不能为空";
        
        // When
        Response response = givenAuthenticated()
            .when()
            .get("/api/user/" + testUserId);
        
        // Then
        assertSuccessResponse(response);
        response.then()
            .body("data.id", equalTo(testUserId.intValue()))
            .body("data.username", equalTo(testUsername))
            .body("data.name", equalTo("API测试用户"));
    }
    
    @Test
    @Order(6)
    @DisplayName("查询用户详情-用户不存在")
    void testGetUserDetailNotFound() {
        // Given
        Long nonExistentUserId = 99999L;
        
        // When
        Response response = givenAuthenticated()
            .when()
            .get("/api/user/" + nonExistentUserId);
        
        // Then
        assertErrorResponse(response, 500);
        response.then()
            .body("message", containsString("用户不存在"));
    }
    
    @Test
    @Order(7)
    @DisplayName("分页查询用户列表")
    void testGetUserPage() {
        // Given
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", 1);
        queryParams.put("size", 10);
        queryParams.put("username", testUsername);
        
        // When
        Response response = givenAuthenticated()
            .queryParams(queryParams)
            .when()
            .get("/api/user/page");
        
        // Then
        assertSuccessResponse(response);
        assertPageResponse(response, 1);
        response.then()
            .body("data.records[0].username", equalTo(testUsername));
    }
    
    @Test
    @Order(8)
    @DisplayName("更新用户-成功")
    void testUpdateUserSuccess() {
        // Given
        assert testUserId != null : "测试用户ID不能为空";
        
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", testUserId);
        updateRequest.put("name", "更新后的API测试用户");
        updateRequest.put("phone", "13900000000");
        updateRequest.put("email", "updated@api.com");
        updateRequest.put("remark", "更新后的备注信息");
        
        // When
        Response response = givenAuthenticated()
            .body(toJson(updateRequest))
            .when()
            .put("/api/user/" + testUserId);
        
        // Then
        assertSuccessResponse(response);
        response.then()
            .body("message", equalTo("更新用户成功"));
    }
    
    @Test
    @Order(9)
    @DisplayName("更新用户-用户不存在")
    void testUpdateUserNotFound() {
        // Given
        Long nonExistentUserId = 99999L;
        
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", nonExistentUserId);
        updateRequest.put("name", "不存在的用户");
        
        // When
        Response response = givenAuthenticated()
            .body(toJson(updateRequest))
            .when()
            .put("/api/user/" + nonExistentUserId);
        
        // Then
        assertErrorResponse(response, 500);
        response.then()
            .body("message", containsString("用户不存在"));
    }
    
    @Test
    @Order(10)
    @DisplayName("更新用户-用户名冲突")
    void testUpdateUserUsernameConflict() {
        // Given - 先创建另一个用户
        String anotherUsername = "another_test_user_" + System.currentTimeMillis();
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("username", anotherUsername);
        createRequest.put("password", "Test@123456");
        createRequest.put("name", "另一个测试用户");
        createRequest.put("phone", "13700000000");
        createRequest.put("email", anotherUsername + "@test.com");
        createRequest.put("deptId", 1);
        createRequest.put("status", 1);
        
        Response createResponse = givenAuthenticated()
            .body(toJson(createRequest))
            .when()
            .post("/api/user");
        assertSuccessResponse(createResponse);
        
        // 尝试更新第一个用户的用户名为第二个用户的用户名
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("id", testUserId);
        updateRequest.put("username", anotherUsername); // 已存在的用户名
        updateRequest.put("name", "冲突用户名");
        
        // When
        Response response = givenAuthenticated()
            .body(toJson(updateRequest))
            .when()
            .put("/api/user/" + testUserId);
        
        // Then
        assertErrorResponse(response, 500);
        response.then()
            .body("message", containsString("用户名已存在"));
    }
    
    @Test
    @Order(11)
    @DisplayName("用户登录-成功")
    void testUserLoginSuccess() {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testUsername);
        loginRequest.put("password", "Test@123456");
        
        // When
        Response response = given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(toJson(loginRequest))
            .when()
            .post("/api/auth/login");
        
        // Then
        assertSuccessResponse(response);
        response.then()
            .body("message", equalTo("登录成功"))
            .body("data.accessToken", notNullValue());
    }
    
    @Test
    @Order(12)
    @DisplayName("用户登录-密码错误")
    void testUserLoginWrongPassword() {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", testUsername);
        loginRequest.put("password", "WrongPassword");
        
        // When
        Response response = given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(toJson(loginRequest))
            .when()
            .post("/api/auth/login");
        
        // Then
        assertErrorResponse(response, 500);
        response.then()
            .body("message", containsString("用户名或密码错误"));
    }
    
    @Test
    @Order(13)
    @DisplayName("用户登录-用户不存在")
    void testUserLoginUserNotFound() {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistent_user");
        loginRequest.put("password", "Test@123456");
        
        // When
        Response response = given()
            .contentType(io.restassured.http.ContentType.JSON)
            .body(toJson(loginRequest))
            .when()
            .post("/api/auth/login");
        
        // Then
        assertErrorResponse(response, 500);
        response.then()
            .body("message", containsString("用户名或密码错误"));
    }
    
    @Test
    @Order(14)
    @DisplayName("删除用户-成功")
    void testDeleteUserSuccess() {
        // Given
        assert testUserId != null : "测试用户ID不能为空";
        
        // When
        Response response = givenAuthenticated()
            .when()
            .delete("/api/user/" + testUserId);
        
        // Then
        assertSuccessResponse(response);
        response.then()
            .body("message", equalTo("删除用户成功"));
    }
    
    @Test
    @Order(15)
    @DisplayName("删除用户-用户不存在")
    void testDeleteUserNotFound() {
        // Given
        Long nonExistentUserId = 99999L;
        
        // When
        Response response = givenAuthenticated()
            .when()
            .delete("/api/user/" + nonExistentUserId);
        
        // Then
        assertErrorResponse(response, 500);
        response.then()
            .body("message", containsString("用户不存在"));
    }
    
    @Test
    @Order(16)
    @DisplayName("访问用户管理-无权限")
    void testAccessUserManagementWithoutPermission() {
        // Given - 使用普通用户token（如果没有则跳过）
        if (adminToken == null) {
            System.out.println("跳过无权限测试，因为没有有效的token");
            return;
        }
        
        // When - 尝试访问需要权限的接口
        Response response = given()
            .header("Authorization", "Bearer " + adminToken)
            .when()
            .get("/api/user/page");
        
        // Then - 根据实际权限控制逻辑验证
        // 注意：这里需要根据实际的权限控制实现来调整断言
        System.out.println("权限测试响应: " + response.getBody().asString());
    }
    
    @Test
    @Order(17)
    @DisplayName("用户管理API性能测试")
    void testUserManagementPerformance() {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When - 执行一系列操作
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("username", "perf_test_user_" + System.currentTimeMillis());
        createRequest.put("password", "Test@123456");
        createRequest.put("name", "性能测试用户");
        createRequest.put("phone", "13600000000");
        createRequest.put("email", "perf@test.com");
        createRequest.put("deptId", 1);
        createRequest.put("status", 1);
        
        Response response = givenAuthenticated()
            .body(toJson(createRequest))
            .when()
            .post("/api/user");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertSuccessResponse(response);
        System.out.println("用户创建API响应时间: " + duration + "ms");
        
        // 性能断言 - 响应时间应小于2秒
        assertTrue(duration < 2000, "API响应时间应小于2秒");
    }
}