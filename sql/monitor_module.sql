-- BankShield监控告警模块数据库脚本

-- 监控指标表
CREATE TABLE IF NOT EXISTS monitor_metric (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
  metric_type VARCHAR(50) COMMENT '指标类型: SYSTEM/SECURITY/DATABASE/SERVICE',
  metric_value DOUBLE COMMENT '指标值',
  metric_unit VARCHAR(20) COMMENT '指标单位',
  threshold DOUBLE COMMENT '阈值',
  status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态: NORMAL/WARNING/CRITICAL',
  collect_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  description TEXT,
  related_resource VARCHAR(200) COMMENT '关联资源',
  INDEX idx_metric_type (metric_type),
  INDEX idx_collect_time (collect_time),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控指标表';

-- 告警规则表
CREATE TABLE IF NOT EXISTS alert_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
  rule_type VARCHAR(50) COMMENT '规则类型',
  monitor_metric VARCHAR(100) COMMENT '监控指标',
  trigger_condition VARCHAR(10) COMMENT '触发条件: > < = >= <= !=',
  threshold DOUBLE COMMENT '阈值',
  alert_level VARCHAR(20) COMMENT '告警级别: INFO/WARNING/CRITICAL/EMERGENCY',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description TEXT,
  UNIQUE KEY uk_rule_name (rule_name),
  INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS alert_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_id BIGINT COMMENT '告警规则ID',
  alert_level VARCHAR(20) COMMENT '告警级别',
  alert_title VARCHAR(200) NOT NULL COMMENT '告警标题',
  alert_content TEXT COMMENT '告警内容',
  alert_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
  alert_status VARCHAR(20) DEFAULT 'UNRESOLVED' COMMENT '告警状态: UNRESOLVED/RESOLVED/IGNORED',
  handler VARCHAR(50) COMMENT '处理人',
  handle_time DATETIME COMMENT '处理时间',
  handle_remark TEXT COMMENT '处理备注',
  notify_status VARCHAR(20) DEFAULT 'UNNOTIFIED' COMMENT '通知状态',
  INDEX idx_rule_id (rule_id),
  INDEX idx_alert_time (alert_time),
  INDEX idx_alert_status (alert_status),
  INDEX idx_alert_level (alert_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- 通知配置表
CREATE TABLE IF NOT EXISTS notification_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  notify_type VARCHAR(50) NOT NULL COMMENT '通知类型: EMAIL/SMS/WEBHOOK/DINGTALK/WECHAT',
  recipients VARCHAR(500) NOT NULL COMMENT '接收人',
  notify_template TEXT COMMENT '通知模板',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  config_params TEXT COMMENT '配置参数(JSON)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description TEXT,
  INDEX idx_notify_type (notify_type),
  INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知配置表';

-- 初始化监控指标类型
INSERT INTO monitor_metric (metric_name, metric_type, metric_unit, description) VALUES
('CPU使用率', 'SYSTEM', '%', '服务器CPU使用率'),
('内存使用率', 'SYSTEM', '%', '服务器内存使用率'),
('磁盘使用率', 'SYSTEM', '%', '服务器磁盘使用率'),
('数据库连接数', 'DATABASE', '个', 'MySQL数据库连接数'),
('活跃用户数', 'SECURITY', '人', '系统活跃用户数量'),
('应用线程数', 'SERVICE', '个', '应用线程数量'),
('GC次数', 'SERVICE', '次', 'JVM垃圾回收次数'),
('慢查询数量', 'DATABASE', '个', '数据库慢查询数量'),
('异常登录次数', 'SECURITY', '次', '异常登录尝试次数') 
ON DUPLICATE KEY UPDATE metric_type=VALUES(metric_type), metric_unit=VALUES(metric_unit), description=VALUES(description);

-- 初始化告警规则（示例）
INSERT INTO alert_rule (rule_name, rule_type, monitor_metric, trigger_condition, threshold, alert_level, description) VALUES
('CPU使用率过高告警', 'SYSTEM', 'CPU使用率', '>', 80, 'WARNING', '当CPU使用率超过80%触发告警'),
('内存使用率过高告警', 'SYSTEM', '内存使用率', '>', 85, 'CRITICAL', '当内存使用率超过85%触发严重告警'),
('磁盘使用率过高告警', 'SYSTEM', '磁盘使用率', '>', 90, 'CRITICAL', '当磁盘使用率超过90%触发严重告警'),
('数据库连接数过多告警', 'DATABASE', '数据库连接数', '>', 100, 'WARNING', '当数据库连接数超过100触发告警'),
('应用线程数过多告警', 'SERVICE', '应用线程数', '>', 200, 'WARNING', '当应用线程数超过200触发告警'),
('异常登录次数告警', 'SECURITY', '异常登录次数', '>', 3, 'WARNING', '当异常登录次数超过3次触发告警'),
('慢查询数量告警', 'DATABASE', '慢查询数量', '>', 5, 'WARNING', '当慢查询数量超过5个触发告警')
ON DUPLICATE KEY UPDATE rule_type=VALUES(rule_type), monitor_metric=VALUES(monitor_metric), 
                      trigger_condition=VALUES(trigger_condition), threshold=VALUES(threshold), 
                      alert_level=VALUES(alert_level), description=VALUES(description);

-- 初始化通知配置（示例）
INSERT INTO notification_config (notify_type, recipients, notify_template, config_params, description) VALUES
('EMAIL', 'admin@bankshield.com', '系统告警: {{title}}', '{"smtp_host":"smtp.bankshield.com","smtp_port":465,"smtp_username":"alert@bankshield.com","smtp_password":"password","smtp_ssl":true}', '管理员邮箱告警配置'),
('WEBHOOK', 'https://hook.bankshield.com/alert', '{"title":"{{title}}","content":"{{content}}","level":"{{level}}","time":"{{time}}"}', '{"method":"POST","headers":{"Content-Type":"application/json"}}', 'Webhook告警配置')
ON DUPLICATE KEY UPDATE notify_type=VALUES(notify_type), recipients=VALUES(recipients), 
                      notify_template=VALUES(notify_template), config_params=VALUES(config_params), 
                      description=VALUES(description);

-- 添加外键约束（可选，根据实际需求）
-- ALTER TABLE alert_record ADD CONSTRAINT fk_alert_record_rule 
--   FOREIGN KEY (rule_id) REFERENCES alert_rule(id) ON DELETE SET NULL;