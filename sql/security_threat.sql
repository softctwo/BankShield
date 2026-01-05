-- =============================================
-- BankShield 安全态势大屏 - 数据库设计
-- 版本：v1.0
-- 日期：2025-01-04
-- =============================================

-- 删除已存在的表
DROP TABLE IF EXISTS security_threat;
DROP TABLE IF EXISTS security_event_log;

-- =============================================
-- 1. 安全威胁表
-- =============================================
CREATE TABLE security_threat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '威胁ID',
    threat_type VARCHAR(50) NOT NULL COMMENT '威胁类型：SQL注入/XSS/暴力破解/DDoS等',
    severity VARCHAR(20) NOT NULL COMMENT '严重程度：CRITICAL/HIGH/MEDIUM/LOW',
    source_ip VARCHAR(50) NOT NULL COMMENT '来源IP',
    source_country VARCHAR(50) COMMENT '来源国家',
    source_city VARCHAR(100) COMMENT '来源城市',
    target_ip VARCHAR(50) COMMENT '目标IP',
    target_system VARCHAR(100) COMMENT '目标系统',
    target_port INT COMMENT '目标端口',
    status VARCHAR(20) NOT NULL DEFAULT 'DETECTED' COMMENT '状态：DETECTED/BLOCKED/RESOLVED',
    description TEXT COMMENT '威胁描述',
    attack_vector TEXT COMMENT '攻击向量',
    impact_level VARCHAR(20) COMMENT '影响级别',
    detect_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    handle_time DATETIME COMMENT '处理时间',
    handler VARCHAR(50) COMMENT '处理人',
    handle_notes TEXT COMMENT '处理备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_threat_type (threat_type),
    INDEX idx_severity (severity),
    INDEX idx_source_ip (source_ip),
    INDEX idx_status (status),
    INDEX idx_detect_time (detect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全威胁表';

-- =============================================
-- 2. 安全事件日志表
-- =============================================
CREATE TABLE security_event_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_level VARCHAR(20) NOT NULL COMMENT '事件级别：INFO/WARNING/ERROR/CRITICAL',
    event_source VARCHAR(100) COMMENT '事件来源',
    event_message TEXT COMMENT '事件消息',
    event_details JSON COMMENT '事件详情（JSON格式）',
    user_id VARCHAR(50) COMMENT '用户ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    request_url TEXT COMMENT '请求URL',
    response_code INT COMMENT '响应码',
    event_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    INDEX idx_event_type (event_type),
    INDEX idx_event_level (event_level),
    INDEX idx_event_time (event_time),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全事件日志表';

-- =============================================
-- 3. 初始化模拟数据
-- =============================================

-- 插入模拟威胁数据（最近24小时）
INSERT INTO security_threat (threat_type, severity, source_ip, source_country, source_city, target_ip, target_system, status, description, detect_time) VALUES
-- 严重威胁
('SQL注入', 'CRITICAL', '203.0.113.45', '美国', '纽约', '192.168.1.100', '用户数据库', 'BLOCKED', '检测到SQL注入攻击尝试，已自动阻断', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('勒索软件', 'CRITICAL', '198.51.100.23', '俄罗斯', '莫斯科', '192.168.1.50', '文件服务器', 'DETECTED', '检测到勒索软件传播行为', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('数据泄露', 'CRITICAL', '185.220.101.32', '中国', '北京', '192.168.1.200', '核心系统', 'RESOLVED', '检测到异常数据导出行为，已处理', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
('零日漏洞利用', 'CRITICAL', '104.244.42.65', '美国', '旧金山', '192.168.1.150', 'Web服务器', 'BLOCKED', '检测到零日漏洞利用尝试', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
('APT攻击', 'CRITICAL', '91.108.56.123', '伊朗', '德黑兰', '192.168.1.80', '邮件系统', 'DETECTED', '检测到高级持续性威胁（APT）', DATE_SUB(NOW(), INTERVAL 15 HOUR)),

-- 高危威胁
('暴力破解', 'HIGH', '45.142.120.10', '中国', '上海', '192.168.1.10', '登录系统', 'BLOCKED', '检测到SSH暴力破解攻击', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('XSS攻击', 'HIGH', '23.95.67.89', '日本', '东京', '192.168.1.120', 'Web应用', 'BLOCKED', '检测到跨站脚本攻击', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('DDoS攻击', 'HIGH', '172.105.88.45', '韩国', '首尔', '192.168.1.1', '网关', 'DETECTED', '检测到分布式拒绝服务攻击', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('恶意软件', 'HIGH', '139.162.123.45', '德国', '柏林', '192.168.1.90', '终端设备', 'RESOLVED', '检测到恶意软件，已清除', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('钓鱼攻击', 'HIGH', '167.99.45.78', '英国', '伦敦', '192.168.1.110', '邮件系统', 'BLOCKED', '检测到钓鱼邮件', DATE_SUB(NOW(), INTERVAL 7 HOUR)),
('权限提升', 'HIGH', '178.128.90.12', '法国', '巴黎', '192.168.1.130', '应用服务器', 'DETECTED', '检测到权限提升尝试', DATE_SUB(NOW(), INTERVAL 9 HOUR)),
('中间人攻击', 'HIGH', '159.89.123.67', '加拿大', '多伦多', '192.168.1.140', '网络设备', 'BLOCKED', '检测到中间人攻击', DATE_SUB(NOW(), INTERVAL 10 HOUR)),

-- 中危威胁
('端口扫描', 'MEDIUM', '134.209.45.89', '新加坡', '新加坡', '192.168.1.1', '防火墙', 'DETECTED', '检测到端口扫描行为', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('异常登录', 'MEDIUM', '188.166.78.90', '澳大利亚', '悉尼', '192.168.1.20', '管理后台', 'RESOLVED', '检测到异常登录尝试', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('文件上传漏洞', 'MEDIUM', '165.227.123.45', '印度', '孟买', '192.168.1.160', '文件服务', 'BLOCKED', '检测到恶意文件上传尝试', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('信息泄露', 'MEDIUM', '142.93.67.89', '巴西', '圣保罗', '192.168.1.170', 'API服务', 'DETECTED', '检测到敏感信息泄露风险', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('弱密码', 'MEDIUM', '157.230.45.67', '墨西哥', '墨西哥城', '192.168.1.180', '用户账户', 'RESOLVED', '检测到弱密码使用', DATE_SUB(NOW(), INTERVAL 7 HOUR)),
('配置错误', 'MEDIUM', '174.138.90.12', '阿根廷', '布宜诺斯艾利斯', '192.168.1.190', '数据库', 'DETECTED', '检测到安全配置错误', DATE_SUB(NOW(), INTERVAL 11 HOUR)),
('会话劫持', 'MEDIUM', '206.189.123.45', '智利', '圣地亚哥', '192.168.1.210', 'Web应用', 'BLOCKED', '检测到会话劫持尝试', DATE_SUB(NOW(), INTERVAL 13 HOUR)),
('目录遍历', 'MEDIUM', '159.203.67.89', '南非', '开普敦', '192.168.1.220', '文件系统', 'DETECTED', '检测到目录遍历攻击', DATE_SUB(NOW(), INTERVAL 16 HOUR)),

-- 低危威胁
('Cookie窃取', 'LOW', '128.199.45.78', '泰国', '曼谷', '192.168.1.230', 'Web应用', 'DETECTED', '检测到Cookie窃取尝试', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('爬虫访问', 'LOW', '146.190.90.23', '越南', '河内', '192.168.1.240', 'Web服务', 'DETECTED', '检测到恶意爬虫访问', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('垃圾邮件', 'LOW', '161.35.123.56', '马来西亚', '吉隆坡', '192.168.1.250', '邮件服务器', 'BLOCKED', '检测到垃圾邮件', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('异常流量', 'LOW', '178.62.67.89', '印度尼西亚', '雅加达', '192.168.1.260', '网络设备', 'DETECTED', '检测到异常流量模式', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('过期证书', 'LOW', '192.241.45.12', '菲律宾', '马尼拉', '192.168.1.270', 'SSL服务', 'RESOLVED', '检测到过期SSL证书', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
('弱加密', 'LOW', '167.172.90.34', '孟加拉国', '达卡', '192.168.1.280', '加密服务', 'DETECTED', '检测到弱加密算法使用', DATE_SUB(NOW(), INTERVAL 14 HOUR)),
('未授权访问', 'LOW', '143.198.123.67', '巴基斯坦', '卡拉奇', '192.168.1.290', 'API接口', 'BLOCKED', '检测到未授权访问尝试', DATE_SUB(NOW(), INTERVAL 17 HOUR)),
('资源滥用', 'LOW', '159.65.45.89', '土耳其', '伊斯坦布尔', '192.168.1.300', '计算资源', 'DETECTED', '检测到资源滥用行为', DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- 插入更多历史数据（用于趋势分析）
INSERT INTO security_threat (threat_type, severity, source_ip, source_country, target_ip, target_system, status, detect_time)
SELECT 
    threat_type,
    severity,
    CONCAT(FLOOR(1 + RAND() * 254), '.', FLOOR(1 + RAND() * 254), '.', FLOOR(1 + RAND() * 254), '.', FLOOR(1 + RAND() * 254)),
    source_country,
    target_ip,
    target_system,
    CASE WHEN RAND() > 0.3 THEN 'BLOCKED' ELSE 'DETECTED' END,
    DATE_SUB(NOW(), INTERVAL FLOOR(1 + RAND() * 168) HOUR)
FROM security_threat
LIMIT 100;

-- 插入安全事件日志
INSERT INTO security_event_log (event_type, event_level, event_source, event_message, ip_address, event_time) VALUES
('登录失败', 'WARNING', '认证系统', '用户登录失败：密码错误', '203.0.113.45', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
('权限拒绝', 'WARNING', '授权系统', '用户尝试访问未授权资源', '198.51.100.23', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
('数据导出', 'INFO', '数据系统', '用户导出敏感数据', '192.168.1.100', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('配置变更', 'WARNING', '配置中心', '安全配置被修改', '192.168.1.50', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('系统异常', 'ERROR', '监控系统', '检测到系统异常行为', '192.168.1.200', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('防火墙阻断', 'INFO', '防火墙', '阻断可疑IP访问', '45.142.120.10', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('病毒查杀', 'CRITICAL', '安全软件', '发现并清除病毒', '192.168.1.90', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('备份完成', 'INFO', '备份系统', '数据备份成功完成', '192.168.1.150', DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- =============================================
-- 4. 创建视图
-- =============================================

-- 威胁统计视图
CREATE OR REPLACE VIEW v_threat_statistics AS
SELECT 
    DATE(detect_time) AS threat_date,
    threat_type,
    severity,
    COUNT(*) AS threat_count,
    SUM(CASE WHEN status = 'BLOCKED' THEN 1 ELSE 0 END) AS blocked_count,
    SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_count
FROM security_threat
GROUP BY DATE(detect_time), threat_type, severity;

-- 攻击来源统计视图
CREATE OR REPLACE VIEW v_attack_source_stats AS
SELECT 
    source_country,
    COUNT(*) AS attack_count,
    COUNT(DISTINCT source_ip) AS unique_ips,
    MAX(detect_time) AS last_attack_time
FROM security_threat
WHERE detect_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY source_country
ORDER BY attack_count DESC;

-- 实时威胁监控视图
CREATE OR REPLACE VIEW v_realtime_threats AS
SELECT 
    id,
    threat_type,
    severity,
    source_ip,
    source_country,
    target_system,
    status,
    detect_time
FROM security_threat
WHERE detect_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY detect_time DESC;

-- =============================================
-- 5. 创建存储过程
-- =============================================

DELIMITER //

-- 计算安全评分
CREATE PROCEDURE sp_calculate_security_score()
BEGIN
    DECLARE v_critical_count INT;
    DECLARE v_high_count INT;
    DECLARE v_medium_count INT;
    DECLARE v_low_count INT;
    DECLARE v_score INT;
    
    -- 统计最近24小时的威胁数量
    SELECT 
        SUM(CASE WHEN severity = 'CRITICAL' THEN 1 ELSE 0 END),
        SUM(CASE WHEN severity = 'HIGH' THEN 1 ELSE 0 END),
        SUM(CASE WHEN severity = 'MEDIUM' THEN 1 ELSE 0 END),
        SUM(CASE WHEN severity = 'LOW' THEN 1 ELSE 0 END)
    INTO v_critical_count, v_high_count, v_medium_count, v_low_count
    FROM security_threat
    WHERE detect_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR);
    
    -- 计算评分（100分制，威胁越多分数越低）
    SET v_score = 100 - (v_critical_count * 10 + v_high_count * 5 + v_medium_count * 2 + v_low_count);
    SET v_score = GREATEST(v_score, 0);
    
    SELECT v_score AS security_score,
           CASE 
               WHEN v_score >= 80 THEN '正常'
               WHEN v_score >= 60 THEN '警告'
               ELSE '高危'
           END AS security_level;
END //

-- 获取TOP攻击源IP
CREATE PROCEDURE sp_get_top_attack_ips(IN p_limit INT)
BEGIN
    SELECT 
        source_ip,
        source_country,
        COUNT(*) AS attack_count,
        MAX(detect_time) AS last_attack_time
    FROM security_threat
    WHERE detect_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
    GROUP BY source_ip, source_country
    ORDER BY attack_count DESC
    LIMIT p_limit;
END //

DELIMITER ;

-- =============================================
-- 说明文档
-- =============================================
/*
安全态势大屏数据库设计说明：

1. 核心表结构：
   - security_threat: 存储安全威胁信息
   - security_event_log: 存储安全事件日志

2. 威胁类型：
   - SQL注入、XSS攻击、暴力破解、DDoS攻击
   - 恶意软件、钓鱼攻击、数据泄露等

3. 严重程度：
   - CRITICAL: 严重威胁
   - HIGH: 高危威胁
   - MEDIUM: 中危威胁
   - LOW: 低危威胁

4. 安全评分计算：
   - 基础分100分
   - 严重威胁扣10分，高危扣5分，中危扣2分，低危扣1分
   - 评分 >= 80: 正常
   - 60 <= 评分 < 80: 警告
   - 评分 < 60: 高危

5. 使用示例：
   -- 计算安全评分
   CALL sp_calculate_security_score();
   
   -- 获取TOP10攻击源IP
   CALL sp_get_top_attack_ips(10);
   
   -- 查看实时威胁
   SELECT * FROM v_realtime_threats;
*/
