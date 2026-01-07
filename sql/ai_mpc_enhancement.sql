-- ============================================
-- AI智能识别增强 + 多方计算协作数据库设计
-- 创建日期: 2026-01-07
-- 功能: AI模型训练、推理和MPC协作
-- ============================================

-- ==================== AI智能识别增强 ====================

-- 1. AI模型训练任务表
CREATE TABLE IF NOT EXISTS ai_training_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_code VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编码',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    model_type VARCHAR(50) NOT NULL COMMENT '模型类型：CLASSIFICATION/REGRESSION/CLUSTERING/ANOMALY_DETECTION',
    algorithm VARCHAR(50) NOT NULL COMMENT '算法：XGBOOST/RANDOM_FOREST/LSTM/TRANSFORMER/ISOLATION_FOREST',
    dataset_id BIGINT COMMENT '数据集ID',
    dataset_path VARCHAR(500) COMMENT '数据集路径',
    feature_columns TEXT COMMENT '特征列（JSON数组）',
    target_column VARCHAR(100) COMMENT '目标列',
    training_params TEXT COMMENT '训练参数（JSON格式）',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/RUNNING/COMPLETED/FAILED',
    progress INT NOT NULL DEFAULT 0 COMMENT '进度（0-100）',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '耗时（秒）',
    model_id BIGINT COMMENT '生成的模型ID',
    accuracy DECIMAL(5,4) COMMENT '准确率',
    precision_score DECIMAL(5,4) COMMENT '精确率',
    recall_score DECIMAL(5,4) COMMENT '召回率',
    f1_score DECIMAL(5,4) COMMENT 'F1分数',
    auc_score DECIMAL(5,4) COMMENT 'AUC分数',
    training_log TEXT COMMENT '训练日志',
    error_message TEXT COMMENT '错误信息',
    creator_id BIGINT COMMENT '创建人ID',
    creator_name VARCHAR(100) COMMENT '创建人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_code (task_code),
    INDEX idx_model_type (model_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型训练任务表';

-- 2. AI模型版本表（增强版）
CREATE TABLE IF NOT EXISTS ai_model_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    version VARCHAR(20) NOT NULL COMMENT '版本号',
    training_task_id BIGINT COMMENT '训练任务ID',
    model_path VARCHAR(500) NOT NULL COMMENT '模型文件路径',
    model_size BIGINT COMMENT '模型大小（字节）',
    model_format VARCHAR(20) COMMENT '模型格式：ONNX/PMML/H5/PKL',
    feature_importance TEXT COMMENT '特征重要性（JSON格式）',
    hyperparameters TEXT COMMENT '超参数（JSON格式）',
    performance_metrics TEXT COMMENT '性能指标（JSON格式）',
    validation_result TEXT COMMENT '验证结果（JSON格式）',
    is_deployed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已部署：0-否，1-是',
    deploy_time DATETIME COMMENT '部署时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DEPRECATED/ARCHIVED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark TEXT COMMENT '备注',
    INDEX idx_model_id (model_id),
    INDEX idx_version (version),
    INDEX idx_is_deployed (is_deployed),
    INDEX idx_status (status),
    FOREIGN KEY (training_task_id) REFERENCES ai_training_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型版本表';

-- 3. AI推理任务表
CREATE TABLE IF NOT EXISTS ai_inference_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '推理ID',
    task_code VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编码',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    model_version_id BIGINT COMMENT '模型版本ID',
    inference_type VARCHAR(50) NOT NULL COMMENT '推理类型：REALTIME/BATCH/STREAMING',
    input_data TEXT COMMENT '输入数据（JSON格式）',
    input_file_path VARCHAR(500) COMMENT '输入文件路径',
    output_data TEXT COMMENT '输出结果（JSON格式）',
    output_file_path VARCHAR(500) COMMENT '输出文件路径',
    prediction_result TEXT COMMENT '预测结果（JSON格式）',
    confidence_score DECIMAL(5,4) COMMENT '置信度',
    inference_time INT COMMENT '推理耗时（毫秒）',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/RUNNING/COMPLETED/FAILED',
    error_message TEXT COMMENT '错误信息',
    request_user_id BIGINT COMMENT '请求用户ID',
    request_user_name VARCHAR(100) COMMENT '请求用户名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_code (task_code),
    INDEX idx_model_id (model_id),
    INDEX idx_inference_type (inference_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (model_id) REFERENCES ai_model(id),
    FOREIGN KEY (model_version_id) REFERENCES ai_model_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推理任务表';

-- 4. AI特征工程表
CREATE TABLE IF NOT EXISTS ai_feature_engineering (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '特征ID',
    feature_name VARCHAR(100) NOT NULL COMMENT '特征名称',
    feature_type VARCHAR(50) NOT NULL COMMENT '特征类型：NUMERICAL/CATEGORICAL/TEXT/DATETIME',
    source_column VARCHAR(100) COMMENT '源列名',
    transformation VARCHAR(50) COMMENT '转换方法：NORMALIZE/STANDARDIZE/ONE_HOT/EMBEDDING',
    transformation_params TEXT COMMENT '转换参数（JSON格式）',
    feature_importance DECIMAL(5,4) COMMENT '特征重要性',
    is_selected TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否选中：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_feature_name (feature_name),
    INDEX idx_feature_type (feature_type),
    INDEX idx_is_selected (is_selected)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI特征工程表';

-- 5. AI模型监控表
CREATE TABLE IF NOT EXISTS ai_model_monitoring (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '监控ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    model_version_id BIGINT COMMENT '模型版本ID',
    monitor_date DATE NOT NULL COMMENT '监控日期',
    total_predictions INT NOT NULL DEFAULT 0 COMMENT '总预测数',
    successful_predictions INT NOT NULL DEFAULT 0 COMMENT '成功预测数',
    failed_predictions INT NOT NULL DEFAULT 0 COMMENT '失败预测数',
    avg_confidence DECIMAL(5,4) COMMENT '平均置信度',
    avg_inference_time INT COMMENT '平均推理时间（毫秒）',
    accuracy_drift DECIMAL(5,4) COMMENT '准确率漂移',
    data_drift_score DECIMAL(5,4) COMMENT '数据漂移分数',
    concept_drift_detected TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否检测到概念漂移',
    alert_triggered TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否触发告警',
    alert_reason TEXT COMMENT '告警原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_model_date (model_id, model_version_id, monitor_date),
    INDEX idx_model_id (model_id),
    INDEX idx_monitor_date (monitor_date),
    INDEX idx_alert_triggered (alert_triggered),
    FOREIGN KEY (model_id) REFERENCES ai_model(id),
    FOREIGN KEY (model_version_id) REFERENCES ai_model_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型监控表';

-- ==================== 多方计算协作 ====================

-- 6. MPC协作任务表
CREATE TABLE IF NOT EXISTS mpc_collaboration_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_code VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编码',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：PSI/SECURE_SUM/JOINT_QUERY/FEDERATED_LEARNING',
    protocol VARCHAR(50) NOT NULL COMMENT '协议：SECRET_SHARING/HOMOMORPHIC_ENCRYPTION/GARBLED_CIRCUIT',
    initiator_party_id BIGINT NOT NULL COMMENT '发起方ID',
    participant_parties TEXT NOT NULL COMMENT '参与方列表（JSON数组）',
    total_parties INT NOT NULL COMMENT '总参与方数',
    confirmed_parties INT NOT NULL DEFAULT 0 COMMENT '已确认参与方数',
    task_params TEXT COMMENT '任务参数（JSON格式）',
    data_schema TEXT COMMENT '数据模式（JSON格式）',
    privacy_budget DECIMAL(10,6) COMMENT '隐私预算（差分隐私）',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PREPARING/COMPUTING/COMPLETED/FAILED/CANCELLED',
    progress INT NOT NULL DEFAULT 0 COMMENT '进度（0-100）',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '耗时（秒）',
    result_summary TEXT COMMENT '结果摘要（JSON格式）',
    result_file_path VARCHAR(500) COMMENT '结果文件路径',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_code (task_code),
    INDEX idx_task_type (task_type),
    INDEX idx_initiator_party (initiator_party_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MPC协作任务表';

-- 7. MPC参与方表
CREATE TABLE IF NOT EXISTS mpc_party_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参与方ID',
    party_code VARCHAR(50) NOT NULL UNIQUE COMMENT '参与方编码',
    party_name VARCHAR(200) NOT NULL COMMENT '参与方名称',
    party_type VARCHAR(50) NOT NULL COMMENT '参与方类型：BANK/INSURANCE/SECURITIES/GOVERNMENT',
    organization VARCHAR(200) COMMENT '所属组织',
    public_key TEXT COMMENT '公钥',
    certificate TEXT COMMENT '证书',
    endpoint_url VARCHAR(500) COMMENT '端点URL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/SUSPENDED',
    trust_level INT NOT NULL DEFAULT 1 COMMENT '信任级别：1-5',
    total_tasks INT NOT NULL DEFAULT 0 COMMENT '总任务数',
    successful_tasks INT NOT NULL DEFAULT 0 COMMENT '成功任务数',
    failed_tasks INT NOT NULL DEFAULT 0 COMMENT '失败任务数',
    last_active_time DATETIME COMMENT '最后活跃时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark TEXT COMMENT '备注',
    INDEX idx_party_code (party_code),
    INDEX idx_party_type (party_type),
    INDEX idx_status (status),
    INDEX idx_trust_level (trust_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MPC参与方表';

-- 8. MPC任务参与记录表
CREATE TABLE IF NOT EXISTS mpc_task_participation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    party_id BIGINT NOT NULL COMMENT '参与方ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：INITIATOR/PARTICIPANT',
    data_contribution TEXT COMMENT '数据贡献描述',
    data_size BIGINT COMMENT '数据大小（字节）',
    computation_contribution INT COMMENT '计算贡献（CPU秒）',
    status VARCHAR(20) NOT NULL DEFAULT 'INVITED' COMMENT '状态：INVITED/CONFIRMED/PREPARING/COMPUTING/COMPLETED/FAILED',
    join_time DATETIME COMMENT '加入时间',
    complete_time DATETIME COMMENT '完成时间',
    result_received TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否收到结果',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_party_id (party_id),
    INDEX idx_role (role),
    INDEX idx_status (status),
    FOREIGN KEY (task_id) REFERENCES mpc_collaboration_task(id),
    FOREIGN KEY (party_id) REFERENCES mpc_party_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MPC任务参与记录表';

-- 9. MPC计算日志表
CREATE TABLE IF NOT EXISTS mpc_computation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    party_id BIGINT COMMENT '参与方ID',
    computation_phase VARCHAR(50) NOT NULL COMMENT '计算阶段：INIT/SHARE/COMPUTE/RECONSTRUCT',
    operation VARCHAR(100) COMMENT '操作',
    input_data_hash VARCHAR(128) COMMENT '输入数据哈希',
    output_data_hash VARCHAR(128) COMMENT '输出数据哈希',
    computation_time INT COMMENT '计算时间（毫秒）',
    memory_usage BIGINT COMMENT '内存使用（字节）',
    network_traffic BIGINT COMMENT '网络流量（字节）',
    status VARCHAR(20) NOT NULL COMMENT '状态：SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_party_id (party_id),
    INDEX idx_computation_phase (computation_phase),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (task_id) REFERENCES mpc_collaboration_task(id),
    FOREIGN KEY (party_id) REFERENCES mpc_party_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MPC计算日志表';

-- 10. MPC协作协议表
CREATE TABLE IF NOT EXISTS mpc_collaboration_protocol (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '协议ID',
    protocol_code VARCHAR(50) NOT NULL UNIQUE COMMENT '协议编码',
    protocol_name VARCHAR(200) NOT NULL COMMENT '协议名称',
    protocol_type VARCHAR(50) NOT NULL COMMENT '协议类型：PSI/SECURE_SUM/JOINT_QUERY',
    protocol_version VARCHAR(20) NOT NULL COMMENT '协议版本',
    cryptographic_scheme VARCHAR(50) COMMENT '密码学方案',
    security_level VARCHAR(20) NOT NULL COMMENT '安全级别：SEMI_HONEST/MALICIOUS',
    communication_rounds INT COMMENT '通信轮数',
    computation_complexity VARCHAR(100) COMMENT '计算复杂度',
    communication_complexity VARCHAR(100) COMMENT '通信复杂度',
    protocol_description TEXT COMMENT '协议描述',
    implementation_class VARCHAR(200) COMMENT '实现类',
    is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_protocol_code (protocol_code),
    INDEX idx_protocol_type (protocol_type),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MPC协作协议表';

-- 创建视图：AI模型性能视图
CREATE OR REPLACE VIEW v_ai_model_performance AS
SELECT 
    m.id as model_id,
    m.model_name,
    m.model_type,
    mv.version,
    mv.is_deployed,
    JSON_EXTRACT(mv.performance_metrics, '$.accuracy') as accuracy,
    JSON_EXTRACT(mv.performance_metrics, '$.f1_score') as f1_score,
    mm.avg_confidence,
    mm.avg_inference_time,
    mm.data_drift_score,
    mm.monitor_date
FROM ai_model m
JOIN ai_model_version mv ON m.id = mv.model_id
LEFT JOIN ai_model_monitoring mm ON mv.id = mm.model_version_id
WHERE mv.status = 'ACTIVE';

-- 创建视图：MPC任务统计视图
CREATE OR REPLACE VIEW v_mpc_task_statistics AS
SELECT 
    t.id as task_id,
    t.task_code,
    t.task_name,
    t.task_type,
    t.status,
    t.total_parties,
    t.confirmed_parties,
    COUNT(p.id) as actual_participants,
    SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_participants,
    t.start_time,
    t.end_time,
    t.duration
FROM mpc_collaboration_task t
LEFT JOIN mpc_task_participation p ON t.id = p.task_id
GROUP BY t.id;

-- 初始化本地参与方信息
INSERT INTO mpc_party_info (party_code, party_name, party_type, status, trust_level, remark)
VALUES ('PARTY_LOCAL', '本机构', 'BANK', 'ACTIVE', 5, '本机构参与方信息');

-- 初始化MPC协议
INSERT INTO mpc_collaboration_protocol (protocol_code, protocol_name, protocol_type, protocol_version, security_level, protocol_description, is_enabled)
VALUES 
('PSI_V1', '隐私集合求交协议', 'PSI', '1.0', 'SEMI_HONEST', '基于ECDH的隐私集合求交协议', 1),
('SECURE_SUM_V1', '安全求和协议', 'SECURE_SUM', '1.0', 'SEMI_HONEST', '基于秘密分享的安全求和协议', 1),
('JOINT_QUERY_V1', '联合查询协议', 'JOINT_QUERY', '1.0', 'SEMI_HONEST', '基于同态加密的联合查询协议', 1);

-- 数据库初始化完成
SELECT 'AI智能识别增强 + 多方计算协作数据库初始化完成' AS status;
