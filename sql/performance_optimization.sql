-- BankShield数据库性能优化脚本
-- 创建时间和用户：2025-12-24
-- 主要包含索引优化、查询优化、分区等

USE bankshield;

-- 1. 审计表性能优化
-- 创建复合索引优化查询性能
CREATE INDEX idx_audit_operation_user_time ON audit_operation(user_id, create_time);
CREATE INDEX idx_audit_operation_type_time ON audit_operation(operation_type, create_time DESC);
CREATE INDEX idx_audit_operation_result_time ON audit_operation(operation_result, create_time DESC);

-- 2. 用户表性能优化
-- 优化用户查询性能
CREATE INDEX idx_sys_user_dept_status ON sys_user(dept_id, status);
CREATE INDEX idx_sys_user_create_time ON sys_user(create_time);
CREATE INDEX idx_sys_user_username_status ON sys_user(username, status);

-- 3. 角色权限相关表优化
-- 优化角色权限查询
CREATE INDEX idx_sys_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role(role_id);
CREATE INDEX idx_sys_role_menu_role_id ON sys_role_menu(role_id);
CREATE INDEX idx_sys_role_menu_menu_id ON sys_role_menu(menu_id);

-- 4. 数据脱敏规则表优化
-- 优化脱敏规则查询
CREATE INDEX idx_masking_rule_table_column ON masking_rule(table_name, column_name);
CREATE INDEX idx_masking_rule_type ON masking_rule(masking_type);
CREATE INDEX idx_masking_rule_status ON masking_rule(status, create_time);

-- 5. 密钥管理表优化
-- 优化密钥查询性能
CREATE INDEX idx_security_key_key_type ON security_key(key_type, status);
CREATE INDEX idx_security_key_create_time ON security_key(create_time DESC);
CREATE INDEX idx_security_key_expire_time ON security_key(expire_time);

-- 6. 分类分级表优化
-- 优化分类分级查询
CREATE INDEX idx_data_asset_classification ON data_asset(classification_level, sensitivity_level);
CREATE INDEX idx_data_asset_dept_id ON data_asset(dept_id);
CREATE INDEX idx_data_asset_create_time ON data_asset(create_time DESC);

-- 7. 水印表优化
-- 优化水印查询
CREATE INDEX idx_watermark_template_type ON watermark_template(template_type, status);
CREATE INDEX idx_watermark_log_create_time ON watermark_log(create_time DESC);
CREATE INDEX idx_watermark_log_user_id ON watermark_log(user_id);

-- 8. 监控数据表优化
-- 优化监控数据查询
CREATE INDEX idx_monitor_metric_metric_name ON monitor_metric(metric_name, create_time DESC);
CREATE INDEX idx_monitor_alert_alert_level ON monitor_alert(alert_level, status);
CREATE INDEX idx_monitor_alert_create_time ON monitor_alert(create_time DESC);

-- 9. 大表分区优化
-- 审计表按时间分区
ALTER TABLE audit_operation 
PARTITION BY RANGE (YEAR(create_time)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 监控数据表按时间分区
ALTER TABLE monitor_metric
PARTITION BY RANGE (TO_DAYS(create_time)) (
    PARTITION p_current_week VALUES LESS THAN (TO_DAYS(DATE_ADD(CURDATE(), INTERVAL 7 DAY))),
    PARTITION p_next_week VALUES LESS THAN (TO_DAYS(DATE_ADD(CURDATE(), INTERVAL 14 DAY))),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 10. 删除冗余索引（如果存在）
-- 检查并删除可能被复合索引覆盖的冗余索引
DROP INDEX IF EXISTS idx_audit_create_time ON audit_operation;
DROP INDEX IF EXISTS idx_sys_user_status ON sys_user;
DROP INDEX IF EXISTS idx_security_key_status ON security_key;

-- 11. 分析表更新统计信息
ANALYZE TABLE audit_operation;
ANALYZE TABLE sys_user;
ANALYZE TABLE sys_role;
ANALYZE TABLE sys_user_role;
ANALYZE TABLE security_key;
ANALYZE TABLE data_asset;

-- 12. 慢查询监控配置
-- 开启慢查询日志（需要在MySQL配置文件中设置）
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1;
-- SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 13. 查询优化建议
-- 以下查询语句已经过优化，可以直接使用

-- 优化用户角色查询（避免N+1问题）
EXPLAIN SELECT u.*, r.role_name, r.role_code 
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.status = 1 AND u.create_time > '2024-01-01'
ORDER BY u.create_time DESC
LIMIT 100;

-- 优化审计日志查询
EXPLAIN SELECT operation_type, COUNT(*) as count, AVG(execution_time) as avg_time
FROM audit_operation 
WHERE create_time BETWEEN '2024-01-01' AND '2024-12-31'
  AND user_id IN (SELECT id FROM sys_user WHERE dept_id = 1)
GROUP BY operation_type
ORDER BY count DESC;

-- 优化密钥状态查询
EXPLAIN SELECT key_type, COUNT(*) as total_count,
       SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as active_count,
       SUM(CASE WHEN expire_time < NOW() THEN 1 ELSE 0 END) as expired_count
FROM security_key
WHERE create_time > DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY key_type;

-- 14. 性能监控视图
CREATE OR REPLACE VIEW performance_audit_summary AS
SELECT 
    DATE(create_time) as audit_date,
    operation_type,
    COUNT(*) as operation_count,
    AVG(execution_time) as avg_execution_time,
    MAX(execution_time) as max_execution_time,
    SUM(CASE WHEN operation_result = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN operation_result = 'FAILURE' THEN 1 ELSE 0 END) as failure_count
FROM audit_operation 
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(create_time), operation_type;

-- 15. 索引使用监控视图
CREATE OR REPLACE VIEW index_usage_stats AS
SELECT 
    table_name,
    index_name,
    cardinality,
    sub_part,
    packed,
    NULLABLE,
    index_type,
    comment,
    index_comment
FROM information_schema.STATISTICS 
WHERE table_schema = 'bankshield'
ORDER BY table_name, index_name;

SELECT '数据库性能优化脚本执行完成' as message;