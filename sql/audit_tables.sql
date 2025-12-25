-- 操作审计表
CREATE TABLE audit_operation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
  operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
  operation_content TEXT COMMENT '操作内容',
  request_url VARCHAR(500) COMMENT '请求URL',
  request_method VARCHAR(10) COMMENT '请求方法',
  request_params TEXT COMMENT '请求参数',
  response_result TEXT COMMENT '响应结果',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  location VARCHAR(200) COMMENT '操作地点',
  status INT DEFAULT 1 COMMENT '操作结果: 0-失败 1-成功',
  error_message TEXT COMMENT '错误信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  INDEX idx_user_id (user_id),
  INDEX idx_create_time (create_time),
  INDEX idx_operation_module (operation_module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计表';

-- 登录审计表
CREATE TABLE audit_login (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  login_ip VARCHAR(50) COMMENT '登录IP',
  login_location VARCHAR(200) COMMENT '登录地点',
  browser VARCHAR(200) COMMENT '浏览器',
  os VARCHAR(200) COMMENT '操作系统',
  status INT DEFAULT 1 COMMENT '登录结果: 0-失败 1-成功',
  failure_reason VARCHAR(500) COMMENT '失败原因',
  logout_time DATETIME COMMENT '退出时间',
  session_duration BIGINT COMMENT '会话时长(秒)',
  INDEX idx_user_id (user_id),
  INDEX idx_login_time (login_time),
  INDEX idx_login_ip (login_ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录审计表';