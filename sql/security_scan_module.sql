-- 安全扫描模块数据库初始化脚本
-- BankShield Security Scan Module

-- 扫描任务表
CREATE TABLE IF NOT EXISTS security_scan_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
  scan_type VARCHAR(50) NOT NULL COMMENT '扫描类型: VULNERABILITY/CONFIG/WEAK_PASSWORD/ANOMALY/ALL',
  scan_target TEXT COMMENT '扫描目标',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT '执行状态: PENDING/RUNNING/SUCCESS/FAILED/PARTIAL',
  start_time DATETIME COMMENT '开始时间',
  end_time DATETIME COMMENT '结束时间',
  risk_count INT DEFAULT 0 COMMENT '发现风险数',
  report_path VARCHAR(500) COMMENT '扫描报告路径',
  created_by VARCHAR(50) COMMENT '创建人',
  error_message TEXT COMMENT '错误信息',
  progress INT DEFAULT 0 COMMENT '扫描进度 0-100',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status (status),
  INDEX idx_create_time (create_time),
  INDEX idx_scan_type (scan_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全扫描任务表';

-- 扫描结果表
CREATE TABLE IF NOT EXISTS security_scan_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL COMMENT '任务ID',
  risk_level VARCHAR(20) COMMENT '风险级别: CRITICAL/HIGH/MEDIUM/LOW/INFO',
  risk_type VARCHAR(50) COMMENT '风险类型: SQL_INJECTION/XSS/WEAK_PASSWORD/...',
  risk_description TEXT COMMENT '风险描述',
  impact_scope TEXT COMMENT '影响范围',
  remediation_advice TEXT COMMENT '修复建议',
  discovered_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
  fix_status VARCHAR(20) DEFAULT 'UNFIXED' COMMENT '修复状态: UNFIXED/RESOLVED/WONT_FIX',
  fix_time DATETIME COMMENT '修复时间',
  fix_by VARCHAR(50) COMMENT '修复人',
  verify_result VARCHAR(20) COMMENT '验证结果',
  risk_details TEXT COMMENT '风险详情（JSON格式）',
  cve_id VARCHAR(20) COMMENT 'CVE编号',
  cvss_score DOUBLE COMMENT 'CVSS评分',
  asset_info TEXT COMMENT '资产信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_task_id (task_id),
  INDEX idx_risk_level (risk_level),
  INDEX idx_fix_status (fix_status),
  INDEX idx_discovered_time (discovered_time),
  INDEX idx_risk_type (risk_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描结果表';

-- 安全基线配置表
CREATE TABLE IF NOT EXISTS security_baseline (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  check_item_name VARCHAR(200) NOT NULL COMMENT '检查项名称',
  compliance_standard VARCHAR(50) COMMENT '合规标准: 等保三级/PCI-DSS/OWASP_TOP10/自定义',
  check_type VARCHAR(50) COMMENT '检查类型: AUTH/SESSION/ENCRYPTION/PASSWORD/ACCESS_CONTROL',
  risk_level VARCHAR(20) COMMENT '风险级别: CRITICAL/HIGH/MEDIUM/LOW/INFO',
  pass_status VARCHAR(20) DEFAULT 'UNKNOWN' COMMENT '通过状态: PASS/FAIL/UNKNOWN/NOT_APPLICABLE',
  check_result TEXT COMMENT '检查结果',
  check_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
  next_check_time DATETIME COMMENT '下次检查时间',
  responsible_person VARCHAR(50) COMMENT '负责人',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  builtin TINYINT(1) DEFAULT 0 COMMENT '是否内置',
  description TEXT COMMENT '检查项描述',
  remedy_advice TEXT COMMENT '修复建议',
  created_by VARCHAR(50) COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_compliance_standard (compliance_standard),
  INDEX idx_pass_status (pass_status),
  INDEX idx_enabled (enabled),
  INDEX idx_next_check_time (next_check_time),
  INDEX idx_check_type (check_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全基线配置表';

-- 初始化安全基线（等保三级核心检查项）
INSERT INTO security_baseline (check_item_name, compliance_standard, check_type, risk_level, description, remedy_advice, builtin, created_by) VALUES
('用户密码复杂度策略', '等保三级', 'PASSWORD', 'HIGH', '检查密码是否符合复杂度要求（8位以上，包含大小写字母、数字、特殊字符）', '配置密码策略：minimum-length=8, require-uppercase=true, require-lowercase=true, require-digit=true, require-special=true', 1, 'system'),
('会话超时自动退出', '等保三级', 'SESSION', 'MEDIUM', '检查会话超时是否设置为30分钟', '设置session-timeout=30分钟，配置最大会话数限制', 1, 'system'),
('敏感数据加密传输', '等保三级', 'ENCRYPTION', 'HIGH', '检查是否使用HTTPS加密传输', '强制使用HTTPS，禁用HTTP访问，配置HSTS', 1, 'system'),
('数据库连接密码加密存储', '等保三级', 'ENCRYPTION', 'HIGH', '检查数据库密码是否加密存储', '使用Jasypt加密配置文件中的数据库密码', 1, 'system'),
('文件上传大小限制', '等保三级', 'ACCESS_CONTROL', 'MEDIUM', '检查文件上传是否限制大小和类型', '配置max-file-size=10MB, allowed-file-types=jpg,png,pdf,doc', 1, 'system'),
('SQL注入防护', '等保三级', 'INPUT_VALIDATION', 'CRITICAL', '检查是否对所有输入进行SQL注入过滤', '使用MyBatis参数化查询，禁止拼接SQL', 1, 'system'),
('XSS攻击防护', '等保三级', 'INPUT_VALIDATION', 'HIGH', '检查是否对所有输出进行XSS过滤', '使用HTML转义库，过滤script标签', 1, 'system'),
('CSRF令牌验证', '等保三级', 'AUTH', 'HIGH', '检查是否启用CSRF防护', '启用Spring Security CSRF保护，所有POST请求验证令牌', 1, 'system'),
('审计日志完整性', '等保三级', 'AUDIT', 'MEDIUM', '检查审计日志是否完整记录', '确保所有增删改操作记录到audit_operation表', 1, 'system'),
('密钥管理合规性', '等保三级', 'KEY_MANAGEMENT', 'HIGH', '检查密钥是否符合管理要求', '使用国密算法，密钥定期轮换（90天），密钥材料加密存储', 1, 'system');

-- PCI-DSS基线检查项
INSERT INTO security_baseline (check_item_name, compliance_standard, check_type, risk_level, description, remedy_advice, builtin, created_by) VALUES
('支付卡数据加密存储', 'PCI-DSS', 'ENCRYPTION', 'CRITICAL', '检查支付卡数据是否加密存储', '使用强加密算法（AES-256）加密存储支付卡数据', 1, 'system'),
('支付卡数据传输加密', 'PCI-DSS', 'ENCRYPTION', 'CRITICAL', '检查支付卡数据传输是否加密', '使用TLS 1.3加密传输支付卡数据', 1, 'system'),
('访问控制策略', 'PCI-DSS', 'ACCESS_CONTROL', 'HIGH', '检查是否实施访问控制策略', '实施最小权限原则，定期审查访问权限', 1, 'system'),
('网络安全防护', 'PCI-DSS', 'NETWORK', 'HIGH', '检查网络安全防护措施', '配置防火墙、入侵检测系统等网络安全设备', 1, 'system'),
('恶意软件防护', 'PCI-DSS', 'MALWARE', 'MEDIUM', '检查恶意软件防护措施', '安装杀毒软件，定期更新病毒库', 1, 'system');

-- OWASP TOP10基线检查项
INSERT INTO security_baseline (check_item_name, compliance_standard, check_type, risk_level, description, remedy_advice, builtin, created_by) VALUES
('注入攻击防护', 'OWASP_TOP10', 'INPUT_VALIDATION', 'CRITICAL', '检查注入攻击防护措施', '使用参数化查询，输入验证和过滤', 1, 'system'),
('失效的身份认证', 'OWASP_TOP10', 'AUTH', 'CRITICAL', '检查身份认证机制', '实施多因素认证，密码复杂度策略', 1, 'system'),
('敏感数据暴露', 'OWASP_TOP10', 'ENCRYPTION', 'HIGH', '检查敏感数据保护措施', '加密存储和传输敏感数据', 1, 'system'),
('XML外部实体攻击', 'OWASP_TOP10', 'INPUT_VALIDATION', 'HIGH', '检查XML外部实体攻击防护', '禁用XML外部实体，使用安全的XML解析器', 1, 'system'),
('失效的访问控制', 'OWASP_TOP10', 'ACCESS_CONTROL', 'HIGH', '检查访问控制机制', '实施基于角色的访问控制（RBAC）', 1, 'system'),
('安全配置错误', 'OWASP_TOP10', 'CONFIG', 'MEDIUM', '检查安全配置是否正确', '使用安全配置模板，定期安全审计', 1, 'system'),
('跨站脚本攻击', 'OWASP_TOP10', 'INPUT_VALIDATION', 'HIGH', '检查XSS攻击防护措施', '输出编码，内容安全策略（CSP）', 1, 'system'),
('不安全的反序列化', 'OWASP_TOP10', 'INPUT_VALIDATION', 'HIGH', '检查反序列化安全措施', '验证反序列化对象，使用安全的反序列化库', 1, 'system'),
('使用含有已知漏洞的组件', 'OWASP_TOP10', 'COMPONENT', 'MEDIUM', '检查组件安全性', '定期更新组件，使用组件漏洞扫描工具', 1, 'system'),
('日志记录和监控不足', 'OWASP_TOP10', 'AUDIT', 'MEDIUM', '检查日志记录和监控', '实施完整的日志记录和监控告警', 1, 'system');