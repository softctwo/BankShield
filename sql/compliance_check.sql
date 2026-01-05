-- =============================================
-- BankShield 合规性检查模块 - 数据库设计
-- 版本：v1.0
-- 日期：2025-01-04
-- =============================================

-- 删除已存在的表
DROP TABLE IF EXISTS compliance_check_result;
DROP TABLE IF EXISTS compliance_check_task;
DROP TABLE IF EXISTS compliance_rule;
DROP TABLE IF EXISTS compliance_report;

-- =============================================
-- 1. 合规规则表
-- =============================================
CREATE TABLE compliance_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_category VARCHAR(50) NOT NULL COMMENT '规则分类：GDPR/个保法/网安法/等保',
    rule_level VARCHAR(20) NOT NULL COMMENT '规则级别：CRITICAL/HIGH/MEDIUM/LOW',
    rule_description TEXT COMMENT '规则描述',
    check_type VARCHAR(50) NOT NULL COMMENT '检查类型：AUTO/MANUAL/SEMI_AUTO',
    check_script TEXT COMMENT '检查脚本（SQL/代码）',
    check_criteria TEXT COMMENT '检查标准（JSON格式）',
    remediation_guide TEXT COMMENT '整改指南',
    reference_links TEXT COMMENT '参考链接',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    weight INT DEFAULT 1 COMMENT '权重',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (rule_category),
    INDEX idx_level (rule_level),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规规则表';

-- =============================================
-- 2. 合规检查任务表
-- =============================================
CREATE TABLE compliance_check_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：FULL/CATEGORY/CUSTOM',
    check_scope VARCHAR(100) COMMENT '检查范围',
    task_status VARCHAR(20) NOT NULL COMMENT '任务状态：PENDING/RUNNING/COMPLETED/FAILED',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    total_rules INT DEFAULT 0 COMMENT '总规则数',
    checked_rules INT DEFAULT 0 COMMENT '已检查规则数',
    passed_rules INT DEFAULT 0 COMMENT '通过规则数',
    failed_rules INT DEFAULT 0 COMMENT '失败规则数',
    compliance_score DECIMAL(5,2) COMMENT '合规评分',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '耗时（秒）',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (task_status),
    INDEX idx_created_by (created_by),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规检查任务表';

-- =============================================
-- 3. 合规检查结果表
-- =============================================
CREATE TABLE compliance_check_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    check_status VARCHAR(20) NOT NULL COMMENT '检查状态：PASS/FAIL/WARNING/SKIP',
    check_result TEXT COMMENT '检查结果详情',
    risk_level VARCHAR(20) COMMENT '风险级别',
    risk_description TEXT COMMENT '风险描述',
    affected_items TEXT COMMENT '影响项（JSON格式）',
    evidence TEXT COMMENT '证据信息',
    remediation_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '整改状态：PENDING/IN_PROGRESS/COMPLETED',
    remediation_plan TEXT COMMENT '整改计划',
    remediation_deadline DATETIME COMMENT '整改期限',
    assigned_to VARCHAR(50) COMMENT '负责人',
    check_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
    INDEX idx_task_id (task_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_check_status (check_status),
    INDEX idx_remediation_status (remediation_status),
    FOREIGN KEY (task_id) REFERENCES compliance_check_task(id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES compliance_rule(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规检查结果表';

-- =============================================
-- 4. 合规报告表
-- =============================================
CREATE TABLE compliance_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报告名称',
    report_type VARCHAR(50) NOT NULL COMMENT '报告类型：SUMMARY/DETAILED/EXECUTIVE',
    report_format VARCHAR(20) NOT NULL COMMENT '报告格式：PDF/HTML/EXCEL',
    report_content LONGTEXT COMMENT '报告内容',
    report_path VARCHAR(500) COMMENT '报告文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    compliance_score DECIMAL(5,2) COMMENT '合规评分',
    total_items INT COMMENT '总检查项',
    passed_items INT COMMENT '通过项',
    failed_items INT COMMENT '失败项',
    generated_by VARCHAR(50) COMMENT '生成人',
    generated_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    INDEX idx_task_id (task_id),
    INDEX idx_report_type (report_type),
    FOREIGN KEY (task_id) REFERENCES compliance_check_task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合规报告表';

-- =============================================
-- 5. 初始化合规规则数据
-- =============================================

-- GDPR相关规则
INSERT INTO compliance_rule (rule_code, rule_name, rule_category, rule_level, rule_description, check_type, check_criteria, remediation_guide) VALUES
('GDPR-001', '数据处理合法性基础', 'GDPR', 'CRITICAL', '确保所有个人数据处理都有合法基础（同意、合同、法律义务等）', 'MANUAL', '{"criteria": "检查数据处理活动记录", "threshold": 100}', '建立数据处理活动记录（ROPA），明确每项处理的合法基础'),
('GDPR-002', '数据主体权利保障', 'GDPR', 'HIGH', '确保数据主体可以行使访问、更正、删除等权利', 'SEMI_AUTO', '{"criteria": "检查权利请求处理流程", "response_time": 30}', '建立数据主体权利请求处理机制，确保30天内响应'),
('GDPR-003', '数据保护影响评估', 'GDPR', 'HIGH', '对高风险处理活动进行DPIA评估', 'MANUAL', '{"criteria": "检查DPIA文档", "coverage": "high_risk_activities"}', '识别高风险处理活动并完成DPIA评估'),
('GDPR-004', '数据跨境传输合规', 'GDPR', 'CRITICAL', '确保跨境数据传输符合GDPR要求', 'SEMI_AUTO', '{"criteria": "检查跨境传输机制", "mechanisms": ["SCC", "BCR", "adequacy"]}', '建立合规的跨境传输机制（标准合同条款、约束性公司规则等）'),
('GDPR-005', '数据泄露通知机制', 'GDPR', 'CRITICAL', '72小时内向监管机构报告数据泄露', 'AUTO', '{"criteria": "检查泄露响应流程", "notification_time": 72}', '建立数据泄露检测和通知机制');

-- 个人信息保护法相关规则
INSERT INTO compliance_rule (rule_code, rule_name, rule_category, rule_level, rule_description, check_type, check_criteria, remediation_guide) VALUES
('PIPL-001', '个人信息处理告知同意', '个保法', 'CRITICAL', '处理个人信息应当取得个人同意', 'SEMI_AUTO', '{"criteria": "检查同意机制", "explicit": true}', '建立明确的告知同意机制，确保用户知情并同意'),
('PIPL-002', '敏感个人信息特别保护', '个保法', 'CRITICAL', '处理敏感个人信息需要取得单独同意', 'AUTO', '{"criteria": "检查敏感信息处理", "separate_consent": true}', '对敏感个人信息实施特别保护措施，取得单独同意'),
('PIPL-003', '个人信息出境安全评估', '个保法', 'HIGH', '向境外提供个人信息应通过安全评估', 'MANUAL', '{"criteria": "检查出境评估", "assessment_required": true}', '完成个人信息出境安全评估或标准合同备案'),
('PIPL-004', '个人信息保护影响评估', '个保法', 'HIGH', '处理敏感信息或大规模处理应进行影响评估', 'MANUAL', '{"criteria": "检查PIA文档", "triggers": ["sensitive", "large_scale"]}', '对符合条件的处理活动开展个人信息保护影响评估'),
('PIPL-006', '个人信息处理者义务履行', '个保法', 'HIGH', '建立个人信息保护管理制度和操作规程', 'SEMI_AUTO', '{"criteria": "检查管理制度", "required_docs": ["policy", "procedure", "training"]}', '建立完善的个人信息保护管理体系');

-- 网络安全法相关规则
INSERT INTO compliance_rule (rule_code, rule_name, rule_category, rule_level, rule_description, check_type, check_criteria, remediation_guide) VALUES
('CSL-001', '网络安全等级保护', '网安法', 'CRITICAL', '关键信息基础设施应达到等保三级以上', 'MANUAL', '{"criteria": "检查等保备案", "level": 3}', '完成等级保护定级备案和测评'),
('CSL-002', '网络安全审查', '网安法', 'HIGH', '关键信息基础设施运营者采购网络产品和服务应进行安全审查', 'MANUAL', '{"criteria": "检查采购审查流程"}', '建立网络产品和服务安全审查机制'),
('CSL-003', '数据安全管理', '网安法', 'HIGH', '采取数据分类、重要数据备份和加密等措施', 'AUTO', '{"criteria": "检查数据安全措施", "required": ["classification", "backup", "encryption"]}', '实施数据分类分级保护，建立备份和加密机制'),
('CSL-004', '网络安全事件应急预案', '网安法', 'MEDIUM', '制定网络安全事件应急预案并定期演练', 'MANUAL', '{"criteria": "检查应急预案", "drill_frequency": "annual"}', '制定应急预案并每年至少演练一次');

-- 数据安全法相关规则
INSERT INTO compliance_rule (rule_code, rule_name, rule_category, rule_level, rule_description, check_type, check_criteria, remediation_guide) VALUES
('DSL-001', '数据分类分级保护', '数安法', 'HIGH', '建立数据分类分级保护制度', 'SEMI_AUTO', '{"criteria": "检查分类分级制度", "levels": ["public", "internal", "confidential", "secret"]}', '建立数据分类分级标准和保护措施'),
('DSL-002', '重要数据识别和保护', '数安法', 'CRITICAL', '识别重要数据并采取严格保护措施', 'SEMI_AUTO', '{"criteria": "检查重要数据清单", "protection_measures": true}', '识别重要数据并实施严格的安全保护措施'),
('DSL-003', '数据安全风险评估', '数安法', 'HIGH', '定期开展数据安全风险评估', 'MANUAL', '{"criteria": "检查风险评估报告", "frequency": "annual"}', '每年至少开展一次数据安全风险评估'),
('DSL-004', '数据安全应急处置', '数安法', 'MEDIUM', '建立数据安全应急处置机制', 'MANUAL', '{"criteria": "检查应急机制", "components": ["plan", "team", "drill"]}', '建立数据安全应急处置预案和响应团队');

-- =============================================
-- 6. 创建视图
-- =============================================

-- 合规检查统计视图
CREATE OR REPLACE VIEW v_compliance_statistics AS
SELECT 
    t.id AS task_id,
    t.task_name,
    t.compliance_score,
    COUNT(r.id) AS total_checks,
    SUM(CASE WHEN r.check_status = 'PASS' THEN 1 ELSE 0 END) AS passed_checks,
    SUM(CASE WHEN r.check_status = 'FAIL' THEN 1 ELSE 0 END) AS failed_checks,
    SUM(CASE WHEN r.check_status = 'WARNING' THEN 1 ELSE 0 END) AS warning_checks,
    SUM(CASE WHEN r.risk_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_risks,
    SUM(CASE WHEN r.risk_level = 'HIGH' THEN 1 ELSE 0 END) AS high_risks,
    t.created_time
FROM compliance_check_task t
LEFT JOIN compliance_check_result r ON t.id = r.task_id
GROUP BY t.id, t.task_name, t.compliance_score, t.created_time;

-- 合规规则分类统计视图
CREATE OR REPLACE VIEW v_compliance_rule_stats AS
SELECT 
    rule_category,
    COUNT(*) AS total_rules,
    SUM(CASE WHEN enabled = TRUE THEN 1 ELSE 0 END) AS enabled_rules,
    SUM(CASE WHEN rule_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_rules,
    SUM(CASE WHEN rule_level = 'HIGH' THEN 1 ELSE 0 END) AS high_rules
FROM compliance_rule
GROUP BY rule_category;

-- =============================================
-- 7. 创建存储过程
-- =============================================

DELIMITER //

-- 计算合规评分
CREATE PROCEDURE sp_calculate_compliance_score(IN p_task_id BIGINT)
BEGIN
    DECLARE v_total INT;
    DECLARE v_passed INT;
    DECLARE v_weighted_score DECIMAL(10,2);
    DECLARE v_total_weight INT;
    
    -- 计算加权评分
    SELECT 
        SUM(cr.weight) INTO v_total_weight
    FROM compliance_check_result ccr
    JOIN compliance_rule cr ON ccr.rule_id = cr.id
    WHERE ccr.task_id = p_task_id;
    
    SELECT 
        SUM(CASE WHEN ccr.check_status = 'PASS' THEN cr.weight ELSE 0 END) INTO v_weighted_score
    FROM compliance_check_result ccr
    JOIN compliance_rule cr ON ccr.rule_id = cr.id
    WHERE ccr.task_id = p_task_id;
    
    -- 更新任务评分
    UPDATE compliance_check_task
    SET compliance_score = ROUND((v_weighted_score / v_total_weight) * 100, 2)
    WHERE id = p_task_id;
END //

-- 生成合规报告
CREATE PROCEDURE sp_generate_compliance_report(
    IN p_task_id BIGINT,
    IN p_report_type VARCHAR(50),
    IN p_generated_by VARCHAR(50)
)
BEGIN
    DECLARE v_report_name VARCHAR(200);
    DECLARE v_compliance_score DECIMAL(5,2);
    DECLARE v_total INT;
    DECLARE v_passed INT;
    DECLARE v_failed INT;
    
    -- 获取任务信息
    SELECT task_name, compliance_score INTO v_report_name, v_compliance_score
    FROM compliance_check_task WHERE id = p_task_id;
    
    -- 统计检查结果
    SELECT 
        COUNT(*),
        SUM(CASE WHEN check_status = 'PASS' THEN 1 ELSE 0 END),
        SUM(CASE WHEN check_status = 'FAIL' THEN 1 ELSE 0 END)
    INTO v_total, v_passed, v_failed
    FROM compliance_check_result
    WHERE task_id = p_task_id;
    
    -- 插入报告记录
    INSERT INTO compliance_report (
        task_id, report_name, report_type, report_format,
        compliance_score, total_items, passed_items, failed_items,
        generated_by
    ) VALUES (
        p_task_id,
        CONCAT(v_report_name, '-', p_report_type, '-', DATE_FORMAT(NOW(), '%Y%m%d')),
        p_report_type,
        'PDF',
        v_compliance_score,
        v_total,
        v_passed,
        v_failed,
        p_generated_by
    );
END //

DELIMITER ;

-- =============================================
-- 说明文档
-- =============================================
/*
合规性检查模块数据库设计说明：

1. 核心表结构：
   - compliance_rule: 存储合规规则
   - compliance_check_task: 存储检查任务
   - compliance_check_result: 存储检查结果
   - compliance_report: 存储合规报告

2. 支持的合规标准：
   - GDPR（欧盟通用数据保护条例）
   - 个保法（中国个人信息保护法）
   - 网安法（中国网络安全法）
   - 数安法（中国数据安全法）

3. 检查类型：
   - AUTO: 自动检查（通过SQL或脚本）
   - MANUAL: 人工检查
   - SEMI_AUTO: 半自动检查

4. 合规评分计算：
   - 基于规则权重的加权评分
   - 100分制，分数越高合规性越好

5. 使用示例：
   -- 创建检查任务
   INSERT INTO compliance_check_task (task_name, task_type, created_by)
   VALUES ('2025年度合规检查', 'FULL', 'admin');
   
   -- 计算合规评分
   CALL sp_calculate_compliance_score(1);
   
   -- 生成合规报告
   CALL sp_generate_compliance_report(1, 'SUMMARY', 'admin');
*/
