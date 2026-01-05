-- =============================================
-- BankShield 数据脱敏引擎升级
-- 版本: v1.0
-- 日期: 2024-12-31
-- 描述: 支持多种脱敏算法和场景的数据脱敏系统
-- =============================================

-- 删除已存在的表（开发环境）
DROP TABLE IF EXISTS desensitization_log;
DROP TABLE IF EXISTS desensitization_template;
DROP TABLE IF EXISTS desensitization_rule;

-- =============================================
-- 1. 脱敏规则表
-- =============================================
CREATE TABLE desensitization_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    data_type VARCHAR(50) NOT NULL COMMENT '数据类型：PHONE,ID_CARD,BANK_CARD,EMAIL,NAME,ADDRESS,CUSTOM',
    algorithm_type VARCHAR(50) NOT NULL COMMENT '脱敏算法：MASK,REPLACE,ENCRYPT,HASH,GENERALIZE,SHUFFLE,TRUNCATE',
    algorithm_config JSON COMMENT '算法配置参数',
    sensitivity_level VARCHAR(20) COMMENT '适用敏感级别：C1,C2,C3,C4,C5',
    apply_scope VARCHAR(50) COMMENT '应用范围：QUERY,EXPORT,DISPLAY,ALL',
    priority INT DEFAULT 100 COMMENT '优先级（数字越小优先级越高）',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态：ENABLED,DISABLED',
    description TEXT COMMENT '规则描述',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_data_type (data_type),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脱敏规则表';

-- =============================================
-- 2. 脱敏模板表
-- =============================================
CREATE TABLE desensitization_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编码',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型：DYNAMIC,STATIC,BATCH',
    target_table VARCHAR(100) COMMENT '目标表名',
    target_fields JSON COMMENT '目标字段配置：[{field, rule_id, rule_code}]',
    role_filter JSON COMMENT '角色过滤：[{role_id, role_name}]',
    condition_filter TEXT COMMENT '条件过滤（SQL WHERE子句）',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态：ENABLED,DISABLED',
    description TEXT COMMENT '模板描述',
    create_by VARCHAR(64) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_type (template_type),
    INDEX idx_target_table (target_table),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脱敏模板表';

-- =============================================
-- 3. 脱敏日志表
-- =============================================
CREATE TABLE desensitization_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    log_type VARCHAR(50) NOT NULL COMMENT '日志类型：DYNAMIC,STATIC,BATCH',
    rule_id BIGINT COMMENT '规则ID',
    rule_code VARCHAR(50) COMMENT '规则编码',
    template_id BIGINT COMMENT '模板ID',
    template_code VARCHAR(50) COMMENT '模板编码',
    user_id VARCHAR(64) COMMENT '操作用户ID',
    user_name VARCHAR(100) COMMENT '操作用户名',
    user_role VARCHAR(100) COMMENT '用户角色',
    target_table VARCHAR(100) COMMENT '目标表',
    target_field VARCHAR(100) COMMENT '目标字段',
    original_value_hash VARCHAR(64) COMMENT '原始值哈希（不存储原始值）',
    desensitized_value VARCHAR(500) COMMENT '脱敏后的值（示例）',
    algorithm_type VARCHAR(50) COMMENT '使用的算法',
    record_count INT DEFAULT 1 COMMENT '处理记录数',
    execution_time INT COMMENT '执行耗时（毫秒）',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    request_uri VARCHAR(200) COMMENT '请求URI',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS,FAILED',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_log_type (log_type),
    INDEX idx_user_id (user_id),
    INDEX idx_target_table (target_table),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脱敏日志表';

-- =============================================
-- 4. 插入默认脱敏规则
-- =============================================

-- 手机号脱敏规则
INSERT INTO desensitization_rule (rule_name, rule_code, data_type, algorithm_type, algorithm_config, sensitivity_level, apply_scope, priority, description) VALUES
('手机号遮盖', 'PHONE_MASK', 'PHONE', 'MASK', '{"pattern": "(\\\\d{3})\\\\d{4}(\\\\d{4})", "replacement": "$1****$2"}', 'C3,C4,C5', 'ALL', 10, '手机号中间4位遮盖为****'),
('手机号加密', 'PHONE_ENCRYPT', 'PHONE', 'ENCRYPT', '{"algorithm": "SM4", "mode": "ECB"}', 'C4,C5', 'EXPORT', 20, '使用SM4算法加密手机号');

-- 身份证脱敏规则
INSERT INTO desensitization_rule (rule_name, rule_code, data_type, algorithm_type, algorithm_config, sensitivity_level, apply_scope, priority, description) VALUES
('身份证遮盖', 'ID_CARD_MASK', 'ID_CARD', 'MASK', '{"pattern": "(\\\\d{6})\\\\d{8}(\\\\d{4})", "replacement": "$1********$2"}', 'C3,C4,C5', 'ALL', 10, '身份证中间8位遮盖为********'),
('身份证哈希', 'ID_CARD_HASH', 'ID_CARD', 'HASH', '{"algorithm": "SM3"}', 'C4,C5', 'EXPORT', 20, '使用SM3算法哈希身份证号');

-- 银行卡脱敏规则
INSERT INTO desensitization_rule (rule_name, rule_code, data_type, algorithm_type, algorithm_config, sensitivity_level, apply_scope, priority, description) VALUES
('银行卡遮盖', 'BANK_CARD_MASK', 'BANK_CARD', 'MASK', '{"pattern": "(\\\\d{4})\\\\d+(\\\\d{4})", "replacement": "$1 **** **** $2"}', 'C3,C4,C5', 'ALL', 10, '银行卡中间位数遮盖'),
('银行卡加密', 'BANK_CARD_ENCRYPT', 'BANK_CARD', 'ENCRYPT', '{"algorithm": "SM4", "mode": "ECB"}', 'C4,C5', 'EXPORT', 20, '使用SM4算法加密银行卡号');

-- 邮箱脱敏规则
INSERT INTO desensitization_rule (rule_name, rule_code, data_type, algorithm_type, algorithm_config, sensitivity_level, apply_scope, priority, description) VALUES
('邮箱遮盖', 'EMAIL_MASK', 'EMAIL', 'MASK', '{"pattern": "(\\\\w{1,3})\\\\w+(@\\\\w+\\\\.\\\\w+)", "replacement": "$1***$2"}', 'C2,C3,C4', 'ALL', 10, '邮箱用户名部分遮盖');

-- 姓名脱敏规则
INSERT INTO desensitization_rule (rule_name, rule_code, data_type, algorithm_type, algorithm_config, sensitivity_level, apply_scope, priority, description) VALUES
('姓名遮盖', 'NAME_MASK', 'NAME', 'MASK', '{"keepFirst": true, "maskChar": "*"}', 'C2,C3,C4', 'ALL', 10, '保留姓氏，名字遮盖'),
('姓名替换', 'NAME_REPLACE', 'NAME', 'REPLACE', '{"replacement": "张**"}', 'C3,C4', 'EXPORT', 20, '替换为通用姓名');

-- 地址脱敏规则
INSERT INTO desensitization_rule (rule_name, rule_code, data_type, algorithm_type, algorithm_config, sensitivity_level, apply_scope, priority, description) VALUES
('地址泛化', 'ADDRESS_GENERALIZE', 'ADDRESS', 'GENERALIZE', '{"level": "CITY"}', 'C2,C3', 'ALL', 10, '地址泛化到城市级别'),
('地址截断', 'ADDRESS_TRUNCATE', 'ADDRESS', 'TRUNCATE', '{"keepLength": 6}', 'C3,C4', 'DISPLAY', 20, '只保留前6个字符');

-- 金额脱敏规则
INSERT INTO desensitization_rule (rule_name, rule_code, data_type, algorithm_type, algorithm_config, sensitivity_level, apply_scope, priority, description) VALUES
('金额泛化', 'AMOUNT_GENERALIZE', 'AMOUNT', 'GENERALIZE', '{"precision": 1000}', 'C2,C3', 'DISPLAY', 10, '金额泛化到千元级别'),
('金额扰乱', 'AMOUNT_SHUFFLE', 'AMOUNT', 'SHUFFLE', '{"range": 0.1}', 'C3,C4', 'EXPORT', 20, '金额加入10%的随机扰动');

-- =============================================
-- 5. 插入默认脱敏模板
-- =============================================

-- 客户信息动态脱敏模板
INSERT INTO desensitization_template (template_name, template_code, template_type, target_table, target_fields, role_filter, description) VALUES
('客户信息查询脱敏', 'CUSTOMER_QUERY_MASK', 'DYNAMIC', 'customer', 
'[
    {"field": "phone", "rule_code": "PHONE_MASK"},
    {"field": "id_card", "rule_code": "ID_CARD_MASK"},
    {"field": "bank_card", "rule_code": "BANK_CARD_MASK"},
    {"field": "email", "rule_code": "EMAIL_MASK"}
]',
'[
    {"role_name": "data_analyst"},
    {"role_name": "customer_service"}
]',
'客户信息查询时的动态脱敏模板');

-- 交易数据导出脱敏模板
INSERT INTO desensitization_template (template_name, template_code, template_type, target_table, target_fields, description) VALUES
('交易数据导出脱敏', 'TRANSACTION_EXPORT_MASK', 'STATIC', 'transaction',
'[
    {"field": "customer_phone", "rule_code": "PHONE_ENCRYPT"},
    {"field": "customer_id_card", "rule_code": "ID_CARD_HASH"},
    {"field": "bank_card", "rule_code": "BANK_CARD_ENCRYPT"},
    {"field": "amount", "rule_code": "AMOUNT_GENERALIZE"}
]',
'交易数据导出时的静态脱敏模板');

-- =============================================
-- 6. 创建视图
-- =============================================

-- 脱敏规则概览视图
CREATE OR REPLACE VIEW v_desensitization_rule_overview AS
SELECT 
    data_type,
    COUNT(*) as rule_count,
    SUM(CASE WHEN status = 'ENABLED' THEN 1 ELSE 0 END) as enabled_count,
    SUM(CASE WHEN status = 'DISABLED' THEN 1 ELSE 0 END) as disabled_count,
    GROUP_CONCAT(DISTINCT algorithm_type) as algorithms
FROM desensitization_rule
GROUP BY data_type;

-- 脱敏日志统计视图
CREATE OR REPLACE VIEW v_desensitization_log_stats AS
SELECT 
    DATE(create_time) as log_date,
    log_type,
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_count,
    SUM(record_count) as total_records,
    AVG(execution_time) as avg_execution_time
FROM desensitization_log
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(create_time), log_type;

-- 用户脱敏操作统计视图
CREATE OR REPLACE VIEW v_user_desensitization_stats AS
SELECT 
    user_id,
    user_name,
    user_role,
    COUNT(*) as operation_count,
    SUM(record_count) as total_records,
    COUNT(DISTINCT target_table) as table_count,
    MAX(create_time) as last_operation_time
FROM desensitization_log
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY user_id, user_name, user_role
ORDER BY operation_count DESC;

-- =============================================
-- 7. 创建存储过程
-- =============================================

DELIMITER //

-- 批量脱敏存储过程
CREATE PROCEDURE sp_batch_desensitize(
    IN p_template_code VARCHAR(50),
    IN p_user_id VARCHAR(64),
    IN p_user_name VARCHAR(100)
)
BEGIN
    DECLARE v_template_id BIGINT;
    DECLARE v_target_table VARCHAR(100);
    DECLARE v_target_fields JSON;
    DECLARE v_record_count INT DEFAULT 0;
    
    -- 获取模板信息
    SELECT id, target_table, target_fields 
    INTO v_template_id, v_target_table, v_target_fields
    FROM desensitization_template
    WHERE template_code = p_template_code AND status = 'ENABLED';
    
    IF v_template_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '脱敏模板不存在或未启用';
    END IF;
    
    -- 记录日志
    INSERT INTO desensitization_log (
        log_type, template_id, template_code, user_id, user_name,
        target_table, record_count, status
    ) VALUES (
        'BATCH', v_template_id, p_template_code, p_user_id, p_user_name,
        v_target_table, v_record_count, 'SUCCESS'
    );
    
    SELECT CONCAT('批量脱敏任务已创建，模板：', p_template_code) as message;
END //

-- 清理过期日志存储过程
CREATE PROCEDURE sp_clean_desensitization_logs(
    IN p_days INT
)
BEGIN
    DECLARE v_deleted_count INT;
    
    DELETE FROM desensitization_log
    WHERE create_time < DATE_SUB(CURDATE(), INTERVAL p_days DAY);
    
    SET v_deleted_count = ROW_COUNT();
    
    SELECT CONCAT('已清理 ', v_deleted_count, ' 条过期日志') as message;
END //

DELIMITER ;

-- =============================================
-- 8. 创建触发器
-- =============================================

-- 脱敏规则更新触发器
DELIMITER //
CREATE TRIGGER tr_desensitization_rule_update
BEFORE UPDATE ON desensitization_rule
FOR EACH ROW
BEGIN
    -- 记录规则变更（可以扩展到审计表）
    SET NEW.update_time = CURRENT_TIMESTAMP;
END //
DELIMITER ;

-- =============================================
-- 9. 创建索引优化
-- =============================================

-- 为脱敏日志表创建复合索引
CREATE INDEX idx_log_user_time ON desensitization_log(user_id, create_time);
CREATE INDEX idx_log_table_time ON desensitization_log(target_table, create_time);

-- =============================================
-- 完成提示
-- =============================================
SELECT '数据脱敏引擎数据库初始化完成！' as status,
       '已创建3个表、3个视图、2个存储过程、1个触发器' as summary,
       '已插入13条默认脱敏规则、2个默认模板' as data_status;
