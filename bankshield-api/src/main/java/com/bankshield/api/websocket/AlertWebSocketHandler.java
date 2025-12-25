package com.bankshield.api.websocket;

import com.bankshield.api.entity.AlertRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警WebSocket处理器
 * 提供实时告警推送功能
 */
@Slf4j
@Component
@ServerEndpoint("/ws/alerts/{userId}")
public class AlertWebSocketHandler {

    // 存储所有连接的WebSocket会话
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    
    // JSON序列化器
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立时调用
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessions.put(userId, session);
        log.info("WebSocket连接建立: userId={}", userId);
        
        // 发送连接成功消息
        sendMessage(session, "CONNECTED", "连接成功");
    }

    /**
     * 连接关闭时调用
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        sessions.remove(userId);
        log.info("WebSocket连接关闭: userId={}", userId);
    }

    /**
     * 收到客户端消息时调用
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("收到WebSocket消息: userId={}, message={}", userId, message);
        
        // 处理心跳消息
        if ("PING".equals(message)) {
            Session session = sessions.get(userId);
            if (session != null) {
                sendMessage(session, "PONG", "心跳响应");
            }
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误", error);
    }

    /**
     * 发送告警通知给指定用户
     */
    public static void sendAlertToUser(String userId, AlertRecord alertRecord) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(alertRecord);
                sendMessage(session, "ALERT", message);
                log.info("发送告警通知给用户: userId={}", userId);
            } catch (Exception e) {
                log.error("发送告警通知失败: userId={}", userId, e);
            }
        }
    }

    /**
     * 广播告警通知给所有在线用户
     */
    public static void broadcastAlert(AlertRecord alertRecord) {
        try {
            String message = objectMapper.writeValueAsString(alertRecord);
            
            for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                Session session = entry.getValue();
                if (session != null && session.isOpen()) {
                    try {
                        sendMessage(session, "ALERT", message);
                    } catch (Exception e) {
                        log.error("广播告警通知失败: userId={}", entry.getKey(), e);
                    }
                }
            }
            
            log.info("广播告警通知完成，在线用户数: {}", sessions.size());
        } catch (Exception e) {
            log.error("广播告警通知失败", e);
        }
    }

    /**
     * 发送系统通知给所有在线用户
     */
    public static void broadcastSystemMessage(String type, String content) {
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            Session session = entry.getValue();
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, type, content);
                } catch (Exception e) {
                    log.error("发送系统通知失败: userId={}", entry.getKey(), e);
                }
            }
        }
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineUserCount() {
        return sessions.size();
    }

    /**
     * 发送消息
     */
    private static void sendMessage(Session session, String type, String content) {
        try {
            if (session.isOpen()) {
                // 构建消息格式
                WebSocketMessage message = new WebSocketMessage(type, content, System.currentTimeMillis());
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getBasicRemote().sendText(jsonMessage);
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * WebSocket消息格式
     */
    private static class WebSocketMessage {
        private String type;
        private String content;
        private long timestamp;

        public WebSocketMessage(String type, String content, long timestamp) {
            this.type = type;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}