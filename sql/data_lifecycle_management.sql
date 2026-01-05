-- =============================================
-- BankShield 数据生命周期管理 SQL脚本
-- 实现数据采集、存储、使用、归档、销毁全生命周期管理
-- =============================================

-- 1. 创建生命周期策略表
CREATE TABLE IF NOT EXISTS lifecycle_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '策略ID',
    policy_name VARCHAR(100) NOT NULL COMMENT '策略名称',
    policy_code VARCHAR(50) NOT NULL COMMENT '策略编码',
    data_type VARCHAR(50) COMMENT '数据类型：DATABASE,FILE,BIGDATA',
    sensitivity_level VARCHAR(10) COMMENT '敏感级别：C1,C2,C3,C4,C5',
    retention_days INT NOT NULL COMMENT '保留天数',
    archive_enabled TINYINT DEFAULT 1 COMMENT '是否启用归档：1-是，0-否',
    archive_days INT COMMENT '归档天数（数据创建后多少天归档）',
    archive_storage VARCHAR(200) COMMENT '归档存储位置',
    destroy_enabled TINYINT DEFAULT 1 COMMENT '是否启用销毁：1-是，0-否',
    destroy_days INT COMMENT '销毁天数（数据创建后多少天销毁）',
    destroy_method VARCHAR(50) COMMENT '销毁方法：LOGICAL,PHYSICAL,OVERWRITE',
    approval_required TINYINT DEFAULT 0 COMMENT '是否需要审批：1-是，0-否',
    notification_enabled TINYINT DEFAULT 1 COMMENT '是否启用通知：1-是，0-否',
    notification_days INT DEFAULT 7 COMMENT '提前通知天数',
    policy_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '策略状态：ACTIVE,INACTIVE',
    priority INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    description TEXT COMMENT '策略描述',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_policy_code (policy_code),
    INDEX idx_data_type (data_type),
    INDEX idx_sensitivity_level (sensitivity_level),
    INDEX idx_policy_status (policy_status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生命周期策略表';

-- 2. 创建生命周期执行记录表
CREATE TABLE IF NOT EXISTS lifecycle_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行ID',
    policy_id BIGINT NOT NULL COMMENT '策略ID',
    execution_type VARCHAR(50) NOT NULL COMMENT '执行类型：ARCHIVE,DESTROY',
    asset_id BIGINT COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    asset_type VARCHAR(50) COMMENT '资产类型',
    execution_status VARCHAR(20) NOT NULL COMMENT '执行状态：PENDING,RUNNING,SUCCESS,FAILED',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '执行耗时（毫秒）',
    affected_count INT DEFAULT 0 COMMENT '影响记录数',
    error_message TEXT COMMENT '错误信息',
    executor VARCHAR(50) COMMENT '执行人',
    execution_mode VARCHAR(20) DEFAULT 'AUTO' COMMENT '执行模式：AUTO,MANUAL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_policy_id (policy_id),
    INDEX idx_execution_type (execution_type),
    INDEX idx_execution_status (execution_status),
    INDEX idx_asset_id (asset_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (policy_id) REFERENCES lifecycle_policy(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生命周期执行记录表';

-- 3. 创建归档数据表
CREATE TABLE IF NOT EXISTS archived_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '归档ID',
    original_table VARCHAR(100) NOT NULL COMMENT '原始表名',
    original_id BIGINT NOT NULL COMMENT '原始记录ID',
    data_content LONGTEXT COMMENT '数据内容（JSON格式）',
    archive_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    archive_storage VARCHAR(200) COMMENT '归档存储位置',
    policy_id BIGINT COMMENT '策略ID',
    asset_type VARCHAR(50) COMMENT '资产类型',
    sensitivity_level VARCHAR(10) COMMENT '敏感级别',
    retention_until DATETIME COMMENT '保留截止日期',
    is_destroyed TINYINT DEFAULT 0 COMMENT '是否已销毁：1-是，0-否',
    destroy_time DATETIME COMMENT '销毁时间',
    create_by VARCHAR(50) COMMENT '创建人',
    INDEX idx_original_table (original_table),
    INDEX idx_original_id (original_id),
    INDEX idx_archive_time (archive_time),
    INDEX idx_retention_until (retention_until),
    INDEX idx_is_destroyed (is_destroyed),
    INDEX idx_policy_id (policy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档数据表';

-- 4. 创建销毁记录表
CREATE TABLE IF NOT EXISTS destruction_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '销毁ID',
    asset_id BIGINT COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    asset_type VARCHAR(50) COMMENT '资产类型',
    data_type VARCHAR(50) COMMENT '数据类型',
    sensitivity_level VARCHAR(10) COMMENT '敏感级别',
    destruction_method VARCHAR(50) NOT NULL COMMENT '销毁方法：LOGICAL,PHYSICAL,OVERWRITE',
    destruction_reason TEXT COMMENT '销毁原因',
    data_snapshot LONGTEXT COMMENT '数据快照（销毁前的数据）',
    destruction_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '销毁时间',
    policy_id BIGINT COMMENT '策略ID',
    approval_status VARCHAR(20) COMMENT '审批状态：PENDING,APPROVED,REJECTED',
    approver VARCHAR(50) COMMENT '审批人',
    approval_time DATETIME COMMENT '审批时间',
    approval_comment TEXT COMMENT '审批意见',
    executor VARCHAR(50) COMMENT '执行人',
    verification_hash VARCHAR(128) COMMENT '验证哈希（用于验证销毁完整性）',
    is_verified TINYINT DEFAULT 0 COMMENT '是否已验证：1-是，0-否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_asset_type (asset_type),
    INDEX idx_destruction_time (destruction_time),
    INDEX idx_policy_id (policy_id),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销毁记录表';

-- 5. 创建生命周期通知表
CREATE TABLE IF NOT EXISTS lifecycle_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    notification_type VARCHAR(50) NOT NULL COMMENT '通知类型：ARCHIVE_WARNING,DESTROY_WARNING,EXECUTION_SUCCESS,EXECUTION_FAILED',
    policy_id BIGINT COMMENT '策略ID',
    asset_id BIGINT COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    notification_title VARCHAR(200) COMMENT '通知标题',
    notification_content TEXT COMMENT '通知内容',
    recipient VARCHAR(100) COMMENT '接收人',
    notification_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '通知状态：PENDING,SENT,FAILED',
    send_time DATETIME COMMENT '发送时间',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_notification_type (notification_type),
    INDEX idx_policy_id (policy_id),
    INDEX idx_notification_status (notification_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生命周期通知表';

-- 6. 创建生命周期统计表
CREATE TABLE IF NOT EXISTS lifecycle_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_policies INT DEFAULT 0 COMMENT '总策略数',
    active_policies INT DEFAULT 0 COMMENT '活跃策略数',
    total_archives INT DEFAULT 0 COMMENT '总归档数',
    today_archives INT DEFAULT 0 COMMENT '今日归档数',
    total_destructions INT DEFAULT 0 COMMENT '总销毁数',
    today_destructions INT DEFAULT 0 COMMENT '今日销毁数',
    pending_approvals INT DEFAULT 0 COMMENT '待审批数',
    execution_success_count INT DEFAULT 0 COMMENT '执行成功数',
    execution_failed_count INT DEFAULT 0 COMMENT '执行失败数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat_date (stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生命周期统计表';

-- 7. 插入默认策略
INSERT INTO lifecycle_policy (policy_name, policy_code, data_type, sensitivity_level, retention_days, archive_days, destroy_days, destroy_method, description, create_by) VALUES
('C5极敏感数据策略', 'POLICY_C5', 'DATABASE', 'C5', 2555, 1825, 2555, 'OVERWRITE', 'C5级极敏感数据：保留7年，5年后归档，7年后物理销毁', 'system'),
('C4高敏感数据策略', 'POLICY_C4', 'DATABASE', 'C4', 1825, 1095, 1825, 'OVERWRITE', 'C4级高敏感数据：保留5年，3年后归档，5年后物理销毁', 'system'),
('C3敏感数据策略', 'POLICY_C3', 'DATABASE', 'C3', 1095, 730, 1095, 'PHYSICAL', 'C3级敏感数据：保留3年，2年后归档，3年后物理删除', 'system'),
('C2内部数据策略', 'POLICY_C2', 'DATABASE', 'C2', 730, 365, 730, 'LOGICAL', 'C2级内部数据：保留2年，1年后归档，2年后逻辑删除', 'system'),
('C1公开数据策略', 'POLICY_C1', 'DATABASE', 'C1', 365, 180, 365, 'LOGICAL', 'C1级公开数据：保留1年，半年后归档，1年后逻辑删除', 'system'),
('审计日志策略', 'POLICY_AUDIT', 'DATABASE', 'C3', 1825, 365, 1825, 'PHYSICAL', '审计日志：保留5年，1年后归档，5年后物理删除', 'system'),
('临时文件策略', 'POLICY_TEMP', 'FILE', 'C1', 30, NULL, 30, 'PHYSICAL', '临时文件：保留30天后直接物理删除', 'system');

-- 8. 创建视图：生命周期概览
CREATE OR REPLACE VIEW v_lifecycle_overview AS
SELECT 
    COUNT(DISTINCT lp.id) as total_policies,
    COUNT(DISTINCT CASE WHEN lp.policy_status = 'ACTIVE' THEN lp.id END) as active_policies,
    COUNT(DISTINCT ad.id) as total_archived,
    COUNT(DISTINCT dr.id) as total_destroyed,
    COUNT(DISTINCT CASE WHEN le.execution_status = 'PENDING' THEN le.id END) as pending_executions,
    COUNT(DISTINCT CASE WHEN dr.approval_status = 'PENDING' THEN dr.id END) as pending_approvals
FROM lifecycle_policy lp
LEFT JOIN archived_data ad ON lp.id = ad.policy_id
LEFT JOIN destruction_record dr ON lp.id = dr.policy_id
LEFT JOIN lifecycle_execution le ON lp.id = le.policy_id;

-- 9. 创建视图：待归档数据
CREATE OR REPLACE VIEW v_pending_archive AS
SELECT 
    da.id as asset_id,
    da.asset_name,
    da.asset_type,
    da.sensitivity_level,
    lp.policy_name,
    lp.archive_days,
    da.create_time as asset_create_time,
    DATE_ADD(da.create_time, INTERVAL lp.archive_days DAY) as archive_due_date,
    DATEDIFF(DATE_ADD(da.create_time, INTERVAL lp.archive_days DAY), NOW()) as days_until_archive
FROM data_asset da
INNER JOIN lifecycle_policy lp ON da.sensitivity_level = lp.sensitivity_level AND lp.policy_status = 'ACTIVE'
WHERE lp.archive_enabled = 1
  AND DATEDIFF(NOW(), da.create_time) >= lp.archive_days
  AND NOT EXISTS (SELECT 1 FROM archived_data ad WHERE ad.original_id = da.id AND ad.original_table = 'data_asset');

-- 10. 创建视图：待销毁数据
CREATE OR REPLACE VIEW v_pending_destruction AS
SELECT 
    ad.id as archive_id,
    ad.original_table,
    ad.original_id,
    ad.asset_type,
    ad.sensitivity_level,
    lp.policy_name,
    lp.destroy_days,
    ad.archive_time,
    ad.retention_until,
    DATEDIFF(ad.retention_until, NOW()) as days_until_destroy
FROM archived_data ad
INNER JOIN lifecycle_policy lp ON ad.policy_id = lp.id
WHERE lp.destroy_enabled = 1
  AND ad.is_destroyed = 0
  AND ad.retention_until <= NOW();

-- 11. 创建存储过程：自动归档数据
DELIMITER //
CREATE PROCEDURE sp_auto_archive_data(IN p_policy_id BIGINT, OUT p_archived_count INT)
BEGIN
    DECLARE v_archive_days INT;
    DECLARE v_data_type VARCHAR(50);
    DECLARE v_sensitivity_level VARCHAR(10);
    
    -- 获取策略信息
    SELECT archive_days, data_type, sensitivity_level
    INTO v_archive_days, v_data_type, v_sensitivity_level
    FROM lifecycle_policy
    WHERE id = p_policy_id AND policy_status = 'ACTIVE' AND archive_enabled = 1;
    
    -- 归档符合条件的数据（简化处理，实际应该在应用层处理）
    SET p_archived_count = 0;
    
    -- 这里应该根据策略归档数据
    -- 实际实现在Java服务层
END //
DELIMITER ;

-- 12. 创建存储过程：自动销毁数据
DELIMITER //
CREATE PROCEDURE sp_auto_destroy_data(IN p_policy_id BIGINT, OUT p_destroyed_count INT)
BEGIN
    DECLARE v_destroy_method VARCHAR(50);
    
    -- 获取策略信息
    SELECT destroy_method
    INTO v_destroy_method
    FROM lifecycle_policy
    WHERE id = p_policy_id AND policy_status = 'ACTIVE' AND destroy_enabled = 1;
    
    -- 销毁符合条件的数据
    UPDATE archived_data 
    SET is_destroyed = 1, destroy_time = NOW()
    WHERE policy_id = p_policy_id 
      AND is_destroyed = 0 
      AND retention_until <= NOW();
    
    SET p_destroyed_count = ROW_COUNT();
END //
DELIMITER ;

-- 13. 创建触发器：更新统计信息
DELIMITER //
CREATE TRIGGER tr_after_archive_insert
AFTER INSERT ON archived_data
FOR EACH ROW
BEGIN
    INSERT INTO lifecycle_statistics (stat_date, total_archives, today_archives)
    VALUES (CURDATE(), 1, 1)
    ON DUPLICATE KEY UPDATE
        total_archives = total_archives + 1,
        today_archives = today_archives + 1;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER tr_after_destruction_insert
AFTER INSERT ON destruction_record
FOR EACH ROW
BEGIN
    INSERT INTO lifecycle_statistics (stat_date, total_destructions, today_destructions)
    VALUES (CURDATE(), 1, 1)
    ON DUPLICATE KEY UPDATE
        total_destructions = total_destructions + 1,
        today_destructions = today_destructions + 1;
END //
DELIMITER ;

-- 14. 创建索引优化查询性能
CREATE INDEX idx_asset_create_time ON data_asset(create_time);
CREATE INDEX idx_archive_retention ON archived_data(retention_until, is_destroyed);

-- 完成
SELECT '数据生命周期管理SQL脚本执行完成！' as message;
SELECT '已创建7个默认策略' as policies;
