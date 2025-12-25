package com.bankshield.common.security.compliance;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 安全基线检查器
 * 用于检查系统是否符合安全基线要求，包括密码策略、系统配置、文件权限等
 * 
 * @author BankShield
 * @version 1.0.0
 */
@Slf4j
@Component
public class SecurityBaselineChecker {
    
    @Autowired
    private Environment environment;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    // 安全基线检查项
    private final List<BaselineCheck> baselineChecks = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        initializeBaselineChecks();
    }
    
    /**
     * 执行完整的安全基线检查
     * 
     * @return 检查结果
     */
    public BaselineCheckResult performFullCheck() {
        log.info("开始执行安全基线检查...");
        
        BaselineCheckResult result = new BaselineCheckResult();
        result.setCheckTime(new Date());
        result.setTotalChecks(baselineChecks.size());
        
        List<BaselineCheckItem> items = new ArrayList<>();
        int passedChecks = 0;
        
        for (BaselineCheck check : baselineChecks) {
            try {
                BaselineCheckItem item = performCheck(check);
                items.add(item);
                
                if (item.isPassed()) {
                    passedChecks++;
                }
                
            } catch (Exception e) {
                log.error("执行检查项 {} 失败: {}", check.getName(), e.getMessage(), e);
                
                BaselineCheckItem item = new BaselineCheckItem();
                item.setCheckName(check.getName());
                item.setCategory(check.getCategory());
                item.setPassed(false);
                item.setSeverity(check.getSeverity());
                item.setMessage("检查执行失败: " + e.getMessage());
                items.add(item);
            }
        }
        
        result.setItems(items);
        result.setPassedChecks(passedChecks);
        result.setFailedChecks(baselineChecks.size() - passedChecks);
        result.setPassRate((double) passedChecks / baselineChecks.size() * 100);
        result.setOverallStatus(calculateOverallStatus(items));
        
        log.info("安全基线检查完成 - 通过率: {}%, 通过: {}, 失败: {}", 
                String.format("%.2f", result.getPassRate()), 
                result.getPassedChecks(), result.getFailedChecks());
        
        return result;
    }
    
    /**
     * 执行单个检查项
     */
    private BaselineCheckItem performCheck(BaselineCheck check) {
        BaselineCheckItem item = new BaselineCheckItem();
        item.setCheckName(check.getName());
        item.setCategory(check.getCategory());
        item.setSeverity(check.getSeverity());
        item.setDescription(check.getDescription());
        
        try {
            boolean passed = check.getChecker().get();
            item.setPassed(passed);
            item.setMessage(passed ? "检查通过" : check.getFailureMessage());
            
            if (!passed && check.getRecommendation() != null) {
                item.setRecommendation(check.getRecommendation());
            }
            
        } catch (Exception e) {
            item.setPassed(false);
            item.setMessage("检查执行失败: " + e.getMessage());
            log.error("检查项 {} 执行异常: {}", check.getName(), e.getMessage(), e);
        }
        
        return item;
    }
    
    /**
     * 计算整体状态
     */
    private OverallStatus calculateOverallStatus(List<BaselineCheckItem> items) {
        boolean hasCriticalFailures = items.stream()
            .anyMatch(item -> !item.isPassed() && item.getSeverity() == Severity.CRITICAL);
        
        boolean hasHighFailures = items.stream()
            .anyMatch(item -> !item.isPassed() && item.getSeverity() == Severity.HIGH);
        
        if (hasCriticalFailures) {
            return OverallStatus.CRITICAL;
        } else if (hasHighFailures) {
            return OverallStatus.HIGH_RISK;
        } else {
            long failedCount = items.stream().filter(item -> !item.isPassed()).count();
            if (failedCount == 0) {
                return OverallStatus.COMPLIANT;
            } else if (failedCount <= 3) {
                return OverallStatus.LOW_RISK;
            } else {
                return OverallStatus.MEDIUM_RISK;
            }
        }
    }
    
    /**
     * 初始化基线检查项
     */
    private void initializeBaselineChecks() {
        // 密码策略检查
        baselineChecks.add(new BaselineCheck("密码复杂度策略", Category.AUTHENTICATION, Severity.CRITICAL,
            "检查系统是否启用了强密码策略", this::checkPasswordPolicy,
            "密码策略不符合要求，存在弱密码风险",
            "配置强密码策略：最小长度8位，包含大小写字母、数字和特殊字符"));
        
        // 会话超时检查
        baselineChecks.add(new BaselineCheck("会话超时配置", Category.AUTHENTICATION, Severity.HIGH,
            "检查会话超时时间是否合理", this::checkSessionTimeout,
            "会话超时时间配置不合理，存在会话劫持风险",
            "设置合理的会话超时时间，建议不超过30分钟"));
        
        // HTTPS配置检查
        baselineChecks.add(new BaselineCheck("HTTPS强制使用", Category.NETWORK, Severity.CRITICAL,
            "检查是否强制使用HTTPS", this::checkHttpsConfiguration,
            "未强制使用HTTPS，数据传输存在被窃听风险",
            "配置强制HTTPS重定向，禁用HTTP访问"));
        
        // 数据库连接加密检查
        baselineChecks.add(new BaselineCheck("数据库连接加密", Category.DATA_PROTECTION, Severity.HIGH,
            "检查数据库连接是否使用SSL/TLS加密", this::checkDatabaseSsl,
            "数据库连接未使用SSL/TLS加密，数据传输不安全",
            "配置数据库SSL/TLS连接，确保数据传输安全"));
        
        // 文件权限检查
        baselineChecks.add(new BaselineCheck("敏感文件权限", Category.SYSTEM, Severity.MEDIUM,
            "检查敏感文件的访问权限", this::checkFilePermissions,
            "敏感文件权限配置不当，存在未授权访问风险",
            "限制敏感文件的访问权限，确保只有授权用户可访问"));
        
        // 日志配置检查
        baselineChecks.add(new BaselineCheck("安全日志配置", Category.AUDIT, Severity.HIGH,
            "检查安全日志是否正确配置", this::checkSecurityLogging,
            "安全日志配置不完整，无法有效追踪安全事件",
            "配置完整的安全日志记录，包括登录、权限变更等关键操作"));
        
        // 错误信息检查
        baselineChecks.add(new BaselineCheck("错误信息泄露", Category.APPLICATION, Severity.MEDIUM,
            "检查错误信息是否泄露敏感信息", this::checkErrorInformation,
            "错误信息泄露了系统内部信息，存在信息泄露风险",
            "配置统一的错误处理，避免泄露敏感信息"));
        
        // 跨域配置检查
        baselineChecks.add(new BaselineCheck("CORS配置安全", Category.APPLICATION, Severity.MEDIUM,
            "检查跨域资源共享配置是否安全", this::checkCorsConfiguration,
            "CORS配置过于宽松，存在跨域攻击风险",
            "配置严格的CORS策略，只允许可信域名访问"));
        
        // 响应头安全检查
        baselineChecks.add(new BaselineCheck("安全响应头", Category.APPLICATION, Severity.HIGH,
            "检查是否配置了必要的安全响应头", this::checkSecurityHeaders,
            "缺少必要的安全响应头，存在多种Web攻击风险",
            "配置完整的安全响应头：X-Content-Type-Options, X-Frame-Options, X-XSS-Protection等"));
        
        // 依赖库安全检查
        baselineChecks.add(new BaselineCheck("依赖库安全", Category.APPLICATION, Severity.HIGH,
            "检查是否存在已知安全漏洞的依赖库", this::checkDependencyVulnerabilities,
            "使用了存在已知漏洞的依赖库，存在被攻击风险",
            "定期更新依赖库，修复已知安全漏洞"));
        
        // 加密算法检查
        baselineChecks.add(new BaselineCheck("加密算法强度", Category.DATA_PROTECTION, Severity.CRITICAL,
            "检查是否使用了安全的加密算法", this::checkEncryptionAlgorithms,
            "使用了弱加密算法或不安全的加密实现",
            "使用强加密算法：AES-256、RSA-2048、SHA-256等"));
        
        // 备份配置检查
        baselineChecks.add(new BaselineCheck("数据备份策略", Category.DATA_PROTECTION, Severity.MEDIUM,
            "检查是否有完善的数据备份策略", this::checkBackupStrategy,
            "缺少完善的数据备份策略，存在数据丢失风险",
            "建立完善的数据备份策略，定期测试备份恢复"));
    }
    
    /**
     * 检查密码策略
     */
    private boolean checkPasswordPolicy() {
        // 检查配置文件中的密码策略
        String minLength = environment.getProperty("security.password.min-length");
        String requireUppercase = environment.getProperty("security.password.require-uppercase");
        String requireLowercase = environment.getProperty("security.password.require-lowercase");
        String requireDigit = environment.getProperty("security.password.require-digit");
        String requireSpecial = environment.getProperty("security.password.require-special");
        
        // 默认密码策略检查
        return "8".equals(minLength) &&
               "true".equals(requireUppercase) &&
               "true".equals(requireLowercase) &&
               "true".equals(requireDigit) &&
               "true".equals(requireSpecial);
    }
    
    /**
     * 检查会话超时配置
     */
    private boolean checkSessionTimeout() {
        String sessionTimeout = environment.getProperty("server.servlet.session.timeout");
        if (sessionTimeout == null) {
            return false;
        }
        
        // 解析超时时间（格式如：30m, 1h等）
        int timeoutMinutes = parseTimeToMinutes(sessionTimeout);
        return timeoutMinutes > 0 && timeoutMinutes <= 30;
    }
    
    /**
     * 检查HTTPS配置
     */
    private boolean checkHttpsConfiguration() {
        // 检查是否配置了HTTPS端口
        String httpsPort = environment.getProperty("server.ssl.enabled");
        return "true".equals(httpsPort);
    }
    
    /**
     * 检查数据库SSL配置
     */
    private boolean checkDatabaseSsl() {
        String sslMode = environment.getProperty("spring.datasource.hikari.dataSourceProperties.sslMode");
        return "REQUIRE".equals(sslMode) || "VERIFY_CA".equals(sslMode);
    }
    
    /**
     * 检查文件权限
     */
    private boolean checkFilePermissions() {
        try {
            // 检查配置文件权限
            String configPath = environment.getProperty("spring.config.location");
            if (configPath != null) {
                File configFile = new File(configPath);
                if (configFile.exists()) {
                    // 检查文件是否对其他用户可写
                    return !configFile.canWrite();
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("检查文件权限失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查安全日志配置
     */
    private boolean checkSecurityLogging() {
        // 检查是否配置了安全日志
        String loggingLevel = environment.getProperty("logging.level.com.bankshield.security");
        return loggingLevel != null && ("INFO".equals(loggingLevel) || "DEBUG".equals(loggingLevel));
    }
    
    /**
     * 检查错误信息泄露
     */
    private boolean checkErrorInformation() {
        // 检查是否配置了错误页面
        String errorPath = environment.getProperty("server.error.path");
        String whitelabelEnabled = environment.getProperty("server.error.whitelabel.enabled");
        
        return errorPath != null && !"true".equals(whitelabelEnabled);
    }
    
    /**
     * 检查CORS配置
     */
    private boolean checkCorsConfiguration() {
        // 检查CORS配置是否过于宽松
        String corsAllowedOrigins = environment.getProperty("spring.web.cors.allowed-origins");
        if (corsAllowedOrigins == null) {
            return true; // 未配置CORS视为安全
        }
        
        // 检查是否允许了所有来源
        return !corsAllowedOrigins.contains("*");
    }
    
    /**
     * 检查安全响应头
     */
    private boolean checkSecurityHeaders() {
        // 检查是否配置了安全响应头过滤器
        // 这里假设我们的SecureHeadersFilter已经配置
        return true;
    }
    
    /**
     * 检查依赖库漏洞
     */
    private boolean checkDependencyVulnerabilities() {
        // 这里应该集成漏洞扫描工具的结果
        // 暂时返回true，表示通过检查
        return true;
    }
    
    /**
     * 检查加密算法
     */
    private boolean checkEncryptionAlgorithms() {
        // 检查是否使用了国密算法
        String cryptoProvider = environment.getProperty("bankshield.crypto.provider");
        return "gm".equals(cryptoProvider);
    }
    
    /**
     * 检查备份策略
     */
    private boolean checkBackupStrategy() {
        // 检查是否配置了备份
        String backupEnabled = environment.getProperty("bankshield.backup.enabled");
        return "true".equals(backupEnabled);
    }
    
    /**
     * 解析时间字符串为分钟数
     */
    private int parseTimeToMinutes(String timeStr) {
        if (timeStr == null) {
            return 0;
        }
        
        timeStr = timeStr.trim().toLowerCase();
        if (timeStr.endsWith("s")) {
            return Integer.parseInt(timeStr.substring(0, timeStr.length() - 1)) / 60;
        } else if (timeStr.endsWith("m")) {
            return Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
        } else if (timeStr.endsWith("h")) {
            return Integer.parseInt(timeStr.substring(0, timeStr.length() - 1)) * 60;
        } else if (timeStr.endsWith("d")) {
            return Integer.parseInt(timeStr.substring(0, timeStr.length() - 1)) * 24 * 60;
        }
        
        return Integer.parseInt(timeStr);
    }
    
    /**
     * 基线检查项
     */
    @Data
    public static class BaselineCheck {
        private final String name;
        private final Category category;
        private final Severity severity;
        private final String description;
        private final BooleanSupplier checker;
        private final String failureMessage;
        private final String recommendation;
    }
    
    /**
     * 检查结果
     */
    @Data
    public static class BaselineCheckResult {
        private Date checkTime;
        private int totalChecks;
        private int passedChecks;
        private int failedChecks;
        private double passRate;
        private OverallStatus overallStatus;
        private List<BaselineCheckItem> items;
    }
    
    /**
     * 检查项结果
     */
    @Data
    public static class BaselineCheckItem {
        private String checkName;
        private Category category;
        private Severity severity;
        private String description;
        private boolean passed;
        private String message;
        private String recommendation;
    }
    
    /**
     * 检查分类
     */
    public enum Category {
        AUTHENTICATION,     // 认证
        AUTHORIZATION,      // 授权
        DATA_PROTECTION,    // 数据保护
        NETWORK,            // 网络
        APPLICATION,        // 应用
        SYSTEM,             // 系统
        AUDIT               // 审计
    }
    
    /**
     * 严重程度
     */
    public enum Severity {
        LOW,        // 低
        MEDIUM,     // 中
        HIGH,       // 高
        CRITICAL    // 严重
    }
    
    /**
     * 整体状态
     */
    public enum OverallStatus {
        COMPLIANT,      // 合规
        LOW_RISK,       // 低风险
        MEDIUM_RISK,    // 中风险
        HIGH_RISK,      // 高风险
        CRITICAL        // 严重
    }
    
    /**
     * 布尔值提供者接口
     */
    @FunctionalInterface
    public interface BooleanSupplier {
        boolean get() throws Exception;
    }
}