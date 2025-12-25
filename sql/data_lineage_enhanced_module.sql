-- BankShield 数据血缘增强模块数据库表结构
-- 支持自动化血缘发现、影响分析、数据地图

USE bankshield;

-- 数据流关系表（详细记录数据流转关系）
CREATE TABLE IF NOT EXISTS data_flow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_table VARCHAR(200) NOT NULL COMMENT '源表名',
  source_column VARCHAR(200) COMMENT '源字段名',
  target_table VARCHAR(200) NOT NULL COMMENT '目标表名',
  target_column VARCHAR(200) COMMENT '目标字段名',
  flow_type VARCHAR(50) NOT NULL COMMENT '流转类型: DIRECT/INDIRECT/TRANSFORMATION',
  confidence DECIMAL(5,2) DEFAULT 1.00 COMMENT '置信度 (0-1)',
  discovery_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
  last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  transformation TEXT COMMENT '转换逻辑描述',
  data_source_id BIGINT COMMENT '数据源ID',
  discovery_method VARCHAR(50) COMMENT '发现方法: SQL_PARSE, LOG_ANALYSIS, METADATA, ML',
  
  -- 索引
  INDEX idx_source_table(source_table),
  INDEX idx_target_table(target_table),
  INDEX idx_flow_type(flow_type),
  INDEX idx_data_source(data_source_id),
  INDEX idx_discovery_method(discovery_method),
  INDEX idx_confidence(confidence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据流关系表';

-- 数据血缘自动发现任务表
CREATE TABLE IF NOT EXISTS data_lineage_discovery_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
  data_source_id BIGINT NOT NULL COMMENT '数据源ID',
  discovery_strategy VARCHAR(50) NOT NULL COMMENT '发现策略: SQL_PARSE, LOG_ANALYSIS, METADATA_CRAWL, ML_INFERENCE',
  status VARCHAR(20) NOT NULL COMMENT '任务状态: RUNNING, SUCCESS, FAILED, PENDING',
  start_time DATETIME COMMENT '开始时间',
  end_time DATETIME COMMENT '结束时间',
  discovered_flows_count INT DEFAULT 0 COMMENT '发现的血缘关系数量',
  config TEXT COMMENT '任务配置(JSON格式)',
  error_message TEXT COMMENT '错误信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引
  INDEX idx_data_source_id(data_source_id),
  INDEX idx_status(status),
  INDEX idx_discovery_strategy(discovery_strategy),
  INDEX idx_start_time(start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据血缘自动发现任务表';

-- 影响分析结果表
CREATE TABLE IF NOT EXISTS data_impact_analysis (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  analysis_name VARCHAR(200) NOT NULL COMMENT '分析名称',
  analysis_type VARCHAR(50) NOT NULL COMMENT '分析类型: COLUMN_CHANGE, TABLE_CHANGE, SCHEMA_CHANGE',
  impact_object_type VARCHAR(50) NOT NULL COMMENT '影响对象类型: TABLE, COLUMN, SCHEMA',
  impact_object_name VARCHAR(200) NOT NULL COMMENT '影响对象名称',
  analysis_target TEXT COMMENT '分析目标（JSON格式）',
  impact_scope TEXT COMMENT '影响范围（JSON格式）',
  impact_path_count INT DEFAULT 0 COMMENT '影响路径数量',
  impact_asset_count INT DEFAULT 0 COMMENT '影响资产数量',
  risk_level VARCHAR(20) COMMENT '风险等级: HIGH, MEDIUM, LOW',
  status VARCHAR(20) NOT NULL COMMENT '分析状态: COMPLETED, FAILED, RUNNING',
  result TEXT COMMENT '分析结果详情（JSON格式）',
  report_path VARCHAR(500) COMMENT '分析报告文件路径',
  start_time DATETIME COMMENT '开始时间',
  end_time DATETIME COMMENT '结束时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  create_by BIGINT COMMENT '创建人ID',
  
  -- 索引
  INDEX idx_analysis_type(analysis_type),
  INDEX idx_impact_object_type(impact_object_type),
  INDEX idx_risk_level(risk_level),
  INDEX idx_status(status),
  INDEX idx_create_by(create_by),
  INDEX idx_start_time(start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影响分析结果表';

-- 数据地图表
CREATE TABLE IF NOT EXISTS data_map (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  map_name VARCHAR(100) NOT NULL COMMENT '地图名称',
  map_type VARCHAR(50) NOT NULL COMMENT '地图类型: GLOBAL, BUSINESS_DOMAIN, DATA_SOURCE, CUSTOM',
  scope TEXT COMMENT '地图范围（JSON格式）',
  config TEXT COMMENT '地图配置（JSON格式）',
  node_count INT DEFAULT 0 COMMENT '节点数量',
  relationship_count INT DEFAULT 0 COMMENT '关系数量',
  map_data LONGTEXT COMMENT '地图数据（JSON格式，存储完整的地图数据）',
  thumbnail_path VARCHAR(500) COMMENT '缩略图路径',
  version INT DEFAULT 1 COMMENT '版本号',
  is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认地图',
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: ACTIVE, INACTIVE, DRAFT',
  create_by BIGINT COMMENT '创建人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark TEXT COMMENT '备注',
  
  -- 索引
  INDEX idx_map_type(map_type),
  INDEX idx_status(status),
  INDEX idx_is_default(is_default),
  INDEX idx_create_by(create_by),
  INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据地图表';

-- 数据血缘路径表（用于存储完整的影响路径）
CREATE TABLE IF NOT EXISTS data_lineage_path (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  path_name VARCHAR(200) COMMENT '路径名称',
  start_table VARCHAR(200) NOT NULL COMMENT '起始表',
  start_column VARCHAR(200) COMMENT '起始字段',
  end_table VARCHAR(200) NOT NULL COMMENT '结束表',
  end_column VARCHAR(200) COMMENT '结束字段',
  path_length INT NOT NULL COMMENT '路径长度',
  path_data TEXT NOT NULL COMMENT '路径数据（JSON格式，存储完整路径）',
  confidence DECIMAL(5,2) DEFAULT 1.00 COMMENT '路径置信度',
  discovery_method VARCHAR(50) COMMENT '发现方法',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  -- 索引
  INDEX idx_start_table(start_table),
  INDEX idx_end_table(end_table),
  INDEX idx_path_length(path_length),
  INDEX idx_confidence(confidence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据血缘路径表';

-- 数据源元数据缓存表（提高发现效率）
CREATE TABLE IF NOT EXISTS data_source_metadata (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  data_source_id BIGINT NOT NULL COMMENT '数据源ID',
  metadata_type VARCHAR(50) NOT NULL COMMENT '元数据类型: TABLE, COLUMN, INDEX, CONSTRAINT',
  metadata_name VARCHAR(200) NOT NULL COMMENT '元数据名称',
  metadata_detail TEXT COMMENT '元数据详情（JSON格式）',
  last_sync_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后同步时间',
  sync_status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '同步状态',
  
  -- 索引
  INDEX idx_data_source_id(data_source_id),
  INDEX idx_metadata_type(metadata_type),
  INDEX idx_metadata_name(metadata_name),
  INDEX idx_last_sync_time(last_sync_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源元数据缓存表';

-- 创建视图：数据血缘统计概览
CREATE OR REPLACE VIEW data_lineage_overview AS
SELECT 
  df.discovery_method,
  df.flow_type,
  COUNT(*) as flow_count,
  AVG(df.confidence) as avg_confidence,
  DATE(df.discovery_time) as discovery_date
FROM data_flow df
GROUP BY df.discovery_method, df.flow_type, DATE(df.discovery_time);

-- 创建视图：热门数据表（按血缘关系数量排序）
CREATE OR REPLACE VIEW hot_data_tables AS
SELECT 
  source_table as table_name,
  COUNT(*) as outgoing_flows,
  'SOURCE' as table_role
FROM data_flow 
GROUP BY source_table
UNION ALL
SELECT 
  target_table as table_name,
  COUNT(*) as incoming_flows,
  'TARGET' as table_role
FROM data_flow 
GROUP BY target_table;

-- 创建视图：数据血缘质量分析
CREATE OR REPLACE VIEW data_lineage_quality AS
SELECT 
  CASE 
    WHEN confidence >= 0.9 THEN 'HIGH_QUALITY'
    WHEN confidence >= 0.7 THEN 'MEDIUM_QUALITY'
    ELSE 'LOW_QUALITY'
  END as quality_level,
  COUNT(*) as flow_count,
  discovery_method,
  flow_type
FROM data_flow 
GROUP BY quality_level, discovery_method, flow_type;

-- 插入示例数据
INSERT INTO data_lineage_discovery_task (task_name, data_source_id, discovery_strategy, status, discovered_flows_count, config) VALUES
('MySQL数据库血缘发现', 1, 'SQL_PARSE', 'SUCCESS', 156, '{"scanTables": true, "scanViews": true, "scanProcedures": true}'),
('Oracle日志分析发现', 2, 'LOG_ANALYSIS', 'SUCCESS', 89, '{"logPath": "/var/log/oracle", "timeRange": "24h"}'),
('Hive元数据爬取', 3, 'METADATA_CRAWL', 'RUNNING', 0, '{"includeExternalTables": true, "scanDepth": 3}'),
('机器学习关系推断', 4, 'ML_INFERENCE', 'PENDING', 0, '{"modelType": "clustering", "trainingDataSize": 10000}');

INSERT INTO data_flow (source_table, source_column, target_table, target_column, flow_type, confidence, transformation, data_source_id, discovery_method) VALUES
('customer_info', 'customer_id', 'account_balance', 'cust_id', 'DIRECT', 1.00, '直接映射', 1, 'SQL_PARSE'),
('transaction_detail', 'amount', 'daily_summary', 'total_amount', 'TRANSFORMATION', 0.95, 'SUM(amount) GROUP BY date', 1, 'SQL_PARSE'),
('user_profile', 'email', 'marketing_campaign', 'contact_email', 'INDIRECT', 0.85, '通过用户ID关联', 2, 'LOG_ANALYSIS'),
('risk_assessment', 'risk_score', 'credit_decision', 'final_score', 'TRANSFORMATION', 0.90, '加权平均计算', 1, 'ML_INFERENCE');

INSERT INTO data_map (map_name, map_type, scope, config, node_count, relationship_count, version, is_default, status, create_by) VALUES
('全局数据地图', 'GLOBAL', '{"includeAll": true}', '{"layout": "force", "depth": 5}', 1250, 3400, 1, true, 'ACTIVE', 1),
('核心业务域地图', 'BUSINESS_DOMAIN', '{"businessLines": ["retail", "corporate"]}', '{"layout": "hierarchical", "depth": 3}', 450, 1200, 1, false, 'ACTIVE', 1),
('数据仓库地图', 'DATA_SOURCE', '{"dataSourceIds": [1, 2, 3]}', '{"layout": "circular", "depth": 4}', 800, 2100, 1, false, 'DRAFT', 1);