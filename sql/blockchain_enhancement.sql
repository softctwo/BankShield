-- ============================================
-- 区块链存证功能增强数据库设计
-- 创建日期: 2026-01-07
-- 功能: 增强区块链存证查询和验证功能
-- ============================================

-- 1. 区块链存证查询表（增强版）
CREATE TABLE IF NOT EXISTS blockchain_anchor_query (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '查询ID',
    query_type VARCHAR(50) NOT NULL COMMENT '查询类型：BLOCK/TRANSACTION/RECORD/RANGE',
    query_params TEXT COMMENT '查询参数（JSON格式）',
    block_id VARCHAR(100) COMMENT '区块ID',
    transaction_id VARCHAR(128) COMMENT '交易ID',
    query_user_id BIGINT COMMENT '查询用户ID',
    query_user_name VARCHAR(100) COMMENT '查询用户名',
    query_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间',
    query_result TEXT COMMENT '查询结果（JSON格式）',
    query_status VARCHAR(20) NOT NULL COMMENT '查询状态：SUCCESS/FAILED',
    response_time INT COMMENT '响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_query_type (query_type),
    INDEX idx_block_id (block_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_query_user (query_user_id),
    INDEX idx_query_time (query_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链存证查询表';

-- 2. 区块链验证记录表
CREATE TABLE IF NOT EXISTS blockchain_verification_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证ID',
    verification_type VARCHAR(50) NOT NULL COMMENT '验证类型：SINGLE_BLOCK/CHAIN/RANGE/MERKLE_ROOT',
    block_id VARCHAR(100) COMMENT '区块ID',
    start_block_index BIGINT COMMENT '起始区块索引',
    end_block_index BIGINT COMMENT '结束区块索引',
    verification_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    verification_result VARCHAR(20) NOT NULL COMMENT '验证结果：VALID/INVALID/PARTIAL',
    is_valid TINYINT(1) NOT NULL COMMENT '是否有效：0-无效，1-有效',
    total_blocks INT COMMENT '总区块数',
    valid_blocks INT COMMENT '有效区块数',
    invalid_blocks INT COMMENT '无效区块数',
    verification_duration INT COMMENT '验证耗时（毫秒）',
    error_details TEXT COMMENT '错误详情（JSON格式）',
    verifier_user_id BIGINT COMMENT '验证人ID',
    verifier_user_name VARCHAR(100) COMMENT '验证人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_verification_type (verification_type),
    INDEX idx_block_id (block_id),
    INDEX idx_verification_time (verification_time),
    INDEX idx_verification_result (verification_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链验证记录表';

-- 3. 区块链交易索引表（用于快速查询）
CREATE TABLE IF NOT EXISTS blockchain_transaction_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '索引ID',
    transaction_id VARCHAR(128) NOT NULL UNIQUE COMMENT '交易ID',
    block_id VARCHAR(100) NOT NULL COMMENT '区块ID',
    block_index BIGINT NOT NULL COMMENT '区块索引',
    transaction_type VARCHAR(50) NOT NULL COMMENT '交易类型：AUDIT/ACCESS/PERMISSION/KEY_ROTATION',
    transaction_data TEXT COMMENT '交易数据（JSON格式）',
    transaction_hash VARCHAR(128) NOT NULL COMMENT '交易哈希',
    timestamp BIGINT NOT NULL COMMENT '时间戳',
    creator_org VARCHAR(100) COMMENT '创建组织',
    creator_msp VARCHAR(100) COMMENT '创建MSP',
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' COMMENT '状态：PENDING/CONFIRMED/FAILED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_block_id (block_id),
    INDEX idx_block_index (block_index),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链交易索引表';

-- 4. 区块链浏览器统计表
CREATE TABLE IF NOT EXISTS blockchain_browser_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_blocks BIGINT NOT NULL DEFAULT 0 COMMENT '总区块数',
    total_transactions BIGINT NOT NULL DEFAULT 0 COMMENT '总交易数',
    new_blocks_today INT NOT NULL DEFAULT 0 COMMENT '今日新增区块',
    new_transactions_today INT NOT NULL DEFAULT 0 COMMENT '今日新增交易',
    avg_block_size DECIMAL(10,2) COMMENT '平均区块大小（KB）',
    avg_transactions_per_block DECIMAL(10,2) COMMENT '平均每区块交易数',
    verification_success_rate DECIMAL(5,2) COMMENT '验证成功率（%）',
    query_count INT NOT NULL DEFAULT 0 COMMENT '查询次数',
    avg_query_response_time INT COMMENT '平均查询响应时间（毫秒）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat_date (stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链浏览器统计表';

-- 5. 区块链存证证书表
CREATE TABLE IF NOT EXISTS blockchain_certificate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '证书ID',
    certificate_code VARCHAR(50) NOT NULL UNIQUE COMMENT '证书编码',
    block_id VARCHAR(100) NOT NULL COMMENT '区块ID',
    transaction_id VARCHAR(128) COMMENT '交易ID',
    certificate_type VARCHAR(50) NOT NULL COMMENT '证书类型：AUDIT/DATA_ACCESS/COMPLIANCE',
    data_hash VARCHAR(128) NOT NULL COMMENT '数据哈希',
    merkle_proof TEXT COMMENT 'Merkle证明路径（JSON格式）',
    timestamp BIGINT NOT NULL COMMENT '时间戳',
    issuer VARCHAR(100) NOT NULL COMMENT '颁发者',
    recipient VARCHAR(100) COMMENT '接收者',
    certificate_data TEXT COMMENT '证书数据（JSON格式）',
    qr_code_url VARCHAR(500) COMMENT '二维码URL',
    verification_url VARCHAR(500) COMMENT '验证URL',
    status VARCHAR(20) NOT NULL DEFAULT 'VALID' COMMENT '状态：VALID/REVOKED/EXPIRED',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_certificate_code (certificate_code),
    INDEX idx_block_id (block_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_certificate_type (certificate_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链存证证书表';

-- 创建视图：最近区块视图
CREATE OR REPLACE VIEW v_recent_blocks AS
SELECT 
    b.id,
    b.block_index,
    b.block_hash,
    b.previous_hash,
    b.merkle_root,
    b.timestamp,
    b.transaction_count,
    b.create_time,
    COUNT(t.id) as indexed_transactions
FROM audit_log_block b
LEFT JOIN blockchain_transaction_index t ON b.id = t.block_id
WHERE b.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY b.id
ORDER BY b.block_index DESC;

-- 创建视图：区块链健康状态视图
CREATE OR REPLACE VIEW v_blockchain_health AS
SELECT 
    COUNT(*) as total_blocks,
    MAX(block_index) as latest_block_index,
    COUNT(DISTINCT DATE(create_time)) as active_days,
    AVG(transaction_count) as avg_transactions_per_block,
    SUM(CASE WHEN create_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) THEN 1 ELSE 0 END) as blocks_last_24h,
    (SELECT COUNT(*) FROM blockchain_verification_record WHERE is_valid = 1 AND verification_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)) as valid_verifications_7d,
    (SELECT COUNT(*) FROM blockchain_verification_record WHERE is_valid = 0 AND verification_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)) as invalid_verifications_7d
FROM audit_log_block;

-- 初始化今日统计数据
INSERT INTO blockchain_browser_statistics (stat_date, total_blocks, total_transactions)
SELECT 
    CURDATE(),
    COUNT(DISTINCT b.id),
    COUNT(t.id)
FROM audit_log_block b
LEFT JOIN blockchain_transaction_index t ON b.id = t.block_id
ON DUPLICATE KEY UPDATE 
    total_blocks = VALUES(total_blocks),
    total_transactions = VALUES(total_transactions);

-- 数据库初始化完成
SELECT '区块链存证功能增强数据库初始化完成' AS status;
