-- 数据脱敏引擎菜单配置
-- 创建时间: 2024-12-31

-- 1. 创建数据脱敏顶级菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(NULL, '数据脱敏', 'desensitization', '/desensitization', NULL, 'Hide', 30, 0, 'desensitization', 1, NOW(), NOW());

-- 获取刚插入的数据脱敏菜单ID
SET @desensitization_menu_id = LAST_INSERT_ID();

-- 2. 脱敏规则管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@desensitization_menu_id, '脱敏规则', 'desensitization-rule', 'rule', '/views/desensitization/rule/index.vue', 'Document', 1, 1, 'desensitization:rule:list', 1, NOW(), NOW());

-- 获取脱敏规则菜单ID
SET @rule_menu_id = LAST_INSERT_ID();

-- 2.1 脱敏规则子菜单 - 查询
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@rule_menu_id, '查询规则', 'rule-query', NULL, NULL, NULL, 1, 2, 'desensitization:rule:query', 1, NOW(), NOW());

-- 2.2 脱敏规则子菜单 - 新增
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@rule_menu_id, '新增规则', 'rule-add', NULL, NULL, NULL, 2, 2, 'desensitization:rule:add', 1, NOW(), NOW());

-- 2.3 脱敏规则子菜单 - 编辑
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@rule_menu_id, '编辑规则', 'rule-edit', NULL, NULL, NULL, 3, 2, 'desensitization:rule:edit', 1, NOW(), NOW());

-- 2.4 脱敏规则子菜单 - 删除
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@rule_menu_id, '删除规则', 'rule-delete', NULL, NULL, NULL, 4, 2, 'desensitization:rule:delete', 1, NOW(), NOW());

-- 2.5 脱敏规则子菜单 - 测试
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@rule_menu_id, '测试规则', 'rule-test', NULL, NULL, NULL, 5, 2, 'desensitization:rule:test', 1, NOW(), NOW());

-- 3. 脱敏模板管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@desensitization_menu_id, '脱敏模板', 'desensitization-template', 'template', '/views/desensitization/template/index.vue', 'Files', 2, 1, 'desensitization:template:list', 1, NOW(), NOW());

-- 获取脱敏模板菜单ID
SET @template_menu_id = LAST_INSERT_ID();

-- 3.1 脱敏模板子菜单 - 查询
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@template_menu_id, '查询模板', 'template-query', NULL, NULL, NULL, 1, 2, 'desensitization:template:query', 1, NOW(), NOW());

-- 3.2 脱敏模板子菜单 - 新增
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@template_menu_id, '新增模板', 'template-add', NULL, NULL, NULL, 2, 2, 'desensitization:template:add', 1, NOW(), NOW());

-- 3.3 脱敏模板子菜单 - 编辑
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@template_menu_id, '编辑模板', 'template-edit', NULL, NULL, NULL, 3, 2, 'desensitization:template:edit', 1, NOW(), NOW());

-- 3.4 脱敏模板子菜单 - 删除
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@template_menu_id, '删除模板', 'template-delete', NULL, NULL, NULL, 4, 2, 'desensitization:template:delete', 1, NOW(), NOW());

-- 3.5 脱敏模板子菜单 - 应用
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@template_menu_id, '应用模板', 'template-apply', NULL, NULL, NULL, 5, 2, 'desensitization:template:apply', 1, NOW(), NOW());

-- 4. 脱敏日志查询菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@desensitization_menu_id, '脱敏日志', 'desensitization-log', 'log', '/views/desensitization/log/index.vue', 'List', 3, 1, 'desensitization:log:list', 1, NOW(), NOW());

-- 获取脱敏日志菜单ID
SET @log_menu_id = LAST_INSERT_ID();

-- 4.1 脱敏日志子菜单 - 查询
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@log_menu_id, '查询日志', 'log-query', NULL, NULL, NULL, 1, 2, 'desensitization:log:query', 1, NOW(), NOW());

-- 4.2 脱敏日志子菜单 - 导出
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@log_menu_id, '导出日志', 'log-export', NULL, NULL, NULL, 2, 2, 'desensitization:log:export', 1, NOW(), NOW());

-- 4.3 脱敏日志子菜单 - 统计
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@log_menu_id, '统计分析', 'log-statistics', NULL, NULL, NULL, 3, 2, 'desensitization:log:statistics', 1, NOW(), NOW());

-- 5. 脱敏测试工具菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@desensitization_menu_id, '脱敏测试', 'desensitization-test', 'test', '/views/desensitization/test/index.vue', 'Operation', 4, 1, 'desensitization:test', 1, NOW(), NOW());

-- 获取脱敏测试菜单ID
SET @test_menu_id = LAST_INSERT_ID();

-- 5.1 脱敏测试子菜单 - 单条测试
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@test_menu_id, '单条测试', 'test-single', NULL, NULL, NULL, 1, 2, 'desensitization:test:single', 1, NOW(), NOW());

-- 5.2 脱敏测试子菜单 - 批量测试
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@test_menu_id, '批量测试', 'test-batch', NULL, NULL, NULL, 2, 2, 'desensitization:test:batch', 1, NOW(), NOW());

-- 查询所有数据脱敏相关菜单
SELECT 
    m1.id,
    m1.parent_id,
    m1.menu_name,
    m1.menu_code,
    m1.path,
    m1.icon,
    m1.sort_order,
    m1.menu_type,
    m1.permission,
    m1.status
FROM sys_menu m1
WHERE m1.menu_code LIKE 'desensitization%' OR m1.menu_name = '数据脱敏'
ORDER BY m1.parent_id, m1.sort_order;
