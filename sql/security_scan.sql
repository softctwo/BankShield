-- =============================================
-- BankShield 安全扫描与漏洞管理模块
-- 功能：SQL注入检测、XSS检测、依赖漏洞扫描、代码安全扫描、漏洞管理
-- 版本：v1.0
-- 日期：2025-01-04
-- =============================================

-- 1. 扫描任务表
DROP TABLE IF EXISTS `security_scan_task`;
CREATE TABLE `security_scan_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型：SQL_INJECTION/XSS/DEPENDENCY/CODE_SCAN/FULL_SCAN',
    `scan_target` VARCHAR(500) COMMENT '扫描目标（URL、文件路径、模块名等）',
    `scan_scope` VARCHAR(50) NOT NULL DEFAULT 'FULL' COMMENT '扫描范围：FULL/PARTIAL/CUSTOM',
    `scan_config` JSON COMMENT '扫描配置（JSON格式）',
    `scan_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '扫描状态：PENDING/RUNNING/COMPLETED/FAILED/CANCELLED',
    `progress` INT DEFAULT 0 COMMENT '扫描进度（0-100）',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration` INT COMMENT '扫描时长（秒）',
    `total_items` INT DEFAULT 0 COMMENT '扫描项总数',
    `scanned_items` INT DEFAULT 0 COMMENT '已扫描项数',
    `vulnerabilities_found` INT DEFAULT 0 COMMENT '发现漏洞数',
    `high_risk_count` INT DEFAULT 0 COMMENT '高危漏洞数',
    `medium_risk_count` INT DEFAULT 0 COMMENT '中危漏洞数',
    `low_risk_count` INT DEFAULT 0 COMMENT '低危漏洞数',
    `scan_result` TEXT COMMENT '扫描结果摘要',
    `error_message` TEXT COMMENT '错误信息',
    `created_by` VARCHAR(100) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_type` (`task_type`),
    KEY `idx_scan_status` (`scan_status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全扫描任务表';

-- 2. 漏洞记录表
DROP TABLE IF EXISTS `vulnerability_record`;
CREATE TABLE `vulnerability_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '漏洞ID',
    `task_id` BIGINT NOT NULL COMMENT '所属扫描任务ID',
    `vuln_code` VARCHAR(100) NOT NULL COMMENT '漏洞编码',
    `vuln_name` VARCHAR(200) NOT NULL COMMENT '漏洞名称',
    `vuln_type` VARCHAR(50) NOT NULL COMMENT '漏洞类型：SQL_INJECTION/XSS/CSRF/XXE/SSRF/RCE/LFI/DEPENDENCY等',
    `severity` VARCHAR(20) NOT NULL COMMENT '严重程度：CRITICAL/HIGH/MEDIUM/LOW/INFO',
    `cvss_score` DECIMAL(3,1) COMMENT 'CVSS评分（0.0-10.0）',
    `cve_id` VARCHAR(50) COMMENT 'CVE编号',
    `cwe_id` VARCHAR(50) COMMENT 'CWE编号',
    `description` TEXT COMMENT '漏洞描述',
    `location` VARCHAR(500) COMMENT '漏洞位置（文件路径、URL、代码行号等）',
    `affected_component` VARCHAR(200) COMMENT '受影响的组件',
    `affected_version` VARCHAR(100) COMMENT '受影响的版本',
    `proof_of_concept` TEXT COMMENT '漏洞证明（PoC）',
    `impact` TEXT COMMENT '影响分析',
    `recommendation` TEXT COMMENT '修复建议',
    `reference_links` TEXT COMMENT '参考链接',
    `status` VARCHAR(50) NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN/IN_PROGRESS/RESOLVED/WONT_FIX/FALSE_POSITIVE',
    `assigned_to` VARCHAR(100) COMMENT '分配给',
    `resolved_by` VARCHAR(100) COMMENT '解决人',
    `resolved_time` DATETIME COMMENT '解决时间',
    `resolution_notes` TEXT COMMENT '解决说明',
    `verification_status` VARCHAR(50) COMMENT '验证状态：PENDING/VERIFIED/FAILED',
    `verified_by` VARCHAR(100) COMMENT '验证人',
    `verified_time` DATETIME COMMENT '验证时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vuln_code` (`vuln_code`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_vuln_type` (`vuln_type`),
    KEY `idx_severity` (`severity`),
    KEY `idx_status` (`status`),
    KEY `idx_cve_id` (`cve_id`),
    CONSTRAINT `fk_vuln_task` FOREIGN KEY (`task_id`) REFERENCES `security_scan_task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='漏洞记录表';

-- 3. 修复计划表
DROP TABLE IF EXISTS `remediation_plan`;
CREATE TABLE `remediation_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    `plan_name` VARCHAR(200) NOT NULL COMMENT '计划名称',
    `vulnerability_ids` TEXT NOT NULL COMMENT '关联的漏洞ID列表（逗号分隔）',
    `plan_type` VARCHAR(50) NOT NULL DEFAULT 'MANUAL' COMMENT '计划类型：MANUAL/AUTO/SCHEDULED',
    `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：CRITICAL/HIGH/MEDIUM/LOW',
    `plan_status` VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/APPROVED/IN_PROGRESS/COMPLETED/CANCELLED',
    `description` TEXT COMMENT '计划描述',
    `remediation_steps` TEXT COMMENT '修复步骤',
    `estimated_effort` VARCHAR(100) COMMENT '预计工作量',
    `scheduled_date` DATE COMMENT '计划日期',
    `deadline` DATE COMMENT '截止日期',
    `assigned_to` VARCHAR(100) COMMENT '负责人',
    `approved_by` VARCHAR(100) COMMENT '审批人',
    `approved_time` DATETIME COMMENT '审批时间',
    `completed_time` DATETIME COMMENT '完成时间',
    `completion_notes` TEXT COMMENT '完成说明',
    `created_by` VARCHAR(100) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_plan_status` (`plan_status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_scheduled_date` (`scheduled_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='修复计划表';

-- 4. 扫描规则表
DROP TABLE IF EXISTS `scan_rule`;
CREATE TABLE `scan_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_code` VARCHAR(100) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(200) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型：SQL_INJECTION/XSS/SECURITY_MISCONFIGURATION等',
    `severity` VARCHAR(20) NOT NULL COMMENT '严重程度：CRITICAL/HIGH/MEDIUM/LOW',
    `description` TEXT COMMENT '规则描述',
    `detection_pattern` TEXT COMMENT '检测模式（正则表达式、规则逻辑等）',
    `rule_config` JSON COMMENT '规则配置',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-否，1-是',
    `false_positive_rate` DECIMAL(5,2) COMMENT '误报率（%）',
    `reference_links` TEXT COMMENT '参考链接',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描规则表';

-- 5. 依赖组件表
DROP TABLE IF EXISTS `dependency_component`;
CREATE TABLE `dependency_component` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '组件ID',
    `component_name` VARCHAR(200) NOT NULL COMMENT '组件名称',
    `component_type` VARCHAR(50) NOT NULL COMMENT '组件类型：MAVEN/NPM/PIP/NUGET等',
    `current_version` VARCHAR(100) NOT NULL COMMENT '当前版本',
    `latest_version` VARCHAR(100) COMMENT '最新版本',
    `group_id` VARCHAR(200) COMMENT 'Group ID（Maven）',
    `artifact_id` VARCHAR(200) COMMENT 'Artifact ID（Maven）',
    `package_name` VARCHAR(200) COMMENT '包名（NPM/PIP）',
    `license` VARCHAR(100) COMMENT '许可证',
    `description` TEXT COMMENT '组件描述',
    `homepage_url` VARCHAR(500) COMMENT '主页URL',
    `repository_url` VARCHAR(500) COMMENT '仓库URL',
    `vulnerability_count` INT DEFAULT 0 COMMENT '已知漏洞数',
    `highest_severity` VARCHAR(20) COMMENT '最高严重程度',
    `last_scan_time` DATETIME COMMENT '最后扫描时间',
    `scan_status` VARCHAR(50) COMMENT '扫描状态：PENDING/SCANNED/ERROR',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_component` (`component_name`, `component_type`, `current_version`),
    KEY `idx_component_type` (`component_type`),
    KEY `idx_vulnerability_count` (`vulnerability_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='依赖组件表';

-- 6. 扫描历史统计表
DROP TABLE IF EXISTS `scan_statistics`;
CREATE TABLE `scan_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `scan_count` INT DEFAULT 0 COMMENT '扫描次数',
    `total_vulnerabilities` INT DEFAULT 0 COMMENT '发现漏洞总数',
    `critical_count` INT DEFAULT 0 COMMENT '严重漏洞数',
    `high_count` INT DEFAULT 0 COMMENT '高危漏洞数',
    `medium_count` INT DEFAULT 0 COMMENT '中危漏洞数',
    `low_count` INT DEFAULT 0 COMMENT '低危漏洞数',
    `resolved_count` INT DEFAULT 0 COMMENT '已解决漏洞数',
    `open_count` INT DEFAULT 0 COMMENT '未解决漏洞数',
    `false_positive_count` INT DEFAULT 0 COMMENT '误报数',
    `avg_resolution_time` INT COMMENT '平均解决时间（小时）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫描历史统计表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认扫描规则
INSERT INTO `scan_rule` (`rule_code`, `rule_name`, `rule_type`, `severity`, `description`, `detection_pattern`, `enabled`) VALUES
-- SQL注入规则
('SQL_001', 'SQL注入 - 单引号未转义', 'SQL_INJECTION', 'HIGH', '检测SQL语句中未转义的单引号', '.*\'.*', 1),
('SQL_002', 'SQL注入 - OR 1=1模式', 'SQL_INJECTION', 'CRITICAL', '检测经典的OR 1=1注入模式', '.*(OR|or)\\s+1\\s*=\\s*1.*', 1),
('SQL_003', 'SQL注入 - UNION SELECT', 'SQL_INJECTION', 'CRITICAL', '检测UNION SELECT注入', '.*(UNION|union)\\s+(SELECT|select).*', 1),
('SQL_004', 'SQL注入 - 注释符', 'SQL_INJECTION', 'HIGH', '检测SQL注释符（--、#、/*）', '.*(--|#|/\\*).*', 1),

-- XSS规则
('XSS_001', 'XSS - Script标签', 'XSS', 'HIGH', '检测<script>标签', '.*<script.*>.*</script>.*', 1),
('XSS_002', 'XSS - 事件处理器', 'XSS', 'HIGH', '检测危险的事件处理器', '.*(onerror|onload|onclick|onmouseover)\\s*=.*', 1),
('XSS_003', 'XSS - JavaScript伪协议', 'XSS', 'MEDIUM', '检测javascript:伪协议', '.*javascript:.*', 1),
('XSS_004', 'XSS - iframe注入', 'XSS', 'HIGH', '检测iframe标签注入', '.*<iframe.*>.*', 1),

-- 安全配置规则
('SEC_001', '硬编码密码', 'SECURITY_MISCONFIGURATION', 'CRITICAL', '检测代码中的硬编码密码', '.*(password|pwd|passwd)\\s*=\\s*["\'].*["\'].*', 1),
('SEC_002', '硬编码密钥', 'SECURITY_MISCONFIGURATION', 'CRITICAL', '检测代码中的硬编码密钥', '.*(secret|key|token)\\s*=\\s*["\'].*["\'].*', 1),
('SEC_003', '不安全的随机数', 'SECURITY_MISCONFIGURATION', 'MEDIUM', '检测使用不安全的随机数生成器', '.*(Math\\.random|Random\\(\\)).*', 1),
('SEC_004', '弱加密算法', 'SECURITY_MISCONFIGURATION', 'HIGH', '检测使用弱加密算法', '.*(MD5|SHA1|DES).*', 1);

-- 插入示例扫描任务
INSERT INTO `security_scan_task` (`task_name`, `task_type`, `scan_target`, `scan_scope`, `scan_status`, `created_by`) VALUES
('全系统安全扫描', 'FULL_SCAN', '/', 'FULL', 'PENDING', 'admin'),
('SQL注入专项扫描', 'SQL_INJECTION', '/api/*', 'PARTIAL', 'PENDING', 'admin'),
('依赖漏洞扫描', 'DEPENDENCY', 'pom.xml,package.json', 'CUSTOM', 'PENDING', 'admin');

-- =============================================
-- 视图定义
-- =============================================

-- 漏洞统计视图
CREATE OR REPLACE VIEW `v_vulnerability_statistics` AS
SELECT 
    v.vuln_type,
    v.severity,
    v.status,
    COUNT(*) AS vuln_count,
    AVG(v.cvss_score) AS avg_cvss_score,
    MIN(v.created_time) AS first_found,
    MAX(v.created_time) AS last_found
FROM vulnerability_record v
GROUP BY v.vuln_type, v.severity, v.status;

-- 扫描任务概览视图
CREATE OR REPLACE VIEW `v_scan_task_overview` AS
SELECT 
    t.task_type,
    t.scan_status,
    COUNT(*) AS task_count,
    SUM(t.vulnerabilities_found) AS total_vulnerabilities,
    SUM(t.high_risk_count) AS total_high_risk,
    AVG(t.duration) AS avg_duration
FROM security_scan_task t
GROUP BY t.task_type, t.scan_status;

-- 待修复漏洞视图
CREATE OR REPLACE VIEW `v_pending_vulnerabilities` AS
SELECT 
    v.id,
    v.vuln_code,
    v.vuln_name,
    v.vuln_type,
    v.severity,
    v.cvss_score,
    v.location,
    v.status,
    v.assigned_to,
    DATEDIFF(NOW(), v.created_time) AS days_open,
    t.task_name,
    t.created_time AS scan_time
FROM vulnerability_record v
INNER JOIN security_scan_task t ON v.task_id = t.id
WHERE v.status IN ('OPEN', 'IN_PROGRESS')
ORDER BY 
    CASE v.severity
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'MEDIUM' THEN 3
        WHEN 'LOW' THEN 4
        ELSE 5
    END,
    v.created_time ASC;

-- =============================================
-- 存储过程
-- =============================================

-- 更新扫描任务进度
DELIMITER //
CREATE PROCEDURE `sp_update_scan_progress`(
    IN p_task_id BIGINT,
    IN p_scanned_items INT,
    IN p_total_items INT
)
BEGIN
    DECLARE v_progress INT;
    
    IF p_total_items > 0 THEN
        SET v_progress = ROUND((p_scanned_items * 100.0) / p_total_items);
    ELSE
        SET v_progress = 0;
    END IF;
    
    UPDATE security_scan_task
    SET scanned_items = p_scanned_items,
        total_items = p_total_items,
        progress = v_progress,
        updated_time = NOW()
    WHERE id = p_task_id;
END //
DELIMITER ;

-- 统计漏洞数量
DELIMITER //
CREATE PROCEDURE `sp_count_vulnerabilities_by_task`(
    IN p_task_id BIGINT
)
BEGIN
    UPDATE security_scan_task t
    SET t.vulnerabilities_found = (
            SELECT COUNT(*) FROM vulnerability_record WHERE task_id = p_task_id
        ),
        t.high_risk_count = (
            SELECT COUNT(*) FROM vulnerability_record 
            WHERE task_id = p_task_id AND severity IN ('CRITICAL', 'HIGH')
        ),
        t.medium_risk_count = (
            SELECT COUNT(*) FROM vulnerability_record 
            WHERE task_id = p_task_id AND severity = 'MEDIUM'
        ),
        t.low_risk_count = (
            SELECT COUNT(*) FROM vulnerability_record 
            WHERE task_id = p_task_id AND severity = 'LOW'
        )
    WHERE t.id = p_task_id;
END //
DELIMITER ;

-- 生成每日统计
DELIMITER //
CREATE PROCEDURE `sp_generate_daily_statistics`(
    IN p_stat_date DATE
)
BEGIN
    INSERT INTO scan_statistics (
        stat_date,
        scan_count,
        total_vulnerabilities,
        critical_count,
        high_count,
        medium_count,
        low_count,
        resolved_count,
        open_count,
        false_positive_count
    )
    SELECT 
        p_stat_date,
        COUNT(DISTINCT t.id),
        COUNT(v.id),
        SUM(CASE WHEN v.severity = 'CRITICAL' THEN 1 ELSE 0 END),
        SUM(CASE WHEN v.severity = 'HIGH' THEN 1 ELSE 0 END),
        SUM(CASE WHEN v.severity = 'MEDIUM' THEN 1 ELSE 0 END),
        SUM(CASE WHEN v.severity = 'LOW' THEN 1 ELSE 0 END),
        SUM(CASE WHEN v.status = 'RESOLVED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN v.status IN ('OPEN', 'IN_PROGRESS') THEN 1 ELSE 0 END),
        SUM(CASE WHEN v.status = 'FALSE_POSITIVE' THEN 1 ELSE 0 END)
    FROM security_scan_task t
    LEFT JOIN vulnerability_record v ON t.id = v.task_id
    WHERE DATE(t.created_time) = p_stat_date
    ON DUPLICATE KEY UPDATE
        scan_count = VALUES(scan_count),
        total_vulnerabilities = VALUES(total_vulnerabilities),
        critical_count = VALUES(critical_count),
        high_count = VALUES(high_count),
        medium_count = VALUES(medium_count),
        low_count = VALUES(low_count),
        resolved_count = VALUES(resolved_count),
        open_count = VALUES(open_count),
        false_positive_count = VALUES(false_positive_count);
END //
DELIMITER ;

-- =============================================
-- 说明文档
-- =============================================

/*
安全扫描与漏洞管理模块说明：

1. 核心功能
   - SQL注入检测：检测SQL语句中的注入漏洞
   - XSS检测：检测跨站脚本攻击漏洞
   - 依赖漏洞扫描：扫描第三方依赖的已知漏洞
   - 代码安全扫描：静态代码分析，检测安全问题
   - 漏洞管理：漏洞记录、修复计划、验证追踪

2. 表结构说明
   - security_scan_task: 扫描任务主表
   - vulnerability_record: 漏洞记录详细信息
   - remediation_plan: 修复计划和追踪
   - scan_rule: 扫描规则配置
   - dependency_component: 依赖组件管理
   - scan_statistics: 历史统计数据

3. 使用示例
   -- 更新扫描进度
   CALL sp_update_scan_progress(1, 50, 100);
   
   -- 统计任务漏洞
   CALL sp_count_vulnerabilities_by_task(1);
   
   -- 生成每日统计
   CALL sp_generate_daily_statistics('2025-01-04');

4. 扫描类型
   - SQL_INJECTION: SQL注入检测
   - XSS: 跨站脚本检测
   - DEPENDENCY: 依赖漏洞扫描
   - CODE_SCAN: 代码安全扫描
   - FULL_SCAN: 全面扫描

5. 严重程度分级
   - CRITICAL: 严重（CVSS 9.0-10.0）
   - HIGH: 高危（CVSS 7.0-8.9）
   - MEDIUM: 中危（CVSS 4.0-6.9）
   - LOW: 低危（CVSS 0.1-3.9）
   - INFO: 信息（CVSS 0.0）

6. 漏洞状态
   - OPEN: 未解决
   - IN_PROGRESS: 修复中
   - RESOLVED: 已解决
   - WONT_FIX: 不修复
   - FALSE_POSITIVE: 误报
*/
