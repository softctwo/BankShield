package com.bankshield.monitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 告警服务
 * 
 * @author BankShield Team
 * @version 1.0.0
 */
@Service
public class AlertingService {

    private static final Logger logger = LoggerFactory.getLogger(AlertingService.class);

    @Value("${monitoring.alerting.alertmanager.url:http://localhost:9093}")
    private String alertManagerUrl;

    @Value("${monitoring.alerting.alertmanager.timeout:10}")
    private int alertManagerTimeout;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MetricsService metricsService;

    private final OkHttpClient httpClient;

    public AlertingService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 发送告警到AlertManager
     */
    public void sendAlert(String alertName, String severity, String summary, String description, Map<String, String> labels) {
        try {
            List<Alert> alerts = new ArrayList<>();
            Alert alert = new Alert();
            alert.setLabels(new HashMap<>());
            alert.setAnnotations(new HashMap<>());

            // 设置标签
            alert.getLabels().put("alertname", alertName);
            alert.getLabels().put("severity", severity);
            alert.getLabels().put("service", "bankshield");
            if (labels != null) {
                alert.getLabels().putAll(labels);
            }

            // 设置注释
            alert.getAnnotations().put("summary", summary);
            alert.getAnnotations().put("description", description);
            alert.getAnnotations().put("timestamp", Instant.now().toString());

            alerts.add(alert);

            // 发送告警
            sendAlertsToAlertManager(alerts);

            // 记录监控指标
            metricsService.recordSecurityAlert(alertName, severity);

            logger.info("Alert sent successfully: {} - {}", alertName, summary);
        } catch (Exception e) {
            logger.error("Failed to send alert: {} - {}", alertName, summary, e);
        }
    }

    /**
     * 发送系统告警
     */
    public void sendSystemAlert(String component, String alertType, String message) {
        Map<String, String> labels = new HashMap<>();
        labels.put("component", component);
        labels.put("type", "system");

        sendAlert("SystemAlert", "warning", 
                "System Alert: " + component, 
                message, labels);
    }

    /**
     * 发送安全告警
     */
    public void sendSecurityAlert(String threatType, String source, String message) {
        Map<String, String> labels = new HashMap<>();
        labels.put("threat_type", threatType);
        labels.put("source", source);
        labels.put("type", "security");

        sendAlert("SecurityAlert", "critical", 
                "Security Threat Detected: " + threatType, 
                message, labels);
    }

    /**
     * 发送业务告警
     */
    public void sendBusinessAlert(String businessFunction, String metric, String message) {
        Map<String, String> labels = new HashMap<>();
        labels.put("function", businessFunction);
        labels.put("metric", metric);
        labels.put("type", "business");

        sendAlert("BusinessAlert", "warning", 
                "Business Alert: " + businessFunction, 
                message, labels);
    }

    /**
     * 发送性能告警
     */
    public void sendPerformanceAlert(String metric, double value, String threshold, String message) {
        Map<String, String> labels = new HashMap<>();
        labels.put("metric", metric);
        labels.put("value", String.valueOf(value));
        labels.put("threshold", threshold);
        labels.put("type", "performance");

        sendAlert("PerformanceAlert", "warning", 
                "Performance Alert: " + metric + " = " + value, 
                message, labels);
    }

    /**
     * 批量发送告警到AlertManager
     */
    private void sendAlertsToAlertManager(List<Alert> alerts) throws IOException {
        String json = objectMapper.writeValueAsString(alerts);
        
        RequestBody body = RequestBody.create(
                json, 
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(alertManagerUrl + "/api/v1/alerts")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
        }
    }

    /**
     * 获取活跃告警
     */
    public List<Alert> getActiveAlerts() {
        try {
            Request request = new Request.Builder()
                    .url(alertManagerUrl + "/api/v1/alerts")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    AlertManagerResponse alertResponse = objectMapper.readValue(
                            responseBody, AlertManagerResponse.class
                    );
                    return alertResponse.getData();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get active alerts", e);
        }
        return Collections.emptyList();
    }

    /**
     * 静默告警
     */
    public void silenceAlert(String alertName, String duration, String comment) {
        try {
            Map<String, Object> silence = new HashMap<>();
            silence.put("matchers", Arrays.asList(
                    Map.of("name", "alertname", "value", alertName, "isRegex", false)
            ));
            silence.put("startsAt", Instant.now().toString());
            silence.put("endsAt", Instant.now().plusSeconds(parseDuration(duration)).toString());
            silence.put("createdBy", "bankshield-monitor");
            silence.put("comment", comment);

            String json = objectMapper.writeValueAsString(silence);
            
            RequestBody body = RequestBody.create(
                    json, 
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(alertManagerUrl + "/api/v1/silences")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code());
                }
            }

            logger.info("Alert {} silenced for {}", alertName, duration);
        } catch (Exception e) {
            logger.error("Failed to silence alert: {}", alertName, e);
        }
    }

    /**
     * 解析时间字符串（如"1h", "30m"）为秒数
     */
    private long parseDuration(String duration) {
        if (duration.endsWith("h")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 3600;
        } else if (duration.endsWith("m")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60;
        } else if (duration.endsWith("s")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1));
        }
        return 3600; // 默认1小时
    }

    /**
     * 告警对象
     */
    public static class Alert {
        private Map<String, String> labels;
        private Map<String, String> annotations;
        private String startsAt;
        private String endsAt;

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }

        public Map<String, String> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(Map<String, String> annotations) {
            this.annotations = annotations;
        }

        public String getStartsAt() {
            return startsAt;
        }

        public void setStartsAt(String startsAt) {
            this.startsAt = startsAt;
        }

        public String getEndsAt() {
            return endsAt;
        }

        public void setEndsAt(String endsAt) {
            this.endsAt = endsAt;
        }
    }

    /**
     * AlertManager响应对象
     */
    public static class AlertManagerResponse {
        private String status;
        private List<Alert> data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<Alert> getData() {
            return data;
        }

        public void setData(List<Alert> data) {
            this.data = data;
        }
    }
}