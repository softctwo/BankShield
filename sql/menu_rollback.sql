-- ============================================
-- BankShield 新模块菜单回滚脚本
-- 用途：删除区块链存证和多方安全计算模块的菜单项
-- 执行时间：2024-12-31
-- ============================================

-- 1. 删除角色菜单关联（按钮权限）
DELETE FROM sys_role_menu 
WHERE menu_id IN (
    SELECT id FROM sys_menu 
    WHERE menu_type = 'F' 
    AND parent_id IN (
        SELECT id FROM sys_menu 
        WHERE parent_id IN (
            SELECT id FROM sys_menu 
            WHERE menu_name IN ('区块链存证', '多方安全计算') 
            AND parent_id = 0
        )
    )
);

-- 2. 删除角色菜单关联（子菜单）
DELETE FROM sys_role_menu 
WHERE menu_id IN (
    SELECT id FROM sys_menu 
    WHERE parent_id IN (
        SELECT id FROM sys_menu 
        WHERE menu_name IN ('区块链存证', '多方安全计算') 
        AND parent_id = 0
    )
);

-- 3. 删除角色菜单关联（一级菜单）
DELETE FROM sys_role_menu 
WHERE menu_id IN (
    SELECT id FROM sys_menu 
    WHERE menu_name IN ('区块链存证', '多方安全计算') 
    AND parent_id = 0
);

-- 4. 删除按钮权限
DELETE FROM sys_menu 
WHERE menu_type = 'F' 
AND parent_id IN (
    SELECT id FROM (
        SELECT id FROM sys_menu 
        WHERE parent_id IN (
            SELECT id FROM sys_menu 
            WHERE menu_name IN ('区块链存证', '多方安全计算') 
            AND parent_id = 0
        )
    ) AS temp
);

-- 5. 删除二级菜单
DELETE FROM sys_menu 
WHERE parent_id IN (
    SELECT id FROM (
        SELECT id FROM sys_menu 
        WHERE menu_name IN ('区块链存证', '多方安全计算') 
        AND parent_id = 0
    ) AS temp
);

-- 6. 删除一级菜单
DELETE FROM sys_menu 
WHERE menu_name IN ('区块链存证', '多方安全计算') 
AND parent_id = 0;

-- 7. 验证删除结果
SELECT COUNT(*) AS '剩余相关菜单数' 
FROM sys_menu 
WHERE menu_name LIKE '%区块链%' 
   OR menu_name LIKE '%MPC%' 
   OR menu_name LIKE '%多方安全计算%';

-- ============================================
-- 说明：
-- 1. 此脚本用于回滚菜单集成操作
-- 2. 执行前请备份数据库
-- 3. 执行后应该看到"剩余相关菜单数"为0
-- ============================================
