-- 银行数据安全管理系统数据库初始化脚本
-- 数据库名称: bankshield
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci

CREATE DATABASE IF NOT EXISTS bankshield DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bankshield;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码（已加密）',
  name VARCHAR(100) NOT NULL COMMENT '真实姓名',
  phone VARCHAR(20) COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  dept_id BIGINT DEFAULT 0 COMMENT '部门ID',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',
  remark VARCHAR(255) COMMENT '备注',
  INDEX idx_username(username),
  INDEX idx_dept_id(dept_id),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
  description VARCHAR(255) COMMENT '描述',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',
  INDEX idx_role_code(role_code),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
  menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
  menu_code VARCHAR(50) COMMENT '菜单编码',
  path VARCHAR(100) COMMENT '路由路径',
  component VARCHAR(100) COMMENT '组件路径',
  icon VARCHAR(50) COMMENT '图标',
  sort_order INT DEFAULT 0 COMMENT '排序',
  menu_type TINYINT DEFAULT 0 COMMENT '类型: 0-菜单, 1-按钮',
  permission VARCHAR(100) COMMENT '权限标识',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_parent_id(parent_id),
  INDEX idx_menu_code(menu_code),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
  dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
  dept_code VARCHAR(50) UNIQUE COMMENT '部门编码',
  leader VARCHAR(50) COMMENT '负责人',
  phone VARCHAR(20) COMMENT '联系电话',
  email VARCHAR(100) COMMENT '邮箱',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_parent_id(parent_id),
  INDEX idx_dept_code(dept_code),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY idx_user_role (user_id, role_id),
  INDEX idx_user_id(user_id),
  INDEX idx_role_id(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY idx_role_menu (role_id, menu_id),
  INDEX idx_role_id(role_id),
  INDEX idx_menu_id(menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 敏感数据类型表
CREATE TABLE IF NOT EXISTS sensitive_data_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  type_name VARCHAR(100) NOT NULL COMMENT '类型名称',
  type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类型编码',
  pattern VARCHAR(255) COMMENT '正则表达式',
  encryption_alg VARCHAR(50) COMMENT '加密算法: SM2, SM4, AES, RSA',
  mask_rule VARCHAR(255) COMMENT '脱敏规则',
  description VARCHAR(500) COMMENT '描述',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',
  INDEX idx_type_code(type_code),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感数据类型表';

-- 数据加密配置表
CREATE TABLE IF NOT EXISTS encrypt_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  table_name VARCHAR(100) NOT NULL COMMENT '表名',
  column_name VARCHAR(100) NOT NULL COMMENT '列名',
  data_type_id BIGINT COMMENT '敏感数据类型ID',
  encrypt_alg VARCHAR(50) COMMENT '加密算法',
  encrypt_key VARCHAR(255) COMMENT '加密密钥',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  description VARCHAR(500) COMMENT '描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',
  INDEX idx_table_column(table_name, column_name),
  INDEX idx_data_type_id(data_type_id),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据加密配置表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  operation VARCHAR(100) COMMENT '操作类型',
  module VARCHAR(50) COMMENT '操作模块',
  method VARCHAR(20) COMMENT '请求方法',
  url VARCHAR(255) COMMENT '请求URL',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  location VARCHAR(100) COMMENT '地理位置',
  user_agent VARCHAR(500) COMMENT 'UserAgent',
  request_params TEXT COMMENT '请求参数',
  response_result TEXT COMMENT '返回结果',
  execution_time BIGINT COMMENT '执行时间(ms)',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功',
  error_msg TEXT COMMENT '错误信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id(user_id),
  INDEX idx_username(username),
  INDEX idx_operation(operation),
  INDEX idx_create_time(create_time),
  INDEX idx_ip_address(ip_address),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 数据访问日志表
CREATE TABLE IF NOT EXISTS data_access_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  data_type VARCHAR(50) COMMENT '数据类型',
  data_id VARCHAR(100) COMMENT '数据ID',
  operation VARCHAR(50) COMMENT '操作类型: QUERY, UPDATE, DELETE, EXPORT',
  table_name VARCHAR(100) COMMENT '表名',
  access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  is_sensitive TINYINT DEFAULT 0 COMMENT '是否为敏感数据: 0-否, 1-是',
  is_masked TINYINT DEFAULT 0 COMMENT '是否脱敏: 0-否, 1-是',
  status TINYINT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id(user_id),
  INDEX idx_table_name(table_name),
  INDEX idx_access_time(access_time),
  INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据访问日志表';

-- 加密密钥表
CREATE TABLE IF NOT EXISTS encrypt_key (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  key_name VARCHAR(100) NOT NULL UNIQUE COMMENT '密钥名称',
  key_type VARCHAR(50) COMMENT '密钥类型: SM2, SM4, AES, RSA',
  key_value TEXT NOT NULL COMMENT '密钥内容',
  key_status TINYINT DEFAULT 1 COMMENT '密钥状态: 0-禁用, 1-启用, 2-已过期',
  expire_time DATETIME COMMENT '过期时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',
  description VARCHAR(500) COMMENT '描述',
  INDEX idx_key_name(key_name),
  INDEX idx_key_status(key_status),
  INDEX idx_expire_time(expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加密密钥表';

-- 系统参数配置表
CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  config_value VARCHAR(500) NOT NULL COMMENT '配置值',
  config_name VARCHAR(100) COMMENT '配置名称',
  description VARCHAR(500) COMMENT '描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',
  INDEX idx_config_key(config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置表';

-- 系统公告表
CREATE TABLE IF NOT EXISTS sys_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  notice_title VARCHAR(100) NOT NULL COMMENT '公告标题',
  notice_type TINYINT COMMENT '公告类型: 1-通知, 2-公告',
  notice_content TEXT COMMENT '公告内容',
  status TINYINT DEFAULT 0 COMMENT '状态: 0-关闭, 1-正常',
  create_by VARCHAR(50) COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by VARCHAR(50) COMMENT '更新人',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(255) COMMENT '备注',
  INDEX idx_notice_type(notice_type),
  INDEX idx_status(status),
  INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 插入初始数据
-- 部门数据
INSERT INTO sys_dept (dept_name, dept_code, leader, phone, sort_order, status) VALUES
('总行', 'HQ', '行长', '010-12345678', 1, 1),
('信息技术部', 'IT', 'IT经理', '010-12345679', 2, 1),
('风险管理部', 'RM', '风控经理', '010-12345680', 3, 1),
('运营管理部', 'OP', '运营经理', '010-12345681', 4, 1);

-- 角色数据
INSERT INTO sys_role (role_name, role_code, description, status) VALUES
('超级管理员', 'SUPER_ADMIN', '系统最高管理员', 1),
('系统管理员', 'ADMIN', '系统管理员', 1),
('安全管理员', 'SECURITY_ADMIN', '负责系统安全管理', 1),
('审计管理员', 'AUDIT_ADMIN', '负责系统审计', 1),
('普通用户', 'USER', '普通操作用户', 1);

-- 用户数据（密码：123456，已使用MD5加密）
INSERT INTO sys_user (username, password, name, phone, email, dept_id, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000001', 'admin@bankshield.com', 1, 1),
('security', 'e10adc3949ba59abbe56e057f20f883e', '安全管理员', '13800000002', 'security@bankshield.com', 2, 1),
('audit', 'e10adc3949ba59abbe56e057f20f883e', '审计管理员', '13800000003', 'audit@bankshield.com', 3, 1),
('user', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', '13800000004', 'user@bankshield.com', 4, 1);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), -- admin -> 超级管理员
(2, 3), -- security -> 安全管理员
(3, 4), -- audit -> 审计管理员
(4, 5); -- user -> 普通用户

-- 敏感数据类型
INSERT INTO sensitive_data_type (type_name, type_code, pattern, encryption_alg, mask_rule, description, status) VALUES
('手机号', 'PHONE', '^1[3-9]\\d{9}$', 'SM4', 'PHONE', '中国大陆手机号', 1),
('身份证号', 'ID_CARD', '^\\d{17}[\\dXx]$', 'SM4', 'ID_CARD', '中国大陆身份证号', 1),
('银行卡号', 'BANK_CARD', '^\\d{16,19}$', 'SM4', 'BANK_CARD', '银行卡号', 1),
('姓名', 'NAME', '', 'SM2', 'NAME', '姓名信息', 1),
('邮箱', 'EMAIL', '^\\w+@[\\w.]+$', 'SM4', 'EMAIL', '邮箱地址', 1),
('地址', 'ADDRESS', '', 'SM4', 'ADDRESS', '详细地址', 1);

-- 系统参数配置
INSERT INTO sys_config (config_key, config_value, config_name, description) VALUES
('sys.name', 'BankShield', '系统名称', '系统名称'),
('sys.version', '1.0.0', '系统版本', '系统版本号'),
('security.password.strength', '3', '密码强度', '1-低, 2-中, 3-高'),
('audit.log.retention.days', '180', '审计日志保留天数', '审计日志保留天数'),
('data.mask.enabled', '1', '数据脱敏开关', '是否启用数据脱敏: 0-关闭, 1-开启'),
('encryption.default.alg', 'SM4', '默认加密算法', '默认使用的加密算法');

COMMIT;