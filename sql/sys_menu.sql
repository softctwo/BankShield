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

-- 清空现有数据
TRUNCATE TABLE sys_menu;
TRUNCATE TABLE sys_role_menu;

-- 插入菜单数据
-- 一级菜单
INSERT INTO sys_menu VALUES(1, '首页', 0, 1, '/dashboard', 'dashboard/index', '', 1, 0, 'C', '0', '0', 'dashboard:view', 'dashboard', 'admin', NOW(), '', NULL, '数据安全管理平台首页');
INSERT INTO sys_menu VALUES(2, '数据加密', 0, 2, '/encryption', NULL, '', 1, 0, 'M', '0', '0', '', 'lock', 'admin', NOW(), '', NULL, '数据加密管理');
INSERT INTO sys_menu VALUES(3, '访问控制', 0, 3, '/access-control', NULL, '', 1, 0, 'M', '0', '0', '', 'user', 'admin', NOW(), '', NULL, '访问控制管理');
INSERT INTO sys_menu VALUES(4, '审计追踪', 0, 4, '/audit', NULL, '', 1, 0, 'M', '0', '0', '', 'document', 'admin', NOW(), '', NULL, '审计追踪');
INSERT INTO sys_menu VALUES(5, '数据脱敏', 0, 5, '/desensitization', NULL, '', 1, 0, 'M', '0', '0', '', 'view', 'admin', NOW(), '', NULL, '数据脱敏管理');
INSERT INTO sys_menu VALUES(6, '合规性检查', 0, 6, '/compliance', NULL, '', 1, 0, 'M', '0', '0', '', 'document-checked', 'admin', NOW(), '', NULL, '合规性检查');
INSERT INTO sys_menu VALUES(7, '安全态势', 0, 7, '/security', NULL, '', 1, 0, 'M', '0', '0', '', 'warning', 'admin', NOW(), '', NULL, '安全态势大屏');
INSERT INTO sys_menu VALUES(8, '数据血缘', 0, 8, '/lineage', NULL, '', 1, 0, 'M', '0', '0', '', 'share', 'admin', NOW(), '', NULL, '数据血缘追踪');
INSERT INTO sys_menu VALUES(9, '安全扫描', 0, 9, '/security-scan', NULL, '', 1, 0, 'M', '0', '0', '', 'search', 'admin', NOW(), '', NULL, '安全扫描');
INSERT INTO sys_menu VALUES(10, '系统管理', 0, 10, '/system', NULL, '', 1, 0, 'M', '0', '0', '', 'setting', 'admin', NOW(), '', NULL, '系统管理');

-- 数据加密子菜单
INSERT INTO sys_menu VALUES(201, '加密管理', 2, 1, '/encryption/manage', 'encryption/manage/index', '', 1, 0, 'C', '0', '0', 'encryption:manage:view', '', 'admin', NOW(), '', NULL, '加密管理');
INSERT INTO sys_menu VALUES(202, '密钥管理', 2, 2, '/encryption/key', 'encryption/key/index', '', 1, 0, 'C', '0', '0', 'encryption:key:view', '', 'admin', NOW(), '', NULL, '密钥管理');

-- 访问控制子菜单
INSERT INTO sys_menu VALUES(301, '用户管理', 3, 1, '/access-control/user', 'access-control/user/index', '', 1, 0, 'C', '0', '0', 'user:view', '', 'admin', NOW(), '', NULL, '用户管理');
INSERT INTO sys_menu VALUES(302, '角色管理', 3, 2, '/access-control/role', 'access-control/role/index', '', 1, 0, 'C', '0', '0', 'role:view', '', 'admin', NOW(), '', NULL, '角色管理');
INSERT INTO sys_menu VALUES(303, '权限管理', 3, 3, '/access-control/permission', 'access-control/permission/index', '', 1, 0, 'C', '0', '0', 'permission:view', '', 'admin', NOW(), '', NULL, '权限管理');

-- 审计追踪子菜单
INSERT INTO sys_menu VALUES(401, '审计日志', 4, 1, '/audit/log', 'audit/log/index', '', 1, 0, 'C', '0', '0', 'audit:log:view', '', 'admin', NOW(), '', NULL, '审计日志');
INSERT INTO sys_menu VALUES(402, '审计分析', 4, 2, '/audit/analysis', 'audit/analysis/index', '', 1, 0, 'C', '0', '0', 'audit:analysis:view', '', 'admin', NOW(), '', NULL, '审计分析');

-- 数据脱敏子菜单
INSERT INTO sys_menu VALUES(501, '脱敏规则', 5, 1, '/desensitization/rule', 'desensitization/rule/index', '', 1, 0, 'C', '0', '0', 'desensitization:rule:view', '', 'admin', NOW(), '', NULL, '脱敏规则');
INSERT INTO sys_menu VALUES(502, '脱敏日志', 5, 2, '/desensitization/log', 'desensitization/log/index', '', 1, 0, 'C', '0', '0', 'desensitization:log:view', '', 'admin', NOW(), '', NULL, '脱敏日志');

-- 合规性检查子菜单
INSERT INTO sys_menu VALUES(601, '合规规则', 6, 1, '/compliance/rule', 'compliance/rule/index', '', 1, 0, 'C', '0', '0', 'compliance:rule:view', '', 'admin', NOW(), '', NULL, '合规规则');
INSERT INTO sys_menu VALUES(602, '检查任务', 6, 2, '/compliance/task', 'compliance/task/index', '', 1, 0, 'C', '0', '0', 'compliance:task:view', '', 'admin', NOW(), '', NULL, '检查任务');
INSERT INTO sys_menu VALUES(603, '合规报告', 6, 3, '/compliance/report', 'compliance/report/index', '', 1, 0, 'C', '0', '0', 'compliance:report:view', '', 'admin', NOW(), '', NULL, '合规报告');

-- 安全态势子菜单
INSERT INTO sys_menu VALUES(701, '安全大屏', 7, 1, '/security/dashboard', 'security/dashboard/index', '', 1, 0, 'C', '0', '0', 'security:dashboard:view', '', 'admin', NOW(), '', NULL, '安全大屏');
INSERT INTO sys_menu VALUES(702, '威胁管理', 7, 2, '/security/threat', 'security/threat/index', '', 1, 0, 'C', '0', '0', 'security:threat:view', '', 'admin', NOW(), '', NULL, '威胁管理');

-- 数据血缘子菜单
INSERT INTO sys_menu VALUES(801, '血缘追踪', 8, 1, '/lineage/track', 'lineage/track/index', '', 1, 0, 'C', '0', '0', 'lineage:track:view', '', 'admin', NOW(), '', NULL, '血缘追踪');
INSERT INTO sys_menu VALUES(802, '影响分析', 8, 2, '/lineage/analysis', 'lineage/analysis/index', '', 1, 0, 'C', '0', '0', 'lineage:analysis:view', '', 'admin', NOW(), '', NULL, '影响分析');

-- 安全扫描子菜单
INSERT INTO sys_menu VALUES(901, '扫描任务', 9, 1, '/security-scan/task', 'security-scan/task/index', '', 1, 0, 'C', '0', '0', 'scan:task:view', '', 'admin', NOW(), '', NULL, '扫描任务');
INSERT INTO sys_menu VALUES(902, '漏洞管理', 9, 2, '/security-scan/vulnerability', 'security-scan/vulnerability/index', '', 1, 0, 'C', '0', '0', 'scan:vulnerability:view', '', 'admin', NOW(), '', NULL, '漏洞管理');

-- 系统管理子菜单
INSERT INTO sys_menu VALUES(1001, '菜单管理', 10, 1, '/system/menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:view', '', 'admin', NOW(), '', NULL, '菜单管理');
INSERT INTO sys_menu VALUES(1002, '字典管理', 10, 2, '/system/dict', 'system/dict/index', '', 1, 0, 'C', '0', '0', 'system:dict:view', '', 'admin', NOW(), '', NULL, '字典管理');
INSERT INTO sys_menu VALUES(1003, '系统配置', 10, 3, '/system/config', 'system/config/index', '', 1, 0, 'C', '0', '0', 'system:config:view', '', 'admin', NOW(), '', NULL, '系统配置');
INSERT INTO sys_menu VALUES(1004, '日志管理', 10, 4, '/system/log', 'system/log/index', '', 1, 0, 'C', '0', '0', 'system:log:view', '', 'admin', NOW(), '', NULL, '日志管理');

-- 按钮权限（以用户管理为例）
INSERT INTO sys_menu VALUES(30101, '用户查询', 301, 1, '', '', '', 1, 0, 'F', '0', '0', 'user:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(30102, '用户新增', 301, 2, '', '', '', 1, 0, 'F', '0', '0', 'user:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(30103, '用户修改', 301, 3, '', '', '', 1, 0, 'F', '0', '0', 'user:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(30104, '用户删除', 301, 4, '', '', '', 1, 0, 'F', '0', '0', 'user:delete', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(30105, '用户导出', 301, 5, '', '', '', 1, 0, 'F', '0', '0', 'user:export', '#', 'admin', NOW(), '', NULL, '');

-- 为管理员角色分配所有菜单权限
INSERT INTO sys_role_menu 
SELECT 1, id FROM sys_menu WHERE status = '0';

-- 为普通用户角色分配基础菜单权限
INSERT INTO sys_role_menu VALUES(2, 1);  -- 首页
INSERT INTO sys_role_menu VALUES(2, 4);  -- 审计追踪
INSERT INTO sys_role_menu VALUES(2, 401); -- 审计日志
INSERT INTO sys_role_menu VALUES(2, 7);  -- 安全态势
INSERT INTO sys_role_menu VALUES(2, 701); -- 安全大屏
