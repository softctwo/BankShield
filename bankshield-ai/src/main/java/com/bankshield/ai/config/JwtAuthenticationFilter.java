package com.bankshield.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 *
 * @author BankShield Team
 * @version 1.0.0
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt != null && validateJwt(jwt)) {
                // 提取用户名（这里简化为从JWT中解析，实际应使用JWT库）
                String username = extractUsernameFromJwt(jwt);

                // 创建用户详情
                UserDetails userDetails = new User(username, "", new ArrayList<>());

                // 创建认证token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证信息到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("无法设置用户认证", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中解析JWT
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
            return headerAuth.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 验证JWT（简化实现）
     * 实际项目中应使用 JJWT 或其他JWT库进行完整验证
     */
    private boolean validateJwt(String jwt) {
        // 简化验证：检查JWT是否非空且长度合理
        // 实际实现应验证签名、过期时间等
        return jwt != null && jwt.length() > 10;
    }

    /**
     * 从JWT中提取用户名（简化实现）
     */
    private String extractUsernameFromJwt(String jwt) {
        // 简化实现：假设JWT的payload部分包含username
        // 实际实现应使用JWT库解码并验证
        try {
            // 这里应该使用JWT库解码，实际项目中请替换为真实实现
            return "ai_user";
        } catch (Exception e) {
            log.error("从JWT中提取用户名失败", e);
            return "anonymous";
        }
    }
}
