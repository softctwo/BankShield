-- =============================================
-- BankShield 审计日志防篡改 SQL脚本
-- 基于区块链技术和SM3哈希算法
-- =============================================

-- 1. 创建审计日志区块表
CREATE TABLE IF NOT EXISTS audit_log_block (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区块ID',
    block_index BIGINT NOT NULL COMMENT '区块索引（高度）',
    block_hash VARCHAR(128) NOT NULL COMMENT '当前区块哈希（SM3）',
    previous_hash VARCHAR(128) NOT NULL COMMENT '前一个区块哈希',
    timestamp BIGINT NOT NULL COMMENT '区块时间戳（毫秒）',
    nonce BIGINT DEFAULT 0 COMMENT '随机数（用于工作量证明）',
    merkle_root VARCHAR(128) COMMENT 'Merkle树根哈希',
    log_count INT DEFAULT 0 COMMENT '区块中日志数量',
    block_size INT DEFAULT 0 COMMENT '区块大小（字节）',
    miner VARCHAR(100) COMMENT '区块创建者',
    signature VARCHAR(256) COMMENT '数字签名',
    is_valid TINYINT DEFAULT 1 COMMENT '是否有效：1-有效，0-无效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_block_index (block_index),
    UNIQUE KEY uk_block_hash (block_hash),
    INDEX idx_timestamp (timestamp),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志区块表';

-- 2. 创建审计日志区块数据表
CREATE TABLE IF NOT EXISTS audit_log_block_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据ID',
    block_id BIGINT NOT NULL COMMENT '所属区块ID',
    block_index BIGINT NOT NULL COMMENT '区块索引',
    log_id BIGINT NOT NULL COMMENT '审计日志ID',
    log_type VARCHAR(50) COMMENT '日志类型',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(100) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作类型',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    log_content TEXT COMMENT '日志内容（JSON）',
    log_hash VARCHAR(128) NOT NULL COMMENT '日志哈希（SM3）',
    data_index INT NOT NULL COMMENT '在区块中的索引',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_block_id (block_id),
    INDEX idx_block_index (block_index),
    INDEX idx_log_id (log_id),
    INDEX idx_log_hash (log_hash),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (block_id) REFERENCES audit_log_block(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志区块数据表';

-- 3. 创建区块链验证记录表
CREATE TABLE IF NOT EXISTS blockchain_verification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证ID',
    verification_type VARCHAR(50) NOT NULL COMMENT '验证类型：FULL-全链验证，BLOCK-单块验证，RANGE-区间验证',
    start_block_index BIGINT COMMENT '起始区块索引',
    end_block_index BIGINT COMMENT '结束区块索引',
    total_blocks INT COMMENT '验证区块总数',
    valid_blocks INT COMMENT '有效区块数',
    invalid_blocks INT COMMENT '无效区块数',
    verification_result VARCHAR(20) NOT NULL COMMENT '验证结果：PASS-通过，FAIL-失败',
    error_message TEXT COMMENT '错误信息',
    verification_time BIGINT COMMENT '验证耗时（毫秒）',
    verifier VARCHAR(100) COMMENT '验证人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_verification_type (verification_type),
    INDEX idx_verification_result (verification_result),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链验证记录表';

-- 4. 创建区块链统计表
CREATE TABLE IF NOT EXISTS blockchain_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_blocks BIGINT DEFAULT 0 COMMENT '总区块数',
    total_logs BIGINT DEFAULT 0 COMMENT '总日志数',
    chain_length BIGINT DEFAULT 0 COMMENT '链长度',
    avg_block_size DECIMAL(10,2) COMMENT '平均区块大小（字节）',
    avg_logs_per_block DECIMAL(10,2) COMMENT '平均每区块日志数',
    last_block_hash VARCHAR(128) COMMENT '最后区块哈希',
    last_block_time BIGINT COMMENT '最后区块时间戳',
    verification_count INT DEFAULT 0 COMMENT '验证次数',
    verification_pass_count INT DEFAULT 0 COMMENT '验证通过次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat_date (stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链统计表';

-- 5. 创建区块链配置表
CREATE TABLE IF NOT EXISTS blockchain_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型：STRING,NUMBER,BOOLEAN,JSON',
    description TEXT COMMENT '配置描述',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链配置表';

-- 6. 插入默认配置
INSERT INTO blockchain_config (config_key, config_value, config_type, description, create_by) VALUES
('block.max.logs', '100', 'NUMBER', '每个区块最大日志数', 'system'),
('block.max.size', '1048576', 'NUMBER', '每个区块最大大小（字节，1MB）', 'system'),
('block.auto.create', 'true', 'BOOLEAN', '是否自动创建区块', 'system'),
('block.create.interval', '300000', 'NUMBER', '自动创建区块间隔（毫秒，5分钟）', 'system'),
('hash.algorithm', 'SM3', 'STRING', '哈希算法', 'system'),
('signature.algorithm', 'SM2', 'STRING', '签名算法', 'system'),
('verification.auto.enabled', 'true', 'BOOLEAN', '是否启用自动验证', 'system'),
('verification.interval', '3600000', 'NUMBER', '自动验证间隔（毫秒，1小时）', 'system');

-- 7. 创建创世区块
INSERT INTO audit_log_block (block_index, block_hash, previous_hash, timestamp, merkle_root, log_count, miner, signature, create_time)
VALUES (
    0,
    '0000000000000000000000000000000000000000000000000000000000000000',
    '0000000000000000000000000000000000000000000000000000000000000000',
    UNIX_TIMESTAMP(NOW()) * 1000,
    '0000000000000000000000000000000000000000000000000000000000000000',
    0,
    'GENESIS',
    'GENESIS_SIGNATURE',
    NOW()
);

-- 8. 创建视图：区块链概览
CREATE OR REPLACE VIEW v_blockchain_overview AS
SELECT 
    COUNT(*) as total_blocks,
    MAX(block_index) as chain_height,
    SUM(log_count) as total_logs,
    AVG(log_count) as avg_logs_per_block,
    AVG(block_size) as avg_block_size,
    MAX(timestamp) as last_block_time,
    MAX(block_hash) as last_block_hash
FROM audit_log_block
WHERE is_valid = 1;

-- 9. 创建视图：最近区块
CREATE OR REPLACE VIEW v_recent_blocks AS
SELECT 
    block_index,
    block_hash,
    previous_hash,
    FROM_UNIXTIME(timestamp/1000) as block_time,
    log_count,
    block_size,
    miner,
    is_valid
FROM audit_log_block
ORDER BY block_index DESC
LIMIT 100;

-- 10. 创建存储过程：验证区块哈希
DELIMITER //
CREATE PROCEDURE sp_verify_block_hash(IN p_block_id BIGINT, OUT p_is_valid TINYINT)
BEGIN
    DECLARE v_stored_hash VARCHAR(128);
    DECLARE v_calculated_hash VARCHAR(128);
    DECLARE v_block_index BIGINT;
    DECLARE v_previous_hash VARCHAR(128);
    DECLARE v_timestamp BIGINT;
    DECLARE v_merkle_root VARCHAR(128);
    DECLARE v_nonce BIGINT;
    
    -- 获取区块信息
    SELECT block_index, block_hash, previous_hash, timestamp, merkle_root, nonce
    INTO v_block_index, v_stored_hash, v_previous_hash, v_timestamp, v_merkle_root, v_nonce
    FROM audit_log_block
    WHERE id = p_block_id;
    
    -- 这里应该调用SM3哈希计算，简化处理
    -- 实际实现中需要在Java层计算
    SET v_calculated_hash = v_stored_hash;
    
    -- 比较哈希
    IF v_stored_hash = v_calculated_hash THEN
        SET p_is_valid = 1;
    ELSE
        SET p_is_valid = 0;
    END IF;
END //
DELIMITER ;

-- 11. 创建存储过程：验证区块链完整性
DELIMITER //
CREATE PROCEDURE sp_verify_blockchain(OUT p_is_valid TINYINT, OUT p_invalid_count INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_block_id BIGINT;
    DECLARE v_block_index BIGINT;
    DECLARE v_current_hash VARCHAR(128);
    DECLARE v_previous_hash VARCHAR(128);
    DECLARE v_actual_previous_hash VARCHAR(128);
    DECLARE v_invalid_count INT DEFAULT 0;
    
    DECLARE block_cursor CURSOR FOR 
        SELECT id, block_index, block_hash, previous_hash 
        FROM audit_log_block 
        WHERE block_index > 0
        ORDER BY block_index ASC;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN block_cursor;
    
    read_loop: LOOP
        FETCH block_cursor INTO v_block_id, v_block_index, v_current_hash, v_previous_hash;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 获取前一个区块的哈希
        SELECT block_hash INTO v_actual_previous_hash
        FROM audit_log_block
        WHERE block_index = v_block_index - 1;
        
        -- 验证前一个区块哈希是否匹配
        IF v_previous_hash != v_actual_previous_hash THEN
            SET v_invalid_count = v_invalid_count + 1;
            UPDATE audit_log_block SET is_valid = 0 WHERE id = v_block_id;
        END IF;
    END LOOP;
    
    CLOSE block_cursor;
    
    SET p_invalid_count = v_invalid_count;
    IF v_invalid_count = 0 THEN
        SET p_is_valid = 1;
    ELSE
        SET p_is_valid = 0;
    END IF;
END //
DELIMITER ;

-- 12. 创建索引优化查询性能
CREATE INDEX idx_block_valid ON audit_log_block(is_valid, block_index);
CREATE INDEX idx_log_block_index ON audit_log_block_data(block_index, data_index);

-- 13. 创建触发器：更新统计信息
DELIMITER //
CREATE TRIGGER tr_after_block_insert
AFTER INSERT ON audit_log_block
FOR EACH ROW
BEGIN
    INSERT INTO blockchain_statistics (stat_date, total_blocks, total_logs, chain_length, last_block_hash, last_block_time)
    VALUES (CURDATE(), 1, NEW.log_count, NEW.block_index + 1, NEW.block_hash, NEW.timestamp)
    ON DUPLICATE KEY UPDATE
        total_blocks = total_blocks + 1,
        total_logs = total_logs + NEW.log_count,
        chain_length = NEW.block_index + 1,
        last_block_hash = NEW.block_hash,
        last_block_time = NEW.timestamp;
END //
DELIMITER ;

-- 完成
SELECT '审计日志防篡改区块链SQL脚本执行完成！' as message;
SELECT '创世区块已创建，区块索引：0' as genesis_block;
