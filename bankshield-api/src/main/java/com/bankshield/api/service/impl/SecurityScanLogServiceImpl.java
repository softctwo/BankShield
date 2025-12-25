package com.bankshield.api.service.impl;

import com.bankshield.api.entity.SecurityScanLog;
import com.bankshield.api.mapper.SecurityScanLogMapper;
import com.bankshield.api.service.SecurityScanLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全扫描任务日志服务实现类
 * @author BankShield
 */
@Slf4j
@Service
public class SecurityScanLogServiceImpl extends ServiceImpl<SecurityScanLogMapper, SecurityScanLog> 
        implements SecurityScanLogService {
    
    @Autowired
    private SecurityScanLogMapper scanLogMapper;
    
    @Override
    @Transactional
    public void log(Long taskId, String message, SecurityScanLog.LogLevel logLevel) {
        try {
            SecurityScanLog log = new SecurityScanLog();
            log.setTaskId(taskId);
            log.setLogLevel(logLevel.getValue());
            log.setMessage(message);
            log.setCreateTime(LocalDateTime.now());
            
            scanLogMapper.insert(log);
            
            // 同时记录到应用日志
            String formattedMessage = String.format("[ScanTask-%d] %s", taskId, message);
            switch (logLevel) {
                case INFO:
                    log.info(formattedMessage);
                    break;
                case WARN:
                    log.warn(formattedMessage);
                    break;
                case ERROR:
                    log.error(formattedMessage);
                    break;
                case DEBUG:
                    log.debug(formattedMessage);
                    break;
            }
            
        } catch (Exception e) {
            log.error("记录扫描日志失败: taskId={}, message={}", taskId, message, e);
        }
    }
    
    @Override
    public void info(Long taskId, String message) {
        log(taskId, message, SecurityScanLog.LogLevel.INFO);
    }
    
    @Override
    public void warn(Long taskId, String message) {
        log(taskId, message, SecurityScanLog.LogLevel.WARN);
    }
    
    @Override
    public void error(Long taskId, String message) {
        log(taskId, message, SecurityScanLog.LogLevel.ERROR);
    }
    
    @Override
    public void debug(Long taskId, String message) {
        log(taskId, message, SecurityScanLog.LogLevel.DEBUG);
    }
    
    @Override
    public List<SecurityScanLog> getTaskLogs(Long taskId) {
        return scanLogMapper.selectByTaskId(taskId);
    }
    
    @Override
    public List<String> getTaskLogContents(Long taskId) {
        List<SecurityScanLog> logs = scanLogMapper.selectByTaskId(taskId);
        return logs.stream()
                .map(log -> String.format("[%s] [%s] %s", 
                        log.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        log.getLogLevel(),
                        log.getMessage()))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteTaskLogs(Long taskId) {
        scanLogMapper.deleteByTaskId(taskId);
    }
    
    @Override
    @Transactional
    public void batchLog(List<SecurityScanLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        
        try {
            scanLogMapper.batchInsert(logs);
        } catch (Exception e) {
            log.error("批量记录扫描日志失败", e);
            // 如果批量插入失败，逐个插入
            for (SecurityScanLog log : logs) {
                try {
                    scanLogMapper.insert(log);
                } catch (Exception ex) {
                    log.error("单个日志记录失败: {}", log, ex);
                }
            }
        }
    }
}