package com.bankshield.common;

import com.bankshield.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * 集成测试基类
 * 提供API测试的通用方法
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    
    @LocalServerPort
    protected int port;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    protected String baseUrl;
    protected String adminToken;
    protected String normalUserToken;
    
    @BeforeAll
    public void setUp() {
        RestAssured.port = port;
        this.baseUrl = "http://localhost:" + port + "/api";
        
        // 准备测试数据
        prepareTestData();
    }
    
    /**
     * 准备测试数据
     */
    private void prepareTestData() {
        // 创建管理员用户并获取token
        this.adminToken = login("admin", "123456");
        
        // 创建普通用户并获取token
        this.normalUserToken = login("user", "123456");
    }
    
    /**
     * 登录获取Token
     */
    protected String login(String username, String password) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);
        
        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginData)
                .when()
                .post("/api/auth/login");
        
        response.then().statusCode(200);
        
        // 提取token
        String token = response.jsonPath().getString("data.accessToken");
        return "Bearer " + token;
    }
    
    /**
     * 发送带认证头的GET请求
     */
    protected Response getWithAuth(String path, String token) {
        return given()
                .header("Authorization", token)
                .when()
                .get(path);
    }
    
    /**
     * 发送带认证头的POST请求
     */
    protected Response postWithAuth(String path, Object body, String token) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .when()
                .post(path);
    }
    
    /**
     * 发送带认证头的PUT请求
     */
    protected Response putWithAuth(String path, Object body, String token) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .when()
                .put(path);
    }
    
    /**
     * 发送带认证头的DELETE请求
     */
    protected Response deleteWithAuth(String path, String token) {
        return given()
                .header("Authorization", token)
                .when()
                .delete(path);
    }
    
    /**
     * 验证响应成功
     */
    protected void assertResponseSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("code", org.hamcrest.Matchers.equalTo(200))
                .body("success", org.hamcrest.Matchers.equalTo(true));
    }
    
    /**
     * 验证响应错误
     */
    protected void assertResponseError(Response response, int expectedCode) {
        response.then()
                .statusCode(expectedCode)
                .body("code", org.hamcrest.Matchers.equalTo(expectedCode))
                .body("success", org.hamcrest.Matchers.equalTo(false));
    }
    
    /**
     * 获取响应中的数据
     */
    protected <T> T getResponseData(Response response, String path) {
        return response.jsonPath().getObject(path, (Class<T>) Object.class);
    }
    
    /**
     * 等待一段时间（用于异步操作测试）
     */
    protected void waitForAsync(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}