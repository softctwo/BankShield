package com.bankshield.common.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 安全响应头过滤器
 * 添加各种安全相关的HTTP响应头，增强Web应用的安全性
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
@Order(2)
public class SecureHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // 添加安全响应头
        addSecurityHeaders(response);
        
        // 移除可能泄露信息的响应头
        removeInformationDisclosureHeaders(response);
        
        filterChain.doFilter(request, response);
    }
    
    private void addSecurityHeaders(HttpServletResponse response) {
        // 防止MIME类型嗅探攻击
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // XSS保护
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // 防止点击劫持攻击
        response.setHeader("X-Frame-Options", "DENY");
        
        // 强制HTTPS传输
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        
        // 内容安全策略
        String cspPolicy = "default-src 'self'; " +
                          "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; " +
                          "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                          "font-src 'self' https://fonts.gstatic.com; " +
                          "img-src 'self' data: https:; " +
                          "connect-src 'self' ws: wss:; " +
                          "frame-ancestors 'none'; " +
                          "base-uri 'self'; " +
                          "form-action 'self';";
        response.setHeader("Content-Security-Policy", cspPolicy);
        
        // 引用策略
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // 权限策略
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()");
        
        // 缓存控制
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // 服务器信息保护
        response.setHeader("Server", "BankShield Security Gateway");
        
        // 跨域策略
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
        
        // DNS预取控制
        response.setHeader("X-DNS-Prefetch-Control", "off");
    }
    
    private void removeInformationDisclosureHeaders(HttpServletResponse response) {
        // 移除可能泄露技术栈信息的响应头
        response.setHeader("X-Powered-By", null);
        response.setHeader("X-AspNet-Version", null);
        response.setHeader("X-AspNetMvc-Version", null);
        response.setHeader("Server", "BankShield Security Gateway"); // 替换默认的服务器头
    }
}