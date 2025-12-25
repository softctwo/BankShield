-- BankShield测试数据初始化脚本

-- 插入测试部门数据
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `dept_code`, `leader`, `phone`, `email`, `sort_order`, `status`, `create_time`, `update_time`) VALUES
(1, 0, '总行', 'HEAD_OFFICE', '张三', '010-12345678', 'head@bankshield.com', 1, 1, NOW(), NOW()),
(2, 1, '技术部', 'TECH_DEPT', '李四', '010-12345679', 'tech@bankshield.com', 2, 1, NOW(), NOW()),
(3, 1, '财务部', 'FINANCE_DEPT', '王五', '010-12345680', 'finance@bankshield.com', 3, 1, NOW(), NOW()),
(4, 1, '风控部', 'RISK_DEPT', '赵六', '010-12345681', 'risk@bankshield.com', 4, 1, NOW(), NOW());

-- 插入测试角色数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, NOW(), NOW(), 'system', 'system'),
(2, '系统管理员', 'SYSTEM_ADMIN', '系统管理员，负责系统配置和管理', 1, NOW(), NOW(), 'system', 'system'),
(3, '业务操作员', 'BUSINESS_OPERATOR', '业务操作员，负责日常业务操作', 1, NOW(), NOW(), 'system', 'system'),
(4, '审计员', 'AUDITOR', '审计员，负责系统审计和日志查看', 1, NOW(), NOW(), 'system', 'system');

-- 插入测试用户数据（密码都是123456，使用BCrypt加密）
-- BCrypt加密后的123456：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUO
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `phone`, `email`, `dept_id`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `remark`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUO', '超级管理员', '13800138000', 'admin@bankshield.com', 1, 1, NOW(), NOW(), 'system', 'system', '系统内置超级管理员'),
(2, 'system', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUO', '系统管理员', '13800138001', 'system@bankshield.com', 2, 1, NOW(), NOW(), 'system', 'system', '系统管理员'),
(3, 'operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUO', '业务操作员', '13800138002', 'operator@bankshield.com', 3, 1, NOW(), NOW(), 'system', 'system', '业务操作员'),
(4, 'auditor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUO', '审计员', '13800138003', 'auditor@bankshield.com', 4, 1, NOW(), NOW(), 'system', 'system', '审计员');

-- 插入用户角色关联数据（这里需要用户角色关联表，暂时注释）
-- INSERT INTO `sys_user_role` (`user_id`, `role_id`, `create_time`, `create_by`) VALUES
-- (1, 1, NOW(), 'system'),
-- (2, 2, NOW(), 'system'),
-- (3, 3, NOW(), 'system'),
-- (4, 4, NOW(), 'system');

-- 插入菜单数据（这里需要菜单表，暂时注释）
-- INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_code`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`) VALUES
-- (1, 0, '系统管理', 'SYSTEM_MGMT', 1, '/system', 'Layout', 'system', 1, 1, NOW(), NOW()),
-- (2, 1, '用户管理', 'USER_MGMT', 2, '/system/user', 'system/user/index', 'user', 1, 1, NOW(), NOW()),
-- (3, 1, '角色管理', 'ROLE_MGMT', 2, '/system/role', 'system/role/index', 'peoples', 2, 1, NOW(), NOW()),
-- (4, 1, '部门管理', 'DEPT_MGMT', 2, '/system/dept', 'system/dept/index', 'tree', 3, 1, NOW(), NOW());

-- 插入角色菜单关联数据（这里需要角色菜单关联表，暂时注释）
-- INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`, `create_by`) VALUES
-- (1, 1, NOW(), 'system'),
-- (1, 2, NOW(), 'system'),
-- (1, 3, NOW(), 'system'),
-- (1, 4, NOW(), 'system'),
-- (2, 1, NOW(), 'system'),
-- (2, 2, NOW(), 'system'),
-- (2, 3, NOW(), 'system'),
-- (2, 4, NOW(), 'system');