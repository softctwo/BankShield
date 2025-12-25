-- BankShield AI智能安全分析模块数据库脚本
-- 创建AI相关表结构

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS bankshield_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bankshield_ai;

-- AI特征表
CREATE TABLE IF NOT EXISTS ai_feature (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  feature_vector TEXT NOT NULL COMMENT '特征向量JSON',
  behavior_type VARCHAR(50) COMMENT '行为类型',
  user_id BIGINT COMMENT '用户ID',
  session_id VARCHAR(100) COMMENT '会话ID',
  ip_address VARCHAR(45) COMMENT 'IP地址',
  location VARCHAR(200) COMMENT '地理位置',
  anomaly_score DECIMAL(5,4) COMMENT '异常分数',
  is_anomaly BOOLEAN DEFAULT FALSE COMMENT '是否异常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_behavior_type (behavior_type),
  INDEX idx_create_time (create_time),
  INDEX idx_anomaly (is_anomaly)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI特征表';

-- AI模型表
CREATE TABLE IF NOT EXISTS ai_model (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
  model_type VARCHAR(50) COMMENT '模型类型：异常检测/告警分类/预测',
  model_subtype VARCHAR(50) COMMENT '模型子类型',
  model_path VARCHAR(500) COMMENT '模型文件路径',
  model_version VARCHAR(20) COMMENT '模型版本',
  accuracy DECIMAL(5,4) COMMENT '准确率',
  precision_rate DECIMAL(5,4) COMMENT '精确率',
  recall_rate DECIMAL(5,4) COMMENT '召回率',
  f1_score DECIMAL(5,4) COMMENT 'F1分数',
  training_samples INT COMMENT '训练样本数量',
  feature_count INT COMMENT '特征数量',
  algorithm VARCHAR(100) COMMENT '算法名称',
  hyperparameters TEXT COMMENT '超参数JSON',
  status VARCHAR(20) DEFAULT 'active' COMMENT '模型状态：active/inactive/training',
  description TEXT COMMENT '模型描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  training_time DATETIME COMMENT '训练完成时间',
  last_used_time DATETIME COMMENT '最后使用时间',
  usage_count INT DEFAULT 0 COMMENT '使用次数',
  deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
  UNIQUE KEY uk_model_name (model_name),
  INDEX idx_model_type (model_type),
  INDEX idx_status (status),
  INDEX idx_accuracy (accuracy)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

-- 用户行为模式表
CREATE TABLE IF NOT EXISTS behavior_pattern (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(100) COMMENT '用户名',
  pattern_type VARCHAR(50) COMMENT '模式类型：登录时间/操作频率/数据访问',
  pattern_data TEXT COMMENT '模式数据JSON',
  confidence DECIMAL(5,4) COMMENT '模式置信度',
  strength DECIMAL(5,4) COMMENT '模式强度',
  sample_count INT DEFAULT 0 COMMENT '样本数量',
  first_seen_time DATETIME COMMENT '首次出现时间',
  last_seen_time DATETIME COMMENT '最后出现时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
  deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
  INDEX idx_user_id (user_id),
  INDEX idx_pattern_type (pattern_type),
  INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为模式表';

-- AI预测结果表
CREATE TABLE IF NOT EXISTS ai_prediction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_id BIGINT COMMENT '模型ID',
  model_name VARCHAR(100) COMMENT '模型名称',
  prediction_type VARCHAR(50) COMMENT '预测类型：异常检测/告警分类/资源预测',
  input_data TEXT COMMENT '输入数据',
  prediction_result TEXT COMMENT '预测结果',
  confidence DECIMAL(5,4) COMMENT '预测概率/置信度',
  prediction_score DECIMAL(5,4) COMMENT '预测分数',
  prediction_label VARCHAR(100) COMMENT '预测标签',
  user_id BIGINT COMMENT '用户ID',
  resource_type VARCHAR(50) COMMENT '资源类型（资源预测时使用）',
  prediction_range VARCHAR(100) COMMENT '预测时间范围（资源预测时使用）',
  start_time DATETIME COMMENT '开始时间',
  end_time DATETIME COMMENT '结束时间',
  prediction_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预测时间',
  is_accurate BOOLEAN COMMENT '是否准确',
  validation_time DATETIME COMMENT '验证时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_model_id (model_id),
  INDEX idx_user_id (user_id),
  INDEX idx_prediction_type (prediction_type),
  INDEX idx_prediction_time (prediction_time),
  INDEX idx_is_accurate (is_accurate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI预测结果表';

-- 异常行为统计表（用于缓存统计结果）
CREATE TABLE IF NOT EXISTS anomaly_statistics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户ID',
  statistics_date DATE COMMENT '统计日期',
  total_behaviors BIGINT DEFAULT 0 COMMENT '总行为数量',
  anomaly_behaviors BIGINT DEFAULT 0 COMMENT '异常行为数量',
  anomaly_rate DECIMAL(5,4) DEFAULT 0 COMMENT '异常率',
  login_anomalies BIGINT DEFAULT 0 COMMENT '登录异常数量',
  location_anomalies BIGINT DEFAULT 0 COMMENT '位置异常数量',
  frequency_anomalies BIGINT DEFAULT 0 COMMENT '频率异常数量',
  permission_anomalies BIGINT DEFAULT 0 COMMENT '权限异常数量',
  data_access_anomalies BIGINT DEFAULT 0 COMMENT '数据访问异常数量',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_date (user_id, statistics_date),
  INDEX idx_statistics_date (statistics_date),
  INDEX idx_anomaly_rate (anomaly_rate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常行为统计表';

-- AI模型训练日志表
CREATE TABLE IF NOT EXISTS ai_training_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_id BIGINT COMMENT '模型ID',
  model_name VARCHAR(100) COMMENT '模型名称',
  training_type VARCHAR(50) COMMENT '训练类型：增量/全量',
  training_data_count INT COMMENT '训练数据数量',
  validation_data_count INT COMMENT '验证数据数量',
  training_start_time DATETIME COMMENT '训练开始时间',
  training_end_time DATETIME COMMENT '训练结束时间',
  training_duration INT COMMENT '训练时长（秒）',
  final_accuracy DECIMAL(5,4) COMMENT '最终准确率',
  final_loss DECIMAL(8,6) COMMENT '最终损失值',
  training_status VARCHAR(20) COMMENT '训练状态：running/success/failed',
  error_message TEXT COMMENT '错误信息',
  training_parameters TEXT COMMENT '训练参数JSON',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_model_id (model_id),
  INDEX idx_training_status (training_status),
  INDEX idx_training_start_time (training_start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型训练日志表';

-- 插入默认AI模型数据
INSERT INTO ai_model (model_name, model_type, model_subtype, model_path, model_version, accuracy, algorithm, description, status) VALUES
('IsolationForest_AnomalyDetection', '异常检测', '无监督学习', 'classpath:models/anomaly-detection/isolation-forest', '1.0.0', 0.9500, 'Isolation Forest', '基于隔离森林算法的异常行为检测模型', 'active'),
('RandomForest_AlertClassification', '告警分类', '监督学习', 'classpath:models/alert-classification/random-forest', '1.0.0', 0.9600, 'Random Forest', '基于随机森林算法的告警智能分类模型', 'active'),
('LSTM_ResourcePrediction', '资源预测', '时间序列', 'classpath:models/prediction/lstm', '1.0.0', 0.9200, 'LSTM', '基于LSTM的资源使用趋势预测模型', 'active'),
('LOF_AnomalyDetection', '异常检测', '无监督学习', 'classpath:models/anomaly-detection/lof', '1.0.0', 0.9300, 'Local Outlier Factor', '基于局部异常因子的异常检测模型', 'active');

-- 创建索引优化查询性能
CREATE INDEX idx_feature_user_time ON ai_feature(user_id, create_time);
CREATE INDEX idx_feature_anomaly_score ON ai_feature(anomaly_score);
CREATE INDEX idx_pattern_user_active ON behavior_pattern(user_id, is_active);
CREATE INDEX idx_prediction_model_time ON ai_prediction(model_id, prediction_time);
CREATE INDEX idx_statistics_user_anomaly ON anomaly_statistics(user_id, anomaly_rate);

-- 创建视图：用户异常行为概览
CREATE OR REPLACE VIEW user_anomaly_overview AS
SELECT 
    u.user_id,
    u.username,
    COUNT(DISTINCT f.id) as total_behaviors,
    COUNT(DISTINCT CASE WHEN f.is_anomaly = 1 THEN f.id END) as anomaly_behaviors,
    ROUND(COUNT(DISTINCT CASE WHEN f.is_anomaly = 1 THEN f.id END) * 100.0 / COUNT(DISTINCT f.id), 2) as anomaly_rate,
    MAX(f.anomaly_score) as max_anomaly_score,
    AVG(f.anomaly_score) as avg_anomaly_score,
    MAX(f.create_time) as last_behavior_time,
    MIN(f.create_time) as first_behavior_time
FROM 
    (SELECT DISTINCT user_id, username FROM behavior_pattern) u
LEFT JOIN 
    ai_feature f ON u.user_id = f.user_id
GROUP BY 
    u.user_id, u.username
HAVING 
    COUNT(DISTINCT f.id) > 0;

-- 创建视图：AI模型性能统计
CREATE OR REPLACE VIEW ai_model_performance AS
SELECT 
    model_type,
    COUNT(*) as model_count,
    ROUND(AVG(accuracy), 4) as avg_accuracy,
    MAX(accuracy) as max_accuracy,
    MIN(accuracy) as min_accuracy,
    SUM(usage_count) as total_usage,
    COUNT(CASE WHEN status = 'active' THEN 1 END) as active_models
FROM 
    ai_model
WHERE 
    deleted = 0
GROUP BY 
    model_type;

-- 创建视图：异常趋势分析
CREATE OR REPLACE VIEW anomaly_trend_analysis AS
SELECT 
    DATE(create_time) as date,
    behavior_type,
    COUNT(*) as total_count,
    COUNT(CASE WHEN is_anomaly = 1 THEN 1 END) as anomaly_count,
    ROUND(COUNT(CASE WHEN is_anomaly = 1 THEN 1 END) * 100.0 / COUNT(*), 2) as anomaly_rate,
    AVG(anomaly_score) as avg_anomaly_score
FROM 
    ai_feature
WHERE 
    create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY 
    DATE(create_time), behavior_type
ORDER BY 
    date DESC, anomaly_rate DESC;

-- 插入测试数据
INSERT INTO ai_feature (feature_vector, behavior_type, user_id, session_id, ip_address, location, anomaly_score, is_anomaly) VALUES
('[-0.5, 0.2, 0.8, 0.1, 0.3, 0.7, 0.4, 0.6, 0.9, 0.2, 0.5, 0.8, 0.3, 0.6, 0.1]', 'login', 1, 'session_001', '192.168.1.100', '北京', 0.8523, 1),
('[0.1, 0.5, 0.2, 0.8, 0.6, 0.3, 0.9, 0.4, 0.7, 0.1, 0.8, 0.2, 0.5, 0.9, 0.3]', 'access', 2, 'session_002', '192.168.1.101', '上海', 0.2341, 0),
('[0.8, 0.1, 0.6, 0.3, 0.9, 0.2, 0.5, 0.7, 0.4, 0.8, 0.6, 0.9, 0.1, 0.3, 0.5]', 'download', 3, 'session_003', '192.168.1.102', '广州', 0.7562, 1);

INSERT INTO behavior_pattern (user_id, username, pattern_type, pattern_data, confidence, strength, sample_count, first_seen_time, last_seen_time, is_active) VALUES
(1, 'admin', 'login', '{\"normal_hours\": [9, 10, 11, 14, 15, 16], \"avg_frequency\": 5}', 0.85, 0.90, 100, '2024-01-01 09:00:00', '2024-12-24 16:30:00', 1),
(2, 'user1', 'access', '{\"normal_resources\": [\"data\", \"report\"], \"avg_frequency\": 20}', 0.78, 0.82, 150, '2024-01-02 10:00:00', '2024-12-24 14:20:00', 1),
(3, 'user2', 'download', '{\"normal_size\": 10485760, \"avg_frequency\": 3}', 0.72, 0.75, 80, '2024-01-03 11:00:00', '2024-12-24 15:45:00', 1);

INSERT INTO ai_prediction (model_id, model_name, prediction_type, input_data, prediction_result, confidence, prediction_score, prediction_label, user_id) VALUES
(1, 'IsolationForest_AnomalyDetection', '异常检测', '{\"user_id\": 1, \"behavior\": \"login\"}', '{\"is_anomaly\": true, \"score\": 0.8523}', 0.8523, 0.8523, '异常', 1),
(2, 'RandomForest_AlertClassification', '告警分类', '{\"alert_type\": \"login_failure\", \"count\": 5}', '{\"classification\": \"real_threat\", \"confidence\": 0.92}', 0.9200, 0.9200, '真正威胁', 1);

-- 创建存储过程：更新异常行为统计
DELIMITER //
CREATE PROCEDURE UpdateAnomalyStatistics(IN p_user_id BIGINT, IN p_date DATE)
BEGIN
    DECLARE v_total_behaviors BIGINT;
    DECLARE v_anomaly_behaviors BIGINT;
    DECLARE v_login_anomalies BIGINT;
    DECLARE v_location_anomalies BIGINT;
    DECLARE v_frequency_anomalies BIGINT;
    DECLARE v_permission_anomalies BIGINT;
    DECLARE v_data_access_anomalies BIGINT;
    
    -- 计算统计数据
    SELECT COUNT(*) INTO v_total_behaviors
    FROM ai_feature
    WHERE user_id = p_user_id AND DATE(create_time) = p_date;
    
    SELECT COUNT(*) INTO v_anomaly_behaviors
    FROM ai_feature
    WHERE user_id = p_user_id AND DATE(create_time) = p_date AND is_anomaly = 1;
    
    SELECT COUNT(*) INTO v_login_anomalies
    FROM ai_feature
    WHERE user_id = p_user_id AND DATE(create_time) = p_date AND is_anomaly = 1 AND behavior_type = 'login';
    
    SELECT COUNT(*) INTO v_location_anomalies
    FROM ai_feature
    WHERE user_id = p_user_id AND DATE(create_time) = p_date AND is_anomaly = 1 AND behavior_type = 'access';
    
    SELECT COUNT(*) INTO v_frequency_anomalies
    FROM ai_feature
    WHERE user_id = p_user_id AND DATE(create_time) = p_date AND is_anomaly = 1 AND behavior_type = 'operation';
    
    SELECT COUNT(*) INTO v_permission_anomalies
    FROM ai_feature
    WHERE user_id = p_user_id AND DATE(create_time) = p_date AND is_anomaly = 1 AND behavior_type = 'permission';
    
    SELECT COUNT(*) INTO v_data_access_anomalies
    FROM ai_feature
    WHERE user_id = p_user_id AND DATE(create_time) = p_date AND is_anomaly = 1 AND behavior_type = 'download';
    
    -- 更新或插入统计记录
    INSERT INTO anomaly_statistics (
        user_id, statistics_date, total_behaviors, anomaly_behaviors, 
        login_anomalies, location_anomalies, frequency_anomalies, 
        permission_anomalies, data_access_anomalies
    ) VALUES (
        p_user_id, p_date, v_total_behaviors, v_anomaly_behaviors,
        v_login_anomalies, v_location_anomalies, v_frequency_anomalies,
        v_permission_anomalies, v_data_access_anomalies
    ) ON DUPLICATE KEY UPDATE
        total_behaviors = v_total_behaviors,
        anomaly_behaviors = v_anomaly_behaviors,
        anomaly_rate = CASE WHEN v_total_behaviors > 0 THEN v_anomaly_behaviors * 100.0 / v_total_behaviors ELSE 0 END,
        login_anomalies = v_login_anomalies,
        location_anomalies = v_location_anomalies,
        frequency_anomalies = v_frequency_anomalies,
        permission_anomalies = v_permission_anomalies,
        data_access_anomalies = v_data_access_anomalies,
        update_time = NOW();
END //
DELIMITER ;

-- 创建事件：每日更新异常统计
CREATE EVENT IF NOT EXISTS daily_anomaly_statistics
ON SCHEDULE EVERY 1 DAY
STARTS '2024-01-01 02:00:00'
DO
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_user_id BIGINT;
    DECLARE v_date DATE;
    
    DECLARE user_cursor CURSOR FOR 
        SELECT DISTINCT user_id, DATE(create_time) 
        FROM ai_feature 
        WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 1 DAY);
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    SET v_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY);
    
    OPEN user_cursor;
    read_loop: LOOP
        FETCH user_cursor INTO v_user_id, v_date;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        CALL UpdateAnomalyStatistics(v_user_id, v_date);
    END LOOP;
    
    CLOSE user_cursor;
END;