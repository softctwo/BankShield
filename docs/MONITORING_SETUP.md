# BankShield 监控告警系统部署指南

## 概述

BankShield监控告警系统基于Prometheus + Grafana + AlertManager构建，提供全方位的系统监控、安全监控、业务监控和合规监控能力。

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    监控数据采集层                           │
├─────────────────────────────────────────────────────────────┤
│  • BankShield Monitor (自定义指标)                        │
│  • Node Exporter (系统指标)                               │
│  • MySQL Exporter (数据库指标)                            │
│  • Redis Exporter (缓存指标)                              │
│  • Blackbox Exporter (网络指标)                           │
├─────────────────────────────────────────────────────────────┤
│                    数据存储层                               │
├─────────────────────────────────────────────────────────────┤
│  • Prometheus (时序数据库)                                │
│  • VictoriaMetrics (备选时序数据库)                       │
│  • Elasticsearch (日志存储)                               │
│  • Loki (日志聚合)                                        │
├─────────────────────────────────────────────────────────────┤
│                    可视化层                                 │
├─────────────────────────────────────────────────────────────┤
│  • Grafana (监控可视化)                                   │
│  • Kibana (日志可视化)                                    │
│  • BankShield UI (自定义监控面板)                         │
├─────────────────────────────────────────────────────────────┤
│                    告警处理层                               │
├─────────────────────────────────────────────────────────────┤
│  • AlertManager (告警管理)                                │
│  • 多渠道通知 (邮件、Slack、Webhook)                      │
└─────────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. Prometheus - 时序数据库
- **端口**: 9090
- **配置**: `/docker/prometheus/prometheus.yml`
- **数据保留**: 200小时
- **采集间隔**: 15秒

### 2. Grafana - 可视化平台
- **端口**: 3001
- **管理员**: admin / BankShield@2024
- **数据源**: Prometheus, MySQL, Loki
- **预置Dashboard**: 系统总览、安全监控、业务监控

### 3. AlertManager - 告警管理
- **端口**: 9093
- **配置**: `/docker/alertmanager/alertmanager.yml`
- **通知渠道**: 邮件、Slack、Webhook

### 4. BankShield Monitor - 自定义监控服务
- **端口**: 8888
- **功能**: 业务指标采集、健康检查、告警处理
- **指标端点**: `/actuator/prometheus`

## 监控指标分类

### 系统指标 (System Metrics)
```yaml
# CPU使用率
node_cpu_seconds_total

# 内存使用率
node_memory_MemAvailable_bytes
node_memory_MemTotal_bytes

# 磁盘使用率
node_filesystem_avail_bytes
node_filesystem_size_bytes

# 网络IO
node_network_receive_bytes_total
node_network_transmit_bytes_total
```

### 应用指标 (Application Metrics)
```yaml
# JVM指标
jvm_memory_used_bytes
jvm_memory_max_bytes
jvm_gc_pause_seconds_sum

# HTTP指标
http_requests_total
http_request_duration_seconds

# Spring Boot指标
system_cpu_usage
system_memory_usage
```

### 业务指标 (Business Metrics)
```yaml
# BankShield自定义指标
bankshield_security_alerts_total        # 安全告警总数
bankshield_data_processing_total        # 数据处理量
bankshield_encryption_operations_total  # 加密操作数
bankshield_audit_logs_total            # 审计日志数
bankshield_api_response_time           # API响应时间
bankshield_system_health_score         # 系统健康分数
bankshield_online_users                # 在线用户数
```

### 安全指标 (Security Metrics)
```yaml
# 用户认证
bankshield_user_login_total
bankshield_permission_checks_total

# 异常检测
bankshield_security_alerts_total
bankshield_system_errors_total
```

## 告警规则

### 关键告警 (Critical)
- 服务宕机：`up{job=~"bankshield-.*"} == 0`
- 磁盘空间不足：磁盘使用率 > 90%
- 数据库连接失败：mysql_up == 0
- Redis连接失败：redis_up == 0

### 警告告警 (Warning)
- CPU使用率过高：> 80%
- 内存使用率过高：> 85%
- API响应时间过长：95分位 > 500ms
- 错误率过高：> 5%

### 安全告警 (Security)
- 安全告警激增：rate > 5/秒
- 异常登录尝试：5分钟内失败 > 10次
- 权限拒绝异常：rate > 3/秒

### 业务告警 (Business)
- 数据处理异常：失败率异常
- 加密操作异常：频率异常降低
- 审计日志缺失：生成速率异常

### 合规告警 (Compliance)
- 密钥轮换逾期：> 90天
- 合规评分过低：< 80分
- 审计日志完整性：< 95%

## 部署步骤

### 1. 基础监控部署
```bash
# 启动基础监控组件
cd /Users/zhangyanlong/workspaces/BankShield/docker
docker-compose up -d prometheus grafana alertmanager

# 验证服务状态
docker-compose ps
```

### 2. 完整监控栈部署
```bash
# 启动完整监控栈
docker-compose -f docker-compose.yml -f docker-compose-monitoring.yml up -d

# 检查日志
docker-compose logs -f prometheus
docker-compose logs -f grafana
docker-compose logs -f alertmanager
```

### 3. 访问监控界面
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/BankShield@2024)
- **AlertManager**: http://localhost:9093
- **BankShield Monitor**: http://localhost:8888

### 4. 配置数据源
Grafana中已预配置以下数据源：
- Prometheus (http://prometheus:9090)
- BankShield Metrics (http://monitor:8888/actuator/prometheus)
- MySQL (bankshield:BankShield@2024@tcp(mysql:3306)/bankshield)

### 5. 导入Dashboard
Dashboard文件已预置在 `/docker/grafana/provisioning/dashboards/`：
- `bankshield-overview.json` - 系统总览
- `bankshield-security.json` - 安全监控

## 监控Dashboard说明

### 系统总览Dashboard
- **系统健康度**: 整体系统健康评分 (0-100%)
- **活跃服务**: 运行中的服务数量
- **在线用户**: 当前在线用户数
- **活跃告警**: 当前未处理的告警数量
- **API响应时间**: 50分位、95分位、99分位响应时间
- **系统资源使用**: CPU、内存使用率趋势
- **安全事件趋势**: 安全告警、登录失败、权限拒绝趋势
- **业务指标**: 数据处理、加密操作、审计日志速率

### 安全监控Dashboard
- **安全告警总数**: 累计安全告警数量
- **登录失败次数**: 认证失败统计
- **权限拒绝次数**: 权限检查失败统计
- **加密操作异常**: 加密服务异常检测
- **安全告警趋势**: 按时间分布的安全告警
- **登录活动趋势**: 成功/失败登录趋势
- **按类型安全告警**: 饼图显示告警类型分布
- **按严重程度安全告警**: 柱状图显示告警级别分布
- **加密操作趋势**: 加密服务运行趋势

## 告警通知配置

### 邮件通知
```yaml
# AlertManager配置
receivers:
  - name: 'bankshield-critical'
    email_configs:
      - to: 'urgent@bankshield.com, oncall@bankshield.com'
        subject: '[CRITICAL] BankShield紧急告警'
        body: |
          🚨 CRITICAL ALERT 🚨
          告警名称: {{ .Annotations.summary }}
          描述: {{ .Annotations.description }}
          服务: {{ .Labels.service }}
          级别: {{ .Labels.severity }}
```

### Slack通知
```yaml
slack_configs:
  - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
    channel: '#bankshield-alerts'
    title: 'BankShield Alert'
    text: |
      *{{ .Annotations.summary }}*
      {{ .Annotations.description }}
    color: 'danger'
```

### Webhook通知
```yaml
webhook_configs:
  - url: 'http://your-webhook-url/api/alerts'
    send_resolved: true
    http_config:
      basic_auth:
        username: 'alertmanager'
        password: 'BankShield@2024'
```

## 维护操作

### 查看服务状态
```bash
docker-compose ps
docker-compose logs [service-name]
```

### 重启服务
```bash
docker-compose restart [service-name]
```

### 数据备份
```bash
# 备份Prometheus数据
docker run --rm -v bankshield_prometheus_data:/data -v $(pwd):/backup alpine tar czf /backup/prometheus-backup.tar.gz /data

# 备份Grafana数据
docker run --rm -v bankshield_grafana_data:/data -v $(pwd):/backup alpine tar czf /backup/grafana-backup.tar.gz /data
```

### 数据清理
```bash
# 清理旧的监控数据
docker exec bankshield-prometheus promtool tsdb delete series --start=$(date -d '7 days ago' +%s) --end=$(date +%s)
```

## 性能优化

### Prometheus优化
```yaml
# prometheus.yml 优化配置
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    monitor: 'bankshield'

# 存储优化
--storage.tsdb.retention.time=200h
--storage.tsdb.retention.size=50GB
--storage.tsdb.wal-compression
```

### Grafana优化
```yaml
# grafana.ini 优化配置
[database]
type = mysql
host = mysql:3306
name = grafana
user = grafana
password = BankShield@2024

[session]
provider = redis
provider_config = redis:6379
```

## 安全考虑

### 1. 访问控制
- 启用Grafana用户认证
- 配置Prometheus基本认证
- 限制AlertManager访问

### 2. 网络安全
- 使用防火墙限制端口访问
- 配置SSL/TLS加密
- 实施网络隔离

### 3. 数据保护
- 加密敏感监控数据
- 定期备份配置和数据
- 实施访问审计

## 故障排除

### 常见问题

#### Prometheus无法采集指标
1. 检查目标服务是否正常运行
2. 验证指标端点是否可访问
3. 检查网络连接和防火墙设置
4. 查看Prometheus日志获取详细错误信息

#### Grafana无法显示数据
1. 检查数据源配置是否正确
2. 验证Prometheus查询语法
3. 检查时间范围设置
4. 查看Grafana日志

#### AlertManager不发送告警
1. 检查告警规则是否正确触发
2. 验证AlertManager配置
3. 检查通知渠道配置
4. 测试告警通知

### 日志查看
```bash
# Prometheus日志
docker logs bankshield-prometheus

# Grafana日志
docker logs bankshield-grafana

# AlertManager日志
docker logs bankshield-alertmanager

# BankShield Monitor日志
docker logs bankshield-monitor
```

## 扩展功能

### 1. 自定义指标开发
```java
// 在BankShield服务中添加自定义指标
@Component
public class CustomMetrics {
    private final Counter businessCounter;
    private final Timer businessTimer;
    
    public CustomMetrics(MeterRegistry registry) {
        this.businessCounter = Counter.builder("bankshield_business_metric")
                .description("Custom business metric")
                .register(registry);
        
        this.businessTimer = Timer.builder("bankshield_business_operation")
                .description("Business operation duration")
                .register(registry);
    }
}
```

### 2. 自定义告警规则
```yaml
# 在alert_rules/目录下添加新的告警规则文件
groups:
  - name: custom-alerts
    rules:
      - alert: CustomBusinessAlert
        expr: bankshield_business_metric > 100
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Business metric high"
          description: "Business metric is {{ $value }}"
```

### 3. 自定义Dashboard
```json
{
  "dashboard": {
    "title": "Custom Dashboard",
    "panels": [
      {
        "title": "Custom Metric",
        "type": "stat",
        "targets": [
          {
            "expr": "bankshield_business_metric",
            "legendFormat": "Business Metric"
          }
        ]
      }
    ]
  }
}
```

## 联系支持

如遇到问题，请联系：
- 技术支持: support@bankshield.com
- 监控专项: monitoring@bankshield.com
- 紧急联系: urgent@bankshield.com

## 参考文档

- [Prometheus官方文档](https://prometheus.io/docs/)
- [Grafana官方文档](https://grafana.com/docs/)
- [AlertManager官方文档](https://prometheus.io/docs/alerting/alertmanager/)
- [Spring Boot Micrometer文档](https://micrometer.io/docs)