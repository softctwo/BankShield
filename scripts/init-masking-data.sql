-- BankShield数据脱敏规则初始化脚本
-- 执行方式：在MySQL中执行 source /path/to/init-masking-data.sql

-- 确保数据库存在
CREATE DATABASE IF NOT EXISTS bankshield CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bankshield;

-- 创建脱敏规则表（如果不存在）
CREATE TABLE IF NOT EXISTS masking_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
  sensitive_data_type VARCHAR(50) NOT NULL COMMENT '敏感数据类型: PHONE/ID_CARD/BANK_CARD/NAME/EMAIL/ADDRESS',
  masking_algorithm VARCHAR(50) NOT NULL COMMENT '脱敏算法: PARTIAL_MASK/FULL_MASK/HASH/SYMMETRIC_ENCRYPT/FORMAT_PRESERVING',
  algorithm_params TEXT COMMENT '算法参数(JSON)',
  applicable_scenarios VARCHAR(200) COMMENT '适用场景: DISPLAY,EXPORT,QUERY,TRANSFER',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) COMMENT '创建人',
  description TEXT,
  INDEX idx_sensitive_type (sensitive_data_type),
  INDEX idx_enabled (enabled),
  UNIQUE KEY uk_rule_name (rule_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据脱敏规则表';

-- 清空现有数据（谨慎操作）
-- TRUNCATE TABLE masking_rule;

-- 插入默认脱敏规则
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description, created_by) VALUES 
('手机号脱敏', 'PHONE', 'PARTIAL_MASK', '{"keepPrefix": 3, "keepSuffix": 4, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '手机号保留前3位后4位，中间用*掩码', 'system'),
('身份证号脱敏', 'ID_CARD', 'PARTIAL_MASK', '{"keepPrefix": 6, "keepSuffix": 4, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '身份证号保留前6位后4位，中间用*掩码', 'system'),
('姓名脱敏', 'NAME', 'PARTIAL_MASK', '{"keepPrefix": 1, "keepSuffix": 0, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '姓名只保留姓氏，其余用*掩码', 'system'),
('银行卡号脱敏', 'BANK_CARD', 'PARTIAL_MASK', '{"keepPrefix": 6, "keepSuffix": 4, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '银行卡号保留前6位后4位，中间用*掩码', 'system'),
('邮箱脱敏', 'EMAIL', 'PARTIAL_MASK', '{"keepPrefix": 1, "keepSuffix": 0, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '邮箱保留首字母和域名，@符号前用*掩码', 'system'),
('地址脱敏', 'ADDRESS', 'PARTIAL_MASK', '{"keepPrefix": 6, "keepSuffix": 0, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '地址保留前6位，其余用*掩码', 'system');

-- 插入哈希算法规则（不可逆脱敏）
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description, created_by) VALUES 
('手机号哈希脱敏', 'PHONE', 'HASH', '{"hashAlgorithm": "SM3"}', 'EXPORT,TRANSFER', '手机号使用SM3哈希算法进行不可逆脱敏', 'system'),
('身份证号哈希脱敏', 'ID_CARD', 'HASH', '{"hashAlgorithm": "SM3"}', 'EXPORT,TRANSFER', '身份证号使用SM3哈希算法进行不可逆脱敏', 'system');

-- 插入格式保留加密规则
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description, created_by) VALUES 
('银行卡号格式保留', 'BANK_CARD', 'FORMAT_PRESERVING', '{"formatPreserveLength": 8}', 'DISPLAY,QUERY', '银行卡号保留格式，前后各4位可见', 'system');

-- 验证数据插入
SELECT 
  id,
  rule_name,
  sensitive_data_type,
  masking_algorithm,
  enabled,
  create_time
FROM masking_rule 
ORDER BY id;

-- 显示统计信息
SELECT 
  sensitive_data_type,
  COUNT(*) as rule_count,
  GROUP_CONCAT(rule_name) as rule_names
FROM masking_rule 
GROUP BY sensitive_data_type
ORDER BY rule_count DESC;