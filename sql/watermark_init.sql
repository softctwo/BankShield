-- 银行数据安全管理系统 - 数据水印模块初始化脚本
-- 创建时间: 2024年
-- 描述: 初始化水印功能相关表结构和数据

USE bankshield;

-- 检查表是否存在，如果不存在则创建
CREATE TABLE IF NOT EXISTS watermark_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  watermark_type VARCHAR(20) NOT NULL COMMENT '水印类型: TEXT/IMAGE/DATABASE',
  watermark_content TEXT NOT NULL COMMENT '水印内容',
  watermark_position VARCHAR(20) COMMENT '水印位置: TOP_LEFT/TOP_RIGHT/BOTTOM_LEFT/BOTTOM_RIGHT/CENTER/FULLSCREEN',
  transparency INT DEFAULT 30 COMMENT '透明度(0-100)',
  font_size INT COMMENT '字体大小',
  font_color VARCHAR(20) COMMENT '字体颜色',
  font_family VARCHAR(50) COMMENT '字体名称',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by VARCHAR(50) COMMENT '创建人',
  description TEXT COMMENT '描述',
  UNIQUE KEY uk_template_name (template_name),
  INDEX idx_watermark_type (watermark_type),
  INDEX idx_enabled (enabled),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水印模板表';

CREATE TABLE IF NOT EXISTS watermark_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
  task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
  task_type VARCHAR(20) NOT NULL COMMENT '任务类型: FILE/DATABASE',
  template_id BIGINT NOT NULL COMMENT '模板ID',
  data_source_id BIGINT COMMENT '数据源ID',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态: PENDING/RUNNING/SUCCESS/FAILED',
  start_time DATETIME COMMENT '开始时间',
  end_time DATETIME COMMENT '结束时间',
  process_count BIGINT DEFAULT 0 COMMENT '处理记录数',
  output_file_path VARCHAR(500) COMMENT '输出文件路径',
  created_by VARCHAR(50) COMMENT '创建人',
  error_message TEXT COMMENT '错误信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_status (status),
  INDEX idx_task_type (task_type),
  INDEX idx_template_id (template_id),
  INDEX idx_create_time (create_time),
  INDEX idx_data_source_id (data_source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水印任务表';

CREATE TABLE IF NOT EXISTS watermark_extract_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  watermark_content TEXT NOT NULL COMMENT '提取的水印内容',
  extract_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提取时间',
  extract_source VARCHAR(500) NOT NULL COMMENT '提取来源',
  extract_result VARCHAR(20) COMMENT '提取结果: SUCCESS/FAIL',
  operator VARCHAR(50) COMMENT '操作人员',
  file_name VARCHAR(255) COMMENT '文件名',
  file_type VARCHAR(50) COMMENT '文件类型',
  file_size BIGINT COMMENT '文件大小',
  extract_duration BIGINT COMMENT '提取耗时(毫秒)',
  error_message TEXT COMMENT '错误信息',
  INDEX idx_extract_time (extract_time),
  INDEX idx_extract_result (extract_result),
  INDEX idx_operator (operator),
  INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水印提取日志表';

-- 添加缺失的列（如果表已存在）
ALTER TABLE watermark_extract_log 
ADD COLUMN IF NOT EXISTS file_name VARCHAR(255) COMMENT '文件名',
ADD COLUMN IF NOT EXISTS file_type VARCHAR(50) COMMENT '文件类型',
ADD COLUMN IF NOT EXISTS file_size BIGINT COMMENT '文件大小',
ADD COLUMN IF NOT EXISTS extract_duration BIGINT COMMENT '提取耗时(毫秒)',
ADD COLUMN IF NOT EXISTS error_message TEXT COMMENT '错误信息';

-- 插入默认水印模板数据（如果不存在）
INSERT IGNORE INTO watermark_template (template_name, watermark_type, watermark_content, watermark_position, 
                               transparency, font_size, font_color, font_family, enabled, created_by, description) VALUES
-- 文本水印模板
('银行内部文档水印', 'TEXT', 'BankShield - 内部文档 - {{TIMESTAMP}}', 'BOTTOM_RIGHT', 30, 12, '#CCCCCC', 'Arial', 1, 'admin', '银行内部文档默认水印'),
('银行机密文档水印', 'TEXT', 'BankShield - 机密文档 - {{TIMESTAMP}} - {{USER}}', 'CENTER', 40, 16, '#FF0000', 'Arial', 1, 'admin', '银行机密文档水印'),
('银行财务报表水印', 'TEXT', 'BankShield - 财务报表 - {{TIMESTAMP}}', 'TOP_RIGHT', 25, 10, '#999999', 'Times New Roman', 1, 'admin', '财务报表专用水印'),

-- 图像水印模板
('银行Logo水印', 'IMAGE', '/assets/watermarks/bank_logo.png', 'TOP_LEFT', 20, NULL, NULL, NULL, 1, 'admin', '银行Logo图像水印'),
('银行印章水印', 'IMAGE', '/assets/watermarks/bank_seal.png', 'CENTER', 15, NULL, NULL, NULL, 1, 'admin', '银行印章图像水印'),

-- 数据库水印模板
('客户信息数据库水印', 'DATABASE', '{{USER}}_{{TIMESTAMP}}', NULL, NULL, NULL, NULL, NULL, 1, 'admin', '客户信息数据库存储水印'),
('交易记录数据库水印', 'DATABASE', 'TX_{{USER}}_{{DATE}}', NULL, NULL, NULL, NULL, NULL, 1, 'admin', '交易记录数据库水印');

-- 插入演示任务数据（如果不存在）
INSERT IGNORE INTO watermark_task (task_name, task_type, template_id, data_source_id, status, 
                           process_count, created_by, error_message) VALUES
('2024年Q1财务报表水印处理', 'FILE', 3, NULL, 'SUCCESS', 150, 'admin', NULL),
('客户数据库水印嵌入任务', 'DATABASE', 6, 1, 'SUCCESS', 50000, 'admin', NULL),
('内部审计文档水印处理', 'FILE', 1, NULL, 'PENDING', 0, 'user1', NULL),
('交易记录水印保护任务', 'DATABASE', 7, 2, 'RUNNING', 25000, 'user2', NULL);

-- 插入演示提取日志数据（如果不存在）
INSERT IGNORE INTO watermark_extract_log (watermark_content, extract_source, extract_result, operator, 
                                 file_name, file_type, file_size, extract_duration) VALUES
('BankShield - 内部文档 - 2024-01-15 10:30:25', '/uploads/document1.pdf', 'SUCCESS', 'admin', 'document1.pdf', 'PDF', 2048576, 150),
('BankShield - 财务报表 - 2024-01-15 14:20:10', '/uploads/financial_report.xlsx', 'SUCCESS', 'user1', 'financial_report.xlsx', 'EXCEL', 5242880, 200),
('BankShield - 机密文档 - 2024-01-15 16:45:30 - admin', '/uploads/confidential.docx', 'SUCCESS', 'admin', 'confidential.docx', 'WORD', 1024000, 180),
('无效水印内容', '/uploads/test_file.pdf', 'FAIL', 'user2', 'test_file.pdf', 'PDF', 1048576, 50);

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_watermark_template_name ON watermark_template(template_name);
CREATE INDEX IF NOT EXISTS idx_watermark_task_status_time ON watermark_task(status, create_time);
CREATE INDEX IF NOT EXISTS idx_extract_log_time_result ON watermark_extract_log(extract_time, extract_result);

-- 添加外键约束（如果存在引用表）
-- 注意：需要先确保引用的表存在，否则会失败
-- ALTER TABLE watermark_task ADD CONSTRAINT fk_task_template 
-- FOREIGN KEY (template_id) REFERENCES watermark_template(id);

-- 添加数据字典（如果不存在）
-- 注意：需要先确保字典表存在，否则会失败
-- INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status) VALUES
-- ('水印类型', 'watermark_type', 1),
-- ('水印位置', 'watermark_position', 1),
-- ('任务类型', 'task_type', 1),
-- ('任务状态', 'task_status', 1),
-- ('提取结果', 'extract_result', 1);

-- 数据字典数据
-- INSERT IGNORE INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, status) VALUES
-- ('watermark_type', '文本水印', 'TEXT', 1, 1),
-- ('watermark_type', '图像水印', 'IMAGE', 2, 1),
-- ('watermark_type', '数据库水印', 'DATABASE', 3, 1),
-- ('watermark_position', '左上角', 'TOP_LEFT', 1, 1),
-- ('watermark_position', '右上角', 'TOP_RIGHT', 2, 1),
-- ('watermark_position', '左下角', 'BOTTOM_LEFT', 3, 1),
-- ('watermark_position', '右下角', 'BOTTOM_RIGHT', 4, 1),
-- ('watermark_position', '中心', 'CENTER', 5, 1),
-- ('watermark_position', '全屏', 'FULLSCREEN', 6, 1),
-- ('task_type', '文件处理', 'FILE', 1, 1),
-- ('task_type', '数据库处理', 'DATABASE', 2, 1),
-- ('task_status', '待处理', 'PENDING', 1, 1),
-- ('task_status', '运行中', 'RUNNING', 2, 1),
-- ('task_status', '成功', 'SUCCESS', 3, 1),
-- ('task_status', '失败', 'FAILED', 4, 1),
-- ('extract_result', '成功', 'SUCCESS', 1, 1),
-- ('extract_result', '失败', 'FAIL', 2, 1);

-- 更新统计信息
ANALYZE TABLE watermark_template;
ANALYZE TABLE watermark_task;
ANALYZE TABLE watermark_extract_log;

-- 显示表结构（用于验证）
SHOW CREATE TABLE watermark_template\G
SHOW CREATE TABLE watermark_task\G
SHOW CREATE TABLE watermark_extract_log\G

-- 显示表数据量
SELECT 'watermark_template' as table_name, COUNT(*) as record_count FROM watermark_template
UNION ALL
SELECT 'watermark_task' as table_name, COUNT(*) as record_count FROM watermark_task
UNION ALL
SELECT 'watermark_extract_log' as table_name, COUNT(*) as record_count FROM watermark_extract_log;