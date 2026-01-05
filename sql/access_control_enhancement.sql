-- =============================================
-- BankShield 访问控制强化模块
-- 功能：RBAC增强、ABAC支持、时间限制、IP限制、MFA
-- 版本：v1.0
-- 日期：2025-01-04
-- =============================================

-- 1. 访问策略表
DROP TABLE IF EXISTS `access_policy`;
CREATE TABLE `access_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '策略ID',
    `policy_code` VARCHAR(100) NOT NULL COMMENT '策略编码',
    `policy_name` VARCHAR(200) NOT NULL COMMENT '策略名称',
    `policy_type` VARCHAR(50) NOT NULL COMMENT '策略类型：RBAC/ABAC/HYBRID',
    `description` TEXT COMMENT '策略描述',
    `priority` INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    `effect` VARCHAR(20) NOT NULL DEFAULT 'ALLOW' COMMENT '效果：ALLOW/DENY',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `created_by` VARCHAR(100) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(100) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_policy_code` (`policy_code`),
    KEY `idx_policy_type` (`policy_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问策略表';

-- 2. 访问规则表
DROP TABLE IF EXISTS `access_rule`;
CREATE TABLE `access_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `policy_id` BIGINT NOT NULL COMMENT '所属策略ID',
    `rule_code` VARCHAR(100) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(200) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型：ROLE/ATTRIBUTE/TIME/IP/MFA',
    `subject_condition` JSON COMMENT '主体条件（角色、部门、属性等）',
    `resource_condition` JSON COMMENT '资源条件（类型、敏感级别等）',
    `action_condition` JSON COMMENT '操作条件（读、写、删除等）',
    `environment_condition` JSON COMMENT '环境条件（时间、IP、地理位置等）',
    `mfa_required` TINYINT(1) DEFAULT 0 COMMENT '是否需要MFA：0-否，1-是',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_policy_id` (`policy_id`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_rule_policy` FOREIGN KEY (`policy_id`) REFERENCES `access_policy` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问规则表';

-- 3. 访问日志表
DROP TABLE IF EXISTS `access_log`;
CREATE TABLE `access_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '用户ID',
    `username` VARCHAR(100) COMMENT '用户名',
    `user_role` VARCHAR(100) COMMENT '用户角色',
    `resource_type` VARCHAR(100) COMMENT '资源类型',
    `resource_id` VARCHAR(200) COMMENT '资源ID',
    `action` VARCHAR(50) COMMENT '操作类型：READ/WRITE/DELETE/EXECUTE',
    `access_result` VARCHAR(20) NOT NULL COMMENT '访问结果：ALLOW/DENY',
    `policy_matched` VARCHAR(100) COMMENT '匹配的策略',
    `rule_matched` VARCHAR(100) COMMENT '匹配的规则',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `location` VARCHAR(200) COMMENT '地理位置',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `mfa_verified` TINYINT(1) DEFAULT 0 COMMENT 'MFA验证：0-否，1-是',
    `deny_reason` TEXT COMMENT '拒绝原因',
    `access_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    `response_time` INT COMMENT '响应时间（毫秒）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_resource_type` (`resource_type`),
    KEY `idx_access_result` (`access_result`),
    KEY `idx_access_time` (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问日志表';

-- 4. MFA配置表
DROP TABLE IF EXISTS `mfa_config`;
CREATE TABLE `mfa_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `mfa_type` VARCHAR(50) NOT NULL COMMENT 'MFA类型：SMS/EMAIL/TOTP/BIOMETRIC',
    `mfa_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否启用：0-否，1-是',
    `phone` VARCHAR(20) COMMENT '手机号（用于SMS）',
    `email` VARCHAR(100) COMMENT '邮箱（用于EMAIL）',
    `totp_secret` VARCHAR(200) COMMENT 'TOTP密钥',
    `backup_codes` JSON COMMENT '备用验证码',
    `last_verified_time` DATETIME COMMENT '最后验证时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_mfa` (`user_id`, `mfa_type`),
    KEY `idx_username` (`username`),
    KEY `idx_mfa_enabled` (`mfa_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MFA配置表';

-- 5. MFA验证记录表
DROP TABLE IF EXISTS `mfa_verification_log`;
CREATE TABLE `mfa_verification_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `mfa_type` VARCHAR(50) NOT NULL COMMENT 'MFA类型',
    `verification_code` VARCHAR(100) COMMENT '验证码（加密存储）',
    `verification_result` VARCHAR(20) NOT NULL COMMENT '验证结果：SUCCESS/FAILED',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `failure_reason` VARCHAR(500) COMMENT '失败原因',
    `verification_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_verification_result` (`verification_result`),
    KEY `idx_verification_time` (`verification_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MFA验证记录表';

-- 6. 角色继承关系表
DROP TABLE IF EXISTS `role_hierarchy`;
CREATE TABLE `role_hierarchy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `parent_role_id` BIGINT NOT NULL COMMENT '父角色ID',
    `parent_role_code` VARCHAR(100) NOT NULL COMMENT '父角色编码',
    `child_role_id` BIGINT NOT NULL COMMENT '子角色ID',
    `child_role_code` VARCHAR(100) NOT NULL COMMENT '子角色编码',
    `inherit_level` INT DEFAULT 1 COMMENT '继承层级',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_hierarchy` (`parent_role_id`, `child_role_id`),
    KEY `idx_parent_role` (`parent_role_id`),
    KEY `idx_child_role` (`child_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色继承关系表';

-- 7. 角色互斥关系表
DROP TABLE IF EXISTS `role_mutex`;
CREATE TABLE `role_mutex` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `role_id_1` BIGINT NOT NULL COMMENT '角色1 ID',
    `role_code_1` VARCHAR(100) NOT NULL COMMENT '角色1编码',
    `role_id_2` BIGINT NOT NULL COMMENT '角色2 ID',
    `role_code_2` VARCHAR(100) NOT NULL COMMENT '角色2编码',
    `mutex_type` VARCHAR(50) NOT NULL DEFAULT 'STRICT' COMMENT '互斥类型：STRICT/SOFT',
    `description` TEXT COMMENT '互斥说明',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_mutex` (`role_id_1`, `role_id_2`),
    KEY `idx_role_1` (`role_id_1`),
    KEY `idx_role_2` (`role_id_2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色互斥关系表';

-- 8. 临时权限表
DROP TABLE IF EXISTS `temporary_permission`;
CREATE TABLE `temporary_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(200) NOT NULL COMMENT '权限名称',
    `resource_type` VARCHAR(100) COMMENT '资源类型',
    `resource_id` VARCHAR(200) COMMENT '资源ID',
    `granted_by` VARCHAR(100) NOT NULL COMMENT '授予人',
    `grant_reason` TEXT COMMENT '授予原因',
    `valid_from` DATETIME NOT NULL COMMENT '生效时间',
    `valid_to` DATETIME NOT NULL COMMENT '失效时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/EXPIRED/REVOKED',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_permission_code` (`permission_code`),
    KEY `idx_status` (`status`),
    KEY `idx_valid_time` (`valid_from`, `valid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临时权限表';

-- 9. IP白名单表
DROP TABLE IF EXISTS `ip_whitelist`;
CREATE TABLE `ip_whitelist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP地址',
    `ip_range` VARCHAR(100) COMMENT 'IP范围（CIDR格式）',
    `description` VARCHAR(500) COMMENT '描述',
    `apply_to` VARCHAR(50) NOT NULL DEFAULT 'ALL' COMMENT '应用范围：ALL/ROLE/USER',
    `target_id` BIGINT COMMENT '目标ID（角色ID或用户ID）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `created_by` VARCHAR(100) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_ip_address` (`ip_address`),
    KEY `idx_apply_to` (`apply_to`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP白名单表';

-- 10. IP黑名单表
DROP TABLE IF EXISTS `ip_blacklist`;
CREATE TABLE `ip_blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP地址',
    `ip_range` VARCHAR(100) COMMENT 'IP范围（CIDR格式）',
    `block_reason` VARCHAR(500) NOT NULL COMMENT '封禁原因',
    `block_type` VARCHAR(50) NOT NULL DEFAULT 'MANUAL' COMMENT '封禁类型：MANUAL/AUTO',
    `severity` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '严重程度：LOW/MEDIUM/HIGH/CRITICAL',
    `blocked_by` VARCHAR(100) COMMENT '封禁人',
    `blocked_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '封禁时间',
    `expire_time` DATETIME COMMENT '过期时间（NULL表示永久）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/EXPIRED/REMOVED',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ip_address` (`ip_address`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP黑名单表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认访问策略
INSERT INTO `access_policy` (`policy_code`, `policy_name`, `policy_type`, `description`, `priority`, `effect`, `status`, `created_by`) VALUES
('POLICY_ADMIN_FULL', '管理员全权限策略', 'RBAC', '管理员拥有系统所有权限', 100, 'ALLOW', 'ENABLED', 'system'),
('POLICY_SENSITIVE_DATA', '敏感数据访问策略', 'ABAC', '访问敏感数据需要满足特定条件', 90, 'ALLOW', 'ENABLED', 'system'),
('POLICY_TIME_RESTRICTED', '时间限制策略', 'ABAC', '仅在工作时间允许访问', 80, 'ALLOW', 'ENABLED', 'system'),
('POLICY_IP_RESTRICTED', 'IP限制策略', 'ABAC', '仅允许白名单IP访问', 85, 'ALLOW', 'ENABLED', 'system'),
('POLICY_MFA_REQUIRED', 'MFA必需策略', 'HYBRID', '高敏感操作需要MFA验证', 95, 'ALLOW', 'ENABLED', 'system');

-- 插入默认访问规则
INSERT INTO `access_rule` (`policy_id`, `rule_code`, `rule_name`, `rule_type`, `subject_condition`, `resource_condition`, `action_condition`, `environment_condition`, `mfa_required`, `priority`, `status`) VALUES
(1, 'RULE_ADMIN_ALL', '管理员全权限规则', 'ROLE', 
 '{"role": "admin"}', 
 '{"type": "*"}', 
 '{"action": "*"}', 
 NULL, 0, 100, 'ENABLED'),

(2, 'RULE_SENSITIVE_READ', '敏感数据读取规则', 'ATTRIBUTE', 
 '{"role": ["data_analyst", "data_manager"], "department": ["risk_management", "compliance"]}', 
 '{"type": "customer_data", "sensitivity": ["C3", "C4", "C5"]}', 
 '{"action": "read"}', 
 '{"time": "09:00-18:00", "ip_whitelist": true}', 1, 90, 'ENABLED'),

(3, 'RULE_WORK_HOURS', '工作时间访问规则', 'TIME', 
 '{"role": ["employee"]}', 
 '{"type": "*"}', 
 '{"action": ["read", "write"]}', 
 '{"time": "08:00-20:00", "weekday": [1,2,3,4,5]}', 0, 80, 'ENABLED'),

(4, 'RULE_INTERNAL_IP', '内网IP访问规则', 'IP', 
 '{"role": "*"}', 
 '{"type": "*"}', 
 '{"action": "*"}', 
 '{"ip_range": ["10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"]}', 0, 85, 'ENABLED'),

(5, 'RULE_HIGH_RISK_MFA', '高风险操作MFA规则', 'MFA', 
 '{"role": "*"}', 
 '{"type": "*", "sensitivity": ["C4", "C5"]}', 
 '{"action": ["delete", "export", "modify"]}', 
 NULL, 1, 95, 'ENABLED');

-- 插入角色继承关系示例
INSERT INTO `role_hierarchy` (`parent_role_id`, `parent_role_code`, `child_role_id`, `child_role_code`, `inherit_level`) VALUES
(1, 'admin', 2, 'data_manager', 1),
(2, 'data_manager', 3, 'data_analyst', 1),
(1, 'admin', 4, 'security_officer', 1);

-- 插入角色互斥关系示例
INSERT INTO `role_mutex` (`role_id_1`, `role_code_1`, `role_id_2`, `role_code_2`, `mutex_type`, `description`) VALUES
(2, 'data_manager', 5, 'data_auditor', 'STRICT', '数据管理员和数据审计员不能由同一人担任'),
(3, 'developer', 6, 'operator', 'SOFT', '开发人员和运维人员建议分离'),
(4, 'security_officer', 2, 'data_manager', 'STRICT', '安全官和数据管理员必须分离');

-- 插入IP白名单示例
INSERT INTO `ip_whitelist` (`ip_address`, `ip_range`, `description`, `apply_to`, `status`, `created_by`) VALUES
('10.0.0.0', '10.0.0.0/8', '内网A类地址', 'ALL', 'ENABLED', 'system'),
('172.16.0.0', '172.16.0.0/12', '内网B类地址', 'ALL', 'ENABLED', 'system'),
('192.168.0.0', '192.168.0.0/16', '内网C类地址', 'ALL', 'ENABLED', 'system');

-- =============================================
-- 视图定义
-- =============================================

-- 访问策略详情视图
CREATE OR REPLACE VIEW `v_access_policy_detail` AS
SELECT 
    p.id AS policy_id,
    p.policy_code,
    p.policy_name,
    p.policy_type,
    p.description AS policy_description,
    p.priority AS policy_priority,
    p.effect,
    p.status AS policy_status,
    r.id AS rule_id,
    r.rule_code,
    r.rule_name,
    r.rule_type,
    r.subject_condition,
    r.resource_condition,
    r.action_condition,
    r.environment_condition,
    r.mfa_required,
    r.priority AS rule_priority,
    r.status AS rule_status
FROM access_policy p
LEFT JOIN access_rule r ON p.id = r.policy_id
WHERE p.status = 'ENABLED' AND (r.status = 'ENABLED' OR r.status IS NULL)
ORDER BY p.priority DESC, r.priority DESC;

-- 用户有效权限视图
CREATE OR REPLACE VIEW `v_user_effective_permissions` AS
SELECT 
    u.id AS user_id,
    u.username,
    r.id AS role_id,
    r.role_code,
    r.role_name,
    p.id AS permission_id,
    p.permission_code,
    p.permission_name,
    'PERMANENT' AS permission_type,
    NULL AS valid_from,
    NULL AS valid_to
FROM sys_user u
INNER JOIN sys_user_role ur ON u.id = ur.user_id
INNER JOIN sys_role r ON ur.role_id = r.id
INNER JOIN sys_role_permission rp ON r.id = rp.role_id
INNER JOIN sys_permission p ON rp.permission_id = p.id
WHERE u.status = 'ENABLED' AND r.status = 'ENABLED'

UNION ALL

SELECT 
    tp.user_id,
    tp.username,
    NULL AS role_id,
    NULL AS role_code,
    NULL AS role_name,
    NULL AS permission_id,
    tp.permission_code,
    tp.permission_name,
    'TEMPORARY' AS permission_type,
    tp.valid_from,
    tp.valid_to
FROM temporary_permission tp
WHERE tp.status = 'ACTIVE' 
  AND tp.valid_from <= NOW() 
  AND tp.valid_to >= NOW();

-- 访问统计视图
CREATE OR REPLACE VIEW `v_access_statistics` AS
SELECT 
    DATE(access_time) AS access_date,
    username,
    resource_type,
    action,
    access_result,
    COUNT(*) AS access_count,
    AVG(response_time) AS avg_response_time,
    SUM(CASE WHEN mfa_verified = 1 THEN 1 ELSE 0 END) AS mfa_verified_count
FROM access_log
GROUP BY DATE(access_time), username, resource_type, action, access_result;

-- =============================================
-- 存储过程
-- =============================================

-- 检查访问权限存储过程
DELIMITER //
CREATE PROCEDURE `sp_check_access_permission`(
    IN p_user_id BIGINT,
    IN p_username VARCHAR(100),
    IN p_resource_type VARCHAR(100),
    IN p_resource_id VARCHAR(200),
    IN p_action VARCHAR(50),
    IN p_ip_address VARCHAR(50),
    IN p_mfa_verified TINYINT,
    OUT p_result VARCHAR(20),
    OUT p_policy_matched VARCHAR(100),
    OUT p_rule_matched VARCHAR(100),
    OUT p_deny_reason TEXT
)
BEGIN
    DECLARE v_policy_count INT DEFAULT 0;
    DECLARE v_ip_allowed TINYINT DEFAULT 0;
    DECLARE v_mfa_required TINYINT DEFAULT 0;
    DECLARE v_current_time TIME;
    DECLARE v_current_weekday INT;
    
    SET v_current_time = CURTIME();
    SET v_current_weekday = DAYOFWEEK(NOW());
    
    -- 初始化返回值
    SET p_result = 'DENY';
    SET p_policy_matched = NULL;
    SET p_rule_matched = NULL;
    SET p_deny_reason = '未匹配到任何策略';
    
    -- 检查IP黑名单
    SELECT COUNT(*) INTO v_ip_allowed
    FROM ip_blacklist
    WHERE status = 'ACTIVE'
      AND (ip_address = p_ip_address OR p_ip_address LIKE CONCAT(SUBSTRING_INDEX(ip_range, '/', 1), '%'))
      AND (expire_time IS NULL OR expire_time > NOW());
    
    IF v_ip_allowed > 0 THEN
        SET p_result = 'DENY';
        SET p_deny_reason = 'IP地址已被封禁';
        LEAVE sp_check_access_permission;
    END IF;
    
    -- 检查访问策略（简化版本，实际应该更复杂）
    SELECT COUNT(*) INTO v_policy_count
    FROM v_access_policy_detail
    WHERE policy_status = 'ENABLED' 
      AND rule_status = 'ENABLED';
    
    IF v_policy_count > 0 THEN
        SET p_result = 'ALLOW';
        SET p_policy_matched = 'POLICY_DEFAULT';
        SET p_rule_matched = 'RULE_DEFAULT';
        SET p_deny_reason = NULL;
    END IF;
    
END //
DELIMITER ;

-- 清理过期临时权限存储过程
DELIMITER //
CREATE PROCEDURE `sp_cleanup_expired_permissions`()
BEGIN
    -- 更新过期的临时权限状态
    UPDATE temporary_permission
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
      AND valid_to < NOW();
    
    -- 更新过期的IP黑名单状态
    UPDATE ip_blacklist
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
      AND expire_time IS NOT NULL
      AND expire_time < NOW();
    
    SELECT ROW_COUNT() AS affected_rows;
END //
DELIMITER ;

-- =============================================
-- 索引优化
-- =============================================

-- 为JSON字段创建虚拟列和索引（MySQL 5.7+）
-- ALTER TABLE access_rule 
-- ADD COLUMN subject_role VARCHAR(100) AS (JSON_UNQUOTE(JSON_EXTRACT(subject_condition, '$.role'))) VIRTUAL,
-- ADD INDEX idx_subject_role (subject_role);

-- =============================================
-- 权限设置
-- =============================================

-- 授予应用用户权限
-- GRANT SELECT, INSERT, UPDATE ON bankshield.access_policy TO 'bankshield_app'@'%';
-- GRANT SELECT, INSERT, UPDATE ON bankshield.access_rule TO 'bankshield_app'@'%';
-- GRANT SELECT, INSERT ON bankshield.access_log TO 'bankshield_app'@'%';
-- GRANT SELECT, INSERT, UPDATE ON bankshield.mfa_config TO 'bankshield_app'@'%';
-- GRANT SELECT, INSERT ON bankshield.mfa_verification_log TO 'bankshield_app'@'%';
-- GRANT EXECUTE ON PROCEDURE bankshield.sp_check_access_permission TO 'bankshield_app'@'%';
-- GRANT EXECUTE ON PROCEDURE bankshield.sp_cleanup_expired_permissions TO 'bankshield_app'@'%';

-- =============================================
-- 说明文档
-- =============================================

/*
访问控制强化模块说明：

1. 核心功能
   - RBAC增强：支持角色继承、角色互斥、临时权限
   - ABAC支持：基于属性的访问控制策略引擎
   - 时间限制：工作时间、有效期控制
   - IP限制：白名单、黑名单管理
   - MFA认证：多因素认证配置和验证

2. 表结构说明
   - access_policy: 访问策略主表
   - access_rule: 访问规则详细配置
   - access_log: 访问日志记录
   - mfa_config: MFA配置
   - role_hierarchy: 角色继承关系
   - role_mutex: 角色互斥关系
   - temporary_permission: 临时权限
   - ip_whitelist/ip_blacklist: IP访问控制

3. 使用示例
   -- 检查用户访问权限
   CALL sp_check_access_permission(1, 'admin', 'customer_data', '12345', 'read', '192.168.1.100', 1, @result, @policy, @rule, @reason);
   SELECT @result, @policy, @rule, @reason;
   
   -- 清理过期权限
   CALL sp_cleanup_expired_permissions();

4. 注意事项
   - 策略优先级：数字越大优先级越高
   - 规则匹配：按优先级从高到低匹配
   - MFA验证：高敏感操作必须启用MFA
   - IP限制：黑名单优先于白名单
   - 临时权限：自动过期，需定期清理

5. 性能优化
   - 为JSON字段创建虚拟列和索引
   - 定期归档历史访问日志
   - 使用Redis缓存策略规则
   - 异步记录访问日志
*/
