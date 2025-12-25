-- 菜单管理模块初始化数据
-- 系统管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(NULL, '系统管理', 'system', '/system', NULL, 'Setting', 10, 0, 'system', 1, NOW(), NOW());

-- 获取刚插入的系统管理菜单ID
SET @system_menu_id = LAST_INSERT_ID();

-- 用户管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@system_menu_id, '用户管理', 'user', 'user', '/views/system/user/index.vue', 'User', 1, 1, 'user:list', 1, NOW(), NOW());

-- 菜单管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@system_menu_id, '菜单管理', 'menu', 'menu', '/views/system/menu/index.vue', 'Menu', 2, 1, 'menu:list', 1, NOW(), NOW());

-- 三权分立管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@system_menu_id, '三权分立', 'role-mutex', 'role-mutex', '/views/system/role-mutex/index.vue', 'Lock', 3, 1, 'role:mutex', 1, NOW(), NOW());

-- 数据分类分级菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(NULL, '数据分类分级', 'classification', '/classification', NULL, 'DataAnalysis', 20, 0, 'classification', 1, NOW(), NOW());

-- 获取刚插入的数据分类分级菜单ID
SET @classification_menu_id = LAST_INSERT_ID();

-- 资产管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@classification_menu_id, '资产管理', 'asset-management', 'asset-management', '/views/classification/asset-management/index.vue', 'Coin', 1, 1, 'asset:list', 1, NOW(), NOW());

-- 资产地图菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@classification_menu_id, '资产地图', 'asset-map', 'asset-map', '/views/classification/asset-map/index.vue', 'MapLocation', 2, 1, 'asset:map', 1, NOW(), NOW());

-- 资产审核菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@classification_menu_id, '资产审核', 'review', 'review', '/views/classification/review/index.vue', 'Check', 3, 1, 'asset:review', 1, NOW(), NOW());

-- 数据源管理菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@classification_menu_id, '数据源管理', 'data-source', 'data-source', '/views/classification/data-source/index.vue', 'Connection', 4, 1, 'datasource:list', 1, NOW(), NOW());

-- 血缘分析菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@classification_menu_id, '血缘分析', 'lineage', 'lineage', '/views/classification/lineage/index.vue', 'Share', 5, 1, 'lineage:list', 1, NOW(), NOW());

-- 敏感数据模板菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(@classification_menu_id, '敏感数据模板', 'template', 'template', '/views/classification/template/index.vue', 'Document', 6, 1, 'template:list', 1, NOW(), NOW());

-- 数据大屏菜单（顶级菜单）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_code`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `permission`, `status`, `create_time`, `update_time`) VALUES
(NULL, '数据大屏', 'dashboard', '/dashboard', '/views/dashboard/index.vue', 'DataBoard', 1, 1, 'dashboard', 1, NOW(), NOW());