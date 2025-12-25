# BankShield监控告警模块

## 概述

BankShield监控告警模块是运维和安全管理的核心组件，提供实时系统监控、安全事件检测、告警规则配置和多渠道通知功能。模块支持图表化展示和实时告警推送，确保系统稳定运行和安全事件及时发现。

## 功能特性

### 🔍 系统监控
- **多维度指标监控**：CPU、内存、磁盘、网络等系统资源
- **数据库监控**：连接数、查询性能、慢查询检测
- **应用监控**：线程数、GC次数、响应时间
- **安全监控**：异常登录、敏感数据访问、密钥使用

### 🚨 智能告警
- **灵活规则配置**：支持多种触发条件和阈值设置
- **分级告警管理**：INFO、WARNING、CRITICAL、EMERGENCY四级告警
- **重复告警控制**：避免告警轰炸，支持频率控制
- **告警状态跟踪**：未处理、已处理、已忽略三种状态

### 📢 多渠道通知
- **邮件通知**：支持SMTP配置，自定义模板
- **短信通知**：集成阿里云等短信服务商
- **Webhook通知**：支持自定义HTTP回调
- **钉钉通知**：企业钉钉群机器人集成
- **企业微信**：企业微信群机器人集成

### 📊 可视化展示
- **实时监控大屏**：Dashboard展示关键指标
- **趋势分析图表**：24小时告警趋势、指标趋势
- **告警分布统计**：饼图展示各级别告警分布
- **系统健康度评分**：综合评估系统健康状态

## 技术架构

### 后端架构
- **Spring Boot**：微服务框架
- **MyBatis-Plus**：ORM框架
- **Quartz**：定时任务调度
- **WebSocket**：实时推送（可选）
- **ECharts**：图表数据接口

### 前端架构
- **Vue 3**：渐进式JavaScript框架
- **TypeScript**：类型安全的JavaScript
- **Element Plus**：UI组件库
- **ECharts**：图表可视化库
- **WebSocket**：实时通信（可选）

### 数据存储
- **MySQL 8.0**：关系型数据库
- **Redis**：缓存和会话存储
- **Elasticsearch**：日志和审计数据（可选）

## 模块结构

```
bankshield-api/
├── entity/                 # 实体类
│   ├── MonitorMetric.java
│   ├── AlertRule.java
│   ├── AlertRecord.java
│   └── NotificationConfig.java
├── enums/                  # 枚举类
│   ├── MetricType.java
│   ├── AlertLevel.java
│   ├── AlertStatus.java
│   └── NotifyType.java
├── mapper/                 # 数据访问层
│   ├── MonitorMetricMapper.java
│   ├── AlertRuleMapper.java
│   ├── AlertRecordMapper.java
│   └── NotificationConfigMapper.java
├── service/                # 业务逻辑层
│   ├── MonitorDataCollectionService.java
│   ├── AlertRuleEngine.java
│   ├── NotificationService.java
│   └── MonitorDashboardService.java
├── controller/             # 控制层
│   ├── MonitorController.java
│   ├── AlertRuleController.java
│   ├── AlertRecordController.java
│   └── NotificationController.java
├── job/                    # 定时任务
│   ├── MonitorDataCollectionJob.java
│   ├── AlertCheckJob.java
│   └── DataRetentionJob.java
└── websocket/              # WebSocket（可选）
    ├── WebSocketConfig.java
    └── AlertWebSocketHandler.java
```

## 数据库设计

### 监控指标表 (monitor_metric)
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT | 主键ID |
| metric_name | VARCHAR(100) | 指标名称 |
| metric_type | VARCHAR(50) | 指标类型 |
| metric_value | DOUBLE | 指标值 |
| metric_unit | VARCHAR(20) | 指标单位 |
| threshold | DOUBLE | 阈值 |
| status | VARCHAR(20) | 状态 |
| collect_time | DATETIME | 采集时间 |
| description | TEXT | 描述 |
| related_resource | VARCHAR(200) | 关联资源 |

### 告警规则表 (alert_rule)
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT | 规则ID |
| rule_name | VARCHAR(100) | 规则名称 |
| rule_type | VARCHAR(50) | 规则类型 |
| monitor_metric | VARCHAR(100) | 监控指标 |
| trigger_condition | VARCHAR(10) | 触发条件 |
| threshold | DOUBLE | 阈值 |
| alert_level | VARCHAR(20) | 告警级别 |
| enabled | TINYINT(1) | 是否启用 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| description | TEXT | 描述 |

### 告警记录表 (alert_record)
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT | 告警ID |
| rule_id | BIGINT | 告警规则ID |
| alert_level | VARCHAR(20) | 告警级别 |
| alert_title | VARCHAR(200) | 告警标题 |
| alert_content | TEXT | 告警内容 |
| alert_time | DATETIME | 告警时间 |
| alert_status | VARCHAR(20) | 告警状态 |
| handler | VARCHAR(50) | 处理人 |
| handle_time | DATETIME | 处理时间 |
| handle_remark | TEXT | 处理备注 |
| notify_status | VARCHAR(20) | 通知状态 |

### 通知配置表 (notification_config)
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT | 通知配置ID |
| notify_type | VARCHAR(50) | 通知类型 |
| recipients | VARCHAR(500) | 接收人 |
| notify_template | TEXT | 通知模板 |
| enabled | TINYINT(1) | 是否启用 |
| config_params | TEXT | 配置参数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| description | TEXT | 描述 |

## API接口

### 监控数据接口
- `GET /api/monitor/metrics/current` - 获取当前监控指标
- `GET /api/monitor/metrics/history` - 获取历史监控数据
- `GET /api/monitor/dashboard/stats` - 获取Dashboard统计数据
- `GET /api/monitor/dashboard/alert-trend` - 获取24小时告警趋势
- `GET /api/monitor/dashboard/alert-distribution` - 获取告警类型分布
- `POST /api/monitor/metrics/collect` - 手动触发监控数据采集

### 告警规则接口
- `GET /api/alert/rule/page` - 分页查询告警规则
- `POST /api/alert/rule` - 创建告警规则
- `PUT /api/alert/rule/{id}` - 更新告警规则
- `DELETE /api/alert/rule/{id}` - 删除告警规则
- `PUT /api/alert/rule/{id}/enable` - 启用/禁用规则
- `POST /api/alert/rule/{id}/test` - 测试告警规则

### 告警记录接口
- `GET /api/alert/record/page` - 分页查询告警记录
- `GET /api/alert/record/{id}` - 获取告警详情
- `PUT /api/alert/record/{id}/resolve` - 处理告警
- `PUT /api/alert/record/{id}/ignore` - 忽略告警
- `GET /api/alert/record/unresolved/count` - 获取未处理告警数

### 通知配置接口
- `GET /api/notification/config/page` - 查询通知配置
- `POST /api/notification/config` - 创建通知配置
- `PUT /api/notification/config/{id}` - 更新通知配置
- `DELETE /api/notification/config/{id}` - 删除通知配置
- `POST /api/notification/test` - 测试通知配置

## 配置说明

### 应用配置 (application.yml)
```yaml
monitor:
  data-collection:
    enabled: true
    system-interval: 300      # 5分钟
    database-interval: 300    # 5分钟
    application-interval: 300 # 5分钟
    security-interval: 300    # 5分钟
  
  alert-check:
    enabled: true
    check-interval: 60        # 1分钟
    repeat-interval: 30       # 30分钟
  
  data-retention:
    metric-days: 30           # 30天
    alert-days: 90            # 90天
    auto-cleanup: true
```

### 通知配置
支持多种通知方式配置：

#### 邮件通知
```yaml
email:
  enabled: true
  smtp-host: smtp.bankshield.com
  smtp-port: 465
  smtp-username: alert@bankshield.com
  smtp-password: ${EMAIL_PASSWORD}
  smtp-ssl: true
```

#### Webhook通知
```yaml
webhook:
  enabled: true
  timeout: 30000
  retry-times: 3
  retry-interval: 5000
```

#### 钉钉通知
```yaml
dingtalk:
  enabled: true
  webhook-url: https://oapi.dingtalk.com/robot/send
  access-token: ${DINGTALK_ACCESS_TOKEN}
  secret: ${DINGTALK_SECRET}
```

## 使用说明

### 1. 初始化配置
运行数据库初始化脚本：
```sql
-- 创建监控相关表
source sql/monitor_module.sql

-- 初始化数据
source sql/init_monitor_data.sql
```

### 2. 配置告警规则
通过管理界面或API配置告警规则：
- 设置监控指标和阈值
- 选择告警级别和通知方式
- 测试规则有效性

### 3. 配置通知渠道
配置各种通知渠道：
- 邮件服务器配置
- 短信服务商配置
- Webhook地址配置
- 钉钉/企业微信机器人配置

### 4. 查看监控数据
- Dashboard查看实时数据
- 趋势图分析历史数据
- 告警记录跟踪处理状态

### 5. 处理告警
- 及时查看告警通知
- 分析告警原因
- 采取相应措施
- 更新告警状态

## 性能优化

### 数据库优化
- 创建合适的索引
- 定期清理历史数据
- 使用连接池

### 缓存优化
- 监控数据缓存
- 告警规则缓存
- 通知频率控制缓存

### 异步处理
- 监控数据采集异步化
- 通知发送异步化
- 使用消息队列

## 安全考虑

### 权限控制
- 基于RBAC的权限管理
- 敏感操作权限验证
- 数据访问权限控制

### 数据安全
- 敏感数据加密存储
- 传输数据加密
- 审计日志记录

### 系统安全
- 输入参数验证
- SQL注入防护
- XSS攻击防护

## 故障排查

### 常见问题
1. **监控数据不更新**
   - 检查定时任务是否正常运行
   - 查看数据采集服务日志
   - 验证数据库连接

2. **告警规则不触发**
   - 检查规则配置是否正确
   - 验证监控指标数据
   - 查看告警引擎日志

3. **通知发送失败**
   - 检查通知配置参数
   - 验证网络连接
   - 查看通知服务日志

### 日志分析
- 监控数据采集日志
- 告警引擎处理日志
- 通知服务发送日志

## 扩展开发

### 自定义监控指标
1. 创建指标实体类
2. 实现数据采集逻辑
3. 配置告警规则
4. 添加前端展示

### 自定义通知方式
1. 实现通知服务接口
2. 配置通知参数
3. 添加前端配置界面
4. 测试通知功能

### 集成第三方系统
1. 提供标准API接口
2. 支持Webhook回调
3. 实现数据格式转换
4. 配置集成参数

## 维护建议

### 日常维护
- 定期检查系统健康度
- 监控告警规则有效性
- 清理过期历史数据
- 更新通知配置

### 性能监控
- 监控模块自身性能
- 数据库性能监控
- 系统资源使用情况
- 告警响应时间

### 备份恢复
- 定期备份配置数据
- 备份历史监控数据
- 测试恢复流程
- 文档化恢复步骤

## 技术支持

如遇到问题，请联系：
- 技术支持：support@bankshield.com
- 文档地址：https://docs.bankshield.com
- 问题反馈：https://github.com/your-org/BankShield/issues

## 版本历史

### v1.0.0 (2024-01)
- 基础监控功能
- 告警规则引擎
- 多渠道通知
- Dashboard展示

### 后续规划
- AI智能告警分析
- 预测性维护
- 移动端支持
- 多云环境适配