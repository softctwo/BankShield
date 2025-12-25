#!/bin/bash

# BankShield合规报表模块初始化脚本
# 执行合规报表相关的数据库表创建和数据初始化

echo "开始初始化合规报表模块数据库..."

# 检查MySQL是否运行
if ! pgrep mysqld > /dev/null; then
    echo "错误：MySQL服务未运行，请先启动MySQL服务"
    exit 1
fi

# 数据库连接配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="bankshield"
DB_USER="root"
DB_PASS="root"

# 执行SQL脚本
echo "执行合规报表模块SQL脚本..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME << EOF
-- 合规报表模块数据库表结构
-- 作者: BankShield开发团队
-- 版本: 1.0.0
-- 创建时间: 2024-12-24

USE bankshield;

-- 报表模板表（已存在，补充字段）
ALTER TABLE report_template 
ADD COLUMN IF NOT EXISTS description TEXT COMMENT '模板描述',
ADD COLUMN IF NOT EXISTS template_config TEXT COMMENT '模板配置（JSON格式）',
ADD COLUMN IF NOT EXISTS template_params TEXT COMMENT '模板参数（JSON格式）',
ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 报表生成任务表（已存在，补充字段）
ALTER TABLE report_generation_task 
ADD COLUMN IF NOT EXISTS report_data TEXT COMMENT '报表数据（JSON格式）',
ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 合规检查历史表
CREATE TABLE IF NOT EXISTS compliance_check_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  compliance_standard VARCHAR(50) NOT NULL COMMENT '合规标准: 等保二级/三级/PCI-DSS/GDPR',
  check_result TEXT NOT NULL COMMENT '检查结果（JSON格式）',
  compliance_score INT COMMENT '合规评分(0-100)',
  check_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
  checker VARCHAR(50) COMMENT '检查人',
  report_path VARCHAR(500) COMMENT '检查报告路径',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_compliance_standard (compliance_standard),
  INDEX idx_check_time (check_time),
  INDEX idx_compliance_score (compliance_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规检查历史表';

-- 合规检查项表（已存在，补充字段）
ALTER TABLE compliance_check_item 
ADD COLUMN IF NOT EXISTS enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
ADD COLUMN IF NOT EXISTS description TEXT COMMENT '检查项描述',
ADD COLUMN IF NOT EXISTS remedy_advice TEXT COMMENT '修复建议',
ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
ADD INDEX idx_enabled (enabled),
ADD INDEX idx_next_check_time (next_check_time);

-- 报表内容表（已存在，补充索引）
ALTER TABLE report_content 
ADD INDEX idx_task_id (task_id),
ADD INDEX idx_chapter_name (chapter_name);

-- 初始化合规检查项数据
INSERT INTO compliance_check_item (check_item_name, compliance_standard, check_type, check_description, remediation) VALUES
-- 等保三级检查项
('访问控制', '等保', 'ACCESS_CONTROL', '检查用户权限管理、角色分配、最小权限原则等', '完善角色权限管理，实施最小权限原则'),
('身份鉴别', '等保', 'AUTH', '检查用户身份验证机制、密码策略、多因素认证等', '加强密码策略，实施多因素认证'),
('安全审计', '等保', 'AUDIT', '检查审计日志完整性、存储期限、覆盖范围等', '完善审计日志记录，确保日志完整性'),
('数据完整性', '等保', 'DATA_INTEGRITY', '检查敏感数据加密、密钥管理、数据备份等', '实施数据加密和密钥管理措施'),
('通信完整性', '等保', 'COMMUNICATION', '检查数据传输完整性保护', '实施数据传输完整性保护措施'),
('通信保密性', '等保', 'COMMUNICATION', '检查数据传输保密性保护', '实施数据传输保密性保护措施'),
('数据备份', '等保', 'BACKUP', '检查数据备份策略和恢复机制', '建立完善的数据备份和恢复机制'),
('剩余信息保护', '等保', 'DATA_CLEANUP', '检查数据删除和清理机制', '建立数据安全删除和清理机制'),
('个人信息保护', '等保', 'PRIVACY', '检查个人信息收集、使用、存储保护', '建立个人信息保护机制'),

-- PCI-DSS检查项
('防火墙配置', 'PCI-DSS', 'FIREWALL', '检查防火墙规则、默认拒绝策略、规则更新等', '完善防火墙配置，实施默认拒绝策略'),
('默认密码管理', 'PCI-DSS', 'PASSWORD', '检查系统默认密码更改情况', '更改所有系统默认密码'),
('持卡人数据保护', 'PCI-DSS', 'DATA_PROTECTION', '检查持卡人数据加密和保护', '实施持卡人数据加密保护'),
('加密传输', 'PCI-DSS', 'ENCRYPTION', '检查SSL/TLS配置、强加密算法、证书有效性等', '启用SSL/TLS，使用强加密算法'),
('防病毒软件', 'PCI-DSS', 'ANTIVIRUS', '检查防病毒软件安装和更新', '安装并定期更新防病毒软件'),
('安全系统开发', 'PCI-DSS', 'SECURE_DEVELOPMENT', '检查安全系统开发和维护', '实施安全系统开发流程'),
('唯一标识', 'PCI-DSS', 'IDENTITY', '检查用户唯一标识分配', '为用户分配唯一标识'),
('物理访问控制', 'PCI-DSS', 'PHYSICAL_ACCESS', '检查物理访问控制措施', '实施物理访问控制措施'),
('网络资源监控', 'PCI-DSS', 'NETWORK_MONITORING', '检查网络资源访问监控', '实施网络资源访问监控'),
('定期测试', 'PCI-DSS', 'TESTING', '检查安全系统和流程定期测试', '定期测试安全系统和流程'),
('安全策略', 'PCI-DSS', 'SECURITY_POLICY', '检查信息安全策略制定和维护', '制定和维护信息安全策略'),

-- GDPR检查项
('数据主体权利', 'GDPR', 'DATA_SUBJECT_RIGHTS', '检查数据访问权、更正权、删除权、可携带权等', '建立完善的数据主体权利保护机制'),
('合法依据', 'GDPR', 'LAWFUL_BASIS', '检查数据处理合法依据', '确保数据处理有合法依据'),
('同意管理', 'GDPR', 'CONSENT', '检查数据主体同意管理机制', '建立数据主体同意管理机制'),
('数据最小化', 'GDPR', 'DATA_MINIMIZATION', '检查数据收集最小化原则', '遵循数据收集最小化原则'),
('准确性', 'GDPR', 'ACCURACY', '检查个人数据准确性维护', '维护个人数据的准确性'),
('存储限制', 'GDPR', 'STORAGE_LIMITATION', '检查个人数据存储期限限制', '限制个人数据存储期限'),
('完整性保密性', 'GDPR', 'INTEGRITY_CONFIDENTIALITY', '检查个人数据完整性和保密性保护', '保护个人数据的完整性和保密性'),
('问责制', 'GDPR', 'ACCOUNTABILITY', '检查数据处理问责制实施', '实施数据处理问责制'),
('数据保护影响评估', 'GDPR', 'DPIA', '检查DPIA实施情况', '实施数据保护影响评估'),
('数据泄露通知', 'GDPR', 'BREACH_NOTIFICATION', '检查数据泄露通知程序', '建立数据泄露通知程序');

-- 插入初始报表模板数据
INSERT INTO report_template (template_name, report_type, template_file_path, generation_frequency, enabled, description, template_config, template_params) VALUES
('等保三级合规报告模板', '等保', 'templates/reports/dengbao-level3.ftl', 'MONTHLY', 1, '等保三级合规性检查报告模板，包含访问控制、安全审计、数据完整性等检查项', '{"title": "银行数据安全管理系统 - 等保三级合规报告", "sections": ["访问控制", "安全审计", "数据完整性", "通信完整性", "通信保密性"]}', '{"reportPeriod": "月度", "complianceStandard": "等保三级"}'),
('PCI-DSS合规报告模板', 'PCI-DSS', 'templates/reports/pci-dss.ftl', 'QUARTERLY', 1, 'PCI-DSS合规性检查报告模板，包含12个核心要求的检查', '{"title": "银行数据安全管理系统 - PCI-DSS合规报告", "sections": ["防火墙配置", "默认密码管理", "持卡人数据保护", "加密传输", "防病毒软件"]}', '{"reportPeriod": "季度", "complianceStandard": "PCI-DSS"}'),
('GDPR合规报告模板', 'GDPR', 'templates/reports/gdpr.ftl', 'MONTHLY', 1, 'GDPR合规性检查报告模板，包含数据主体权利、数据保护等检查项', '{"title": "银行数据安全管理系统 - GDPR合规报告", "sections": ["数据主体权利", "合法依据", "同意管理", "数据最小化", "准确性"]}', '{"reportPeriod": "月度", "complianceStandard": "GDPR"}'),
('综合合规报告模板', '自定义', 'templates/reports/comprehensive.ftl', 'WEEKLY', 1, '综合合规性检查报告模板，包含多种合规标准的综合评估', '{"title": "银行数据安全管理系统 - 综合合规报告", "sections": ["等保合规", "PCI-DSS合规", "GDPR合规", "安全态势", "风险评估"]}', '{"reportPeriod": "周度", "complianceStandard": "综合"}');

echo "合规报表模块数据库初始化完成！"
EOF

echo "数据库初始化脚本执行完成"