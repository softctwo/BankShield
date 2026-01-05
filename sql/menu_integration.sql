-- ============================================
-- BankShield 新模块菜单集成脚本
-- 用途：添加区块链存证和多方安全计算模块的菜单项
-- 执行时间：2024-12-31
-- ============================================

-- 1. 区块链存证模块菜单
-- 1.1 一级菜单：区块链存证
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('区块链存证', 0, 8, 'blockchain', NULL, 'M', '0', '0', NULL, 'Link', 'admin', NOW(), 'admin', NOW(), '区块链数据存证管理');

-- 1.2 二级菜单：存证概览
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '存证概览', id, 1, 'dashboard', 'blockchain/Dashboard', 'C', '0', '0', 'blockchain:dashboard:view', 'DataBoard', 'admin', NOW(), 'admin', NOW(), '区块链存证概览'
FROM sys_menu WHERE menu_name = '区块链存证' AND parent_id = 0;

-- 1.3 二级菜单：存证记录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '存证记录', id, 2, 'records', 'blockchain/RecordList', 'C', '0', '0', 'blockchain:record:list', 'Document', 'admin', NOW(), 'admin', NOW(), '区块链存证记录管理'
FROM sys_menu WHERE menu_name = '区块链存证' AND parent_id = 0;

-- 1.4 存证记录按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '查看详情', id, 1, '', NULL, 'F', '0', '0', 'blockchain:record:view', '#', 'admin', NOW(), 'admin', NOW(), '查看存证记录详情'
FROM sys_menu WHERE menu_name = '存证记录' AND parent_id != 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '验证完整性', id, 2, '', NULL, 'F', '0', '0', 'blockchain:record:verify', '#', 'admin', NOW(), 'admin', NOW(), '验证存证记录完整性'
FROM sys_menu WHERE menu_name = '存证记录' AND parent_id != 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '生成证书', id, 3, '', NULL, 'F', '0', '0', 'blockchain:record:certificate', '#', 'admin', NOW(), 'admin', NOW(), '生成存证证书'
FROM sys_menu WHERE menu_name = '存证记录' AND parent_id != 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '批量存证', id, 4, '', NULL, 'F', '0', '0', 'blockchain:record:batch', '#', 'admin', NOW(), 'admin', NOW(), '批量数据存证'
FROM sys_menu WHERE menu_name = '存证记录' AND parent_id != 0;

-- 2. 多方安全计算模块菜单
-- 2.1 一级菜单：多方安全计算
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('多方安全计算', 0, 9, 'mpc', NULL, 'M', '0', '0', NULL, 'Connection', 'admin', NOW(), 'admin', NOW(), '多方安全计算管理');

-- 2.2 二级菜单：MPC概览
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 'MPC概览', id, 1, 'dashboard', 'mpc/Dashboard', 'C', '0', '0', 'mpc:dashboard:view', 'DataBoard', 'admin', NOW(), 'admin', NOW(), '多方安全计算概览'
FROM sys_menu WHERE menu_name = '多方安全计算' AND parent_id = 0;

-- 2.3 二级菜单：任务列表
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '任务列表', id, 2, 'jobs', 'mpc/JobList', 'C', '0', '0', 'mpc:job:list', 'List', 'admin', NOW(), 'admin', NOW(), 'MPC任务列表管理'
FROM sys_menu WHERE menu_name = '多方安全计算' AND parent_id = 0;

-- 2.4 二级菜单：隐私求交
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '隐私求交', id, 3, 'psi', 'mpc/PSI', 'C', '0', '0', 'mpc:psi:execute', 'Connection', 'admin', NOW(), 'admin', NOW(), '执行隐私求交计算'
FROM sys_menu WHERE menu_name = '多方安全计算' AND parent_id = 0;

-- 2.5 二级菜单：安全求和
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '安全求和', id, 4, 'secure-sum', 'mpc/SecureSum', 'C', '0', '0', 'mpc:sum:execute', 'Plus', 'admin', NOW(), 'admin', NOW(), '执行安全求和计算'
FROM sys_menu WHERE menu_name = '多方安全计算' AND parent_id = 0;

-- 2.6 二级菜单：联合查询
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '联合查询', id, 5, 'joint-query', 'mpc/JointQuery', 'C', '0', '0', 'mpc:query:execute', 'Search', 'admin', NOW(), 'admin', NOW(), '执行联合查询'
FROM sys_menu WHERE menu_name = '多方安全计算' AND parent_id = 0;

-- 2.7 二级菜单：参与方管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '参与方管理', id, 6, 'parties', 'mpc/PartyManagement', 'C', '0', '0', 'mpc:party:list', 'UserFilled', 'admin', NOW(), 'admin', NOW(), 'MPC参与方管理'
FROM sys_menu WHERE menu_name = '多方安全计算' AND parent_id = 0;

-- 2.8 任务列表按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '查看详情', id, 1, '', NULL, 'F', '0', '0', 'mpc:job:view', '#', 'admin', NOW(), 'admin', NOW(), '查看任务详情'
FROM sys_menu WHERE menu_name = '任务列表' AND parent_id != 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '取消任务', id, 2, '', NULL, 'F', '0', '0', 'mpc:job:cancel', '#', 'admin', NOW(), 'admin', NOW(), '取消运行中的任务'
FROM sys_menu WHERE menu_name = '任务列表' AND parent_id != 0;

-- 2.9 参与方管理按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '注册参与方', id, 1, '', NULL, 'F', '0', '0', 'mpc:party:register', '#', 'admin', NOW(), 'admin', NOW(), '注册新参与方'
FROM sys_menu WHERE menu_name = '参与方管理' AND parent_id != 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '更新状态', id, 2, '', NULL, 'F', '0', '0', 'mpc:party:update', '#', 'admin', NOW(), 'admin', NOW(), '更新参与方状态'
FROM sys_menu WHERE menu_name = '参与方管理' AND parent_id != 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '删除参与方', id, 3, '', NULL, 'F', '0', '0', 'mpc:party:delete', '#', 'admin', NOW(), 'admin', NOW(), '删除参与方'
FROM sys_menu WHERE menu_name = '参与方管理' AND parent_id != 0;

-- 3. 为管理员角色分配新菜单权限
-- 假设管理员角色ID为1，需要根据实际情况调整
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_name IN ('区块链存证', '多方安全计算')
AND parent_id = 0
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = sys_menu.id
);

-- 为管理员分配所有子菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE parent_id IN (
    SELECT id FROM sys_menu WHERE menu_name IN ('区块链存证', '多方安全计算') AND parent_id = 0
)
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = sys_menu.id
);

-- 为管理员分配所有按钮权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_type = 'F' AND parent_id IN (
    SELECT id FROM sys_menu WHERE parent_id IN (
        SELECT id FROM sys_menu WHERE menu_name IN ('区块链存证', '多方安全计算') AND parent_id = 0
    )
)
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = sys_menu.id
);

-- 4. 查询验证
SELECT 
    m1.menu_name AS '一级菜单',
    m2.menu_name AS '二级菜单',
    m3.menu_name AS '按钮权限',
    m2.path AS '路径',
    m2.component AS '组件',
    m2.perms AS '权限标识'
FROM sys_menu m1
LEFT JOIN sys_menu m2 ON m1.id = m2.parent_id
LEFT JOIN sys_menu m3 ON m2.id = m3.parent_id
WHERE m1.menu_name IN ('区块链存证', '多方安全计算')
AND m1.parent_id = 0
ORDER BY m1.order_num, m2.order_num, m3.order_num;

-- ============================================
-- 说明：
-- 1. menu_type: M=目录, C=菜单, F=按钮
-- 2. visible: 0=显示, 1=隐藏
-- 3. status: 0=正常, 1=停用
-- 4. 执行前请确认sys_menu表结构与脚本匹配
-- 5. 如果角色ID不是1，请修改第3部分的role_id值
-- ============================================
