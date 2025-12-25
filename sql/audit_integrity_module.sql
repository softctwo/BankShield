-- 审计日志完整性校验模块
-- 创建审计日志区块表和关联表

-- 审计日志区块表
CREATE TABLE audit_block (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  block_number BIGINT NOT NULL COMMENT '区块号（递增）',
  previous_hash VARCHAR(128) NOT NULL COMMENT '前一个区块的哈希',
  current_hash VARCHAR(128) NOT NULL COMMENT '当前区块的哈希',
  merkle_root VARCHAR(128) NOT NULL COMMENT 'Merkle树根哈希',
  audit_count BIGINT NOT NULL COMMENT '区块内审计日志数量',
  block_time DATETIME NOT NULL COMMENT '区块生成时间',
  operator VARCHAR(50) NOT NULL COMMENT '区块创建者',
  metadata TEXT COMMENT '区块元数据（JSON格式）',
  status INT DEFAULT 1 COMMENT '0-验证中, 1-已确认, 2-异常',
  blockchain_tx_hash VARCHAR(128) COMMENT '区块链交易哈希（可选）',
  blockchain_confirm_time DATETIME COMMENT '区块链确认时间',
  UNIQUE KEY uk_block_number (block_number),
  INDEX idx_block_time (block_time),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志区块表';

-- 审计日志与区块关联表
CREATE TABLE audit_operation_block (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  audit_id BIGINT NOT NULL COMMENT '审计日志ID',
  block_id BIGINT NOT NULL COMMENT '区块ID',
  merkle_path TEXT COMMENT 'Merkle路径',
  index_in_block INT NOT NULL COMMENT '在区块中的索引',
  UNIQUE KEY uk_audit_block (audit_id, block_id),
  INDEX idx_block_id (block_id),
  INDEX idx_audit_id (audit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志与区块关联表';

-- 在audit_operation表中添加block_id字段
ALTER TABLE audit_operation 
ADD COLUMN block_id BIGINT COMMENT '关联的区块ID（用于完整性校验）' AFTER create_time,
ADD INDEX idx_block_id (block_id);

-- 添加完整性校验相关的索引
CREATE INDEX idx_audit_block_status ON audit_block(status, block_time);
CREATE INDEX idx_audit_operation_block_relation ON audit_operation_block(block_id, audit_id);