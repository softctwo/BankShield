-- BankShield系统密钥轮换机制优化 - 数据库更新脚本
-- 添加平滑轮换所需的字段和索引

-- 1. 更新加密密钥表，添加轮换相关字段
ALTER TABLE encrypt_key 
ADD COLUMN IF NOT EXISTS rotation_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '轮换状态' AFTER key_status;

ALTER TABLE encrypt_key 
ADD COLUMN IF NOT EXISTS rotation_start_time DATETIME COMMENT '轮换开始时间' AFTER rotation_status;

ALTER TABLE encrypt_key 
ADD COLUMN IF NOT EXISTS prev_key_id BIGINT COMMENT '前向加密密钥ID（用于解密旧数据）' AFTER rotation_start_time;

ALTER TABLE encrypt_key 
ADD COLUMN IF NOT EXISTS next_key_id BIGINT COMMENT '下一密钥ID（已轮换的新密钥）' AFTER prev_key_id;

ALTER TABLE encrypt_key 
ADD COLUMN IF NOT EXISTS rotation_complete_time DATETIME COMMENT '轮换完成预计时间' AFTER next_key_id;

-- 2. 创建轮换状态索引
CREATE INDEX IF NOT EXISTS idx_rotation_status ON encrypt_key(rotation_status);
CREATE INDEX IF NOT EXISTS idx_rotation_complete_time ON encrypt_key(rotation_complete_time);
CREATE INDEX IF NOT EXISTS idx_prev_key_id ON encrypt_key(prev_key_id);
CREATE INDEX IF NOT EXISTS idx_next_key_id ON encrypt_key(next_key_id);

-- 3. 更新数据源表，支持双密钥模式
ALTER TABLE data_source 
ADD COLUMN IF NOT EXISTS secondary_encrypt_key_id BIGINT COMMENT '备用加密密钥ID' AFTER encrypt_key_id;

ALTER TABLE data_source 
ADD COLUMN IF NOT EXISTS dual_encrypt_mode TINYINT(1) DEFAULT 0 COMMENT '是否双密钥模式' AFTER secondary_encrypt_key_id;

-- 4. 添加轮换计划表（可选，如果需要持久化轮换计划）
CREATE TABLE IF NOT EXISTS key_rotation_plan (
    id VARCHAR(50) PRIMARY KEY COMMENT '轮换计划ID',
    old_key_id BIGINT NOT NULL COMMENT '旧密钥ID',
    new_key_id BIGINT NOT NULL COMMENT '新密钥ID',
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT '计划状态',
    rotation_reason VARCHAR(500) COMMENT '轮换原因',
    warm_up_start_time DATETIME COMMENT '预热期开始时间',
    warm_up_end_time DATETIME COMMENT '预热期结束时间',
    dual_active_start_time DATETIME COMMENT '双密钥期开始时间',
    dual_active_end_time DATETIME COMMENT '双密钥期结束时间',
    decrypt_only_start_time DATETIME COMMENT '仅解密期开始时间',
    decrypt_only_end_time DATETIME COMMENT '仅解密期结束时间',
    complete_time DATETIME COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_old_key_id (old_key_id),
    INDEX idx_new_key_id (new_key_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密钥轮换计划表';

-- 5. 更新轮换历史表，添加更多字段
ALTER TABLE key_rotation_history 
ADD COLUMN IF NOT EXISTS plan_id VARCHAR(50) COMMENT '轮换计划ID' AFTER id;

ALTER TABLE key_rotation_history 
ADD COLUMN IF NOT EXISTS phase VARCHAR(50) COMMENT '轮换阶段' AFTER rotation_status;

ALTER TABLE key_rotation_history 
ADD COLUMN IF NOT EXISTS rotation_complete_time DATETIME COMMENT '轮换完成时间' AFTER rotation_time;

-- 6. 创建轮换监控视图
CREATE OR REPLACE VIEW key_rotation_monitor_view AS
SELECT 
    ek.id,
    ek.key_name,
    ek.key_type,
    ek.key_status,
    ek.rotation_status,
    ek.rotation_start_time,
    ek.rotation_complete_time,
    ek.expire_time,
    ek.prev_key_id,
    ek.next_key_id,
    prev_key.key_name as prev_key_name,
    next_key.key_name as next_key_name,
    CASE 
        WHEN ek.rotation_status = 'WARMING_UP' THEN '预热期'
        WHEN ek.rotation_status = 'DUAL_ACTIVE' THEN '双密钥期'
        WHEN ek.rotation_status = 'DECRYPT_ONLY' THEN '仅解密期'
        WHEN ek.rotation_status = 'EXPIRED' THEN '已过期'
        ELSE '活跃'
    END as rotation_status_desc,
    CASE 
        WHEN ek.expire_time IS NOT NULL AND ek.expire_time <= DATE_ADD(NOW(), INTERVAL 30 DAY) THEN 1
        ELSE 0
    END as is_expiring_soon
FROM encrypt_key ek
LEFT JOIN encrypt_key prev_key ON ek.prev_key_id = prev_key.id
LEFT JOIN encrypt_key next_key ON ek.next_key_id = next_key.id
WHERE ek.deleted = 0;

-- 7. 添加轮换相关的存储过程
DELIMITER //

-- 获取需要轮换的密钥列表
CREATE PROCEDURE IF NOT EXISTS sp_get_keys_need_rotation(
    IN days_ahead INT
)
BEGIN
    SELECT 
        ek.*,
        prev_key.key_name as prev_key_name,
        next_key.key_name as next_key_name
    FROM encrypt_key ek
    LEFT JOIN encrypt_key prev_key ON ek.prev_key_id = prev_key.id
    LEFT JOIN encrypt_key next_key ON ek.next_key_id = next_key.id
    WHERE ek.key_status = 'ACTIVE'
    AND ek.rotation_status = 'ACTIVE'
    AND ek.expire_time <= DATE_ADD(NOW(), INTERVAL days_ahead DAY)
    AND ek.deleted = 0
    ORDER BY ek.expire_time ASC;
END //

-- 获取轮换统计信息
CREATE PROCEDURE IF NOT EXISTS sp_get_rotation_statistics()
BEGIN
    SELECT 
        COUNT(*) as total_keys,
        SUM(CASE WHEN rotation_status = 'WARMING_UP' THEN 1 ELSE 0 END) as warming_up_count,
        SUM(CASE WHEN rotation_status = 'DUAL_ACTIVE' THEN 1 ELSE 0 END) as dual_active_count,
        SUM(CASE WHEN rotation_status = 'DECRYPT_ONLY' THEN 1 ELSE 0 END) as decrypt_only_count,
        SUM(CASE WHEN rotation_status = 'EXPIRED' THEN 1 ELSE 0 END) as expired_count,
        SUM(CASE WHEN expire_time <= DATE_ADD(NOW(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as expiring_soon_count,
        SUM(CASE WHEN key_status = 'ACTIVE' THEN 1 ELSE 0 END) as active_count
    FROM encrypt_key
    WHERE deleted = 0;
END //

DELIMITER ;

-- 8. 更新现有密钥的轮换状态
UPDATE encrypt_key SET rotation_status = 'ACTIVE' WHERE key_status = 'ACTIVE' AND rotation_status IS NULL;
UPDATE encrypt_key SET rotation_status = 'EXPIRED' WHERE key_status = 'EXPIRED' AND rotation_status IS NULL;

-- 9. 插入测试数据（可选）
-- 创建测试密钥用于轮换测试
INSERT INTO encrypt_key (key_name, key_type, key_length, key_usage, key_status, rotation_status, 
                        key_fingerprint, key_material, created_by, expire_time, rotation_cycle, 
                        rotation_count, description, rotation_start_time, rotation_complete_time)
VALUES 
('TEST_ROTATION_KEY_01', 'SM4', 128, 'ENCRYPT', 'ACTIVE', 'ACTIVE', 
 'test_rotation_fingerprint_01', 'encrypted_test_rotation_key_01', 'system', 
 DATE_ADD(NOW(), INTERVAL 7 DAY), 90, 0, '测试轮换密钥01', NOW(), DATE_ADD(NOW(), INTERVAL 37 DAY)),
('TEST_ROTATION_KEY_02', 'SM2', 256, 'SIGN', 'ACTIVE', 'ACTIVE', 
 'test_rotation_fingerprint_02', 'encrypted_test_rotation_key_02', 'system', 
 DATE_ADD(NOW(), INTERVAL 15 DAY), 90, 0, '测试轮换密钥02', NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY))
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 10. 添加权限相关的数据（如果需要）
-- 这些权限应该已经存在于权限系统中，这里只是作为参考
-- INSERT INTO sys_permission (permission_name, permission_code, description) VALUES
-- ('密钥轮换监控', 'KEY_ROTATION_MONITOR', '查看密钥轮换监控数据'),
-- ('强制密钥轮换', 'KEY_FORCE_ROTATE', '强制轮换密钥'),
-- ('取消密钥轮换', 'KEY_CANCEL_ROTATE', '取消正在进行的密钥轮换');

-- 11. 创建定时任务相关的配置表
CREATE TABLE IF NOT EXISTS key_rotation_job_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_cron VARCHAR(50) NOT NULL COMMENT 'Cron表达式',
    job_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    warm_up_days INT DEFAULT 3 COMMENT '预热期天数',
    dual_active_days INT DEFAULT 7 COMMENT '双密钥期天数',
    decrypt_only_days INT DEFAULT 30 COMMENT '仅解密期天数',
    early_warning_days INT DEFAULT 30 COMMENT '提前告警天数',
    description TEXT COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_name (job_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密钥轮换任务配置表';

-- 插入默认配置
INSERT INTO key_rotation_job_config (job_name, job_cron, description, warm_up_days, dual_active_days, decrypt_only_days)
VALUES 
('密钥到期检查', '0 0 2 * * ?', '每天凌晨2点检查即将到期的密钥', 3, 7, 30),
('自动密钥轮换', '0 0 3 * * ?', '每天凌晨3点执行自动密钥轮换', 3, 7, 30),
('轮换状态监控', '0 */5 * * * ?', '每5分钟检查轮换状态', 3, 7, 30)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 12. 添加性能优化索引
CREATE INDEX IF NOT EXISTS idx_encrypt_key_rotation_composite ON encrypt_key(key_status, rotation_status, expire_time);
CREATE INDEX IF NOT EXISTS idx_key_rotation_history_time ON key_rotation_history(rotation_time, rotation_status);
CREATE INDEX IF NOT EXISTS idx_data_source_encrypt_keys ON data_source(encrypt_key_id, secondary_encrypt_key_id);

PRINT '密钥轮换机制数据库更新完成！';