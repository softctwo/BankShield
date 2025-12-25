-- 加密密钥表
CREATE TABLE IF NOT EXISTS encrypt_key (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  key_name VARCHAR(100) NOT NULL COMMENT '密钥名称',
  key_type VARCHAR(20) NOT NULL COMMENT '密钥类型: SM2/SM3/SM4/AES/RSA',
  key_length INT COMMENT '密钥长度',
  key_usage VARCHAR(50) COMMENT '密钥用途: ENCRYPT/DECRYPT/SIGN/VERIFY',
  key_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '密钥状态',
  key_fingerprint VARCHAR(100) NOT NULL COMMENT '密钥指纹(SHA256)',
  key_material TEXT NOT NULL COMMENT '密钥材料(加密存储)',
  created_by VARCHAR(50) COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  expire_time DATETIME COMMENT '过期时间',
  rotation_cycle INT DEFAULT 90 COMMENT '轮换周期(天)',
  last_rotation_time DATETIME COMMENT '上次轮换时间',
  rotation_count INT DEFAULT 0 COMMENT '轮换次数',
  description TEXT,
  data_source_id BIGINT COMMENT '关联数据源ID',
  deleted INT DEFAULT 0 COMMENT '逻辑删除标志',
  INDEX idx_key_type (key_type),
  INDEX idx_key_status (key_status),
  INDEX idx_expire_time (expire_time),
  INDEX idx_create_time (create_time),
  UNIQUE KEY uk_key_name (key_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加密密钥表';

-- 密钥轮换历史表
CREATE TABLE IF NOT EXISTS key_rotation_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  old_key_id BIGINT NOT NULL COMMENT '旧密钥ID',
  new_key_id BIGINT NOT NULL COMMENT '新密钥ID',
  rotation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '轮换时间',
  rotation_reason VARCHAR(500) COMMENT '轮换原因',
  rotated_by VARCHAR(50) COMMENT '操作人员',
  rotation_status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '轮换状态',
  failure_reason TEXT COMMENT '失败原因',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_old_key_id (old_key_id),
  INDEX idx_new_key_id (new_key_id),
  INDEX idx_rotation_time (rotation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密钥轮换历史表';

-- 密钥使用审计表
CREATE TABLE IF NOT EXISTS key_usage_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  key_id BIGINT NOT NULL COMMENT '密钥ID',
  operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
  operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  operator VARCHAR(50) COMMENT '操作人员',
  operation_result VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '操作结果',
  data_size BIGINT COMMENT '加密数据量(字节)',
  data_source_id BIGINT COMMENT '数据源ID',
  description TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_key_id (key_id),
  INDEX idx_operation_time (operation_time),
  INDEX idx_operator (operator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密钥使用审计表';

-- 预置系统主密钥（用于加密其他密钥）
INSERT INTO encrypt_key (key_name, key_type, key_length, key_usage, key_status, key_fingerprint, key_material, created_by, expire_time, rotation_cycle, rotation_count, description)
VALUES ('SYSTEM_MASTER_KEY', 'SM4', 128, 'ENCRYPT', 'ACTIVE', 'system_master_key_fingerprint', 'encrypted_master_key_material', 'system', '2025-12-31 23:59:59', 365, 0, '系统主密钥，用于加密其他密钥材料')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 创建测试密钥
INSERT INTO encrypt_key (key_name, key_type, key_length, key_usage, key_status, key_fingerprint, key_material, created_by, expire_time, rotation_cycle, rotation_count, description)
VALUES 
('TEST_SM4_DATA_KEY', 'SM4', 128, 'ENCRYPT', 'ACTIVE', 'test_sm4_fingerprint', 'encrypted_test_sm4_key', 'admin', '2024-12-31 23:59:59', 90, 0, '测试SM4数据加密密钥'),
('TEST_SM2_SIGN_KEY', 'SM2', 256, 'SIGN', 'ACTIVE', 'test_sm2_fingerprint', 'encrypted_test_sm2_key', 'admin', '2024-12-31 23:59:59', 90, 0, '测试SM2签名密钥')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 创建索引优化查询性能
CREATE INDEX idx_encrypt_key_composite ON encrypt_key(key_type, key_status, expire_time);
CREATE INDEX idx_key_rotation_composite ON key_rotation_history(old_key_id, new_key_id, rotation_status);
CREATE INDEX idx_key_usage_composite ON key_usage_audit(key_id, operation_type, operation_time);