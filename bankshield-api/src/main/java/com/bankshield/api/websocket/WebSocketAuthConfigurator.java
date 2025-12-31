package com.bankshield.api.websocket;

import com.bankshield.api.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;

/**
 * WebSocket 身份验证配置器
 * 在 WebSocket 握手阶段验证 JWT token
 */
@Slf4j
public class WebSocketAuthConfigurator extends ServerEndpointConfig.Configurator {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        try {
            // 从查询参数或请求头中获取 token
            String token = null;

            // 尝试从查询参数获取 token
            List<String> tokenParams = request.getParameterMap().get("token");
            if (tokenParams != null && !tokenParams.isEmpty()) {
                token = tokenParams.get(0);
            }

            // 如果查询参数中没有，尝试从 header 获取
            if (token == null) {
                List<String> authHeaders = request.getHeaders().get("Authorization");
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    String authHeader = authHeaders.get(0);
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                }
            }

            // 验证 token
            if (token != null && !token.isEmpty()) {
                JwtUtil jwtUtil = getJwtUtil();
                if (jwtUtil != null && jwtUtil.validateToken(token)) {
                    Long userId = JwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        // 将验证后的用户ID存储到用户属性中
                        sec.getUserProperties().put("authenticatedUserId", userId.toString());
                        log.debug("WebSocket握手验证成功: userId={}", userId);
                    }
                } else {
                    log.warn("WebSocket握手验证失败: token无效");
                }
            } else {
                log.warn("WebSocket握手验证失败: 缺少token");
            }
        } catch (Exception e) {
            log.error("WebSocket握手验证异常", e);
        }
    }

    private JwtUtil getJwtUtil() {
        if (applicationContext != null) {
            return applicationContext.getBean(JwtUtil.class);
        }
        return null;
    }
}
