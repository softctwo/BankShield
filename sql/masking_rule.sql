-- 数据脱敏规则表
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

-- 初始化数据
-- 手机号脱敏规则（保留前3后4）
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description) 
VALUES ('手机号脱敏', 'PHONE', 'PARTIAL_MASK', '{"keepPrefix": 3, "keepSuffix": 4, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '手机号保留前3位后4位，中间用*掩码');

-- 身份证号脱敏规则（保留前6后4）
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description) 
VALUES ('身份证号脱敏', 'ID_CARD', 'PARTIAL_MASK', '{"keepPrefix": 6, "keepSuffix": 4, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '身份证号保留前6位后4位，中间用*掩码');

-- 姓名脱敏规则（保留姓）
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description) 
VALUES ('姓名脱敏', 'NAME', 'PARTIAL_MASK', '{"keepPrefix": 1, "keepSuffix": 0, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '姓名只保留姓氏，其余用*掩码');

-- 银行卡号脱敏规则（保留前6后4）
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description) 
VALUES ('银行卡号脱敏', 'BANK_CARD', 'PARTIAL_MASK', '{"keepPrefix": 6, "keepSuffix": 4, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '银行卡号保留前6位后4位，中间用*掩码');

-- 邮箱脱敏规则（保留首字母和域名）
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description) 
VALUES ('邮箱脱敏', 'EMAIL', 'PARTIAL_MASK', '{"keepPrefix": 1, "keepSuffix": 0, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '邮箱保留首字母和域名，@符号前用*掩码');

-- 地址脱敏规则（保留前6位）
INSERT INTO masking_rule (rule_name, sensitive_data_type, masking_algorithm, algorithm_params, applicable_scenarios, description) 
VALUES ('地址脱敏', 'ADDRESS', 'PARTIAL_MASK', '{"keepPrefix": 6, "keepSuffix": 0, "maskChar": "*"}', 'DISPLAY,EXPORT,QUERY', '地址保留前6位，其余用*掩码');