-- BankShield 菜单权限数据初始化脚本
-- 根据实际表结构编写

USE bankshield;

-- 清空现有菜单数据
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM sys_menu WHERE id > 0;
SET FOREIGN_KEY_CHECKS = 1;

-- 重置自增ID
ALTER TABLE sys_menu AUTO_INCREMENT = 1;

-- 插入一级菜单（10个）
-- menu_type: 0=目录, 1=菜单, 2=按钮
-- status: 0=禁用, 1=启用

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (0, '首页', 'dashboard', '/dashboard', 'dashboard/index', 'dashboard', 1, 1, 'dashboard:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '数据加密', 'encryption', '/encryption', NULL, 'lock', 2, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '访问控制', 'access-control', '/access-control', NULL, 'user', 3, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '审计追踪', 'audit', '/audit', NULL, 'document', 4, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '数据脱敏', 'desensitization', '/desensitization', NULL, 'view', 5, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '合规性检查', 'compliance', '/compliance', NULL, 'document-checked', 6, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '安全态势', 'security', '/security', NULL, 'warning', 7, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '数据血缘', 'lineage', '/lineage', NULL, 'share', 8, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '安全扫描', 'security-scan', '/security-scan', NULL, 'search', 9, 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, status) 
VALUES (0, '系统管理', 'system', '/system', NULL, 'setting', 10, 0, 1);

-- 数据加密子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (2, '加密管理', 'encryption-manage', '/encryption/manage', 'encryption/manage/index', '', 1, 1, 'encryption:manage:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (2, '密钥管理', 'encryption-key', '/encryption/key', 'encryption/key/index', '', 2, 1, 'encryption:key:view', 1);

-- 访问控制子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (3, '用户管理', 'user-manage', '/access-control/user', 'access-control/user/index', '', 1, 1, 'user:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (3, '角色管理', 'role-manage', '/access-control/role', 'access-control/role/index', '', 2, 1, 'role:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (3, '权限管理', 'permission-manage', '/access-control/permission', 'access-control/permission/index', '', 3, 1, 'permission:view', 1);

-- 审计追踪子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (4, '审计日志', 'audit-log', '/audit/log', 'audit/log/index', '', 1, 1, 'audit:log:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (4, '审计分析', 'audit-analysis', '/audit/analysis', 'audit/analysis/index', '', 2, 1, 'audit:analysis:view', 1);

-- 数据脱敏子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (5, '脱敏规则', 'desensitization-rule', '/desensitization/rule', 'desensitization/rule/index', '', 1, 1, 'desensitization:rule:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (5, '脱敏日志', 'desensitization-log', '/desensitization/log', 'desensitization/log/index', '', 2, 1, 'desensitization:log:view', 1);

-- 合规性检查子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (6, '合规规则', 'compliance-rule', '/compliance/rule', 'compliance/rule/index', '', 1, 1, 'compliance:rule:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (6, '检查任务', 'compliance-task', '/compliance/task', 'compliance/task/index', '', 2, 1, 'compliance:task:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (6, '合规报告', 'compliance-report', '/compliance/report', 'compliance/report/index', '', 3, 1, 'compliance:report:view', 1);

-- 安全态势子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (7, '安全大屏', 'security-dashboard', '/security/dashboard', 'security/dashboard/index', '', 1, 1, 'security:dashboard:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (7, '威胁管理', 'security-threat', '/security/threat', 'security/threat/index', '', 2, 1, 'security:threat:view', 1);

-- 数据血缘子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (8, '血缘追踪', 'lineage-track', '/lineage/track', 'lineage/track/index', '', 1, 1, 'lineage:track:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (8, '影响分析', 'lineage-analysis', '/lineage/analysis', 'lineage/analysis/index', '', 2, 1, 'lineage:analysis:view', 1);

-- 安全扫描子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (9, '扫描任务', 'scan-task', '/security-scan/task', 'security-scan/task/index', '', 1, 1, 'scan:task:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (9, '漏洞管理', 'scan-vulnerability', '/security-scan/vulnerability', 'security-scan/vulnerability/index', '', 2, 1, 'scan:vulnerability:view', 1);

-- 系统管理子菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (10, '菜单管理', 'system-menu', '/system/menu', 'system/menu/index', '', 1, 1, 'system:menu:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (10, '字典管理', 'system-dict', '/system/dict', 'system/dict/index', '', 2, 1, 'system:dict:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (10, '系统配置', 'system-config', '/system/config', 'system/config/index', '', 3, 1, 'system:config:view', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, path, component, icon, sort_order, menu_type, permission, status) 
VALUES (10, '日志管理', 'system-log', '/system/log', 'system/log/index', '', 4, 1, 'system:log:view', 1);

-- 用户管理按钮权限
INSERT INTO sys_menu (parent_id, menu_name, menu_code, sort_order, menu_type, permission, status) 
VALUES (13, '用户查询', 'user-query', 1, 2, 'user:query', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, sort_order, menu_type, permission, status) 
VALUES (13, '用户新增', 'user-add', 2, 2, 'user:add', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, sort_order, menu_type, permission, status) 
VALUES (13, '用户修改', 'user-edit', 3, 2, 'user:edit', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, sort_order, menu_type, permission, status) 
VALUES (13, '用户删除', 'user-delete', 4, 2, 'user:delete', 1);

INSERT INTO sys_menu (parent_id, menu_name, menu_code, sort_order, menu_type, permission, status) 
VALUES (13, '用户导出', 'user-export', 5, 2, 'user:export', 1);

-- 查询验证
SELECT '=== 菜单初始化完成 ===' AS info;

SELECT 
    CASE menu_type
        WHEN 0 THEN '目录'
        WHEN 1 THEN '菜单'
        WHEN 2 THEN '按钮'
        ELSE '未知'
    END AS 类型,
    COUNT(*) AS 数量
FROM sys_menu
GROUP BY menu_type;

SELECT '=== 一级菜单列表 ===' AS info;
SELECT id, menu_name, path, icon, sort_order
FROM sys_menu
WHERE parent_id = 0
ORDER BY sort_order;

SELECT CONCAT('共初始化 ', COUNT(*), ' 个菜单项') AS 统计
FROM sys_menu;
