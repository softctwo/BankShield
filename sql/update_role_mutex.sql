-- 更新数据库以支持三权分立机制
-- 在现有数据库基础上添加三权分立相关表和数据

USE bankshield;

-- 如果已经存在相关表，先删除
DROP TABLE IF EXISTS role_violation;
DROP TABLE IF EXISTS role_mutex;

-- 创建角色互斥规则表
CREATE TABLE role_mutex (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  mutex_role_code VARCHAR(50) NOT NULL COMMENT '互斥角色编码',
  description VARCHAR(255) COMMENT '互斥规则描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  create_by VARCHAR(50) COMMENT '创建人',
  UNIQUE KEY uk_role_mutex (role_code, mutex_role_code),
  INDEX idx_role_code(role_code),
  INDEX idx_mutex_role_code(mutex_role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色互斥规则表';

-- 创建角色违规记录表
CREATE TABLE role_violation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  mutex_role_code VARCHAR(50) NOT NULL COMMENT '互斥角色编码',
  violation_type TINYINT NOT NULL COMMENT '违规类型: 1-手动分配, 2-系统检测',
  violation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '违规时间',
  operator_id BIGINT COMMENT '操作人ID',
  operator_name VARCHAR(50) COMMENT '操作人姓名',
  status TINYINT DEFAULT 0 COMMENT '处理状态: 0-未处理, 1-已处理, 2-已忽略',
  handle_time DATETIME COMMENT '处理时间',
  handle_remark VARCHAR(500) COMMENT '处理备注',
  alert_sent TINYINT DEFAULT 0 COMMENT '是否已发送告警: 0-未发送, 1-已发送',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id(user_id),
  INDEX idx_violation_time(violation_time),
  INDEX idx_status(status),
  INDEX idx_alert_sent(alert_sent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色违规记录表';

-- 插入等保三级要求的角色互斥规则
INSERT INTO role_mutex (role_code, mutex_role_code, description, create_by) VALUES
('SYSTEM_ADMIN', 'SECURITY_ADMIN', '系统管理员与安全策略员互斥', 'system'),
('SYSTEM_ADMIN', 'AUDIT_ADMIN', '系统管理员与合规审计员互斥', 'system'),
('SECURITY_ADMIN', 'AUDIT_ADMIN', '安全策略员与合规审计员互斥', 'system'),
('SECURITY_ADMIN', 'SYSTEM_ADMIN', '安全策略员与系统管理员互斥', 'system'),
('AUDIT_ADMIN', 'SYSTEM_ADMIN', '合规审计员与系统管理员互斥', 'system'),
('AUDIT_ADMIN', 'SECURITY_ADMIN', '合规审计员与安全策略员互斥', 'system');

-- 创建三权分立专用角色（如果不存在）
INSERT IGNORE INTO sys_role (role_name, role_code, description, status, create_by) VALUES
('系统管理员', 'SYSTEM_ADMIN', '系统管理员：负责用户/角色管理、系统监控、备份恢复', 1, 'system'),
('安全策略员', 'SECURITY_ADMIN', '安全策略员：负责策略配置、密钥管理、风险规则', 1, 'system'),
('合规审计员', 'AUDIT_ADMIN', '合规审计员：负责日志查询（只读）、报表生成、策略监控', 1, 'system');

-- 创建系统配置项
INSERT IGNORE INTO sys_config (config_key, config_value, config_name, description) VALUES
('role.mutex.enabled', '1', '角色互斥检查开关', '是否启用三权分立角色互斥检查: 0-关闭, 1-开启'),
('role.violation.alert.enabled', '1', '违规告警开关', '是否启用角色违规告警: 0-关闭, 1-开启'),
('role.check.job.cron', '0 0 2 * * ?', '角色检查任务定时', '角色互斥检查任务执行时间表达式'),
('role.violation.alert.channels', 'email,sms,wechat', '违规告警渠道', '角色违规告警通知渠道，多个用逗号分隔');

-- 添加三权分立相关菜单
INSERT IGNORE INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) VALUES
(0, '三权分立管理', 'ROLE_MUTEX', '/system/role-mutex', 'system/role-mutex/index', 'el-icon-lock', 100, 0, 'system:role:mutex', 1),
(0, '系统管理', 'SYSTEM_MGMT', '/system', 'Layout', 'el-icon-setting', 1, 0, 'system:manage', 1);

-- 给超级管理员添加三权分立管理权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE menu_code IN ('ROLE_MUTEX', 'SYSTEM_MGMT');

-- 创建测试数据（可选）
-- 注意：以下测试数据会创建角色违规情况，仅用于测试
-- INSERT INTO role_violation (user_id, username, role_code, mutex_role_code, violation_type, operator_id, operator_name, status, create_time) VALUES
-- (1, 'admin', 'SYSTEM_ADMIN', 'SECURITY_ADMIN', 2, null, 'system', 0, NOW()),
-- (1, 'admin', 'SYSTEM_ADMIN', 'AUDIT_ADMIN', 2, null, 'system', 0, NOW());

COMMIT;

-- 验证数据
SELECT '角色互斥规则数量' as description, COUNT(*) as count FROM role_mutex
UNION ALL
SELECT '三权分立角色数量' as description, COUNT(*) as count FROM sys_role WHERE role_code IN ('SYSTEM_ADMIN', 'SECURITY_ADMIN', 'AUDIT_ADMIN')
UNION ALL
SELECT '系统配置项数量' as description, COUNT(*) as count FROM sys_config WHERE config_key LIKE 'role.%';