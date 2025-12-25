package com.bankshield.api.enums;

/**
 * 风险类型枚举
 * @author BankShield
 */
public enum RiskType {
    // 漏洞扫描相关
    SQL_INJECTION("SQL注入", ScanType.VULNERABILITY),
    XSS("跨站脚本攻击", ScanType.VULNERABILITY),
    CSRF("跨站请求伪造", ScanType.VULNERABILITY),
    DIRECTORY_TRAVERSAL("目录遍历", ScanType.VULNERABILITY),
    COMMAND_INJECTION("命令注入", ScanType.VULNERABILITY),
    FILE_UPLOAD("文件上传漏洞", ScanType.VULNERABILITY),
    
    // 配置检查相关
    PASSWORD_POLICY("密码策略", ScanType.CONFIG),
    SESSION_TIMEOUT("会话超时", ScanType.CONFIG),
    ENCRYPTION_CONFIG("加密配置", ScanType.CONFIG),
    FILE_UPLOAD_LIMIT("文件上传限制", ScanType.CONFIG),
    CORS_CONFIG("CORS配置", ScanType.CONFIG),
    SENSITIVE_INFO_LEAK("敏感信息泄露", ScanType.CONFIG),
    
    // 弱密码检测相关
    WEAK_PASSWORD("弱密码", ScanType.WEAK_PASSWORD),
    DEFAULT_PASSWORD("默认密码", ScanType.WEAK_PASSWORD),
    EXPIRED_PASSWORD("过期密码", ScanType.WEAK_PASSWORD),
    
    // 异常行为检测相关
    ABNORMAL_LOGIN_TIME("异常登录时间", ScanType.ANOMALY),
    ABNORMAL_IP("异常IP地址", ScanType.ANOMALY),
    HIGH_FREQUENCY_OPERATION("高频操作", ScanType.ANOMALY),
    PRIVILEGE_ESCALATION("权限提升", ScanType.ANOMALY),
    SESSION_ANOMALY("会话异常", ScanType.ANOMALY);

    private final String description;
    private final ScanType scanType;

    RiskType(String description, ScanType scanType) {
        this.description = description;
        this.scanType = scanType;
    }

    public String getDescription() {
        return description;
    }

    public ScanType getScanType() {
        return scanType;
    }
}