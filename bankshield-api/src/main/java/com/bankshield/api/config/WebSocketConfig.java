package com.bankshield.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置
 * 用于实时推送安全威胁、审计日志、合规检查结果等信息
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * /topic - 用于广播消息
     * /queue - 用于点对点消息
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于向客户端推送消息
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀，用于客户端发送消息到服务器
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * 客户端通过此端点连接WebSocket服务
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点
        registry.addEndpoint("/ws")
                // 允许跨域
                .setAllowedOriginPatterns("*")
                // 启用SockJS支持（用于不支持WebSocket的浏览器）
                .withSockJS();
    }
}
