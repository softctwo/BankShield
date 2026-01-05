-- =============================================
-- BankShield 数据分类分级升级 SQL脚本
-- 从3级扩展到5级（C1-C5）符合JR/T 0197-2020标准
-- =============================================

-- 1. 扩展数据资产表，添加5级分类字段
ALTER TABLE data_asset 
ADD COLUMN sensitivity_level VARCHAR(10) COMMENT '敏感级别：C1-公开,C2-内部,C3-敏感,C4-高敏感,C5-极敏感',
ADD COLUMN classification_method VARCHAR(20) DEFAULT 'MANUAL' COMMENT '分级方式：MANUAL-手动,AUTO-自动',
ADD COLUMN classification_time DATETIME COMMENT '分级时间',
ADD COLUMN classification_reason TEXT COMMENT '分级原因',
ADD COLUMN last_review_time DATETIME COMMENT '最后审核时间',
ADD COLUMN reviewer VARCHAR(50) COMMENT '审核人';

-- 2. 创建数据分级规则表
CREATE TABLE IF NOT EXISTS data_classification_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) UNIQUE NOT NULL COMMENT '规则编码',
    data_type VARCHAR(50) COMMENT '数据类型',
    field_pattern VARCHAR(200) COMMENT '字段匹配模式（正则表达式）',
    content_pattern VARCHAR(200) COMMENT '内容匹配模式（正则表达式）',
    sensitivity_level VARCHAR(10) NOT NULL COMMENT '敏感级别',
    priority INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    rule_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '规则状态：ACTIVE-启用,INACTIVE-禁用',
    description TEXT COMMENT '规则描述',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_code (rule_code),
    INDEX idx_sensitivity_level (sensitivity_level),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分级规则表';

-- 3. 创建分级历史表
CREATE TABLE IF NOT EXISTS data_classification_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    old_level VARCHAR(10) COMMENT '原级别',
    new_level VARCHAR(10) NOT NULL COMMENT '新级别',
    classification_method VARCHAR(20) COMMENT '分级方式',
    reason TEXT COMMENT '变更原因',
    operator VARCHAR(50) COMMENT '操作人',
    classify_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分级时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_classify_time (classify_time),
    INDEX idx_operator (operator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分级历史表';

-- 4. 创建分级审核表
CREATE TABLE IF NOT EXISTS data_classification_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    proposed_level VARCHAR(10) NOT NULL COMMENT '建议级别',
    current_level VARCHAR(10) COMMENT '当前级别',
    review_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核,APPROVED-已通过,REJECTED-已拒绝',
    review_comment TEXT COMMENT '审核意见',
    submitter VARCHAR(50) COMMENT '提交人',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    reviewer VARCHAR(50) COMMENT '审核人',
    review_time DATETIME COMMENT '审核时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_review_status (review_status),
    INDEX idx_submit_time (submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分级审核表';

-- 5. 创建分级统计表
CREATE TABLE IF NOT EXISTS data_classification_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    level_c1_count INT DEFAULT 0 COMMENT 'C1级数量',
    level_c2_count INT DEFAULT 0 COMMENT 'C2级数量',
    level_c3_count INT DEFAULT 0 COMMENT 'C3级数量',
    level_c4_count INT DEFAULT 0 COMMENT 'C4级数量',
    level_c5_count INT DEFAULT 0 COMMENT 'C5级数量',
    total_count INT DEFAULT 0 COMMENT '总数量',
    auto_classified_count INT DEFAULT 0 COMMENT '自动分级数量',
    manual_classified_count INT DEFAULT 0 COMMENT '手动分级数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_stat_date (stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分级统计表';

-- 6. 插入默认分级规则
INSERT INTO data_classification_rule (rule_name, rule_code, data_type, field_pattern, content_pattern, sensitivity_level, priority, description, create_by) VALUES
-- C5级：极敏感数据
('身份证号识别', 'RULE_ID_CARD', 'PERSONAL', '(id_card|idcard|身份证)', '\\d{17}[0-9Xx]', 'C5', 100, '识别身份证号码', 'system'),
('银行卡号识别', 'RULE_BANK_CARD', 'FINANCIAL', '(card_no|cardno|卡号|银行卡)', '\\d{16,19}', 'C5', 100, '识别银行卡号', 'system'),
('密码字段识别', 'RULE_PASSWORD', 'SECURITY', '(password|passwd|pwd|密码)', '.*', 'C5', 100, '识别密码字段', 'system'),
('生物特征识别', 'RULE_BIOMETRIC', 'PERSONAL', '(fingerprint|face|iris|指纹|人脸|虹膜)', '.*', 'C5', 100, '识别生物特征数据', 'system'),

-- C4级：高敏感数据
('手机号识别', 'RULE_MOBILE', 'PERSONAL', '(mobile|phone|手机)', '1[3-9]\\d{9}', 'C4', 90, '识别手机号码', 'system'),
('邮箱地址识别', 'RULE_EMAIL', 'PERSONAL', '(email|mail|邮箱)', '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}', 'C4', 90, '识别邮箱地址', 'system'),
('账户余额识别', 'RULE_BALANCE', 'FINANCIAL', '(balance|余额|账户金额)', '\\d+\\.?\\d*', 'C4', 90, '识别账户余额', 'system'),
('交易金额识别', 'RULE_AMOUNT', 'FINANCIAL', '(amount|金额|交易额)', '\\d+\\.?\\d*', 'C4', 90, '识别交易金额', 'system'),

-- C3级：敏感数据
('姓名识别', 'RULE_NAME', 'PERSONAL', '(name|姓名|真实姓名)', '.*', 'C3', 80, '识别姓名', 'system'),
('地址识别', 'RULE_ADDRESS', 'PERSONAL', '(address|地址|住址)', '.*', 'C3', 80, '识别地址信息', 'system'),
('交易记录识别', 'RULE_TRANSACTION', 'FINANCIAL', '(transaction|交易记录)', '.*', 'C3', 80, '识别交易记录', 'system'),
('用户权限识别', 'RULE_PERMISSION', 'SECURITY', '(permission|权限|角色)', '.*', 'C3', 80, '识别权限数据', 'system'),

-- C2级：内部数据
('内部编号识别', 'RULE_INTERNAL_CODE', 'INTERNAL', '(code|编号|工号)', '.*', 'C2', 70, '识别内部编号', 'system'),
('部门信息识别', 'RULE_DEPARTMENT', 'INTERNAL', '(department|dept|部门)', '.*', 'C2', 70, '识别部门信息', 'system'),
('操作日志识别', 'RULE_OPERATION_LOG', 'INTERNAL', '(log|日志|操作记录)', '.*', 'C2', 70, '识别操作日志', 'system'),

-- C1级：公开数据
('公开文档识别', 'RULE_PUBLIC_DOC', 'PUBLIC', '(public|公开|公告)', '.*', 'C1', 60, '识别公开文档', 'system'),
('系统配置识别', 'RULE_CONFIG', 'PUBLIC', '(config|配置|参数)', '.*', 'C1', 60, '识别系统配置', 'system');

-- 7. 创建分级规则应用日志表
CREATE TABLE IF NOT EXISTS data_classification_rule_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_code VARCHAR(50) COMMENT '规则编码',
    asset_id BIGINT COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    match_result VARCHAR(20) COMMENT '匹配结果：MATCHED-匹配,NOT_MATCHED-不匹配',
    matched_field VARCHAR(100) COMMENT '匹配字段',
    matched_content TEXT COMMENT '匹配内容',
    classified_level VARCHAR(10) COMMENT '分级结果',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '应用时间',
    INDEX idx_rule_id (rule_id),
    INDEX idx_asset_id (asset_id),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分级规则应用日志表';

-- 8. 创建视图：分级概览
CREATE OR REPLACE VIEW v_classification_overview AS
SELECT 
    sensitivity_level,
    COUNT(*) as asset_count,
    COUNT(CASE WHEN classification_method = 'AUTO' THEN 1 END) as auto_count,
    COUNT(CASE WHEN classification_method = 'MANUAL' THEN 1 END) as manual_count,
    MAX(classification_time) as last_classification_time
FROM data_asset
WHERE sensitivity_level IS NOT NULL
GROUP BY sensitivity_level;

-- 9. 创建存储过程：自动分级
DELIMITER //
CREATE PROCEDURE sp_auto_classify_data()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_asset_id BIGINT;
    DECLARE v_asset_name VARCHAR(200);
    DECLARE v_data_type VARCHAR(50);
    DECLARE v_suggested_level VARCHAR(10);
    
    DECLARE asset_cursor CURSOR FOR 
        SELECT id, name, type FROM data_asset WHERE sensitivity_level IS NULL OR sensitivity_level = '';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN asset_cursor;
    
    read_loop: LOOP
        FETCH asset_cursor INTO v_asset_id, v_asset_name, v_data_type;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 根据规则自动分级（简化逻辑，实际应用中需要更复杂的匹配）
        SELECT sensitivity_level INTO v_suggested_level
        FROM data_classification_rule
        WHERE rule_status = 'ACTIVE'
        AND (data_type = v_data_type OR data_type IS NULL)
        ORDER BY priority DESC
        LIMIT 1;
        
        IF v_suggested_level IS NOT NULL THEN
            UPDATE data_asset 
            SET sensitivity_level = v_suggested_level,
                classification_method = 'AUTO',
                classification_time = NOW(),
                classification_reason = '自动分级引擎'
            WHERE id = v_asset_id;
            
            INSERT INTO data_classification_history (asset_id, asset_name, old_level, new_level, classification_method, reason, operator, classify_time)
            VALUES (v_asset_id, v_asset_name, NULL, v_suggested_level, 'AUTO', '自动分级引擎', 'system', NOW());
        END IF;
    END LOOP;
    
    CLOSE asset_cursor;
END //
DELIMITER ;

-- 10. 创建定时统计任务（需要配合Quartz使用）
-- 每日凌晨1点统计前一天的分级数据
INSERT INTO data_classification_statistics (stat_date, level_c1_count, level_c2_count, level_c3_count, level_c4_count, level_c5_count, total_count, auto_classified_count, manual_classified_count)
SELECT 
    CURDATE() - INTERVAL 1 DAY as stat_date,
    SUM(CASE WHEN sensitivity_level = 'C1' THEN 1 ELSE 0 END) as level_c1_count,
    SUM(CASE WHEN sensitivity_level = 'C2' THEN 1 ELSE 0 END) as level_c2_count,
    SUM(CASE WHEN sensitivity_level = 'C3' THEN 1 ELSE 0 END) as level_c3_count,
    SUM(CASE WHEN sensitivity_level = 'C4' THEN 1 ELSE 0 END) as level_c4_count,
    SUM(CASE WHEN sensitivity_level = 'C5' THEN 1 ELSE 0 END) as level_c5_count,
    COUNT(*) as total_count,
    SUM(CASE WHEN classification_method = 'AUTO' THEN 1 ELSE 0 END) as auto_classified_count,
    SUM(CASE WHEN classification_method = 'MANUAL' THEN 1 ELSE 0 END) as manual_classified_count
FROM data_asset
WHERE sensitivity_level IS NOT NULL
ON DUPLICATE KEY UPDATE
    level_c1_count = VALUES(level_c1_count),
    level_c2_count = VALUES(level_c2_count),
    level_c3_count = VALUES(level_c3_count),
    level_c4_count = VALUES(level_c4_count),
    level_c5_count = VALUES(level_c5_count),
    total_count = VALUES(total_count),
    auto_classified_count = VALUES(auto_classified_count),
    manual_classified_count = VALUES(manual_classified_count);

-- 11. 创建索引优化查询性能
CREATE INDEX idx_asset_sensitivity_level ON data_asset(sensitivity_level);
CREATE INDEX idx_asset_classification_time ON data_asset(classification_time);
CREATE INDEX idx_asset_classification_method ON data_asset(classification_method);

-- 完成
SELECT '数据分类分级升级SQL脚本执行完成！' as message;
