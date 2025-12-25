-- BankShield Gateway Security Enhancement Module Database Schema
-- API网关安全增强模块数据库设计

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS bankshield DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bankshield;

-- API访问日志表
CREATE TABLE IF NOT EXISTS api_access_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  request_path VARCHAR(500) NOT NULL COMMENT '请求路径',
  request_method VARCHAR(10) COMMENT '请求方法(GET/POST/PUT/DELETE等)',
  request_params TEXT COMMENT '请求参数',
  request_headers TEXT COMMENT '请求头信息（过滤敏感信息）',
  response_status INT COMMENT '响应状态码',
  response_content TEXT COMMENT '响应内容',
  execute_time BIGINT COMMENT '执行时间（毫秒）',
  ip_address VARCHAR(50) COMMENT '客户端IP地址',
  user_id BIGINT COMMENT '用户ID（如果已认证）',
  access_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  access_result VARCHAR(20) COMMENT '访问结果：SUCCESS/FAIL',
  error_message TEXT COMMENT '错误信息',
  user_agent VARCHAR(500) COMMENT '用户代理',
  request_body_size BIGINT COMMENT '请求体大小（字节）',
  response_body_size BIGINT COMMENT '响应体大小（字节）',
  
  INDEX idx_request_path (request_path(255)),
  INDEX idx_user_id (user_id),
  INDEX idx_access_time (access_time),
  INDEX idx_ip_address (ip_address),
  INDEX idx_execute_time (execute_time),
  INDEX idx_access_result (access_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API访问日志表';

-- 限流规则表
CREATE TABLE IF NOT EXISTS rate_limit_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
  limit_dimension VARCHAR(20) NOT NULL COMMENT '限流维度：IP/USER/API/GLOBAL',
  limit_threshold INT NOT NULL COMMENT '限流阈值（每秒请求数）',
  limit_window INT NOT NULL COMMENT '限流窗口（秒）',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  description TEXT COMMENT '规则描述',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）',
  
  UNIQUE KEY uk_rule_name (rule_name),
  INDEX idx_enabled (enabled),
  INDEX idx_dimension (limit_dimension)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='限流规则表';

-- IP黑名单表
CREATE TABLE IF NOT EXISTS blacklist_ip (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  ip_address VARCHAR(50) NOT NULL COMMENT 'IP地址',
  block_reason TEXT COMMENT '封禁原因',
  block_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '封禁时间',
  unblock_time DATETIME COMMENT '解封时间',
  block_status VARCHAR(20) NOT NULL DEFAULT 'BLOCKED' COMMENT '封禁状态：BLOCKED/UNBLOCKED/EXPIRED',
  operator VARCHAR(50) COMMENT '操作人员',
  version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）',
  
  INDEX idx_ip_address (ip_address),
  INDEX idx_block_status (block_status),
  INDEX idx_unblock_time (unblock_time),
  INDEX idx_block_time (block_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IP黑名单表';

-- API路由配置表
CREATE TABLE IF NOT EXISTS api_route_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  route_id VARCHAR(100) NOT NULL COMMENT '路由ID',
  route_path VARCHAR(500) NOT NULL COMMENT '路由路径',
  target_service VARCHAR(100) NOT NULL COMMENT '目标服务',
  require_auth TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要认证：1-需要，0-不需要',
  required_roles VARCHAR(500) COMMENT '所需角色（多个角色用逗号分隔）',
  rate_limit_threshold INT DEFAULT 100 COMMENT '限流阈值（每秒请求数）',
  signature_verification_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用签名验证：1-启用，0-禁用',
  signature_secret VARCHAR(500) COMMENT '签名密钥',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  description TEXT COMMENT '描述',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）',
  
  UNIQUE KEY uk_route_id (route_id),
  UNIQUE KEY uk_route_path (route_path(255)),
  INDEX idx_enabled (enabled),
  INDEX idx_target_service (target_service)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API路由配置表';

-- 应用密钥表（用于签名验证）
CREATE TABLE IF NOT EXISTS app_secret (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  app_id VARCHAR(100) NOT NULL COMMENT '应用ID',
  app_name VARCHAR(200) NOT NULL COMMENT '应用名称',
  secret_key VARCHAR(500) NOT NULL COMMENT '签名密钥',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  description TEXT COMMENT '描述',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  expire_time DATETIME COMMENT '过期时间',
  version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）',
  
  UNIQUE KEY uk_app_id (app_id),
  INDEX idx_enabled (enabled),
  INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用密钥表';

-- 安全事件日志表
CREATE TABLE IF NOT EXISTS security_event_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  event_type VARCHAR(50) NOT NULL COMMENT '事件类型：RATE_LIMIT/BLACKLIST/SIGNATURE_ERROR/ANTI_BRUSH',
  event_level VARCHAR(20) NOT NULL COMMENT '事件级别：INFO/WARN/ERROR/CRITICAL',
  ip_address VARCHAR(50) COMMENT '相关IP地址',
  user_id BIGINT COMMENT '相关用户ID',
  request_path VARCHAR(500) COMMENT '请求路径',
  event_message TEXT COMMENT '事件描述',
  event_data TEXT COMMENT '事件数据（JSON格式）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  INDEX idx_event_type (event_type),
  INDEX idx_event_level (event_level),
  INDEX idx_ip_address (ip_address),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全事件日志表';

-- 插入默认限流规则
INSERT INTO rate_limit_rule (rule_name, limit_dimension, limit_threshold, limit_window, enabled, description) VALUES
('IP默认限流', 'IP', 100, 1, 1, '每个IP每秒最多100个请求'),
('用户默认限流', 'USER', 200, 1, 1, '每个用户每秒最多200个请求'),
('API默认限流', 'API', 1000, 1, 1, '每个API每秒最多1000个请求'),
('全局默认限流', 'GLOBAL', 10000, 1, 1, '全局每秒最多10000个请求');

-- 插入默认API路由配置
INSERT INTO api_route_config (route_id, route_path, target_service, require_auth, required_roles, rate_limit_threshold, signature_verification_enabled, description) VALUES
('bankshield-api', '/api/**', 'bankshield-api', 1, 'USER,ADMIN', 100, 0, 'API服务路由'),
('bankshield-auth', '/auth/**', 'bankshield-auth', 0, NULL, 200, 0, '认证服务路由'),
('bankshield-user', '/user/**', 'bankshield-user', 1, 'USER,ADMIN', 100, 0, '用户服务路由'),
('bankshield-admin', '/admin/**', 'bankshield-admin', 1, 'ADMIN', 50, 1, '后台管理服务路由（需要签名验证）');

-- 插入默认应用密钥（测试用，生产环境请更换）
INSERT INTO app_secret (app_id, app_name, secret_key, description, expire_time) VALUES
('test-app', '测试应用', 'test-secret-key-2024-bankshield-gateway-security', '测试应用密钥', '2025-12-31 23:59:59'),
('web-app', 'Web应用', 'web-secret-key-2024-bankshield-frontend', 'Web前端应用密钥', '2025-12-31 23:59:59'),
('mobile-app', '移动应用', 'mobile-secret-key-2024-bankshield-mobile', '移动应用密钥', '2025-12-31 23:59:59');

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_api_access_log_time_path ON api_access_log(access_time, request_path(100));
CREATE INDEX IF NOT EXISTS idx_api_access_log_time_ip ON api_access_log(access_time, ip_address);
CREATE INDEX IF NOT EXISTS idx_api_access_log_time_user ON api_access_log(access_time, user_id);
CREATE INDEX IF NOT EXISTS idx_rate_limit_rule_dimension_enabled ON rate_limit_rule(limit_dimension, enabled);
CREATE INDEX IF NOT EXISTS idx_blacklist_ip_status_time ON blacklist_ip(block_status, block_time);

-- 创建分区表（可选，用于大数据量优化）
-- 按月份分区API访问日志表
ALTER TABLE api_access_log 
PARTITION BY RANGE (YEAR(access_time) * 100 + MONTH(access_time)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    PARTITION p202403 VALUES LESS THAN (202404),
    PARTITION p202404 VALUES LESS THAN (202405),
    PARTITION p202405 VALUES LESS THAN (202406),
    PARTITION p202406 VALUES LESS THAN (202407),
    PARTITION p202407 VALUES LESS THAN (202408),
    PARTITION p202408 VALUES LESS THAN (202409),
    PARTITION p202409 VALUES LESS THAN (202410),
    PARTITION p202410 VALUES LESS THAN (202411),
    PARTITION p202411 VALUES LESS THAN (202412),
    PARTITION p202412 VALUES LESS THAN (202501),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 创建视图方便查询
CREATE OR REPLACE VIEW v_api_access_summary AS
SELECT 
    DATE(access_time) as access_date,
    request_path,
    COUNT(*) as total_count,
    COUNT(CASE WHEN access_result = 'SUCCESS' THEN 1 END) as success_count,
    COUNT(CASE WHEN access_result = 'FAIL' THEN 1 END) as fail_count,
    AVG(execute_time) as avg_execute_time,
    MAX(execute_time) as max_execute_time,
    MIN(execute_time) as min_execute_time,
    COUNT(DISTINCT ip_address) as unique_ips,
    COUNT(DISTINCT user_id) as unique_users
FROM api_access_log 
WHERE access_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(access_time), request_path;

CREATE OR REPLACE VIEW v_rate_limit_statistics AS
SELECT 
    r.rule_name,
    r.limit_dimension,
    r.limit_threshold,
    r.limit_window,
    r.enabled,
    COUNT(a.id) as hit_count,
    MAX(a.access_time) as last_hit_time
FROM rate_limit_rule r
LEFT JOIN api_access_log a ON (
    (r.limit_dimension = 'IP' AND r.rule_name LIKE CONCAT('%', a.ip_address, '%')) OR
    (r.limit_dimension = 'USER' AND r.rule_name LIKE CONCAT('%', a.user_id, '%')) OR
    (r.limit_dimension = 'API' AND r.rule_name LIKE CONCAT('%', a.request_path, '%'))
)
WHERE a.access_result = 'FAIL' AND a.access_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY r.id, r.rule_name, r.limit_dimension, r.limit_threshold, r.limit_window, r.enabled;

CREATE OR REPLACE VIEW v_blacklist_statistics AS
SELECT 
    block_status,
    COUNT(*) as count,
    COUNT(CASE WHEN unblock_time > NOW() THEN 1 END) as active_count,
    COUNT(CASE WHEN unblock_time <= NOW() THEN 1 END) as expired_count,
    AVG(TIMESTAMPDIFF(SECOND, block_time, unblock_time)) as avg_block_duration
FROM blacklist_ip 
GROUP BY block_status;

-- 创建存储过程（可选，用于复杂业务逻辑）
DELIMITER //

-- 自动清理过期日志的存储过程
CREATE PROCEDURE IF NOT EXISTS sp_cleanup_old_logs(IN days_to_keep INT)
BEGIN
    DELETE FROM api_access_log 
    WHERE access_time < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SELECT ROW_COUNT() as deleted_rows;
END //

-- 批量添加IP到黑名单的存储过程
CREATE PROCEDURE IF NOT EXISTS sp_batch_add_blacklist(
    IN ip_list TEXT,  -- 逗号分隔的IP列表
    IN block_reason TEXT,
    IN block_duration INT,
    IN operator_name VARCHAR(50)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE ip_addr VARCHAR(50);
    DECLARE cur CURSOR FOR 
        SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(ip_list, ',', numbers.n), ',', -1)) as ip
        FROM (
            SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
            UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
        ) numbers
        WHERE CHAR_LENGTH(ip_list) - CHAR_LENGTH(REPLACE(ip_list, ',', '')) >= numbers.n - 1;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO ip_addr;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 检查是否已存在
        IF NOT EXISTS (SELECT 1 FROM blacklist_ip WHERE ip_address = ip_addr AND block_status = 'BLOCKED') THEN
            INSERT INTO blacklist_ip (ip_address, block_reason, block_status, operator, unblock_time)
            VALUES (ip_addr, block_reason, 'BLOCKED', operator_name, 
                    DATE_ADD(NOW(), INTERVAL block_duration SECOND));
        END IF;
    END LOOP;
    
    CLOSE cur;
    
    SELECT ROW_COUNT() as processed_count;
END //

DELIMITER ;

-- 插入测试数据（可选）
-- 测试限流规则
INSERT INTO rate_limit_rule (rule_name, limit_dimension, limit_threshold, limit_window, enabled, description) VALUES
('登录接口限流', 'IP', 10, 60, 1, '登录接口每IP每分钟最多10次请求'),
('支付接口限流', 'USER', 5, 60, 1, '支付接口每用户每分钟最多5次请求'),
('查询接口限流', 'API', 1000, 1, 1, '查询接口每秒最多1000次请求');

-- 测试黑名单数据
INSERT INTO blacklist_ip (ip_address, block_reason, block_status, operator, unblock_time) VALUES
('192.168.1.100', '异常访问模式检测', 'BLOCKED', 'SYSTEM', DATE_ADD(NOW(), INTERVAL 1 HOUR)),
('10.0.0.50', '手动封禁', 'BLOCKED', 'ADMIN', DATE_ADD(NOW(), INTERVAL 24 HOUR));

-- 权限说明：
-- SECURITY_ADMIN角色：可以管理所有安全相关配置（限流规则、黑名单、API审计）
-- ADMIN角色：可以查看安全相关信息，但不能修改配置
-- USER角色：只能查看自己的访问日志

-- 创建权限视图（用于前端权限控制）
CREATE OR REPLACE VIEW v_security_permissions AS
SELECT 
    'RATE_LIMIT_MANAGE' as permission_code, '限流规则管理' as permission_name, 'SECURITY_ADMIN' as required_role
UNION ALL
SELECT 'BLACKLIST_MANAGE', '黑名单管理', 'SECURITY_ADMIN'
UNION ALL
SELECT 'API_AUDIT_VIEW', 'API审计查看', 'ADMIN'
UNION ALL
SELECT 'API_AUDIT_MANAGE', 'API审计管理', 'SECURITY_ADMIN'
UNION ALL
SELECT 'GATEWAY_CONFIG_MANAGE', '网关配置管理', 'SECURITY_ADMIN';