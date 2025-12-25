package com.bankshield.common.security.filter;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Web应用防火墙过滤器
 * 用于检测和防御常见的Web攻击，如SQL注入、XSS攻击等
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
@Order(1)
public class WafFilter extends OncePerRequestFilter {
    
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile(".*\\b(union|select|insert|update|delete|drop|create|alter|exec|execute|script|declare|truncate)\\b.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*['\"];.*[;].*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\b(or|and)\\s+\\d+=[\\d']+.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\b(or|and)\\s+['\"][^'\"]*['\"]=['\"][^'\"]*['\"].*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\bwaitfor\\s+delay\\s+['\"][^'\"]*['\"].*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\bbenchmark\\s*\\(.*,.*\\).*", Pattern.CASE_INSENSITIVE)
    };
    
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile(".*<script.*>.*</script.*>.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*javascript:.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*vbscript:.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onload.*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onerror.*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onclick.*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onmouseover.*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*<iframe.*>.*</iframe.*>.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*<object.*>.*</object.*>.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*<embed.*>.*</embed.*>.*", Pattern.CASE_INSENSITIVE)
    };
    
    private static final Pattern[] COMMAND_INJECTION_PATTERNS = {
        Pattern.compile(".*[;&|`]\\s*(ls|cat|echo|rm|cp|mv|chmod|chown|wget|curl|nc|bash|sh|cmd|powershell).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\$\\(.*\\).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*`.*`.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\${.*}.*", Pattern.CASE_INSENSITIVE)
    };
    
    private static final Pattern[] PATH_TRAVERSAL_PATTERNS = {
        Pattern.compile(".*\\.\\./.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.\\.\\\\.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*%2e%2e%2f.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*%2e%2e%5c.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*%252e%252e%252f.*", Pattern.CASE_INSENSITIVE)
    };
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String contentType = request.getHeader("Content-Type");
        
        log.debug("WAF检查请求: {}?{}", requestUri, queryString);
        
        try {
            // 检查URL参数
            if (queryString != null && isMalicious(queryString)) {
                log.warn("WAF检测到恶意URL参数: {}?{}", requestUri, queryString);
                sendBlockedResponse(response, "MALICIOUS_URL_PARAM", "检测到恶意URL参数");
                return;
            }
            
            // 检查请求头
            if (userAgent != null && userAgent.length() > 500) {
                log.warn("WAF检测到异常User-Agent长度: {} ({})", userAgent.length(), requestUri);
                sendBlockedResponse(response, "LONG_USER_AGENT", "User-Agent过长");
                return;
            }
            
            // 检查Content-Type
            if (contentType != null && contentType.length() > 100) {
                log.warn("WAF检测到异常Content-Type长度: {} ({})", contentType.length(), requestUri);
                sendBlockedResponse(response, "LONG_CONTENT_TYPE", "Content-Type过长");
                return;
            }
            
            // 检查请求体（POST/PUT请求）
            if ("POST".equalsIgnoreCase(request.getMethod()) || 
                "PUT".equalsIgnoreCase(request.getMethod()) ||
                "PATCH".equalsIgnoreCase(request.getMethod())) {
                
                String body = getRequestBody(request);
                if (body != null && !body.trim().isEmpty() && isMalicious(body)) {
                    log.warn("WAF检测到恶意请求体: {} ({})", requestUri, body.substring(0, Math.min(body.length(), 100)));
                    sendBlockedResponse(response, "MALICIOUS_REQUEST_BODY", "检测到恶意请求体");
                    return;
                }
            }
            
        } catch (Exception e) {
            log.error("WAF处理请求时发生错误: {}", e.getMessage(), e);
            sendBlockedResponse(response, "WAF_ERROR", "安全检测错误");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isMalicious(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        // 检查SQL注入
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logSecurityEvent("SQL_INJECTION", input);
                return true;
            }
        }
        
        // 检查XSS攻击
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logSecurityEvent("XSS_ATTACK", input);
                return true;
            }
        }
        
        // 检查命令注入
        for (Pattern pattern : COMMAND_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logSecurityEvent("COMMAND_INJECTION", input);
                return true;
            }
        }
        
        // 检查路径遍历
        for (Pattern pattern : PATH_TRAVERSAL_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logSecurityEvent("PATH_TRAVERSAL", input);
                return true;
            }
        }
        
        return false;
    }
    
    private String getRequestBody(HttpServletRequest request) {
        try {
            return IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取请求体失败: {}", e.getMessage());
            return "";
        }
    }
    
    private void sendBlockedResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}",
            code, message, System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    private void logSecurityEvent(String attackType, String evidence) {
        log.warn("安全事件 - 攻击类型: {}, 证据: {}", attackType, 
                evidence.length() > 200 ? evidence.substring(0, 200) + "..." : evidence);
    }
}