-- BankShield监控告警模块初始化数据

-- 初始化监控指标数据（模拟数据）
INSERT INTO monitor_metric (metric_name, metric_type, metric_value, metric_unit, threshold, status, collect_time, description, related_resource) VALUES
('CPU使用率', 'SYSTEM', 45.2, '%', 80.0, 'NORMAL', NOW(), '服务器CPU使用率', 'system'),
('内存使用率', 'SYSTEM', 62.8, '%', 85.0, 'NORMAL', NOW(), '服务器内存使用率', 'system'),
('磁盘使用率', 'SYSTEM', 38.5, '%', 90.0, 'NORMAL', NOW(), '服务器磁盘使用率', 'system'),
('数据库连接数', 'DATABASE', 23, '个', 100.0, 'NORMAL', NOW(), 'MySQL数据库连接数', 'database'),
('活跃用户数', 'SECURITY', 15, '人', 50.0, 'NORMAL', NOW(), '系统活跃用户数量', 'security'),
('应用线程数', 'SERVICE', 89, '个', 200.0, 'NORMAL', NOW(), '应用线程数量', 'application'),
('GC次数', 'SERVICE', 12, '次', 100.0, 'NORMAL', NOW(), '垃圾回收次数', 'application'),
('慢查询数量', 'DATABASE', 2, '个', 5.0, 'NORMAL', NOW(), '数据库慢查询数量', 'database'),
('异常登录次数', 'SECURITY', 0, '次', 3.0, 'NORMAL', NOW(), '异常登录尝试次数', 'security');

-- 初始化告警规则（生产环境配置）
INSERT INTO alert_rule (rule_name, rule_type, monitor_metric, trigger_condition, threshold, alert_level, description) VALUES
('CPU使用率过高告警', 'SYSTEM', 'CPU使用率', '>', 80, 'WARNING', '当CPU使用率超过80%触发告警，建议检查系统负载'),
('CPU使用率严重告警', 'SYSTEM', 'CPU使用率', '>', 95, 'CRITICAL', '当CPU使用率超过95%触发严重告警，需要立即处理'),
('内存使用率过高告警', 'SYSTEM', '内存使用率', '>', 85, 'CRITICAL', '当内存使用率超过85%触发严重告警，可能导致系统崩溃'),
('磁盘使用率过高告警', 'SYSTEM', '磁盘使用率', '>', 90, 'CRITICAL', '当磁盘使用率超过90%触发严重告警，需要清理磁盘空间'),
('数据库连接数过多告警', 'DATABASE', '数据库连接数', '>', 100, 'WARNING', '当数据库连接数超过100触发告警，可能存在连接泄漏'),
('数据库连接数严重告警', 'DATABASE', '数据库连接数', '>', 150, 'CRITICAL', '当数据库连接数超过150触发严重告警，数据库可能无法响应'),
('应用线程数过多告警', 'SERVICE', '应用线程数', '>', 200, 'WARNING', '当应用线程数超过200触发告警，可能存在线程泄漏'),
('应用线程数严重告警', 'SERVICE', '应用线程数', '>', 300, 'CRITICAL', '当应用线程数超过300触发严重告警，应用可能崩溃'),
('异常登录次数告警', 'SECURITY', '异常登录次数', '>', 3, 'WARNING', '当异常登录次数超过3次触发告警，可能存在暴力破解'),
('异常登录次数严重告警', 'SECURITY', '异常登录次数', '>', 10, 'CRITICAL', '当异常登录次数超过10次触发严重告警，系统可能遭受攻击'),
('慢查询数量告警', 'DATABASE', '慢查询数量', '>', 5, 'WARNING', '当慢查询数量超过5个触发告警，数据库性能下降'),
('慢查询数量严重告警', 'DATABASE', '慢查询数量', '>', 15, 'CRITICAL', '当慢查询数量超过15个触发严重告警，数据库性能严重下降'),
('GC次数过多告警', 'SERVICE', 'GC次数', '>', 100, 'WARNING', '当GC次数超过100次触发告警，可能存在内存泄漏'),
('GC次数严重告警', 'SERVICE', 'GC次数', '>', 200, 'CRITICAL', '当GC次数超过200次触发严重告警，JVM性能严重下降');

-- 初始化通知配置（生产环境配置）
INSERT INTO notification_config (notify_type, recipients, notify_template, config_params, description) VALUES
('EMAIL', 'admin@bankshield.com,ops@bankshield.com', '【BankShield告警】{{title}}', '{"smtp_host":"smtp.bankshield.com","smtp_port":465,"smtp_username":"alert@bankshield.com","smtp_password":"your_password","smtp_ssl":true,"smtp_auth":true}', '系统管理员邮箱告警配置'),
('EMAIL', 'security@bankshield.com', '【BankShield安全告警】{{title}}', '{"smtp_host":"smtp.bankshield.com","smtp_port":465,"smtp_username":"alert@bankshield.com","smtp_password":"your_password","smtp_ssl":true,"smtp_auth":true}', '安全管理员邮箱告警配置'),
('WEBHOOK', 'https://hook.bankshield.com/alert', '{"title":"{{title}}","content":"{{content}}","level":"{{level}}","time":"{{time}}","system":"BankShield"}', '{"method":"POST","headers":{"Content-Type":"application/json","Authorization":"Bearer your_token"},"timeout":30000}', '系统Webhook告警配置'),
('DINGTALK', 'https://oapi.dingtalk.com/robot/send?access_token=your_token', '## 【BankShield告警】\n\n**告警标题:** {{title}}\n\n**告警内容:** {{content}}\n\n**告警级别:** {{level}}\n\n**告警时间:** {{time}}\n\n**系统名称:** BankShield\n\n请及时处理此告警！', '{"access_token":"your_token","secret":"your_secret","at_all":false}', '钉钉群机器人告警配置'),
('WECHAT', 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=your_key', '{"msgtype":"text","text":{"content":"【BankShield告警】\n告警标题: {{title}}\n告警内容: {{content}}\n告警级别: {{level}}\n告警时间: {{time}}\n请及时处理此告警！"}}', '{"webhook_url":"https://qyapi.weixin.qq.com/cgi-bin/webhook/send","access_token":"your_key"}', '企业微信群机器人告警配置');

-- 初始化历史监控数据（最近7天模拟数据）
INSERT INTO monitor_metric (metric_name, metric_type, metric_value, metric_unit, threshold, status, collect_time, description, related_resource)
SELECT 
  'CPU使用率' as metric_name,
  'SYSTEM' as metric_type,
  ROUND(30 + RAND() * 40, 2) as metric_value,
  '%' as metric_unit,
  80.0 as threshold,
  CASE WHEN ROUND(30 + RAND() * 40, 2) > 80 THEN 'WARNING' ELSE 'NORMAL' END as status,
  DATE_SUB(NOW(), INTERVAL n DAY) as collect_time,
  '服务器CPU使用率' as description,
  'system' as related_resource
FROM (SELECT 0 as n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) days;

-- 初始化历史告警数据（最近7天模拟数据）
INSERT INTO alert_record (rule_id, alert_level, alert_title, alert_content, alert_time, alert_status, handler, handle_time, handle_remark, notify_status)
SELECT 
  1 + FLOOR(RAND() * 14) as rule_id,
  CASE WHEN RAND() > 0.8 THEN 'CRITICAL' WHEN RAND() > 0.5 THEN 'WARNING' ELSE 'INFO' END as alert_level,
  CASE 
    WHEN 1 + FLOOR(RAND() * 14) = 1 THEN 'CPU使用率过高告警'
    WHEN 1 + FLOOR(RAND() * 14) = 2 THEN 'CPU使用率严重告警'
    WHEN 1 + FLOOR(RAND() * 14) = 3 THEN '内存使用率过高告警'
    WHEN 1 + FLOOR(RAND() * 14) = 4 THEN '磁盘使用率过高告警'
    WHEN 1 + FLOOR(RAND() * 14) = 5 THEN '数据库连接数过多告警'
    WHEN 1 + FLOOR(RAND() * 14) = 6 THEN '数据库连接数严重告警'
    WHEN 1 + FLOOR(RAND() * 14) = 7 THEN '应用线程数过多告警'
    WHEN 1 + FLOOR(RAND() * 14) = 8 THEN '应用线程数严重告警'
    WHEN 1 + FLOOR(RAND() * 14) = 9 THEN '异常登录次数告警'
    WHEN 1 + FANDOM() * 14) = 10 THEN '异常登录次数严重告警'
    WHEN 1 + FLOOR(RAND() * 14) = 11 THEN '慢查询数量告警'
    WHEN 1 + FLOOR(RAND() * 14) = 12 THEN '慢查询数量严重告警'
    WHEN 1 + FLOOR(RAND() * 14) = 13 THEN 'GC次数过多告警'
    WHEN 1 + FLOOR(RAND() * 14) = 14 THEN 'GC次数严重告警'
  END as alert_title,
  '监控指标触发告警规则，请及时处理' as alert_content,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY) as alert_time,
  CASE WHEN RAND() > 0.7 THEN 'RESOLVED' WHEN RAND() > 0.4 THEN 'UNRESOLVED' ELSE 'IGNORED' END as alert_status,
  CASE WHEN RAND() > 0.7 THEN 'admin' ELSE NULL END as handler,
  CASE WHEN RAND() > 0.7 THEN DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 3) DAY) ELSE NULL END as handle_time,
  CASE WHEN RAND() > 0.7 THEN '已处理完毕' ELSE NULL END as handle_remark,
  CASE WHEN RAND() > 0.9 THEN 'FAILED' WHEN RAND() > 0.7 THEN 'NOTIFIED' ELSE 'UNNOTIFIED' END as notify_status
FROM (SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) nums;

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_monitor_metric_time_type ON monitor_metric(collect_time, metric_type);
CREATE INDEX IF NOT EXISTS idx_alert_record_time_status ON alert_record(alert_time, alert_status);
CREATE INDEX IF NOT EXISTS idx_alert_record_rule_time ON alert_record(rule_id, alert_time);
CREATE INDEX IF NOT EXISTS idx_notification_config_type_status ON notification_config(notify_type, enabled);