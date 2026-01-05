package com.bankshield.api.websocket;

import com.bankshield.api.dto.SecurityEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 安全威胁WebSocket处理器
 * 实时推送安全威胁信息到前端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityThreatWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    private static final String[] THREAT_TYPES = {
        "SQL注入", "XSS攻击", "暴力破解", "DDoS攻击", "恶意软件", 
        "钓鱼攻击", "数据泄露", "端口扫描", "异常登录"
    };

    private static final String[] SEVERITIES = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};

    /**
     * 推送实时安全威胁
     * 每5秒推送一次模拟威胁数据
     */
    @Scheduled(fixedRate = 5000)
    public void pushSecurityThreat() {
        try {
            SecurityEventDTO event = generateMockThreat();
            // 推送到/topic/security/threats主题，所有订阅的客户端都会收到
            messagingTemplate.convertAndSend("/topic/security/threats", event);
            log.debug("推送安全威胁: {}", event.getEventMessage());
        } catch (Exception e) {
            log.error("推送安全威胁失败", e);
        }
    }

    /**
     * 推送安全事件到指定用户
     */
    public void pushToUser(String username, SecurityEventDTO event) {
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/security/events", event);
            log.debug("推送安全事件到用户 {}: {}", username, event.getEventMessage());
        } catch (Exception e) {
            log.error("推送安全事件到用户失败: {}", username, e);
        }
    }

    /**
     * 推送高危威胁告警
     */
    public void pushCriticalThreat(SecurityEventDTO event) {
        try {
            messagingTemplate.convertAndSend("/topic/security/critical", event);
            log.warn("推送高危威胁告警: {}", event.getEventMessage());
        } catch (Exception e) {
            log.error("推送高危威胁告警失败", e);
        }
    }

    /**
     * 生成模拟威胁数据
     */
    private SecurityEventDTO generateMockThreat() {
        SecurityEventDTO event = new SecurityEventDTO();
        event.setEventType(THREAT_TYPES[random.nextInt(THREAT_TYPES.length)]);
        event.setEventLevel(SEVERITIES[random.nextInt(SEVERITIES.length)]);
        event.setEventSource("安全监控系统");
        event.setEventMessage(String.format("检测到%s攻击，来源IP: %s", 
            event.getEventType(), generateRandomIp()));
        event.setEventTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return event;
    }

    /**
     * 生成随机IP地址
     */
    private String generateRandomIp() {
        return String.format("%d.%d.%d.%d",
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256));
    }
}
