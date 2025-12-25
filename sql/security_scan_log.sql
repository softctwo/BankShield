-- 安全扫描任务日志表
CREATE TABLE IF NOT EXISTS `security_scan_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `log_level` varchar(10) NOT NULL COMMENT '日志级别（INFO, WARN, ERROR, DEBUG）',
  `message` text NOT NULL COMMENT '日志内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全扫描任务日志表';

-- 添加外键约束（可选，根据实际需求）
-- ALTER TABLE `security_scan_log` ADD CONSTRAINT `fk_scan_log_task` 
-- FOREIGN KEY (`task_id`) REFERENCES `security_scan_task` (`id`) ON DELETE CASCADE;