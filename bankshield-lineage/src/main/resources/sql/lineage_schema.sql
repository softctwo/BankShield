-- 数据血缘模块数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS bankshield_lineage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bankshield_lineage;

-- 血缘节点表
CREATE TABLE IF NOT EXISTS data_lineage_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_type VARCHAR(50) NOT NULL COMMENT '节点类型: TABLE/COLUMN/REPORT/ETL',
    node_name VARCHAR(200) NOT NULL COMMENT '节点名称',
    node_code VARCHAR(200) NOT NULL COMMENT '节点编码（唯一标识）',
    data_source_id BIGINT COMMENT '数据源ID',
    database_name VARCHAR(100) COMMENT '数据库名称',
    schema_name VARCHAR(100) COMMENT 'Schema名称',
    table_name VARCHAR(100) COMMENT '表名称',
    column_name VARCHAR(100) COMMENT '字段名称',
    data_type VARCHAR(50) COMMENT '数据类型',
    description TEXT COMMENT '描述信息',
    properties TEXT COMMENT '扩展属性JSON',
    quality_score DOUBLE COMMENT '质量评分',
    importance_level VARCHAR(20) COMMENT '重要性级别：HIGH/MEDIUM/LOW',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_node_type (node_type),
    INDEX idx_data_source (data_source_id),
    INDEX idx_table_name (table_name),
    INDEX idx_node_code (node_code),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_node_code (node_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘节点表';

-- 血缘边表
CREATE TABLE IF NOT EXISTS data_lineage_edge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_node_id BIGINT NOT NULL COMMENT '源节点ID',
    target_node_id BIGINT NOT NULL COMMENT '目标节点ID',
    relationship_type VARCHAR(50) COMMENT '关系类型：DIRECT/INDIRECT/VIEW/ETL',
    transformation TEXT COMMENT '转换逻辑描述',
    transformation_sql TEXT COMMENT '转换SQL',
    impact_weight INT DEFAULT 1 COMMENT '影响权重（1-10）',
    path_depth INT DEFAULT 1 COMMENT '血缘路径深度',
    active TINYINT(1) DEFAULT 1 COMMENT '是否活跃',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_source (source_node_id),
    INDEX idx_target (target_node_id),
    INDEX idx_relationship (relationship_type),
    INDEX idx_active (active),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_relation (source_node_id, target_node_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘边表';

-- 数据质量规则表
CREATE TABLE IF NOT EXISTS data_quality_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) COMMENT '规则类型：完整性/准确性/一致性/及时性',
    description TEXT COMMENT '规则描述',
    check_sql TEXT NOT NULL COMMENT '检查SQL',
    threshold DOUBLE COMMENT '阈值（百分比）',
    weight DOUBLE DEFAULT 1.0 COMMENT '权重',
    severity VARCHAR(20) COMMENT '严重程度：HIGH/MEDIUM/LOW',
    target_table_id BIGINT COMMENT '目标表ID',
    target_column_id BIGINT COMMENT '目标字段ID',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    execution_cron VARCHAR(50) COMMENT '调度表达式',
    last_execution_time DATETIME COMMENT '最后执行时间',
    last_execution_result TEXT COMMENT '最后执行结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    version INT DEFAULT 1 COMMENT '版本号',
    INDEX idx_rule_type (rule_type),
    INDEX idx_target_table (target_table_id),
    INDEX idx_enabled (enabled),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_rule_name (rule_name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据质量规则表';

-- 数据质量结果表
CREATE TABLE IF NOT EXISTS data_quality_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    table_id BIGINT NOT NULL COMMENT '目标表ID',
    column_id BIGINT COMMENT '目标字段ID',
    quality_score DOUBLE COMMENT '质量评分',
    check_result TEXT COMMENT '检查结果',
    check_status VARCHAR(20) COMMENT '检查状态：SUCCESS/FAILURE/WARNING',
    error_count BIGINT DEFAULT 0 COMMENT '错误数量',
    total_count BIGINT DEFAULT 0 COMMENT '总数量',
    error_samples TEXT COMMENT '错误样本',
    execution_time BIGINT COMMENT '执行时间（毫秒）',
    check_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(50) COMMENT '创建人',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_rule_id (rule_id),
    INDEX idx_table_id (table_id),
    INDEX idx_column_id (column_id),
    INDEX idx_check_time (check_time),
    INDEX idx_check_status (check_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据质量结果表';

-- 数据源配置表
CREATE TABLE IF NOT EXISTS data_source_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    source_type VARCHAR(50) NOT NULL COMMENT '数据源类型：MYSQL/ORACLE/POSTGRESQL',
    connection_url TEXT NOT NULL COMMENT '连接URL',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码',
    driver_class VARCHAR(200) COMMENT '驱动类名',
    connection_properties TEXT COMMENT '连接属性',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    last_test_time DATETIME COMMENT '最后测试时间',
    last_test_result VARCHAR(20) COMMENT '最后测试结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_source_type (source_type),
    INDEX idx_enabled (enabled),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_source_name (source_name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- SQL执行日志表（用于血缘发现）
CREATE TABLE IF NOT EXISTS sql_execution_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    data_source_id BIGINT NOT NULL COMMENT '数据源ID',
    sql_text TEXT NOT NULL COMMENT 'SQL语句',
    sql_type VARCHAR(20) COMMENT 'SQL类型：SELECT/INSERT/UPDATE/DELETE',
    execution_time BIGINT COMMENT '执行时间（毫秒）',
    execution_status VARCHAR(20) COMMENT '执行状态：SUCCESS/FAILURE',
    error_message TEXT COMMENT '错误信息',
    execution_time_stamp DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间戳',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_data_source (data_source_id),
    INDEX idx_sql_type (sql_type),
    INDEX idx_execution_status (execution_status),
    INDEX idx_execution_time (execution_time_stamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQL执行日志表';

-- ETL任务配置表
CREATE TABLE IF NOT EXISTS etl_task_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：KETTLE/DATAX/SQOOP',
    task_config TEXT NOT NULL COMMENT '任务配置（JSON）',
    source_connection_id BIGINT COMMENT '源连接ID',
    target_connection_id BIGINT COMMENT '目标连接ID',
    schedule_cron VARCHAR(50) COMMENT '调度表达式',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    last_execution_time DATETIME COMMENT '最后执行时间',
    last_execution_status VARCHAR(20) COMMENT '最后执行状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_task_type (task_type),
    INDEX idx_enabled (enabled),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_task_name (task_name, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETL任务配置表';

-- 插入示例数据
INSERT INTO data_lineage_node (node_type, node_name, node_code, database_name, table_name, description, importance_level, create_by, update_by) VALUES
('DATABASE', '银行核心系统', 'core_banking', 'core_banking', NULL, '银行核心业务系统数据库', 'HIGH', 'system', 'system'),
('DATABASE', '数据仓库', 'data_warehouse', 'data_warehouse', NULL, '企业级数据仓库', 'HIGH', 'system', 'system'),
('TABLE', '客户信息表', 'core_banking.customer_info', 'core_banking', 'customer_info', '存储客户基本信息', 'HIGH', 'system', 'system'),
('TABLE', '账户信息表', 'core_banking.account_info', 'core_banking', 'account_info', '存储账户基本信息', 'HIGH', 'system', 'system'),
('TABLE', '交易流水表', 'core_banking.transaction_log', 'core_banking', 'transaction_log', '存储交易流水记录', 'HIGH', 'system', 'system'),
('TABLE', '客户维度表', 'data_warehouse.dim_customer', 'data_warehouse', 'dim_customer', '客户维度数据', 'MEDIUM', 'system', 'system'),
('TABLE', '账户维度表', 'data_warehouse.dim_account', 'data_warehouse', 'dim_account', '账户维度数据', 'MEDIUM', 'system', 'system'),
('TABLE', '交易事实表', 'data_warehouse.fact_transaction', 'data_warehouse', 'fact_transaction', '交易事实数据', 'HIGH', 'system', 'system');

-- 插入血缘关系示例数据
INSERT INTO data_lineage_edge (source_node_id, target_node_id, relationship_type, transformation, impact_weight, path_depth, create_by, update_by) VALUES
(3, 6, 'ETL', '客户数据抽取转换', 8, 1, 'system', 'system'),
(4, 7, 'ETL', '账户数据抽取转换', 8, 1, 'system', 'system'),
(5, 8, 'ETL', '交易数据抽取转换', 10, 1, 'system', 'system'),
(6, 8, 'DIRECT', '客户维度关联', 5, 1, 'system', 'system'),
(7, 8, 'DIRECT', '账户维度关联', 5, 1, 'system', 'system');

-- 插入数据质量规则示例数据
INSERT INTO data_quality_rule (rule_name, rule_type, description, check_sql, threshold, weight, severity, target_table_id, execution_cron, create_by, update_by) VALUES
('客户信息完整性检查', 'COMPLETENESS', '检查客户信息完整性', 'SELECT COUNT(*) FROM customer_info WHERE customer_name IS NULL OR id_number IS NULL', 95.0, 2.0, 'HIGH', 3, '0 0 */6 * * ?', 'system', 'system'),
('账户状态一致性检查', 'CONSISTENCY', '检查账户状态一致性', 'SELECT COUNT(*) FROM account_info WHERE account_status NOT IN ("ACTIVE", "INACTIVE", "CLOSED")', 99.0, 1.5, 'MEDIUM', 4, '0 0 */12 * * ?', 'system', 'system'),
('交易金额有效性检查', 'VALIDITY', '检查交易金额有效性', 'SELECT COUNT(*) FROM transaction_log WHERE transaction_amount <= 0', 99.5, 2.0, 'HIGH', 5, '0 */30 * * * ?', 'system', 'system');

-- 创建视图
CREATE OR REPLACE VIEW v_lineage_summary AS
SELECT 
    n.node_type,
    n.node_name,
    n.database_name,
    n.table_name,
    COUNT(e.id) as downstream_count,
    n.quality_score,
    n.importance_level,
    n.create_time
FROM data_lineage_node n
LEFT JOIN data_lineage_edge e ON n.id = e.source_node_id AND e.active = 1 AND e.deleted = 0
WHERE n.deleted = 0
GROUP BY n.id;

CREATE OR REPLACE VIEW v_quality_summary AS
SELECT 
    r.rule_name,
    r.rule_type,
    r.severity,
    r.threshold,
    AVG(q.quality_score) as avg_score,
    COUNT(q.id) as check_count,
    MAX(q.check_time) as last_check_time
FROM data_quality_rule r
LEFT JOIN data_quality_result q ON r.id = q.rule_id AND q.deleted = 0
WHERE r.deleted = 0 AND r.enabled = 1
GROUP BY r.id;

-- 创建索引优化查询性能
CREATE INDEX idx_node_quality_score ON data_lineage_node(quality_score);
CREATE INDEX idx_node_importance ON data_lineage_node(importance_level);
CREATE INDEX idx_edge_impact_weight ON data_lineage_edge(impact_weight);
CREATE INDEX idx_quality_result_score ON data_quality_result(quality_score);
CREATE INDEX idx_sql_log_timestamp ON sql_execution_log(execution_time_stamp);

-- 数据保留策略（30天前数据自动清理）
CREATE EVENT IF NOT EXISTS clean_old_lineage_data
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
    DELETE FROM data_quality_result WHERE check_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

CREATE EVENT IF NOT EXISTS clean_old_sql_logs
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
    DELETE FROM sql_execution_log WHERE execution_time_stamp < DATE_SUB(NOW(), INTERVAL 7 DAY);