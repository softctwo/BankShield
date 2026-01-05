-- =============================================
-- BankShield 安全扫描与漏洞管理模块 - 菜单配置
-- 版本：v1.0
-- 日期：2025-01-04
-- =============================================

-- 删除已存在的菜单（如果有）
DELETE FROM sys_menu WHERE menu_id >= 3000 AND menu_id < 3100;

-- =============================================
-- 1. 顶级菜单：安全扫描
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3000, '安全扫描', 0, 3, 'security-scan', NULL, 1, 0, 'M', '0', '0', NULL, 'shield', 'admin', NOW(), '', NULL, '安全扫描与漏洞管理');

-- =============================================
-- 2. 功能菜单
-- =============================================

-- 2.1 扫描仪表板
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3001, '扫描仪表板', 3000, 1, 'dashboard', 'security-scan/dashboard/index', 1, 0, 'C', '0', '0', 'scan:dashboard:view', 'dashboard', 'admin', NOW(), '', NULL, '安全扫描仪表板');

-- 2.2 扫描任务
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3002, '扫描任务', 3000, 2, 'task', 'security-scan/task/index', 1, 0, 'C', '0', '0', 'scan:task:query', 'list', 'admin', NOW(), '', NULL, '安全扫描任务管理');

-- 2.3 漏洞管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3003, '漏洞管理', 3000, 3, 'vulnerability', 'security-scan/vulnerability/index', 1, 0, 'C', '0', '0', 'scan:vuln:query', 'bug', 'admin', NOW(), '', NULL, '漏洞记录管理');

-- 2.4 扫描规则
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3004, '扫描规则', 3000, 4, 'rule', 'security-scan/rule/index', 1, 0, 'C', '0', '0', 'scan:rule:query', 'guide', 'admin', NOW(), '', NULL, '扫描规则配置');

-- 2.5 修复计划
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3005, '修复计划', 3000, 5, 'remediation', 'security-scan/remediation/index', 1, 0, 'C', '0', '0', 'scan:plan:query', 'edit', 'admin', NOW(), '', NULL, '漏洞修复计划');

-- 2.6 依赖组件
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3006, '依赖组件', 3000, 6, 'component', 'security-scan/component/index', 1, 0, 'C', '0', '0', 'scan:component:query', 'component', 'admin', NOW(), '', NULL, '依赖组件管理');

-- =============================================
-- 3. 扫描任务按钮权限
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3011, '任务查询', 3002, 1, '', NULL, 1, 0, 'F', '0', '0', 'scan:task:query', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3012, '任务新增', 3002, 2, '', NULL, 1, 0, 'F', '0', '0', 'scan:task:add', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3013, '任务启动', 3002, 3, '', NULL, 1, 0, 'F', '0', '0', 'scan:task:start', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3014, '任务停止', 3002, 4, '', NULL, 1, 0, 'F', '0', '0', 'scan:task:stop', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3015, '任务删除', 3002, 5, '', NULL, 1, 0, 'F', '0', '0', 'scan:task:remove', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3016, '任务导出', 3002, 6, '', NULL, 1, 0, 'F', '0', '0', 'scan:task:export', '#', 'admin', NOW(), '', NULL, '');

-- =============================================
-- 4. 漏洞管理按钮权限
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3021, '漏洞查询', 3003, 1, '', NULL, 1, 0, 'F', '0', '0', 'scan:vuln:query', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3022, '漏洞详情', 3003, 2, '', NULL, 1, 0, 'F', '0', '0', 'scan:vuln:detail', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3023, '漏洞分配', 3003, 3, '', NULL, 1, 0, 'F', '0', '0', 'scan:vuln:assign', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3024, '漏洞解决', 3003, 4, '', NULL, 1, 0, 'F', '0', '0', 'scan:vuln:resolve', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3025, '标记误报', 3003, 5, '', NULL, 1, 0, 'F', '0', '0', 'scan:vuln:false', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3026, '漏洞导出', 3003, 6, '', NULL, 1, 0, 'F', '0', '0', 'scan:vuln:export', '#', 'admin', NOW(), '', NULL, '');

-- =============================================
-- 5. 扫描规则按钮权限
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3031, '规则查询', 3004, 1, '', NULL, 1, 0, 'F', '0', '0', 'scan:rule:query', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3032, '规则新增', 3004, 2, '', NULL, 1, 0, 'F', '0', '0', 'scan:rule:add', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3033, '规则修改', 3004, 3, '', NULL, 1, 0, 'F', '0', '0', 'scan:rule:edit', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3034, '规则删除', 3004, 4, '', NULL, 1, 0, 'F', '0', '0', 'scan:rule:remove', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3035, '规则启用', 3004, 5, '', NULL, 1, 0, 'F', '0', '0', 'scan:rule:toggle', '#', 'admin', NOW(), '', NULL, '');

-- =============================================
-- 6. 修复计划按钮权限
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3041, '计划查询', 3005, 1, '', NULL, 1, 0, 'F', '0', '0', 'scan:plan:query', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3042, '计划新增', 3005, 2, '', NULL, 1, 0, 'F', '0', '0', 'scan:plan:add', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3043, '计划修改', 3005, 3, '', NULL, 1, 0, 'F', '0', '0', 'scan:plan:edit', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3044, '计划删除', 3005, 4, '', NULL, 1, 0, 'F', '0', '0', 'scan:plan:remove', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3045, '计划审批', 3005, 5, '', NULL, 1, 0, 'F', '0', '0', 'scan:plan:approve', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3046, '计划完成', 3005, 6, '', NULL, 1, 0, 'F', '0', '0', 'scan:plan:complete', '#', 'admin', NOW(), '', NULL, '');

-- =============================================
-- 7. 依赖组件按钮权限
-- =============================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3051, '组件查询', 3006, 1, '', NULL, 1, 0, 'F', '0', '0', 'scan:component:query', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3052, '组件扫描', 3006, 2, '', NULL, 1, 0, 'F', '0', '0', 'scan:component:scan', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3053, '组件导出', 3006, 3, '', NULL, 1, 0, 'F', '0', '0', 'scan:component:export', '#', 'admin', NOW(), '', NULL, '');

-- =============================================
-- 菜单统计
-- =============================================
-- 顶级菜单：1个
-- 功能菜单：6个
-- 按钮权限：28个
-- 总计：35个菜单项

-- =============================================
-- 说明文档
-- =============================================
/*
安全扫描与漏洞管理菜单结构：

1. 安全扫描（顶级菜单）
   ├── 扫描仪表板
   ├── 扫描任务
   │   ├── 任务查询
   │   ├── 任务新增
   │   ├── 任务启动
   │   ├── 任务停止
   │   ├── 任务删除
   │   └── 任务导出
   ├── 漏洞管理
   │   ├── 漏洞查询
   │   ├── 漏洞详情
   │   ├── 漏洞分配
   │   ├── 漏洞解决
   │   ├── 标记误报
   │   └── 漏洞导出
   ├── 扫描规则
   │   ├── 规则查询
   │   ├── 规则新增
   │   ├── 规则修改
   │   ├── 规则删除
   │   └── 规则启用
   ├── 修复计划
   │   ├── 计划查询
   │   ├── 计划新增
   │   ├── 计划修改
   │   ├── 计划删除
   │   ├── 计划审批
   │   └── 计划完成
   └── 依赖组件
       ├── 组件查询
       ├── 组件扫描
       └── 组件导出

菜单ID范围：3000-3099
*/
