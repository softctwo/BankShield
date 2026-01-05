-- BankShield 用户角色权限初始化脚本

USE bankshield;

-- 创建角色菜单关联表（如果不存在）
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 创建用户角色关联表（如果不存在）
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 清空现有权限数据
DELETE FROM sys_role_menu WHERE role_id > 0;
DELETE FROM sys_user_role WHERE user_id > 0;

-- 为管理员角色(roleId=1)分配所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE status = 1;

-- 为普通用户角色(roleId=2)分配基础菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 2, id FROM sys_menu WHERE id IN (
    1,   -- 首页
    4,   -- 审计追踪
    16,  -- 审计日志
    17,  -- 审计分析
    7,   -- 安全态势
    23,  -- 安全大屏
    24   -- 威胁管理
);

-- 为测试用户(userId=1)分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 为测试用户(userId=2)分配普通用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 查询验证
SELECT '=== 角色权限分配统计 ===' AS info;

SELECT 
    rm.role_id AS 角色ID,
    CASE rm.role_id
        WHEN 1 THEN '管理员'
        WHEN 2 THEN '普通用户'
        ELSE '其他角色'
    END AS 角色名称,
    COUNT(rm.menu_id) AS 菜单数量
FROM sys_role_menu rm
GROUP BY rm.role_id;

SELECT '=== 用户角色分配 ===' AS info;

SELECT 
    ur.user_id AS 用户ID,
    ur.role_id AS 角色ID,
    CASE ur.role_id
        WHEN 1 THEN '管理员'
        WHEN 2 THEN '普通用户'
        ELSE '其他角色'
    END AS 角色名称
FROM sys_user_role ur
ORDER BY ur.user_id;

SELECT '=== 管理员可访问的菜单 ===' AS info;

SELECT 
    m.id,
    m.menu_name AS 菜单名称,
    m.path AS 路由,
    CASE m.menu_type
        WHEN 0 THEN '目录'
        WHEN 1 THEN '菜单'
        WHEN 2 THEN '按钮'
    END AS 类型,
    m.permission AS 权限标识
FROM sys_menu m
INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
WHERE rm.role_id = 1 AND m.parent_id = 0
ORDER BY m.sort_order;

SELECT '用户角色权限初始化完成！' AS result;
