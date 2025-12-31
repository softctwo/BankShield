import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全扫描测试套件
 * 检查潜在的安全漏洞和风险
 */
public class SecurityScanTestSuite {

    private static int passed = 0;
    private static int failed = 0;
    private static int warnings = 0;
    private static List<String> testResults = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("\n===========================================");
        System.out.println("    BankShield 安全扫描测试套件");
        System.out.println("===========================================\n");

        // 测试身份认证
        testAuthenticationSecurity();

        // 测试授权控制
        testAuthorizationSecurity();

        // 测试数据加密
        testDataEncryption();

        // 测试敏感信息泄露
        testSensitiveDataExposure();

        // 测试输入验证
        testInputValidation();

        // 测试日志安全
        testLoggingSecurity();

        // 输出总结
        printSummary();
    }

    private static void testAuthenticationSecurity() {
        System.out.println("【安全扫描】身份认证安全");
        System.out.println("-------------------------------------------");

        // 检查密码加密
        Set<String> encryptUtilFiles = findFiles("**/EncryptUtil.java");
        boolean hasSecurePassword = false;
        boolean hasWeakPassword = false;

        for (String file : encryptUtilFiles) {
            String content = readFile(file);
            if (content.contains("BCrypt")) {
                hasSecurePassword = true;
            }
            if (content.contains("MD5") || content.contains("SHA1")) {
                hasWeakPassword = true;
            }
        }

        if (hasSecurePassword) {
            pass("使用BCrypt加密密码");
        } else {
            fail("未使用BCrypt加密密码");
        }

        if (!hasWeakPassword) {
            pass("未使用弱哈希算法（MD5/SHA1）");
        } else {
            warn("发现弱哈希算法使用");
        }

        // 检查JWT配置
        String parentPom = readFile("/Users/zhangyanlong/workspaces/BankShield/pom.xml");
        if (parentPom.contains("jjwt")) {
            pass("JWT依赖已配置");
        } else {
            fail("JWT依赖未配置");
        }

        System.out.println();
    }

    private static void testAuthorizationSecurity() {
        System.out.println("【安全扫描】授权控制安全");
        System.out.println("-------------------------------------------");

        // 检查控制器权限注解
        Set<String> controllerFiles = findFiles("**/controller/*.java");
        int securedControllers = 0;

        for (String controller : controllerFiles) {
            String content = readFile(controller);
            if (content.contains("@PreAuthorize") || content.contains("@Secured")) {
                securedControllers++;
            }
        }

        if (securedControllers >= 4) {
            pass("足够的控制器添加了权限控制: " + securedControllers);
        } else {
            fail("权限控制不足: " + securedControllers);
        }

        // 检查管理员权限
        Set<String> gatewayControllers = controllerFiles.stream()
            .filter(f -> f.contains("gateway"))
            .collect(Collectors.toSet());

        int adminProtected = 0;
        for (String controller : gatewayControllers) {
            String content = readFile(controller);
            if (content.contains("hasRole('ADMIN')")) {
                adminProtected++;
            }
        }

        if (adminProtected >= 2) {
            pass("网关控制器正确使用ADMIN权限");
        } else {
            fail("网关控制器ADMIN权限不足");
        }

        System.out.println();
    }

    private static void testDataEncryption() {
        System.out.println("【安全扫描】数据加密安全");
        System.out.println("-------------------------------------------");

        // 检查强加密算法使用
        String encryptUtil = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-common/src/main/java/com/bankshield/common/utils/EncryptUtil.java");

        boolean hasSM4 = encryptUtil.contains("SM4");
        boolean hasSM2 = encryptUtil.contains("SM2");
        boolean hasAES = encryptUtil.contains("AES");
        boolean hasRSA = encryptUtil.contains("RSA");

        if (hasSM4) pass("使用SM4对称加密");
        else fail("未使用SM4对称加密");

        if (hasSM2) pass("使用SM2非对称加密");
        else fail("未使用SM2非对称加密");

        if (hasAES) pass("使用AES对称加密");
        else fail("未使用AES对称加密");

        if (hasRSA) pass("使用RSA非对称加密");
        else fail("未使用RSA非对称加密");

        // 检查加密模式
        String sm4Util = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-common/src/main/java/com/bankshield/common/crypto/SM4Util.java");
        if (sm4Util.contains("CBC") || sm4Util.contains("CTR")) {
            pass("SM4使用安全模式（CBC/CTR）");
        } else if (sm4Util.contains("ECB")) {
            warn("SM4仍使用ECB模式（不推荐）");
        } else {
            fail("SM4未明确加密模式");
        }

        System.out.println();
    }

    private static void testSensitiveDataExposure() {
        System.out.println("【安全扫描】敏感信息泄露");
        System.out.println("-------------------------------------------");

        // 检查硬编码密码/密钥
        Set<String> javaFiles = findFiles("**/*.java");
        int hardcodedSecrets = 0;

        for (String file : javaFiles) {
            String content = readFile(file);
            String fileName = file.toLowerCase();
            if ((content.contains("password") || content.contains("secret") || content.contains("key")) &&
                !fileName.contains("test") && !fileName.contains("config")) {
                // 检查是否在注释或日志中
                String[] lines = content.split("\n");
                for (String line : lines) {
                    if (!line.trim().startsWith("//") && !line.trim().startsWith("/*") &&
                        (line.contains("=\"") || line.contains("= \""))) {
                        hardcodedSecrets++;
                        break;
                    }
                }
            }
        }

        if (hardcodedSecrets < 5) {
            pass("硬编码敏感信息较少: " + hardcodedSecrets);
        } else {
            warn("发现较多潜在硬编码敏感信息: " + hardcodedSecrets);
        }

        // 检查MPC隐私保护
        String psiService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-mpc/src/main/java/com/bankshield/mpc/service/PsiService.java");
        String jointQueryService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-mpc/src/main/java/com/bankshield/mpc/service/JointQueryService.java");
        String mpcClientService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-mpc/src/main/java/com/bankshield/mpc/service/MpcClientService.java");

        boolean hasPrivacyProtection = false;
        if (psiService.contains("null, // 不返回明文") || psiService.contains("hashData")) {
            hasPrivacyProtection = true;
        }
        if (jointQueryService.contains("hashBigInteger") || jointQueryService.contains("generateResultHash")) {
            hasPrivacyProtection = true;
        }
        if (mpcClientService.contains("hashString")) {
            hasPrivacyProtection = true;
        }

        if (hasPrivacyProtection) {
            pass("MPC服务实现隐私保护");
        } else {
            fail("MPC服务未实现隐私保护");
        }

        System.out.println();
    }

    private static void testInputValidation() {
        System.out.println("【安全扫描】输入验证安全");
        System.out.println("-------------------------------------------");

        // 检查参数验证
        Set<String> controllerFiles = findFiles("**/controller/*.java");
        int validatedControllers = 0;

        for (String controller : controllerFiles) {
            String content = readFile(controller);
            if (content.contains("@Valid") || content.contains("@NotNull") ||
                content.contains("validate") || content.contains("check")) {
                validatedControllers++;
            }
        }

        if (validatedControllers >= 5) {
            pass("输入验证充足: " + validatedControllers + " 个控制器");
        } else {
            fail("输入验证不足: " + validatedControllers + " 个控制器");
        }

        // 检查SQL注入防护
        Set<String> mapperFiles = findFiles("**/mapper/*.java");
        int safeMappers = 0;

        for (String mapper : mapperFiles) {
            String content = readFile(mapper);
            // 检查使用注解或XML参数化查询
            if (content.contains("@Param") || content.contains("#{") || content.contains("${")) {
                safeMappers++;
            }
        }

        if (safeMappers >= 10) {
            pass("SQL注入防护充足: " + safeMappers + " 个Mapper");
        } else {
            fail("SQL注入防护不足: " + safeMappers + " 个Mapper");
        }

        System.out.println();
    }

    private static void testLoggingSecurity() {
        System.out.println("【安全扫描】日志安全");
        System.out.println("-------------------------------------------");

        // 检查敏感信息日志
        Set<String> javaFiles = findFiles("**/*.java");
        int sensitiveLogging = 0;

        for (String file : javaFiles) {
            String content = readFile(file);
            // 检查是否记录密码、密钥等敏感信息
            if (content.contains("log.info") || content.contains("log.debug")) {
                String[] lines = content.split("\n");
                for (String line : lines) {
                    if ((line.contains("password") || line.contains("secret") || line.contains("key")) &&
                        line.contains("log.") && !line.trim().startsWith("//")) {
                        sensitiveLogging++;
                        break;
                    }
                }
            }
        }

        if (sensitiveLogging < 3) {
            pass("敏感信息日志较少: " + sensitiveLogging);
        } else {
            warn("发现敏感信息日志: " + sensitiveLogging);
        }

        // 检查日志级别配置
        String applicationProps = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-common/src/main/resources/application.properties");
        if (applicationProps.contains("logging.level") || applicationProps.contains("logback")) {
            pass("日志级别已配置");
        } else {
            fail("日志级别未配置");
        }

        System.out.println();
    }

    private static Set<String> findFiles(String pattern) {
        try {
            Path rootPath = Paths.get("/Users/zhangyanlong/workspaces/BankShield");
            return Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .map(p -> p.toString())
                .filter(p -> p.matches(".*" + pattern.replace("**", ".*").replace("*", "[^/]*") + ".*"))
                .collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private static String readFile(String path) {
        try {
            Path filePath = Paths.get(path);
            return Files.exists(filePath) ? Files.readString(filePath) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static void pass(String message) {
        passed++;
        testResults.add("✅ PASS: " + message);
        System.out.println("  ✅ " + message);
    }

    private static void fail(String message) {
        failed++;
        testResults.add("❌ FAIL: " + message);
        System.out.println("  ❌ " + message);
    }

    private static void warn(String message) {
        warnings++;
        testResults.add("⚠️  WARN: " + message);
        System.out.println("  ⚠️  " + message);
    }

    private static void printSummary() {
        System.out.println("\n===========================================");
        System.out.println("          安全扫描测试总结");
        System.out.println("===========================================");
        System.out.println("总测试数: " + (passed + failed + warnings));
        System.out.println("通过: " + passed + " ✅");
        System.out.println("失败: " + failed + " ❌");
        System.out.println("警告: " + warnings + " ⚠️");
        System.out.println("通过率: " + String.format("%.1f%%", (passed * 100.0 / (passed + failed))));
        System.out.println("===========================================\n");

        if (failed > 0) {
            System.out.println("失败的安全检查:");
            for (String result : testResults) {
                if (result.startsWith("❌")) {
                    System.out.println("  " + result);
                }
            }
        }

        if (warnings > 0) {
            System.out.println("\n安全警告:");
            for (String result : testResults) {
                if (result.startsWith("⚠️")) {
                    System.out.println("  " + result);
                }
            }
        }

        System.out.println("\n✅ 安全扫描测试完成！");
    }
}
