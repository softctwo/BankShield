-- BankShield 菜单权限初始化脚本
-- 执行前请确保已创建 bankshield 数据库

USE bankshield;

-- 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    query VARCHAR(255) DEFAULT NULL COMMENT '路由参数',
    is_frame INT DEFAULT 1 COMMENT '是否为外链（0是 1否）',
    is_cache INT DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
    menu_type CHAR(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    visible CHAR(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
    status CHAR(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色和菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 清空现有数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_menu;
TRUNCATE TABLE sys_role_menu;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入菜单数据
-- 一级菜单（10个）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, remark) 
VALUES ('首页', 0, 1, '/dashboard', 'dashboard/index', 'C', '0', '0', 'dashboard:view', 'dashboard', 'admin', '数据安全管理平台首页');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('数据加密', 0, 2, '/encryption', NULL, 'M', '0', '0', 'lock', 'admin', '数据加密管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('访问控制', 0, 3, '/access-control', NULL, 'M', '0', '0', 'user', 'admin', '访问控制管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('审计追踪', 0, 4, '/audit', NULL, 'M', '0', '0', 'document', 'admin', '审计追踪');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('数据脱敏', 0, 5, '/desensitization', NULL, 'M', '0', '0', 'view', 'admin', '数据脱敏管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('合规性检查', 0, 6, '/compliance', NULL, 'M', '0', '0', 'document-checked', 'admin', '合规性检查');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('安全态势', 0, 7, '/security', NULL, 'M', '0', '0', 'warning', 'admin', '安全态势大屏');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('数据血缘', 0, 8, '/lineage', NULL, 'M', '0', '0', 'share', 'admin', '数据血缘追踪');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('安全扫描', 0, 9, '/security-scan', NULL, 'M', '0', '0', 'search', 'admin', '安全扫描');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, icon, create_by, remark) 
VALUES ('系统管理', 0, 10, '/system', NULL, 'M', '0', '0', 'setting', 'admin', '系统管理');

-- 数据加密子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('加密管理', 2, 1, '/encryption/manage', 'encryption/manage/index', 'C', '0', '0', 'encryption:manage:view', 'admin', '加密管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('密钥管理', 2, 2, '/encryption/key', 'encryption/key/index', 'C', '0', '0', 'encryption:key:view', 'admin', '密钥管理');

-- 访问控制子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('用户管理', 3, 1, '/access-control/user', 'access-control/user/index', 'C', '0', '0', 'user:view', 'admin', '用户管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('角色管理', 3, 2, '/access-control/role', 'access-control/role/index', 'C', '0', '0', 'role:view', 'admin', '角色管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('权限管理', 3, 3, '/access-control/permission', 'access-control/permission/index', 'C', '0', '0', 'permission:view', 'admin', '权限管理');

-- 审计追踪子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('审计日志', 4, 1, '/audit/log', 'audit/log/index', 'C', '0', '0', 'audit:log:view', 'admin', '审计日志');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('审计分析', 4, 2, '/audit/analysis', 'audit/analysis/index', 'C', '0', '0', 'audit:analysis:view', 'admin', '审计分析');

-- 数据脱敏子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('脱敏规则', 5, 1, '/desensitization/rule', 'desensitization/rule/index', 'C', '0', '0', 'desensitization:rule:view', 'admin', '脱敏规则');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('脱敏日志', 5, 2, '/desensitization/log', 'desensitization/log/index', 'C', '0', '0', 'desensitization:log:view', 'admin', '脱敏日志');

-- 合规性检查子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('合规规则', 6, 1, '/compliance/rule', 'compliance/rule/index', 'C', '0', '0', 'compliance:rule:view', 'admin', '合规规则');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('检查任务', 6, 2, '/compliance/task', 'compliance/task/index', 'C', '0', '0', 'compliance:task:view', 'admin', '检查任务');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('合规报告', 6, 3, '/compliance/report', 'compliance/report/index', 'C', '0', '0', 'compliance:report:view', 'admin', '合规报告');

-- 安全态势子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('安全大屏', 7, 1, '/security/dashboard', 'security/dashboard/index', 'C', '0', '0', 'security:dashboard:view', 'admin', '安全大屏');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('威胁管理', 7, 2, '/security/threat', 'security/threat/index', 'C', '0', '0', 'security:threat:view', 'admin', '威胁管理');

-- 数据血缘子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('血缘追踪', 8, 1, '/lineage/track', 'lineage/track/index', 'C', '0', '0', 'lineage:track:view', 'admin', '血缘追踪');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('影响分析', 8, 2, '/lineage/analysis', 'lineage/analysis/index', 'C', '0', '0', 'lineage:analysis:view', 'admin', '影响分析');

-- 安全扫描子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('扫描任务', 9, 1, '/security-scan/task', 'security-scan/task/index', 'C', '0', '0', 'scan:task:view', 'admin', '扫描任务');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('漏洞管理', 9, 2, '/security-scan/vulnerability', 'security-scan/vulnerability/index', 'C', '0', '0', 'scan:vulnerability:view', 'admin', '漏洞管理');

-- 系统管理子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('菜单管理', 10, 1, '/system/menu', 'system/menu/index', 'C', '0', '0', 'system:menu:view', 'admin', '菜单管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('字典管理', 10, 2, '/system/dict', 'system/dict/index', 'C', '0', '0', 'system:dict:view', 'admin', '字典管理');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('系统配置', 10, 3, '/system/config', 'system/config/index', 'C', '0', '0', 'system:config:view', 'admin', '系统配置');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, remark) 
VALUES ('日志管理', 10, 4, '/system/log', 'system/log/index', 'C', '0', '0', 'system:log:view', 'admin', '日志管理');

-- 按钮权限（以用户管理为例）
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, create_by) 
VALUES ('用户查询', 13, 1, 'F', '0', '0', 'user:query', 'admin');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, create_by) 
VALUES ('用户新增', 13, 2, 'F', '0', '0', 'user:add', 'admin');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, create_by) 
VALUES ('用户修改', 13, 3, 'F', '0', '0', 'user:edit', 'admin');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, create_by) 
VALUES ('用户删除', 13, 4, 'F', '0', '0', 'user:delete', 'admin');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, create_by) 
VALUES ('用户导出', 13, 5, 'F', '0', '0', 'user:export', 'admin');

-- 为管理员角色(roleId=1)分配所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE status = '0';

-- 为普通用户角色(roleId=2)分配基础菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),   -- 首页
(2, 4),   -- 审计追踪
(2, 16),  -- 审计日志
(2, 7),   -- 安全态势
(2, 23);  -- 安全大屏

-- 为测试用户(userId=1)分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 查询验证
SELECT '=== 菜单统计 ===' AS info;
SELECT 
    menu_type,
    COUNT(*) AS count,
    CASE menu_type
        WHEN 'M' THEN '目录'
        WHEN 'C' THEN '菜单'
        WHEN 'F' THEN '按钮'
        ELSE '未知'
    END AS type_name
FROM sys_menu
GROUP BY menu_type;

SELECT '=== 一级菜单列表 ===' AS info;
SELECT id, menu_name, path, icon, order_num
FROM sys_menu
WHERE parent_id = 0
ORDER BY order_num;

SELECT '=== 权限分配统计 ===' AS info;
SELECT 
    r.role_id,
    COUNT(rm.menu_id) AS menu_count
FROM sys_role_menu rm
LEFT JOIN (SELECT DISTINCT role_id FROM sys_role_menu) r ON rm.role_id = r.role_id
GROUP BY r.role_id;

SELECT '菜单权限初始化完成！' AS result;
