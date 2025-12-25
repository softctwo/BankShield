package com.bankshield.common.security;

import com.bankshield.common.security.filter.WafFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WAF过滤器测试
 * 
 * @author BankShield
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
public class WafFilterTest {

    @InjectMocks
    private WafFilter wafFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain mockFilterChain;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        mockFilterChain = new MockFilterChain();
    }

    @Test
    public void testNormalRequest() throws ServletException, IOException {
        // 设置正常请求
        request.setRequestURI("/api/user/login");
        request.setQueryString("username=test&password=123456");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"username\":\"test\",\"password\":\"123456\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求通过
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSqlInjectionInQueryString() throws ServletException, IOException {
        // 设置SQL注入请求
        request.setRequestURI("/api/user/search");
        request.setQueryString("name=test' OR '1'='1");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
        String responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("MALICIOUS_URL_PARAM"));
    }

    @Test
    public void testSqlInjectionInRequestBody() throws ServletException, IOException {
        // 设置SQL注入请求体
        request.setRequestURI("/api/user/login");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"username\":\"admin\",\"password\":\"' OR '1'='1\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
        String responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("MALICIOUS_REQUEST_BODY"));
    }

    @Test
    public void testXssAttack() throws ServletException, IOException {
        // 设置XSS攻击请求
        request.setRequestURI("/api/user/comment");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"content\":\"<script>alert('xss')</script>\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
        String responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("MALICIOUS_REQUEST_BODY"));
    }

    @Test
    public void testCommandInjection() throws ServletException, IOException {
        // 设置命令注入请求
        request.setRequestURI("/api/file/upload");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"filename\":\"test.txt; rm -rf /\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testPathTraversal() throws ServletException, IOException {
        // 设置路径遍历请求
        request.setRequestURI("/api/file/download");
        request.setQueryString("file=../../../etc/passwd");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testLongUserAgent() throws ServletException, IOException {
        // 设置过长的User-Agent
        request.setRequestURI("/api/user/info");
        request.setMethod("GET");
        request.addHeader("User-Agent", "A".repeat(600));

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
        String responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("LONG_USER_AGENT"));
    }

    @Test
    public void testLongContentType() throws ServletException, IOException {
        // 设置过长的Content-Type
        request.setRequestURI("/api/user/info");
        request.setMethod("POST");
        request.addHeader("Content-Type", "application/json" + "; charset=".repeat(20) + "utf-8");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
        String responseContent = response.getContentAsString();
        assertTrue(responseContent.contains("LONG_CONTENT_TYPE"));
    }

    @Test
    public void testUnionBasedSqlInjection() throws ServletException, IOException {
        // 测试UNION注入
        request.setRequestURI("/api/user/search");
        request.setQueryString("id=1 UNION SELECT * FROM users");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testBooleanBasedSqlInjection() throws ServletException, IOException {
        // 测试布尔盲注
        request.setRequestURI("/api/user/search");
        request.setQueryString("id=1 AND 1=1");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testTimeBasedSqlInjection() throws ServletException, IOException {
        // 测试时间盲注
        request.setRequestURI("/api/user/search");
        request.setQueryString("id=1 AND SLEEP(5)");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testJavaScriptXss() throws ServletException, IOException {
        // 测试JavaScript XSS
        request.setRequestURI("/api/user/comment");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"content\":\"javascript:alert('xss')\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testEventHandlerXss() throws ServletException, IOException {
        // 测试事件处理器XSS
        request.setRequestURI("/api/user/profile");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"bio\":\"<img src=x onerror=alert('xss')>\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCommandInjectionWithBackticks() throws ServletException, IOException {
        // 测试反引号命令注入
        request.setRequestURI("/api/file/process");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"command\":\"ls `whoami`\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCommandInjectionWithDollar() throws ServletException, IOException {
        // 测试$()命令注入
        request.setRequestURI("/api/file/process");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"command\":\"ls $(whoami)\"}".getBytes());

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testEncodedPathTraversal() throws ServletException, IOException {
        // 测试编码的路径遍历
        request.setRequestURI("/api/file/download");
        request.setQueryString("file=%2e%2e%2f%2e%2e%2fetc%2fpasswd");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testDoubleEncodedPathTraversal() throws ServletException, IOException {
        // 测试双重编码的路径遍历
        request.setRequestURI("/api/file/download");
        request.setQueryString("file=%252e%252e%252f%252e%252e%252fetc%252fpasswd");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求被阻断
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testEmptyRequest() throws ServletException, IOException {
        // 测试空请求
        request.setRequestURI("/api/user/info");
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求通过
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNullQueryString() throws ServletException, IOException {
        // 测试空查询字符串
        request.setRequestURI("/api/user/info");
        request.setQueryString(null);
        request.setMethod("GET");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求通过
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testEmptyRequestBody() throws ServletException, IOException {
        // 测试空请求体
        request.setRequestURI("/api/user/login");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent(new byte[0]);

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求通过
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNormalUserAgent() throws ServletException, IOException {
        // 测试正常User-Agent
        request.setRequestURI("/api/user/info");
        request.setMethod("GET");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求通过
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNormalContentType() throws ServletException, IOException {
        // 测试正常Content-Type
        request.setRequestURI("/api/user/info");
        request.setMethod("POST");
        request.addHeader("Content-Type", "application/json; charset=utf-8");

        // 执行过滤
        wafFilter.doFilterInternal(request, response, mockFilterChain);

        // 验证请求通过
        assertEquals(200, response.getStatus());
    }
}