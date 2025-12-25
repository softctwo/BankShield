package com.bankshield.auth;

import com.bankshield.common.entity.OperationAudit;
import com.bankshield.common.result.Result;
import com.bankshield.monitor.service.OperationAuditService;
import com.bankshield.auth.controller.UserController;
import com.bankshield.auth.entity.User;
import com.bankshield.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class AuditInterceptorTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private OperationAuditService auditService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encrypted-password");
        testUser.setNickname("Test User");
        testUser.setCreateTime(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("创建用户时记录审计日志")
    void testAuditLogOnCreateUser() throws Exception {
        // Given
        String requestBody = "{\"username\":\"testuser\",\"password\":\"123456\",\"nickname\":\"Test User\"}";
        
        when(userService.createUser(any(User.class))).thenReturn(Result.success(1L));
        
        // When
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
        
        // Then - 验证异步审计日志被触发
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(1)).asyncLog(any(OperationAudit.class));
            });
    }
    
    @Test
    @DisplayName("更新用户时记录审计日志")
    void testAuditLogOnUpdateUser() throws Exception {
        // Given
        Long userId = 1L;
        String requestBody = "{\"nickname\":\"Updated User\",\"email\":\"updated@example.com\"}";
        
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(Result.success(true));
        
        // When
        mockMvc.perform(put("/api/user/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // Then - 验证异步审计日志被触发
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(1)).asyncLog(any(OperationAudit.class));
            });
    }
    
    @Test
    @DisplayName("删除用户时记录审计日志")
    void testAuditLogOnDeleteUser() throws Exception {
        // Given
        Long userId = 1L;
        
        when(userService.deleteUser(userId)).thenReturn(Result.success(true));
        
        // When
        mockMvc.perform(delete("/api/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // Then - 验证异步审计日志被触发
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(1)).asyncLog(any(OperationAudit.class));
            });
    }
    
    @Test
    @DisplayName("查询用户时不记录审计日志")
    void testNoAuditLogOnQueryUser() throws Exception {
        // Given
        Long userId = 1L;
        
        when(userService.getUserById(userId)).thenReturn(Result.success(testUser));
        
        // When
        mockMvc.perform(get("/api/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
        
        // Then - 验证没有审计日志记录
        Awaitility.await()
            .pollDelay(2, TimeUnit.SECONDS)
            .atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, never()).asyncLog(any(OperationAudit.class));
            });
    }
    
    @Test
    @DisplayName("批量创建用户时记录多条审计日志")
    void testAuditLogOnBatchCreateUsers() throws Exception {
        // Given
        String requestBody = "["
            + "{\"username\":\"user1\",\"password\":\"123456\",\"nickname\":\"User 1\"},"
            + "{\"username\":\"user2\",\"password\":\"123456\",\"nickname\":\"User 2\"},"
            + "{\"username\":\"user3\",\"password\":\"123456\",\"nickname\":\"User 3\"}"
            + "]";
        
        when(userService.batchCreateUsers(anyList())).thenReturn(Result.success(Arrays.asList(1L, 2L, 3L)));
        
        // When
        mockMvc.perform(post("/api/user/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // Then - 验证批量操作记录了多条审计日志
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(3)).asyncLog(any(OperationAudit.class));
            });
    }
    
    @Test
    @DisplayName("审计日志包含正确的操作信息")
    void testAuditLogContainsCorrectInformation() throws Exception {
        // Given
        String requestBody = "{\"username\":\"audituser\",\"password\":\"123456\",\"nickname\":\"Audit User\"}";
        ArgumentCaptor<OperationAudit> auditCaptor = ArgumentCaptor.forClass(OperationAudit.class);
        
        when(userService.createUser(any(User.class))).thenReturn(Result.success(1L));
        
        // When
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "admin")
                .header("X-User-Name", "admin")
                .content(requestBody));
        
        // Then - 验证审计日志内容
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(1)).asyncLog(auditCaptor.capture());
                
                OperationAudit audit = auditCaptor.getValue();
                assertThat(audit.getModule()).isEqualTo("用户管理");
                assertThat(audit.getOperation()).isEqualTo("创建用户");
                assertThat(audit.getOperator()).isEqualTo("admin");
                assertThat(audit.getTargetType()).isEqualTo("USER");
                assertThat(audit.getTargetId()).isEqualTo("1");
                assertThat(audit.getRequestParams()).contains("audituser");
                assertThat(audit.getIpAddress()).isNotNull();
                assertThat(audit.getUserAgent()).isNotNull();
                assertThat(audit.getCreateTime()).isNotNull();
            });
    }
    
    @Test
    @DisplayName("异常操作时记录审计日志")
    void testAuditLogOnOperationFailure() throws Exception {
        // Given
        String requestBody = "{\"username\":\"existinguser\",\"password\":\"123456\",\"nickname\":\"Existing User\"}";
        ArgumentCaptor<OperationAudit> auditCaptor = ArgumentCaptor.forClass(OperationAudit.class);
        
        when(userService.createUser(any(User.class)))
            .thenReturn(Result.error(400, "用户名已存在"));
        
        // When
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
        
        // Then - 验证失败的审计日志也记录了
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(1)).asyncLog(auditCaptor.capture());
                
                OperationAudit audit = auditCaptor.getValue();
                assertThat(audit.getStatus()).isEqualTo("FAILURE");
                assertThat(audit.getErrorMessage()).contains("用户名已存在");
            });
    }
    
    @Test
    @DisplayName("敏感数据脱敏处理")
    void testSensitiveDataMasking() throws Exception {
        // Given
        String requestBody = "{\"username\":\"testuser\",\"password\":\"123456\",\"idCard\":\"110101199001011234\",\"phone\":\"13800138000\"}";
        ArgumentCaptor<OperationAudit> auditCaptor = ArgumentCaptor.forClass(OperationAudit.class);
        
        when(userService.createUser(any(User.class))).thenReturn(Result.success(1L));
        
        // When
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
        
        // Then - 验证敏感数据被脱敏
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(1)).asyncLog(auditCaptor.capture());
                
                OperationAudit audit = auditCaptor.getValue();
                String requestParams = audit.getRequestParams();
                
                // 密码应该被完全脱敏
                assertThat(requestParams).doesNotContain("123456");
                assertThat(requestParams).contains("\"password\":\"******\"");
                
                // 身份证号应该被部分脱敏
                assertThat(requestParams).doesNotContain("110101199001011234");
                assertThat(requestParams).contains("1234"); // 只显示后4位
                
                // 手机号应该被部分脱敏
                assertThat(requestParams).doesNotContain("13800138000");
                assertThat(requestParams).contains("8000"); // 只显示后4位
            });
    }
    
    @Test
    @DisplayName("忽略静态资源和健康检查请求")
    void testIgnoreStaticResourcesAndHealthChecks() throws Exception {
        // When - 访问静态资源
        mockMvc.perform(get("/static/css/style.css"))
                .andExpect(status().isOk());
        
        // When - 健康检查
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        
        // Then - 验证没有审计日志记录
        Awaitility.await()
            .pollDelay(2, TimeUnit.SECONDS)
            .atMost(3, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, never()).asyncLog(any(OperationAudit.class));
            });
    }
    
    @Test
    @DisplayName("并发请求审计日志记录")
    void testConcurrentAuditLogRecording() throws Exception {
        // Given
        when(userService.createUser(any(User.class))).thenReturn(Result.success(1L));
        
        // When - 并发发送多个请求
        for (int i = 0; i < 10; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    String requestBody = String.format(
                        "{\"username\":\"concurrent%d\",\"password\":\"123456\",\"nickname\":\"Concurrent User %d\"}",
                        index, index
                    );
                    mockMvc.perform(post("/api/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
        // Then - 等待所有请求完成，验证审计日志记录
        Thread.sleep(3000); // 等待并发请求完成
        
        Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(10)).asyncLog(any(OperationAudit.class));
            });
    }
    
    @Test
    @DisplayName("审计日志异步处理异常不影响主流程")
    void testAuditLogFailureDoesNotAffectMainFlow() throws Exception {
        // Given
        String requestBody = "{\"username\":\"testuser\",\"password\":\"123456\",\"nickname\":\"Test User\"}";
        
        when(userService.createUser(any(User.class))).thenReturn(Result.success(1L));
        doThrow(new RuntimeException("Audit service error"))
            .when(auditService).asyncLog(any(OperationAudit.class));
        
        // When
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // Then - 即使审计日志失败，主流程仍然成功
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(auditService, times(1)).asyncLog(any(OperationAudit.class));
            });
    }
}