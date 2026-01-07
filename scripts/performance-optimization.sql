-- BankShield 数据库性能优化SQL脚本
-- 执行日期: 2026-01-07
-- 目标: 优化慢查询，提升数据库性能

-- ==================== 索引优化 ====================

-- 1. 审计日志表索引优化
-- 审计日志按时间范围查询频繁，添加复合索引
CREATE INDEX IF NOT EXISTS idx_audit_log_time_level 
ON audit_log(create_time DESC, log_level);

CREATE INDEX IF NOT EXISTS idx_audit_log_user_time 
ON audit_log(user_id, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_audit_log_operation_time 
ON audit_log(operation_type, create_time DESC);

-- 2. 告警记录表索引优化
CREATE INDEX IF NOT EXISTS idx_alert_record_status_time 
ON alert_record(alert_status, alert_time DESC);

CREATE INDEX IF NOT EXISTS idx_alert_record_level_time 
ON alert_record(alert_level, alert_time DESC);

CREATE INDEX IF NOT EXISTS idx_alert_record_rule_time 
ON alert_record(rule_id, alert_time DESC);

-- 3. 数据资产表索引优化
CREATE INDEX IF NOT EXISTS idx_data_asset_classification 
ON data_asset(classification_level, update_time DESC);

CREATE INDEX IF NOT EXISTS idx_data_asset_sensitive 
ON data_asset(is_sensitive, update_time DESC);

CREATE INDEX IF NOT EXISTS idx_data_asset_table_field 
ON data_asset(table_name, field_name);

-- 4. 脱敏日志表索引优化
CREATE INDEX IF NOT EXISTS idx_desensitization_log_time 
ON desensitization_log(create_time DESC);

CREATE INDEX IF NOT EXISTS idx_desensitization_log_user 
ON desensitization_log(user_name, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_desensitization_log_table 
ON desensitization_log(target_table, create_time DESC);

-- 5. 合规检查结果表索引优化
CREATE INDEX IF NOT EXISTS idx_compliance_check_time 
ON compliance_check_result(check_time DESC);

CREATE INDEX IF NOT EXISTS idx_compliance_check_status 
ON compliance_check_result(check_status, check_time DESC);

-- ==================== 查询优化 ====================

-- 6. 创建审计日志统计视图（避免重复计算）
CREATE OR REPLACE VIEW v_audit_log_statistics AS
SELECT 
    DATE(create_time) as log_date,
    log_level,
    COUNT(*) as log_count,
    COUNT(DISTINCT user_id) as user_count
FROM audit_log
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(create_time), log_level;

-- 7. 创建告警统计视图
CREATE OR REPLACE VIEW v_alert_statistics AS
SELECT 
    DATE(alert_time) as alert_date,
    alert_level,
    alert_status,
    COUNT(*) as alert_count,
    AVG(TIMESTAMPDIFF(MINUTE, alert_time, handle_time)) as avg_handle_minutes
FROM alert_record
WHERE alert_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(alert_time), alert_level, alert_status;

-- 8. 创建数据资产统计视图
CREATE OR REPLACE VIEW v_data_asset_statistics AS
SELECT 
    classification_level,
    is_sensitive,
    COUNT(*) as asset_count,
    COUNT(DISTINCT table_name) as table_count
FROM data_asset
GROUP BY classification_level, is_sensitive;

-- ==================== 分区表优化 ====================

-- 9. 审计日志表按月分区（如果数据量大）
-- 注意：需要先备份数据，然后重建表
/*
ALTER TABLE audit_log
PARTITION BY RANGE (TO_DAYS(create_time)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
*/

-- ==================== 慢查询优化 ====================

-- 10. 优化审计日志分页查询
-- 使用覆盖索引避免回表
CREATE INDEX IF NOT EXISTS idx_audit_log_covering 
ON audit_log(create_time DESC, id, user_id, operation_type, log_level);

-- 11. 优化告警记录关联查询
-- 添加外键索引
CREATE INDEX IF NOT EXISTS idx_alert_record_rule_fk 
ON alert_record(rule_id);

-- ==================== 数据归档策略 ====================

-- 12. 创建归档表（用于存储历史数据）
CREATE TABLE IF NOT EXISTS audit_log_archive LIKE audit_log;

-- 13. 创建归档存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_archive_audit_logs(IN days_to_keep INT)
BEGIN
    DECLARE archive_date DATE;
    SET archive_date = DATE_SUB(CURDATE(), INTERVAL days_to_keep DAY);
    
    -- 开始事务
    START TRANSACTION;
    
    -- 复制到归档表
    INSERT INTO audit_log_archive
    SELECT * FROM audit_log
    WHERE create_time < archive_date;
    
    -- 删除旧数据
    DELETE FROM audit_log
    WHERE create_time < archive_date;
    
    -- 提交事务
    COMMIT;
    
    SELECT CONCAT('归档完成，归档日期: ', archive_date) as result;
END //
DELIMITER ;

-- ==================== 表维护 ====================

-- 14. 优化表结构
OPTIMIZE TABLE audit_log;
OPTIMIZE TABLE alert_record;
OPTIMIZE TABLE data_asset;
OPTIMIZE TABLE desensitization_log;

-- 15. 分析表统计信息
ANALYZE TABLE audit_log;
ANALYZE TABLE alert_record;
ANALYZE TABLE data_asset;
ANALYZE TABLE desensitization_log;

-- ==================== 配置优化建议 ====================

-- 16. 查看当前慢查询配置
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- 建议配置（需要在my.cnf中设置）:
-- slow_query_log = ON
-- slow_query_log_file = /var/log/mysql/slow-query.log
-- long_query_time = 1
-- log_queries_not_using_indexes = ON

-- ==================== 性能监控查询 ====================

-- 17. 查询最慢的SQL（需要开启慢查询日志）
-- SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;

-- 18. 查询表大小和索引大小
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb,
    ROUND((data_length / 1024 / 1024), 2) AS data_mb,
    ROUND((index_length / 1024 / 1024), 2) AS index_mb,
    table_rows
FROM information_schema.tables
WHERE table_schema = DATABASE()
AND table_name IN ('audit_log', 'alert_record', 'data_asset', 'desensitization_log')
ORDER BY (data_length + index_length) DESC;

-- 19. 查询未使用的索引
SELECT 
    t.table_schema,
    t.table_name,
    s.index_name,
    s.column_name
FROM information_schema.tables t
INNER JOIN information_schema.statistics s 
    ON t.table_schema = s.table_schema 
    AND t.table_name = s.table_name
LEFT JOIN information_schema.index_statistics i 
    ON t.table_schema = i.table_schema 
    AND t.table_name = i.table_name 
    AND s.index_name = i.index_name
WHERE t.table_schema = DATABASE()
AND i.index_name IS NULL
AND s.index_name != 'PRIMARY'
ORDER BY t.table_name, s.index_name;

-- ==================== 执行完成 ====================
SELECT '数据库性能优化脚本执行完成' as status;
