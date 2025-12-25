-- BankShield 数据分类分级管理模块数据库表结构
-- 符合JR/T 0197-2020和JR/T 0171-2020标准

USE bankshield;

-- 数据资产主表
CREATE TABLE IF NOT EXISTS data_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  asset_name VARCHAR(200) NOT NULL COMMENT '资产名称',
  asset_type VARCHAR(50) NOT NULL COMMENT '资产类型: DATABASE, FILE, BIGDATA',
  storage_location VARCHAR(500) COMMENT '存储位置',
  data_source_id BIGINT COMMENT '数据源ID',
  
  -- 分类分级信息
  security_level TINYINT COMMENT '安全等级: 1:C1, 2:C2, 3:C3, 4:C4',
  classification_basis TEXT COMMENT '分类依据',
  automatic_level TINYINT COMMENT '自动识别等级',
  manual_level TINYINT COMMENT '人工标注等级',
  final_level TINYINT COMMENT '最终等级',
  
  -- 识别信息
  recognize_method VARCHAR(50) COMMENT '识别方法: REGEX, KEYWORD, ML',
  recognize_confidence DECIMAL(5,2) COMMENT '识别置信度',
  pattern_matched VARCHAR(500) COMMENT '匹配的规则',
  
  -- 业务信息
  business_owner VARCHAR(100) COMMENT '业务负责人',
  technical_owner VARCHAR(100) COMMENT '技术负责人',
  business_line VARCHAR(50) COMMENT '业务条线',
  
  -- 状态管理
  status TINYINT DEFAULT 0 COMMENT '状态: 0:待审核, 1:已生效, 2:已下线',
  reviewer_id BIGINT COMMENT '审核人ID',
  review_time DATETIME COMMENT '审核时间',
  review_comment TEXT COMMENT '审核意见',
  
  -- 时间戳
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- 索引
  INDEX idx_storage_location(storage_location),
  INDEX idx_security_level(security_level),
  INDEX idx_business_line(business_line),
  INDEX idx_status(status),
  INDEX idx_data_source(data_source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据资产主表';

-- 数据源配置表
CREATE TABLE IF NOT EXISTS data_source (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
  source_type VARCHAR(50) NOT NULL COMMENT '数据源类型: MYSQL, ORACLE, POSTGRESQL, HDFS, HIVE, HBASE, KAFKA',
  connection_config TEXT COMMENT '连接配置(JSON)',
  scan_status TINYINT DEFAULT 0 COMMENT '扫描状态: 0:未扫描, 1:扫描中, 2:完成, 3:失败',
  last_scan_time DATETIME COMMENT '最后扫描时间',
  scan_error TEXT COMMENT '扫描错误信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_source_type(source_type),
  INDEX idx_scan_status(scan_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- 分类分级历史表
CREATE TABLE IF NOT EXISTS classification_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  asset_id BIGINT NOT NULL COMMENT '资产ID',
  old_level TINYINT COMMENT '原等级',
  new_level TINYINT COMMENT '新等级',
  change_reason TEXT COMMENT '变更原因',
  operator_id BIGINT NOT NULL COMMENT '操作人',
  operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_asset_id(asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类分级变更历史表';

-- 敏感数据类型模板表
CREATE TABLE IF NOT EXISTS sensitive_data_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type_name VARCHAR(100) NOT NULL COMMENT '类型名称',
  type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类型编码',
  security_level TINYINT NOT NULL COMMENT '安全等级',
  pattern TEXT COMMENT '正则表达式',
  keywords TEXT COMMENT '关键词（JSON数组）',
  description TEXT COMMENT '描述',
  standard_ref VARCHAR(100) COMMENT '标准引用: JR/T0197, JR/T0171',
  examples TEXT COMMENT '示例数据',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_security_level(security_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感数据类型模板表';

-- 数据血缘关系表
CREATE TABLE IF NOT EXISTS data_lineage (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_asset_id BIGINT NOT NULL COMMENT '源资产ID',
  target_asset_id BIGINT NOT NULL COMMENT '目标资产ID',
  source_field VARCHAR(100) COMMENT '源字段',
  target_field VARCHAR(100) COMMENT '目标字段',
  transformation TEXT COMMENT '转换逻辑',
  lineage_type VARCHAR(50) COMMENT '血缘类型: DIRECT, TRANSFORMED',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_source_asset(source_asset_id),
  INDEX idx_target_asset(target_asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据血缘关系表';

-- 资产扫描任务表
CREATE TABLE IF NOT EXISTS scan_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
  data_source_id BIGINT NOT NULL COMMENT '数据源ID',
  scan_type VARCHAR(50) NOT NULL COMMENT '扫描类型: FULL, INCREMENTAL',
  scan_config TEXT COMMENT '扫描配置(JSON)',
  task_status TINYINT DEFAULT 0 COMMENT '任务状态: 0:待执行, 1:执行中, 2:完成, 3:失败',
  progress_percent INT DEFAULT 0 COMMENT '进度百分比',
  start_time DATETIME COMMENT '开始时间',
  end_time DATETIME COMMENT '结束时间',
  error_message TEXT COMMENT '错误信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_data_source(data_source_id),
  INDEX idx_task_status(task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产扫描任务表';

-- 插入金融行业模板数据
INSERT INTO sensitive_data_template (type_name, type_code, security_level, pattern, keywords, standard_ref, examples) VALUES
('员工工号', 'EMPLOYEE_ID', 1, '^EMP\\d{6}$', '["工号", "employee", "staff"]', 'JR/T0197', '["EMP123456"]'),
('客户性别', 'GENDER', 2, '^(男|女|未知)$', '["性别", "gender"]', 'JR/T0197', '["男", "女", "未知"]'),
('客户年龄段', 'AGE_RANGE', 2, '^(18-30|31-40|41-50|50\\+)$', '["年龄", "age"]', 'JR/T0197', '["18-30", "31-40"]'),
('手机号码', 'PHONE', 3, '^1[3-9]\\d{9}$', '["手机", "电话", "mobile", "phone"]', 'JR/T0171', '["13812345678", "13987654321"]'),
('身份证号', 'ID_CARD', 3, '^\\d{17}[\\dXx]$', '["身份证", "idcard"]', 'JR/T0171', '["110101199001011234", "11010119900101123X"]'),
('银行卡号', 'BANK_CARD', 3, '^\\d{16,19}$', '["银行卡", "bankcard", "card"]', 'JR/T0171', '["6222021234567890123", "621226123456789012"]'),
('姓名', 'NAME', 3, '', '["姓名", "name", "客户名称"]', 'JR/T0171', '["张三", "李四", "王五"]'),
('邮箱地址', 'EMAIL', 3, '^\\w+@[\\w.]+$', '["邮箱", "email"]', 'JR/T0171', '["zhangsan@example.com", "lisi@163.com"]'),
('家庭住址', 'ADDRESS', 3, '', '["地址", "住址"]', 'JR/T0171', '["北京市朝阳区xxx街道xxx号"]'),
('指纹特征', 'FINGERPRINT', 4, '', '["指纹", "fingerprint", "生物"]', 'JR/T0171', NULL),
('面部特征', 'FACE_FEATURE', 4, '', '["人脸", "面部", "face"]', 'JR/T0171', NULL),
('交易密码密文', 'TRADE_PASSWORD', 4, '', '["密码", "password"]', 'JR/T0171', NULL);

-- 创建视图：资产统计概览
CREATE OR REPLACE VIEW asset_overview AS
SELECT 
  security_level,
  COUNT(*) as asset_count,
  asset_type,
  business_line,
  status
FROM data_asset 
GROUP BY security_level, asset_type, business_line, status;

-- 创建视图：待审核资产
CREATE OR REPLACE VIEW pending_review_assets AS
SELECT 
  a.*,
  u.username as reviewer_name
FROM data_asset a
LEFT JOIN sys_user u ON a.reviewer_id = u.id
WHERE a.status = 0;

-- 创建视图：资产风险统计
CREATE OR REPLACE VIEW asset_risk_statistics AS
SELECT 
  security_level,
  COUNT(*) as count,
  CASE 
    WHEN security_level = 4 THEN '极高风险'
    WHEN security_level = 3 THEN '高风险'
    WHEN security_level = 2 THEN '中等风险'
    ELSE '低风险'
  END as risk_level
FROM data_asset 
WHERE status = 1
GROUP BY security_level;