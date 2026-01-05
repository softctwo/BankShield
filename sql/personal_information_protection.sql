-- =============================================
-- BankShield 个人信息保护模块 SQL脚本
-- 实现告知同意管理、个人权利行使、影响评估等功能
-- =============================================

-- 1. 创建告知同意记录表
CREATE TABLE IF NOT EXISTS consent_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id VARCHAR(100) NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    user_email VARCHAR(100) COMMENT '用户邮箱',
    consent_type VARCHAR(50) NOT NULL COMMENT '同意类型：COLLECT,USE,SHARE,TRANSFER',
    consent_purpose TEXT COMMENT '同意目的',
    data_categories TEXT COMMENT '数据类别（JSON数组）',
    consent_method VARCHAR(50) COMMENT '同意方式：EXPLICIT,IMPLICIT,OPT_IN,OPT_OUT',
    consent_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '同意状态：ACTIVE,REVOKED,EXPIRED',
    consent_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '同意时间',
    revoke_time DATETIME COMMENT '撤回时间',
    expire_time DATETIME COMMENT '过期时间',
    consent_content TEXT COMMENT '同意内容',
    consent_version VARCHAR(20) COMMENT '同意版本',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    device_info VARCHAR(200) COMMENT '设备信息',
    consent_evidence TEXT COMMENT '同意证据（截图、录音等）',
    third_party_name VARCHAR(200) COMMENT '第三方名称（共享/转移时）',
    third_party_purpose TEXT COMMENT '第三方使用目的',
    is_sensitive TINYINT DEFAULT 0 COMMENT '是否涉及敏感信息：1-是，0-否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_consent_type (consent_type),
    INDEX idx_consent_status (consent_status),
    INDEX idx_consent_time (consent_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告知同意记录表';

-- 2. 创建个人权利请求表
CREATE TABLE IF NOT EXISTS personal_rights_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '请求ID',
    request_no VARCHAR(50) NOT NULL COMMENT '请求编号',
    user_id VARCHAR(100) NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    user_email VARCHAR(100) COMMENT '用户邮箱',
    identity_verified TINYINT DEFAULT 0 COMMENT '身份已验证：1-是，0-否',
    verification_method VARCHAR(50) COMMENT '验证方式：ID_CARD,PHONE,EMAIL,FACE',
    request_type VARCHAR(50) NOT NULL COMMENT '请求类型：QUERY,CORRECT,DELETE,EXPORT,RESTRICT,OBJECT',
    request_reason TEXT COMMENT '请求原因',
    request_details TEXT COMMENT '请求详情（JSON格式）',
    request_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '请求状态：PENDING,PROCESSING,COMPLETED,REJECTED',
    request_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    process_time DATETIME COMMENT '处理时间',
    complete_time DATETIME COMMENT '完成时间',
    processor VARCHAR(50) COMMENT '处理人',
    process_result TEXT COMMENT '处理结果',
    reject_reason TEXT COMMENT '拒绝原因',
    response_deadline DATETIME COMMENT '响应截止时间',
    is_overdue TINYINT DEFAULT 0 COMMENT '是否超期：1-是，0-否',
    export_file_path VARCHAR(500) COMMENT '导出文件路径',
    export_format VARCHAR(20) COMMENT '导出格式：JSON,CSV,PDF',
    affected_data_count INT DEFAULT 0 COMMENT '影响数据条数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_request_no (request_no),
    INDEX idx_user_id (user_id),
    INDEX idx_request_type (request_type),
    INDEX idx_request_status (request_status),
    INDEX idx_request_time (request_time),
    INDEX idx_is_overdue (is_overdue)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人权利请求表';

-- 3. 创建个人信息影响评估表
CREATE TABLE IF NOT EXISTS privacy_impact_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评估ID',
    assessment_no VARCHAR(50) NOT NULL COMMENT '评估编号',
    assessment_name VARCHAR(200) NOT NULL COMMENT '评估名称',
    project_name VARCHAR(200) COMMENT '项目名称',
    project_type VARCHAR(50) COMMENT '项目类型：NEW_SYSTEM,SYSTEM_UPDATE,DATA_SHARING,NEW_BUSINESS',
    assessment_type VARCHAR(50) COMMENT '评估类型：PRELIMINARY,FORMAL,FOLLOW_UP',
    assessment_status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '评估状态：DRAFT,IN_PROGRESS,COMPLETED,APPROVED',
    risk_level VARCHAR(20) COMMENT '风险等级：LOW,MEDIUM,HIGH,CRITICAL',
    data_categories TEXT COMMENT '涉及数据类别（JSON数组）',
    data_volume BIGINT COMMENT '数据量',
    data_subjects TEXT COMMENT '数据主体类型',
    processing_purpose TEXT COMMENT '处理目的',
    processing_method TEXT COMMENT '处理方式',
    data_retention_period INT COMMENT '数据保留期（天）',
    third_party_involved TINYINT DEFAULT 0 COMMENT '是否涉及第三方：1-是，0-否',
    third_party_list TEXT COMMENT '第三方列表（JSON数组）',
    cross_border_transfer TINYINT DEFAULT 0 COMMENT '是否跨境传输：1-是，0-否',
    transfer_countries TEXT COMMENT '传输国家/地区',
    identified_risks TEXT COMMENT '识别的风险（JSON数组）',
    risk_mitigation_measures TEXT COMMENT '风险缓解措施（JSON数组）',
    residual_risk_level VARCHAR(20) COMMENT '残余风险等级',
    legal_basis TEXT COMMENT '法律依据',
    compliance_requirements TEXT COMMENT '合规要求',
    assessor VARCHAR(50) COMMENT '评估人',
    assessment_date DATE COMMENT '评估日期',
    reviewer VARCHAR(50) COMMENT '审核人',
    review_date DATE COMMENT '审核日期',
    review_comment TEXT COMMENT '审核意见',
    next_assessment_date DATE COMMENT '下次评估日期',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_assessment_no (assessment_no),
    INDEX idx_project_name (project_name),
    INDEX idx_assessment_status (assessment_status),
    INDEX idx_risk_level (risk_level),
    INDEX idx_assessment_date (assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人信息影响评估表';

-- 4. 创建个人信息处理活动记录表
CREATE TABLE IF NOT EXISTS processing_activity_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    activity_name VARCHAR(200) NOT NULL COMMENT '活动名称',
    activity_type VARCHAR(50) COMMENT '活动类型：COLLECTION,USE,STORAGE,SHARING,DELETION',
    business_purpose TEXT COMMENT '业务目的',
    legal_basis VARCHAR(100) COMMENT '法律依据',
    data_categories TEXT COMMENT '数据类别（JSON数组）',
    data_subjects TEXT COMMENT '数据主体类型',
    data_sources TEXT COMMENT '数据来源',
    data_recipients TEXT COMMENT '数据接收方',
    retention_period INT COMMENT '保留期限（天）',
    security_measures TEXT COMMENT '安全措施',
    cross_border TINYINT DEFAULT 0 COMMENT '是否跨境：1-是，0-否',
    transfer_mechanism VARCHAR(100) COMMENT '传输机制',
    responsible_person VARCHAR(50) COMMENT '责任人',
    department VARCHAR(100) COMMENT '所属部门',
    system_name VARCHAR(200) COMMENT '涉及系统',
    activity_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '活动状态：ACTIVE,SUSPENDED,TERMINATED',
    pia_required TINYINT DEFAULT 0 COMMENT '是否需要PIA：1-是，0-否',
    pia_id BIGINT COMMENT '关联的PIA评估ID',
    last_review_date DATE COMMENT '最后审查日期',
    next_review_date DATE COMMENT '下次审查日期',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_activity_type (activity_type),
    INDEX idx_activity_status (activity_status),
    INDEX idx_responsible_person (responsible_person),
    INDEX idx_pia_id (pia_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人信息处理活动记录表';

-- 5. 创建数据主体权利配置表
CREATE TABLE IF NOT EXISTS data_subject_rights_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    right_type VARCHAR(50) NOT NULL COMMENT '权利类型：QUERY,CORRECT,DELETE,EXPORT,RESTRICT,OBJECT',
    right_name VARCHAR(100) NOT NULL COMMENT '权利名称',
    right_description TEXT COMMENT '权利描述',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用：1-是，0-否',
    response_deadline_days INT DEFAULT 15 COMMENT '响应期限（天）',
    identity_verification_required TINYINT DEFAULT 1 COMMENT '是否需要身份验证：1-是，0-否',
    verification_methods TEXT COMMENT '验证方式（JSON数组）',
    auto_process_enabled TINYINT DEFAULT 0 COMMENT '是否启用自动处理：1-是，0-否',
    approval_required TINYINT DEFAULT 0 COMMENT '是否需要审批：1-是，0-否',
    notification_enabled TINYINT DEFAULT 1 COMMENT '是否启用通知：1-是，0-否',
    notification_template TEXT COMMENT '通知模板',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_right_type (right_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据主体权利配置表';

-- 6. 创建个人信息泄露事件表
CREATE TABLE IF NOT EXISTS data_breach_incident (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',
    incident_no VARCHAR(50) NOT NULL COMMENT '事件编号',
    incident_name VARCHAR(200) NOT NULL COMMENT '事件名称',
    incident_type VARCHAR(50) COMMENT '事件类型：UNAUTHORIZED_ACCESS,DATA_LOSS,DATA_THEFT,SYSTEM_BREACH',
    severity_level VARCHAR(20) COMMENT '严重程度：LOW,MEDIUM,HIGH,CRITICAL',
    discovery_time DATETIME COMMENT '发现时间',
    occurrence_time DATETIME COMMENT '发生时间',
    affected_data_categories TEXT COMMENT '受影响数据类别',
    affected_user_count INT COMMENT '受影响用户数',
    affected_user_list TEXT COMMENT '受影响用户列表',
    breach_description TEXT COMMENT '事件描述',
    breach_cause TEXT COMMENT '事件原因',
    immediate_actions TEXT COMMENT '即时措施',
    remediation_plan TEXT COMMENT '补救计划',
    notification_required TINYINT DEFAULT 0 COMMENT '是否需要通知：1-是，0-否',
    authority_notified TINYINT DEFAULT 0 COMMENT '是否已通知监管机构：1-是，0-否',
    authority_notification_time DATETIME COMMENT '监管机构通知时间',
    users_notified TINYINT DEFAULT 0 COMMENT '是否已通知用户：1-是，0-否',
    user_notification_time DATETIME COMMENT '用户通知时间',
    incident_status VARCHAR(20) DEFAULT 'OPEN' COMMENT '事件状态：OPEN,INVESTIGATING,RESOLVED,CLOSED',
    responsible_person VARCHAR(50) COMMENT '责任人',
    handler VARCHAR(50) COMMENT '处理人',
    resolution_time DATETIME COMMENT '解决时间',
    lessons_learned TEXT COMMENT '经验教训',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_incident_no (incident_no),
    INDEX idx_incident_type (incident_type),
    INDEX idx_severity_level (severity_level),
    INDEX idx_incident_status (incident_status),
    INDEX idx_discovery_time (discovery_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人信息泄露事件表';

-- 7. 插入默认权利配置
INSERT INTO data_subject_rights_config (right_type, right_name, right_description, response_deadline_days, verification_methods) VALUES
('QUERY', '查询权', '个人有权查询其个人信息的处理情况', 15, '["ID_CARD","PHONE","EMAIL"]'),
('CORRECT', '更正权', '个人有权更正不准确或不完整的个人信息', 15, '["ID_CARD","PHONE","EMAIL"]'),
('DELETE', '删除权', '个人有权要求删除其个人信息', 15, '["ID_CARD","PHONE","FACE"]'),
('EXPORT', '可携带权', '个人有权以结构化、常用格式导出其个人信息', 15, '["ID_CARD","PHONE","EMAIL"]'),
('RESTRICT', '限制处理权', '个人有权限制对其个人信息的处理', 15, '["ID_CARD","PHONE"]'),
('OBJECT', '反对权', '个人有权反对对其个人信息的处理', 15, '["ID_CARD","PHONE"]');

-- 8. 创建视图：同意状态概览
CREATE OR REPLACE VIEW v_consent_overview AS
SELECT 
    consent_type,
    consent_status,
    COUNT(*) as count,
    COUNT(CASE WHEN is_sensitive = 1 THEN 1 END) as sensitive_count
FROM consent_record
GROUP BY consent_type, consent_status;

-- 9. 创建视图：权利请求统计
CREATE OR REPLACE VIEW v_rights_request_stats AS
SELECT 
    request_type,
    request_status,
    COUNT(*) as count,
    AVG(TIMESTAMPDIFF(HOUR, request_time, complete_time)) as avg_processing_hours,
    COUNT(CASE WHEN is_overdue = 1 THEN 1 END) as overdue_count
FROM personal_rights_request
GROUP BY request_type, request_status;

-- 10. 创建视图：待处理权利请求
CREATE OR REPLACE VIEW v_pending_rights_requests AS
SELECT 
    id,
    request_no,
    user_name,
    request_type,
    request_time,
    response_deadline,
    DATEDIFF(response_deadline, NOW()) as days_remaining,
    CASE 
        WHEN response_deadline < NOW() THEN 1
        ELSE 0
    END as is_overdue
FROM personal_rights_request
WHERE request_status IN ('PENDING', 'PROCESSING')
ORDER BY response_deadline ASC;

-- 11. 创建视图：影响评估概览
CREATE OR REPLACE VIEW v_pia_overview AS
SELECT 
    assessment_status,
    risk_level,
    COUNT(*) as count,
    COUNT(CASE WHEN cross_border_transfer = 1 THEN 1 END) as cross_border_count,
    COUNT(CASE WHEN third_party_involved = 1 THEN 1 END) as third_party_count
FROM privacy_impact_assessment
GROUP BY assessment_status, risk_level;

-- 12. 创建存储过程：自动更新超期请求
DELIMITER //
CREATE PROCEDURE sp_update_overdue_requests()
BEGIN
    UPDATE personal_rights_request
    SET is_overdue = 1
    WHERE request_status IN ('PENDING', 'PROCESSING')
      AND response_deadline < NOW()
      AND is_overdue = 0;
    
    SELECT ROW_COUNT() as updated_count;
END //
DELIMITER ;

-- 13. 创建存储过程：生成请求编号
DELIMITER //
CREATE PROCEDURE sp_generate_request_no(OUT p_request_no VARCHAR(50))
BEGIN
    DECLARE v_date VARCHAR(8);
    DECLARE v_seq INT;
    
    SET v_date = DATE_FORMAT(NOW(), '%Y%m%d');
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(request_no, 10) AS UNSIGNED)), 0) + 1
    INTO v_seq
    FROM personal_rights_request
    WHERE request_no LIKE CONCAT('REQ', v_date, '%');
    
    SET p_request_no = CONCAT('REQ', v_date, LPAD(v_seq, 4, '0'));
END //
DELIMITER ;

-- 14. 创建触发器：自动设置响应截止时间
DELIMITER //
CREATE TRIGGER tr_set_response_deadline
BEFORE INSERT ON personal_rights_request
FOR EACH ROW
BEGIN
    DECLARE v_deadline_days INT DEFAULT 15;
    
    SELECT response_deadline_days INTO v_deadline_days
    FROM data_subject_rights_config
    WHERE right_type = NEW.request_type
    LIMIT 1;
    
    IF NEW.response_deadline IS NULL THEN
        SET NEW.response_deadline = DATE_ADD(NEW.request_time, INTERVAL v_deadline_days DAY);
    END IF;
END //
DELIMITER ;

-- 15. 创建触发器：同意撤回时更新状态
DELIMITER //
CREATE TRIGGER tr_consent_revoke
BEFORE UPDATE ON consent_record
FOR EACH ROW
BEGIN
    IF NEW.consent_status = 'REVOKED' AND OLD.consent_status != 'REVOKED' THEN
        SET NEW.revoke_time = NOW();
    END IF;
END //
DELIMITER ;

-- 16. 创建索引优化查询性能
CREATE INDEX idx_consent_user_type ON consent_record(user_id, consent_type);
CREATE INDEX idx_request_user_status ON personal_rights_request(user_id, request_status);
CREATE INDEX idx_pia_project_status ON privacy_impact_assessment(project_name, assessment_status);

-- 完成
SELECT '个人信息保护模块SQL脚本执行完成！' as message;
SELECT '已创建6个核心表、4个视图、2个存储过程、2个触发器' as summary;
