-- =============================================
-- BankShield 访问控制模块菜单配置
-- 功能：访问策略、MFA配置、IP访问控制
-- 版本：v1.0
-- 日期：2025-01-04
-- =============================================

-- 1. 顶级菜单：访问控制
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES (0, '访问控制', 'M', '/access-control', NULL, NULL, 'Lock', 6, '0', '访问控制强化模块');

-- 获取刚插入的顶级菜单ID
SET @access_control_menu_id = LAST_INSERT_ID();

-- 2. 功能菜单：访问策略管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES (@access_control_menu_id, '访问策略', 'C', 'policy', 'access-control/policy/index', 'access:policy:query', 'Document', 1, '0', '访问策略管理');

SET @policy_menu_id = LAST_INSERT_ID();

-- 访问策略子权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES 
(@policy_menu_id, '策略查询', 'F', NULL, NULL, 'access:policy:query', NULL, 1, '0', '查询访问策略'),
(@policy_menu_id, '策略新增', 'F', NULL, NULL, 'access:policy:add', NULL, 2, '0', '新增访问策略'),
(@policy_menu_id, '策略编辑', 'F', NULL, NULL, 'access:policy:edit', NULL, 3, '0', '编辑访问策略'),
(@policy_menu_id, '策略删除', 'F', NULL, NULL, 'access:policy:delete', NULL, 4, '0', '删除访问策略'),
(@policy_menu_id, '策略测试', 'F', NULL, NULL, 'access:policy:test', NULL, 5, '0', '测试访问策略');

-- 3. 功能菜单：访问规则管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES (@access_control_menu_id, '访问规则', 'C', 'rule', 'access-control/rule/index', 'access:rule:query', 'List', 2, '0', '访问规则管理');

SET @rule_menu_id = LAST_INSERT_ID();

-- 访问规则子权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES 
(@rule_menu_id, '规则查询', 'F', NULL, NULL, 'access:rule:query', NULL, 1, '0', '查询访问规则'),
(@rule_menu_id, '规则新增', 'F', NULL, NULL, 'access:rule:add', NULL, 2, '0', '新增访问规则'),
(@rule_menu_id, '规则编辑', 'F', NULL, NULL, 'access:rule:edit', NULL, 3, '0', '编辑访问规则'),
(@rule_menu_id, '规则删除', 'F', NULL, NULL, 'access:rule:delete', NULL, 4, '0', '删除访问规则'),
(@rule_menu_id, '规则测试', 'F', NULL, NULL, 'access:rule:test', NULL, 5, '0', '测试访问规则');

-- 4. 功能菜单：MFA配置
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES (@access_control_menu_id, 'MFA配置', 'C', 'mfa', 'access-control/mfa/index', 'access:mfa:config', 'Key', 3, '0', 'MFA多因素认证配置');

SET @mfa_menu_id = LAST_INSERT_ID();

-- MFA配置子权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES 
(@mfa_menu_id, 'MFA查询', 'F', NULL, NULL, 'access:mfa:query', NULL, 1, '0', '查询MFA配置'),
(@mfa_menu_id, 'MFA配置', 'F', NULL, NULL, 'access:mfa:config', NULL, 2, '0', '配置MFA'),
(@mfa_menu_id, 'MFA验证', 'F', NULL, NULL, 'access:mfa:verify', NULL, 3, '0', '验证MFA');

-- 5. 功能菜单：临时权限管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES (@access_control_menu_id, '临时权限', 'C', 'temp-permission', 'access-control/temp-permission/index', 'access:temp:query', 'Timer', 4, '0', '临时权限管理');

SET @temp_menu_id = LAST_INSERT_ID();

-- 临时权限子权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES 
(@temp_menu_id, '权限查询', 'F', NULL, NULL, 'access:temp:query', NULL, 1, '0', '查询临时权限'),
(@temp_menu_id, '权限授予', 'F', NULL, NULL, 'access:temp:grant', NULL, 2, '0', '授予临时权限'),
(@temp_menu_id, '权限撤销', 'F', NULL, NULL, 'access:temp:revoke', NULL, 3, '0', '撤销临时权限'),
(@temp_menu_id, '权限管理', 'F', NULL, NULL, 'access:temp:manage', NULL, 4, '0', '管理临时权限');

-- 6. 功能菜单：IP访问控制
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES (@access_control_menu_id, 'IP访问控制', 'C', 'ip', 'access-control/ip/index', 'access:ip:query', 'Monitor', 5, '0', 'IP白名单和黑名单管理');

SET @ip_menu_id = LAST_INSERT_ID();

-- IP访问控制子权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES 
(@ip_menu_id, 'IP查询', 'F', NULL, NULL, 'access:ip:query', NULL, 1, '0', '查询IP访问控制'),
(@ip_menu_id, 'IP管理', 'F', NULL, NULL, 'access:ip:manage', NULL, 2, '0', '管理IP白名单和黑名单'),
(@ip_menu_id, 'IP检查', 'F', NULL, NULL, 'access:ip:check', NULL, 3, '0', '检查IP状态');

-- 7. 功能菜单：访问日志
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES (@access_control_menu_id, '访问日志', 'C', 'log', 'access-control/log/index', 'access:log:query', 'Document', 6, '0', '访问日志查询和统计');

SET @log_menu_id = LAST_INSERT_ID();

-- 访问日志子权限
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `order_num`, `status`, `remark`)
VALUES 
(@log_menu_id, '日志查询', 'F', NULL, NULL, 'access:log:query', NULL, 1, '0', '查询访问日志'),
(@log_menu_id, '日志统计', 'F', NULL, NULL, 'access:log:statistics', NULL, 2, '0', '访问统计分析'),
(@log_menu_id, '日志导出', 'F', NULL, NULL, 'access:log:export', NULL, 3, '0', '导出访问日志');

-- =============================================
-- 菜单结构说明
-- =============================================

/*
菜单层级结构：

访问控制 (顶级菜单)
├── 访问策略 (功能菜单)
│   ├── 策略查询 (按钮权限)
│   ├── 策略新增 (按钮权限)
│   ├── 策略编辑 (按钮权限)
│   ├── 策略删除 (按钮权限)
│   └── 策略测试 (按钮权限)
├── 访问规则 (功能菜单)
│   ├── 规则查询 (按钮权限)
│   ├── 规则新增 (按钮权限)
│   ├── 规则编辑 (按钮权限)
│   ├── 规则删除 (按钮权限)
│   └── 规则测试 (按钮权限)
├── MFA配置 (功能菜单)
│   ├── MFA查询 (按钮权限)
│   ├── MFA配置 (按钮权限)
│   └── MFA验证 (按钮权限)
├── 临时权限 (功能菜单)
│   ├── 权限查询 (按钮权限)
│   ├── 权限授予 (按钮权限)
│   ├── 权限撤销 (按钮权限)
│   └── 权限管理 (按钮权限)
├── IP访问控制 (功能菜单)
│   ├── IP查询 (按钮权限)
│   ├── IP管理 (按钮权限)
│   └── IP检查 (按钮权限)
└── 访问日志 (功能菜单)
    ├── 日志查询 (按钮权限)
    ├── 日志统计 (按钮权限)
    └── 日志导出 (按钮权限)

总计：
- 1个顶级菜单
- 6个功能菜单
- 26个按钮权限
- 共33个菜单项
*/

-- =============================================
-- 权限标识说明
-- =============================================

/*
权限标识格式：access:{模块}:{操作}

访问策略：
- access:policy:query    - 查询策略
- access:policy:add      - 新增策略
- access:policy:edit     - 编辑策略
- access:policy:delete   - 删除策略
- access:policy:test     - 测试策略

访问规则：
- access:rule:query      - 查询规则
- access:rule:add        - 新增规则
- access:rule:edit       - 编辑规则
- access:rule:delete     - 删除规则
- access:rule:test       - 测试规则

MFA配置：
- access:mfa:query       - 查询MFA配置
- access:mfa:config      - 配置MFA
- access:mfa:verify      - 验证MFA

临时权限：
- access:temp:query      - 查询临时权限
- access:temp:grant      - 授予临时权限
- access:temp:revoke     - 撤销临时权限
- access:temp:manage     - 管理临时权限

IP访问控制：
- access:ip:query        - 查询IP访问控制
- access:ip:manage       - 管理IP白名单和黑名单
- access:ip:check        - 检查IP状态

访问日志：
- access:log:query       - 查询访问日志
- access:log:statistics  - 访问统计分析
- access:log:export      - 导出访问日志
*/

-- =============================================
-- 使用说明
-- =============================================

/*
1. 执行此脚本前，请确保已经创建了sys_menu表

2. 执行脚本：
   mysql -u root -p bankshield < access_control_menu.sql

3. 为角色分配权限：
   -- 为管理员角色分配所有访问控制权限
   INSERT INTO sys_role_menu (role_id, menu_id)
   SELECT 1, id FROM sys_menu WHERE perms LIKE 'access:%';

4. 前端路由配置：
   - 路由文件：src/router/modules/access-control.ts
   - 路由路径：/access-control/*
   - 权限指令：v-permission="'access:policy:add'"

5. 后端接口权限：
   - 使用@PreAuthorize注解
   - 示例：@PreAuthorize("hasAuthority('access:policy:add')")
*/

-- =============================================
-- 验证查询
-- =============================================

-- 查看访问控制模块的所有菜单
SELECT 
    m1.menu_name AS '一级菜单',
    m2.menu_name AS '二级菜单',
    m3.menu_name AS '三级菜单',
    m3.menu_type AS '类型',
    m3.perms AS '权限标识',
    m3.path AS '路由路径',
    m3.component AS '组件路径'
FROM sys_menu m1
LEFT JOIN sys_menu m2 ON m1.id = m2.parent_id
LEFT JOIN sys_menu m3 ON m2.id = m3.parent_id
WHERE m1.menu_name = '访问控制'
ORDER BY m1.order_num, m2.order_num, m3.order_num;

-- 统计菜单数量
SELECT 
    menu_type,
    CASE menu_type
        WHEN 'M' THEN '目录'
        WHEN 'C' THEN '菜单'
        WHEN 'F' THEN '按钮'
    END AS '类型说明',
    COUNT(*) AS '数量'
FROM sys_menu
WHERE perms LIKE 'access:%' OR menu_name = '访问控制'
GROUP BY menu_type;
