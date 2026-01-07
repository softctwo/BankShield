-- ============================================
-- 跨机构数据共享模块数据库设计
-- 创建日期: 2026-01-07
-- 功能: 支持多机构间的安全数据共享
-- ============================================

-- 1. 机构信息表
CREATE TABLE IF NOT EXISTS institution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '机构ID',
    institution_code VARCHAR(50) NOT NULL UNIQUE COMMENT '机构编码',
    institution_name VARCHAR(200) NOT NULL COMMENT '机构名称',
    institution_type VARCHAR(50) NOT NULL COMMENT '机构类型：BANK/INSURANCE/SECURITIES/TRUST',
    contact_person VARCHAR(100) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    address VARCHAR(500) COMMENT '机构地址',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/SUSPENDED',
    trust_level INT NOT NULL DEFAULT 1 COMMENT '信任级别：1-5',
    public_key TEXT COMMENT '机构公钥（用于数据加密）',
    certificate TEXT COMMENT '机构证书',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    remark TEXT COMMENT '备注',
    INDEX idx_institution_code (institution_code),
    INDEX idx_institution_type (institution_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构信息表';

-- 2. 数据共享协议表
CREATE TABLE IF NOT EXISTS data_sharing_agreement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '协议ID',
    agreement_code VARCHAR(50) NOT NULL UNIQUE COMMENT '协议编码',
    agreement_name VARCHAR(200) NOT NULL COMMENT '协议名称',
    provider_institution_id BIGINT NOT NULL COMMENT '数据提供方机构ID',
    consumer_institution_id BIGINT NOT NULL COMMENT '数据使用方机构ID',
    agreement_type VARCHAR(50) NOT NULL COMMENT '协议类型：BILATERAL/MULTILATERAL',
    data_scope TEXT NOT NULL COMMENT '数据范围（JSON格式）',
    sharing_purpose TEXT NOT NULL COMMENT '共享目的',
    validity_start_date DATE NOT NULL COMMENT '生效日期',
    validity_end_date DATE NOT NULL COMMENT '失效日期',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PENDING/APPROVED/REJECTED/ACTIVE/EXPIRED/TERMINATED',
    approval_status VARCHAR(20) COMMENT '审批状态：PENDING/APPROVED/REJECTED',
    approver VARCHAR(100) COMMENT '审批人',
    approval_time DATETIME COMMENT '审批时间',
    approval_comment TEXT COMMENT '审批意见',
    terms_and_conditions TEXT COMMENT '条款和条件',
    data_security_level VARCHAR(20) NOT NULL COMMENT '数据安全级别：PUBLIC/INTERNAL/CONFIDENTIAL/SECRET',
    encryption_required TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否需要加密：0-否，1-是',
    audit_required TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否需要审计：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    remark TEXT COMMENT '备注',
    INDEX idx_agreement_code (agreement_code),
    INDEX idx_provider_institution (provider_institution_id),
    INDEX idx_consumer_institution (consumer_institution_id),
    INDEX idx_status (status),
    INDEX idx_validity (validity_start_date, validity_end_date),
    FOREIGN KEY (provider_institution_id) REFERENCES institution(id),
    FOREIGN KEY (consumer_institution_id) REFERENCES institution(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据共享协议表';

-- 3. 数据共享请求表
CREATE TABLE IF NOT EXISTS data_sharing_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '请求ID',
    request_code VARCHAR(50) NOT NULL UNIQUE COMMENT '请求编码',
    agreement_id BIGINT NOT NULL COMMENT '关联协议ID',
    requester_institution_id BIGINT NOT NULL COMMENT '请求方机构ID',
    requester_user_id BIGINT NOT NULL COMMENT '请求用户ID',
    requester_user_name VARCHAR(100) NOT NULL COMMENT '请求用户名',
    request_type VARCHAR(50) NOT NULL COMMENT '请求类型：QUERY/EXPORT/SUBSCRIBE',
    data_category VARCHAR(100) NOT NULL COMMENT '数据类别',
    data_fields TEXT COMMENT '请求字段（JSON数组）',
    filter_conditions TEXT COMMENT '过滤条件（JSON格式）',
    request_purpose TEXT NOT NULL COMMENT '请求目的',
    request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED/PROCESSING/COMPLETED/FAILED',
    approval_status VARCHAR(20) COMMENT '审批状态：PENDING/APPROVED/REJECTED',
    approver VARCHAR(100) COMMENT '审批人',
    approval_time DATETIME COMMENT '审批时间',
    approval_comment TEXT COMMENT '审批意见',
    process_start_time DATETIME COMMENT '处理开始时间',
    process_end_time DATETIME COMMENT '处理结束时间',
    result_file_path VARCHAR(500) COMMENT '结果文件路径',
    result_record_count INT COMMENT '结果记录数',
    error_message TEXT COMMENT '错误信息',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark TEXT COMMENT '备注',
    INDEX idx_request_code (request_code),
    INDEX idx_agreement (agreement_id),
    INDEX idx_requester_institution (requester_institution_id),
    INDEX idx_requester_user (requester_user_id),
    INDEX idx_status (status),
    INDEX idx_request_time (request_time),
    FOREIGN KEY (agreement_id) REFERENCES data_sharing_agreement(id),
    FOREIGN KEY (requester_institution_id) REFERENCES institution(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据共享请求表';

-- 4. 数据共享日志表
CREATE TABLE IF NOT EXISTS data_sharing_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    request_id BIGINT NOT NULL COMMENT '请求ID',
    agreement_id BIGINT NOT NULL COMMENT '协议ID',
    provider_institution_id BIGINT NOT NULL COMMENT '提供方机构ID',
    consumer_institution_id BIGINT NOT NULL COMMENT '使用方机构ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：QUERY/EXPORT/DOWNLOAD/ACCESS',
    operation_user_id BIGINT COMMENT '操作用户ID',
    operation_user_name VARCHAR(100) COMMENT '操作用户名',
    data_category VARCHAR(100) COMMENT '数据类别',
    data_count INT COMMENT '数据条数',
    data_size BIGINT COMMENT '数据大小（字节）',
    access_ip VARCHAR(50) COMMENT '访问IP',
    access_location VARCHAR(200) COMMENT '访问地点',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operation_duration INT COMMENT '操作耗时（毫秒）',
    operation_status VARCHAR(20) NOT NULL COMMENT '操作状态：SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    data_hash VARCHAR(128) COMMENT '数据哈希值（用于完整性校验）',
    encryption_algorithm VARCHAR(50) COMMENT '加密算法',
    is_sensitive TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否敏感数据：0-否，1-是',
    security_level VARCHAR(20) COMMENT '安全级别',
    blockchain_tx_hash VARCHAR(128) COMMENT '区块链交易哈希',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_request (request_id),
    INDEX idx_agreement (agreement_id),
    INDEX idx_provider (provider_institution_id),
    INDEX idx_consumer (consumer_institution_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operation_user (operation_user_id),
    FOREIGN KEY (request_id) REFERENCES data_sharing_request(id),
    FOREIGN KEY (agreement_id) REFERENCES data_sharing_agreement(id),
    FOREIGN KEY (provider_institution_id) REFERENCES institution(id),
    FOREIGN KEY (consumer_institution_id) REFERENCES institution(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据共享日志表';

-- 5. 数据共享权限表
CREATE TABLE IF NOT EXISTS data_sharing_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    agreement_id BIGINT NOT NULL COMMENT '协议ID',
    institution_id BIGINT NOT NULL COMMENT '机构ID',
    user_id BIGINT COMMENT '用户ID（可选，为空表示机构级权限）',
    user_name VARCHAR(100) COMMENT '用户名',
    permission_type VARCHAR(50) NOT NULL COMMENT '权限类型：READ/WRITE/EXPORT/DELETE',
    data_scope TEXT COMMENT '数据范围（JSON格式）',
    field_permissions TEXT COMMENT '字段权限（JSON格式）',
    row_filter TEXT COMMENT '行级过滤条件',
    max_records_per_request INT COMMENT '单次请求最大记录数',
    max_requests_per_day INT COMMENT '每日最大请求次数',
    valid_from DATE NOT NULL COMMENT '生效日期',
    valid_to DATE NOT NULL COMMENT '失效日期',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/EXPIRED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    remark TEXT COMMENT '备注',
    INDEX idx_agreement (agreement_id),
    INDEX idx_institution (institution_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_validity (valid_from, valid_to),
    FOREIGN KEY (agreement_id) REFERENCES data_sharing_agreement(id),
    FOREIGN KEY (institution_id) REFERENCES institution(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据共享权限表';

-- 6. 数据共享配额表
CREATE TABLE IF NOT EXISTS data_sharing_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配额ID',
    institution_id BIGINT NOT NULL COMMENT '机构ID',
    agreement_id BIGINT COMMENT '协议ID（可选）',
    quota_type VARCHAR(50) NOT NULL COMMENT '配额类型：DAILY/MONTHLY/YEARLY/TOTAL',
    quota_limit BIGINT NOT NULL COMMENT '配额限制',
    quota_used BIGINT NOT NULL DEFAULT 0 COMMENT '已使用配额',
    quota_unit VARCHAR(20) NOT NULL COMMENT '配额单位：RECORDS/BYTES/REQUESTS',
    reset_cycle VARCHAR(20) COMMENT '重置周期：DAILY/MONTHLY/YEARLY',
    last_reset_time DATETIME COMMENT '上次重置时间',
    next_reset_time DATETIME COMMENT '下次重置时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/EXCEEDED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark TEXT COMMENT '备注',
    INDEX idx_institution (institution_id),
    INDEX idx_agreement (agreement_id),
    INDEX idx_quota_type (quota_type),
    INDEX idx_status (status),
    FOREIGN KEY (institution_id) REFERENCES institution(id),
    FOREIGN KEY (agreement_id) REFERENCES data_sharing_agreement(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据共享配额表';

-- 7. 数据共享审批流程表
CREATE TABLE IF NOT EXISTS data_sharing_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审批ID',
    request_id BIGINT COMMENT '请求ID',
    agreement_id BIGINT COMMENT '协议ID',
    approval_type VARCHAR(50) NOT NULL COMMENT '审批类型：AGREEMENT/REQUEST',
    approval_level INT NOT NULL COMMENT '审批级别：1-初审，2-复审，3-终审',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(100) NOT NULL COMMENT '审批人姓名',
    approver_role VARCHAR(50) NOT NULL COMMENT '审批人角色',
    approval_status VARCHAR(20) NOT NULL COMMENT '审批状态：PENDING/APPROVED/REJECTED/RETURNED',
    approval_time DATETIME COMMENT '审批时间',
    approval_comment TEXT COMMENT '审批意见',
    next_approver_id BIGINT COMMENT '下一审批人ID',
    is_final TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否最终审批：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_request (request_id),
    INDEX idx_agreement (agreement_id),
    INDEX idx_approver (approver_id),
    INDEX idx_approval_status (approval_status),
    INDEX idx_approval_time (approval_time),
    FOREIGN KEY (request_id) REFERENCES data_sharing_request(id),
    FOREIGN KEY (agreement_id) REFERENCES data_sharing_agreement(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据共享审批流程表';

-- 8. 数据共享统计表
CREATE TABLE IF NOT EXISTS data_sharing_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    institution_id BIGINT NOT NULL COMMENT '机构ID',
    agreement_id BIGINT COMMENT '协议ID',
    total_requests INT NOT NULL DEFAULT 0 COMMENT '总请求数',
    successful_requests INT NOT NULL DEFAULT 0 COMMENT '成功请求数',
    failed_requests INT NOT NULL DEFAULT 0 COMMENT '失败请求数',
    total_records BIGINT NOT NULL DEFAULT 0 COMMENT '总记录数',
    total_data_size BIGINT NOT NULL DEFAULT 0 COMMENT '总数据量（字节）',
    avg_response_time INT COMMENT '平均响应时间（毫秒）',
    max_response_time INT COMMENT '最大响应时间（毫秒）',
    min_response_time INT COMMENT '最小响应时间（毫秒）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat_date_institution (stat_date, institution_id, agreement_id),
    INDEX idx_stat_date (stat_date),
    INDEX idx_institution (institution_id),
    INDEX idx_agreement (agreement_id),
    FOREIGN KEY (institution_id) REFERENCES institution(id),
    FOREIGN KEY (agreement_id) REFERENCES data_sharing_agreement(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据共享统计表';

-- 初始化数据：插入本机构信息
INSERT INTO institution (institution_code, institution_name, institution_type, status, trust_level, create_by, remark)
VALUES ('INST_LOCAL', '本机构', 'BANK', 'ACTIVE', 5, 'system', '本机构信息');

-- 创建视图：活跃协议视图
CREATE OR REPLACE VIEW v_active_agreements AS
SELECT 
    a.id,
    a.agreement_code,
    a.agreement_name,
    p.institution_name AS provider_name,
    c.institution_name AS consumer_name,
    a.validity_start_date,
    a.validity_end_date,
    a.status,
    a.data_security_level
FROM data_sharing_agreement a
JOIN institution p ON a.provider_institution_id = p.id
JOIN institution c ON a.consumer_institution_id = c.id
WHERE a.status = 'ACTIVE'
AND CURDATE() BETWEEN a.validity_start_date AND a.validity_end_date;

-- 创建视图：待审批请求视图
CREATE OR REPLACE VIEW v_pending_requests AS
SELECT 
    r.id,
    r.request_code,
    r.request_type,
    i.institution_name AS requester_institution,
    r.requester_user_name,
    r.request_time,
    r.status,
    a.agreement_name
FROM data_sharing_request r
JOIN institution i ON r.requester_institution_id = i.id
JOIN data_sharing_agreement a ON r.agreement_id = a.id
WHERE r.status = 'PENDING';

-- 数据库初始化完成
SELECT '跨机构数据共享模块数据库初始化完成' AS status;
