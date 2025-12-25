-- BankShield测试数据初始化脚本

-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS bankshield_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bankshield_test;

-- 插入测试部门数据
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) VALUES
(1, 0, '0', '总部', 1, '张三', '13800138000', 'zhangsan@bankshield.com', 0, 0, 'system', NOW(), 'system', NOW()),
(2, 1, '0,1', '技术部', 1, '李四', '13800138001', 'lisi@bankshield.com', 0, 0, 'system', NOW(), 'system', NOW()),
(3, 1, '0,1', '业务部', 2, '王五', '13800138002', 'wangwu@bankshield.com', 0, 0, 'system', NOW(), 'system', NOW()),
(4, 1, '0,1', '风控部', 3, '赵六', '13800138003', 'zhaoliu@bankshield.com', 0, 0, 'system', NOW(), 'system', NOW()),
(5, 1, '0,1', '审计部', 4, '钱七', '13800138004', 'qianqi@bankshield.com', 0, 0, 'system', NOW(), 'system', NOW());

-- 插入测试角色数据
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark) VALUES
(1, '超级管理员', 'admin', 1, '1', 1, 1, 0, 0, 'system', NOW(), 'system', NOW(), '超级管理员'),
(2, '普通用户', 'common', 2, '2', 1, 1, 0, 0, 'system', NOW(), 'system', NOW(), '普通用户角色'),
(3, '数据分析师', 'analyst', 3, '2', 1, 1, 0, 0, 'system', NOW(), 'system', NOW(), '数据分析师角色'),
(4, '审计员', 'auditor', 4, '1', 1, 1, 0, 0, 'system', NOW(), 'system', NOW(), '审计员角色'),
(5, '系统管理员', 'system_admin', 5, '1', 1, 1, 0, 0, 'system', NOW(), 'system', NOW(), '系统管理员角色');

-- 插入测试菜单数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(1, '系统管理', 0, 1, 'system', NULL, 1, 0, 'M', 0, 0, 'system', 'el-icon-setting', 'system', NOW(), 'system', NOW(), '系统管理目录'),
(2, '用户管理', 1, 1, 'user', 'system/user/index', 1, 0, 'C', 0, 0, 'system:user', 'el-icon-user', 'system', NOW(), 'system', NOW(), '用户管理菜单'),
(3, '角色管理', 1, 2, 'role', 'system/role/index', 1, 0, 'C', 0, 0, 'system:role', 'el-icon-s-custom', 'system', NOW(), 'system', NOW(), '角色管理菜单'),
(4, '菜单管理', 1, 3, 'menu', 'system/menu/index', 1, 0, 'C', 0, 0, 'system:menu', 'el-icon-menu', 'system', NOW(), 'system', NOW(), '菜单管理菜单'),
(5, '部门管理', 1, 4, 'dept', 'system/dept/index', 1, 0, 'C', 0, 0, 'system:dept', 'el-icon-office-building', 'system', NOW(), 'system', NOW(), '部门管理菜单'),
(6, '数据安全', 0, 2, 'security', NULL, 1, 0, 'M', 0, 0, 'security', 'el-icon-lock', 'system', NOW(), 'system', NOW(), '数据安全目录'),
(7, '密钥管理', 6, 1, 'key', 'security/key/index', 1, 0, 'C', 0, 0, 'security:key', 'el-icon-key', 'system', NOW(), 'system', NOW(), '密钥管理菜单'),
(8, '数据加密', 6, 2, 'encrypt', 'security/encrypt/index', 1, 0, 'C', 0, 0, 'security:encrypt', 'el-icon-lock', 'system', NOW(), 'system', NOW(), '数据加密菜单'),
(9, '数据脱敏', 6, 3, 'masking', 'security/masking/index', 1, 0, 'C', 0, 0, 'security:masking', 'el-icon-view', 'system', NOW(), 'system', NOW(), '数据脱敏菜单'),
(10, '审计管理', 0, 3, 'audit', NULL, 1, 0, 'M', 0, 0, 'audit', 'el-icon-document', 'system', NOW(), 'system', NOW(), '审计管理目录'),
(11, '操作审计', 10, 1, 'operation', 'audit/operation/index', 1, 0, 'C', 0, 0, 'audit:operation', 'el-icon-tickets', 'system', NOW(), 'system', NOW(), '操作审计菜单'),
(12, '登录审计', 10, 2, 'login', 'audit/login/index', 1, 0, 'C', 0, 0, 'audit:login', 'el-icon-time', 'system', NOW(), 'system', NOW(), '登录审计菜单'),
(13, '监控告警', 0, 4, 'monitor', NULL, 1, 0, 'M', 0, 0, 'monitor', 'el-icon-data-line', 'system', NOW(), 'system', NOW(), '监控告警目录'),
(14, '实时监控', 13, 1, 'realtime', 'monitor/realtime/index', 1, 0, 'C', 0, 0, 'monitor:realtime', 'el-icon-monitor', 'system', NOW(), 'system', NOW(), '实时监控菜单'),
(15, '告警管理', 13, 2, 'alert', 'monitor/alert/index', 1, 0, 'C', 0, 0, 'monitor:alert', 'el-icon-warning', 'system', NOW(), 'system', NOW(), '告警管理菜单');

-- 插入测试用户数据（密码为123456，BCrypt加密）
INSERT INTO sys_user (user_id, dept_id, username, password, name, phone, email, avatar, status, del_flag, login_ip, login_date, create_by, create_time, update_by, update_time, remark) VALUES
(1, 2, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO', '超级管理员', '13800138000', 'admin@bankshield.com', '', 0, 0, '127.0.0.1', NOW(), 'system', NOW(), 'system', NOW(), '超级管理员'),
(2, 2, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO', '测试用户', '13800138001', 'test@bankshield.com', '', 0, 0, '127.0.0.1', NOW(), 'system', NOW(), 'system', NOW(), '测试用户'),
(3, 3, 'analyst', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO', '数据分析师', '13800138002', 'analyst@bankshield.com', '', 0, 0, '127.0.0.1', NOW(), 'system', NOW(), 'system', NOW(), '数据分析师'),
(4, 4, 'auditor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO', '审计员', '13800138003', 'auditor@bankshield.com', '', 0, 0, '127.0.0.1', NOW(), 'system', NOW(), 'system', NOW(), '审计员'),
(5, 5, 'system_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9KFLHcpN7W/YKaO', '系统管理员', '13800138004', 'system_admin@bankshield.com', '', 0, 0, '127.0.0.1', NOW(), 'system', NOW(), 'system', NOW(), '系统管理员');

-- 插入用户角色关联数据
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), -- admin -> 超级管理员
(2, 2), -- test -> 普通用户
(3, 3), -- analyst -> 数据分析师
(4, 4), -- auditor -> 审计员
(5, 5); -- system_admin -> 系统管理员

-- 插入角色菜单关联数据
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 超级管理员拥有所有菜单权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
-- 普通用户权限
(2, 6), (2, 7), (2, 8), (2, 9), (2, 13), (2, 14), (2, 15),
-- 数据分析师权限
(3, 6), (3, 7), (3, 8), (3, 9), (3, 13), (3, 14), (3, 15),
-- 审计员权限
(4, 10), (4, 11), (4, 12), (4, 13), (4, 14), (4, 15),
-- 系统管理员权限
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 10), (5, 11), (5, 12), (5, 13), (5, 14), (5, 15);

-- 插入测试数据源配置
INSERT INTO data_source (source_id, source_name, source_type, host, port, database_name, username, password, connection_params, status, create_time, update_time, remark) VALUES
(1, '测试MySQL数据库', 'MySQL', 'localhost', 3306, 'bankshield_test', 'root', 'test123', '{"useSSL":false,"serverTimezone":"Asia/Shanghai"}', 1, NOW(), NOW(), '测试MySQL数据源'),
(2, '测试Oracle数据库', 'Oracle', 'localhost', 1521, 'ORCL', 'system', 'oracle123', '{"driverType":"thin"}', 1, NOW(), NOW(), '测试Oracle数据源'),
(3, '测试PostgreSQL数据库', 'PostgreSQL', 'localhost', 5432, 'bankshield_test', 'postgres', 'postgres123', '{"sslmode":"disable"}', 1, NOW(), NOW(), '测试PostgreSQL数据源');

-- 插入测试数据资产
INSERT INTO data_asset (asset_id, asset_name, asset_type, source_id, database_name, table_name, field_name, sensitive_level, data_size, record_count, status, create_time, update_time, remark) VALUES
(1, '客户信息表', 'TABLE', 1, 'bankshield_test', 'customer_info', NULL, 'HIGH', 1048576, 10000, 1, NOW(), NOW(), '存储客户基本信息'),
(2, '交易记录表', 'TABLE', 1, 'bankshield_test', 'transaction_records', NULL, 'HIGH', 5242880, 50000, 1, NOW(), NOW(), '存储交易记录信息'),
(3, '员工信息表', 'TABLE', 1, 'bankshield_test', 'employee_info', NULL, 'MEDIUM', 524288, 500, 1, NOW(), NOW(), '存储员工基本信息'),
(4, '系统日志表', 'TABLE', 1, 'bankshield_test', 'system_logs', NULL, 'LOW', 10485760, 100000, 1, NOW(), NOW(), '存储系统日志信息'),
(5, '用户行为表', 'TABLE', 1, 'bankshield_test', 'user_behavior', NULL, 'MEDIUM', 2097152, 20000, 1, NOW(), NOW(), '存储用户行为数据');

-- 插入测试脱敏规则
INSERT INTO data_masking_rule (rule_id, rule_name, rule_type, algorithm_params, target_type, target_field, rule_status, priority, create_time, update_time, remark) VALUES
(1, '手机号脱敏规则', 'REPLACE', '{"replaceChar":"*","startPos":3,"endPos":7}', 'PHONE', 'phone', 1, 1, NOW(), NOW(), '将手机号中间4位替换为*'),
(2, '身份证号脱敏规则', 'REPLACE', '{"replaceChar":"*","startPos":6,"endPos":14}', 'ID_CARD', 'id_card', 1, 2, NOW(), NOW(), '将身份证号中间8位替换为*'),
(3, '银行卡号脱敏规则', 'REPLACE', '{"replaceChar":"*","startPos":4,"endPos":12}', 'BANK_CARD', 'bank_card', 1, 3, NOW(), NOW(), '将银行卡号中间8位替换为*'),
(4, '邮箱脱敏规则', 'HASH', '{"algorithm":"SHA256"}', 'EMAIL', 'email', 1, 4, NOW(), NOW(), '使用SHA256算法对邮箱进行哈希处理'),
(5, '姓名脱敏规则', 'TRUNCATE', '{"length":1,"suffix":"**"}', 'NAME', 'name', 1, 5, NOW(), NOW(), '只保留姓氏，其余用**代替');

-- 插入测试告警规则
INSERT INTO alert_rule (rule_id, rule_name, rule_type, metric_type, threshold_value, alert_level, alert_channel, rule_status, create_time, update_time, remark) VALUES
(1, '登录失败次数告警', 'THRESHOLD', 'LOGIN_FAILURE_COUNT', 5, 'MEDIUM', 'EMAIL', 1, NOW(), NOW(), '当登录失败次数达到5次时触发告警'),
(2, '数据访问异常告警', 'THRESHOLD', 'DATA_ACCESS_COUNT', 100, 'HIGH', 'SMS', 1, NOW(), NOW(), '当数据访问次数异常时触发告警'),
(3, '密钥使用异常告警', 'THRESHOLD', 'KEY_USAGE_COUNT', 1000, 'HIGH', 'EMAIL', 1, NOW(), NOW(), '当密钥使用次数异常时触发告警'),
(4, '系统资源告警', 'THRESHOLD', 'CPU_USAGE', 80, 'MEDIUM', 'WEBHOOK', 1, NOW(), NOW(), '当CPU使用率超过80%时触发告警'),
(5, '响应时间告警', 'THRESHOLD', 'RESPONSE_TIME', 5000, 'LOW', 'EMAIL', 1, NOW(), NOW(), '当响应时间超过5秒时触发告警');

-- 插入测试报告模板
INSERT INTO report_template (template_id, template_name, report_type, template_content, template_status, create_time, update_time, remark) VALUES
(1, '合规性检查报告模板', 'COMPLIANCE', '{"sections":[{"title":"合规性概述","type":"summary"},{"title":"详细检查结果","type":"details"},{"title":"整改建议","type":"recommendations"}]}', 1, NOW(), NOW(), '用于生成合规性检查报告'),
(2, '数据安全评估报告模板', 'SECURITY', '{"sections":[{"title":"安全评估概述","type":"summary"},{"title":"风险分析","type":"risk-analysis"},{"title":"安全建议","type":"recommendations"}]}', 1, NOW(), NOW(), '用于生成数据安全评估报告'),
(3, '密钥管理报告模板', 'KEY_MANAGEMENT', '{"sections":[{"title":"密钥概览","type":"overview"},{"title":"密钥使用情况","type":"usage"},{"title":"密钥轮换记录","type":"rotation"}]}', 1, NOW(), NOW(), '用于生成密钥管理报告'),
(4, '审计报告模板', 'AUDIT', '{"sections":[{"title":"审计概述","type":"summary"},{"title":"操作统计","type":"statistics"},{"title":"异常分析","type":"anomalies"}]}', 1, NOW(), NOW(), '用于生成审计报告'),
(5, '监控报告模板', 'MONITOR', '{"sections":[{"title":"监控概览","type":"overview"},{"title":"性能指标","type":"metrics"},{"title":"告警统计","type":"alerts"}]}', 1, NOW(), NOW(), '用于生成监控报告');

-- 插入测试水印模板
INSERT INTO watermark_template (template_id, template_name, watermark_type, watermark_content, font_size, opacity, position, rotation, status, create_time, update_time, remark) VALUES
(1, '机密文档水印模板', 'TEXT', 'CONFIDENTIAL', 12, 0.5, 'CENTER', 45, 1, NOW(), NOW(), '用于机密文档的水印'),
(2, '绝密文档水印模板', 'TEXT', 'TOP SECRET', 16, 0.7, 'DIAGONAL', 315, 1, NOW(), NOW(), '用于绝密文档的水印'),
(3, '内部文档水印模板', 'TEXT', 'INTERNAL USE ONLY', 10, 0.3, 'FOOTER', 0, 1, NOW(), NOW(), '用于内部文档的水印'),
(4, '用户名水印模板', 'DYNAMIC', '{username}', 8, 0.4, 'HEADER', 0, 1, NOW(), NOW(), '动态显示当前用户名的水印'),
(5, '时间戳水印模板', 'DYNAMIC', '{timestamp}', 8, 0.4, 'FOOTER', 0, 1, NOW(), NOW(), '动态显示时间戳的水印');

-- 插入测试合规检查项
INSERT INTO compliance_check_item (item_id, item_name, compliance_standard, check_content, check_method, check_script, expected_result, item_status, create_time, update_time, remark) VALUES
(1, '数据加密合规检查', 'GB_T_35273', '检查敏感数据是否正确加密存储', 'AUTOMATIC', 'SELECT COUNT(*) FROM sensitive_data WHERE encryption_status != "ENCRYPTED"', '0', 1, NOW(), NOW(), '检查敏感数据是否加密'),
(2, '访问权限合规检查', 'GB_T_35273', '检查用户访问权限是否符合最小权限原则', 'AUTOMATIC', 'SELECT COUNT(*) FROM user_permissions WHERE permission_level > "NECESSARY"', '0', 1, NOW(), NOW(), '检查访问权限配置'),
(3, '数据脱敏合规检查', 'GB_T_35273', '检查敏感数据是否正确脱敏', 'AUTOMATIC', 'SELECT COUNT(*) FROM masked_data WHERE masking_status != "MASKED"', '0', 1, NOW(), NOW(), '检查数据脱敏配置'),
(4, '密钥管理合规检查', 'GB_T_32905', '检查密钥生命周期管理是否符合标准', 'AUTOMATIC', 'SELECT COUNT(*) FROM encryption_keys WHERE lifecycle_status != "ACTIVE"', '0', 1, NOW(), NOW(), '检查密钥管理合规性'),
(5, '审计日志合规检查', 'GB_T_35273', '检查审计日志是否完整记录', 'AUTOMATIC', 'SELECT COUNT(*) FROM audit_logs WHERE completeness_status != "COMPLETE"', '0', 1, NOW(), NOW(), '检查审计日志完整性');

-- 插入测试操作审计数据
INSERT INTO operation_audit (audit_id, user_id, username, operation_type, operation_module, request_path, request_method, request_params, response_result, ip_address, user_agent, status, execute_time, create_time) VALUES
(1, 1, 'admin', 'CREATE_USER', 'SYSTEM', '/api/user', 'POST', '{"username":"test_user","password":"***"}', '{"code":200,"message":"success"}', '192.168.1.100', 'Mozilla/5.0', 1, 150, NOW()),
(2, 1, 'admin', 'UPDATE_USER', 'SYSTEM', '/api/user/1', 'PUT', '{"name":"updated_name"}', '{"code":200,"message":"success"}', '192.168.1.100', 'Mozilla/5.0', 1, 120, NOW()),
(3, 2, 'test', 'LOGIN', 'AUTH', '/api/auth/login', 'POST', '{"username":"test"}', '{"code":200,"message":"success"}', '192.168.1.101', 'Mozilla/5.0', 1, 100, NOW()),
(4, 3, 'analyst', 'QUERY_DATA', 'DATA', '/api/data/query', 'GET', '{"page":1,"size":10}', '{"code":200,"message":"success"}', '192.168.1.102', 'Mozilla/5.0', 1, 200, NOW()),
(5, 4, 'auditor', 'GENERATE_REPORT', 'REPORT', '/api/report/generate', 'POST', '{"template":"audit"}', '{"code":200,"message":"success"}', '192.168.1.103', 'Mozilla/5.0', 1, 500, NOW());

-- 插入测试登录审计数据
INSERT INTO login_audit (audit_id, user_id, username, login_ip, login_location, browser, os, login_status, login_message, create_time) VALUES
(1, 1, 'admin', '192.168.1.100', '北京市', 'Chrome', 'Windows 10', 1, '登录成功', NOW()),
(2, 2, 'test', '192.168.1.101', '上海市', 'Firefox', 'Mac OS', 1, '登录成功', NOW()),
(3, 3, 'analyst', '192.168.1.102', '广州市', 'Safari', 'iOS', 1, '登录成功', NOW()),
(4, 4, 'auditor', '192.168.1.103', '深圳市', 'Edge', 'Windows 11', 1, '登录成功', NOW()),
(5, 999, 'invalid_user', '192.168.1.200', '未知', 'Chrome', 'Windows 10', 0, '用户名不存在', NOW());

-- 插入测试监控指标数据
INSERT INTO monitor_metric (metric_id, metric_name, metric_type, current_value, threshold_value, unit, status, create_time, update_time) VALUES
(1, 'CPU使用率', 'SYSTEM', 45.2, 80, '%', 1, NOW(), NOW()),
(2, '内存使用率', 'SYSTEM', 62.8, 85, '%', 1, NOW(), NOW()),
(3, '磁盘使用率', 'SYSTEM', 38.5, 90, '%', 1, NOW(), NOW()),
(4, '数据库连接数', 'DATABASE', 25, 100, 'count', 1, NOW(), NOW()),
(5, 'API响应时间', 'API', 120, 2000, 'ms', 1, NOW(), NOW());

-- 插入测试告警记录数据
INSERT INTO alert_record (record_id, rule_id, alert_title, alert_content, alert_level, alert_status, handle_user, handle_time, handle_result, create_time, update_time) VALUES
(1, 1, '登录失败次数告警', '用户admin在5分钟内登录失败5次', 'MEDIUM', 1, 'system_admin', NOW(), '已处理，建议检查账户安全', NOW(), NOW()),
(2, 2, '数据访问异常告警', '系统检测到异常的数据访问模式', 'HIGH', 0, NULL, NULL, NULL, NOW(), NOW()),
(3, 3, '密钥使用异常告警', '密钥使用次数超过正常范围', 'HIGH', 1, 'security_admin', NOW(), '已确认，属于正常业务增长', NOW(), NOW()),
(4, 4, '系统资源告警', 'CPU使用率超过80%', 'MEDIUM', 1, 'system_admin', NOW(), '已处理，系统负载已恢复正常', NOW(), NOW()),
(5, 5, '响应时间告警', 'API响应时间超过5秒', 'LOW', 0, NULL, NULL, NULL, NOW(), NOW());

-- 插入测试数据血缘关系数据
INSERT INTO data_lineage (lineage_id, source_asset_id, target_asset_id, lineage_type, relationship_desc, create_time, update_time) VALUES
(1, 1, 2, 'FLOW', '客户信息 -> 交易记录', NOW(), NOW()),
(2, 2, 4, 'FLOW', '交易记录 -> 系统日志', NOW(), NOW()),
(3, 3, 1, 'DEPENDENCY', '员工信息依赖客户信息', NOW(), NOW()),
(4, 1, 5, 'FLOW', '客户信息 -> 用户行为', NOW(), NOW()),
(5, 5, 4, 'FLOW', '用户行为 -> 系统日志', NOW(), NOW());

-- 创建测试完成标记
SELECT '测试数据初始化完成' AS init_status;