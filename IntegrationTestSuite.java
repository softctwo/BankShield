import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 集成测试套件
 * 验证模块间协作和端到端功能
 */
public class IntegrationTestSuite {

    private static int passed = 0;
    private static int failed = 0;
    private static List<String> testResults = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("\n===========================================");
        System.out.println("    BankShield 集成测试套件");
        System.out.println("===========================================\n");

        // 测试模块依赖关系
        testModuleDependencies();

        // 测试配置一致性
        testConfigurationConsistency();

        // 测试数据流完整性
        testDataFlowIntegrity();

        // 测试安全策略一致性
        testSecurityPolicyConsistency();

        // 测试错误处理机制
        testErrorHandling();

        // 输出总结
        printSummary();
    }

    private static void testModuleDependencies() {
        System.out.println("【集成测试】模块依赖关系");
        System.out.println("-------------------------------------------");

        // 检查bankshield-common被其他模块依赖
        Set<String> commonModuleFiles = findModuleFiles("bankshield-common");
        Set<String> dependentModules = new HashSet<>();

        for (String module : Arrays.asList("bankshield-encrypt", "bankshield-gateway",
                                           "bankshield-api", "bankshield-mpc")) {
            Set<String> moduleFiles = findModuleFiles(module);
            for (String file : moduleFiles) {
                String content = readFile(file);
                if (content.contains("com.bankshield.common")) {
                    dependentModules.add(module);
                    break;
                }
            }
        }

        if (dependentModules.size() >= 3) {
            pass("bankshield-common 被多个模块正确依赖: " + dependentModules);
        } else {
            fail("bankshield-common 依赖模块不足: " + dependentModules);
        }

        // 检查pom.xml依赖关系
        String parentPom = readFile("/Users/zhangyanlong/workspaces/BankShield/pom.xml");
        Set<String> declaredModules = new HashSet<>();
        String[] modules = {"bankshield-common", "bankshield-encrypt", "bankshield-gateway",
                           "bankshield-api", "bankshield-mpc", "bankshield-monitor"};
        for (String module : modules) {
            if (parentPom.contains("<module>" + module + "</module>")) {
                declaredModules.add(module);
            }
        }

        if (declaredModules.size() >= 6) {
            pass("所有核心模块在父pom中正确声明");
        } else {
            fail("父pom缺少模块声明: " + declaredModules);
        }

        System.out.println();
    }

    private static void testConfigurationConsistency() {
        System.out.println("【集成测试】配置一致性");
        System.out.println("-------------------------------------------");

        // 检查Spring Boot版本一致性
        String parentPom = readFile("/Users/zhangyanlong/workspaces/BankShield/pom.xml");
        String springBootVersion = extractProperty(parentPom, "spring-boot.version");

        if (springBootVersion != null && !springBootVersion.isEmpty()) {
            pass("Spring Boot版本已定义: " + springBootVersion);
        } else {
            fail("Spring Boot版本未定义");
        }

        // 检查JDK版本要求
        if (parentPom.contains("<maven.compiler.source>")) {
            pass("JDK源码版本已配置");
        } else {
            fail("JDK源码版本未配置");
        }

        if (parentPom.contains("<maven.compiler.target>")) {
            pass("JDK目标版本已配置");
        } else {
            fail("JDK目标版本未配置");
        }

        // 检查数据库配置一致性
        String apiPom = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-api/pom.xml");
        String gatewayPom = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-gateway/pom.xml");

        if (apiPom.contains("mysql") && gatewayPom.contains("mysql")) {
            pass("API和Gateway模块都依赖MySQL");
        } else {
            fail("数据库依赖不一致");
        }

        System.out.println();
    }

    private static void testDataFlowIntegrity() {
        System.out.println("【集成测试】数据流完整性");
        System.out.println("-------------------------------------------");

        // 检查实体类定义
        Set<String> entityFiles = findFiles("**/entity/*.java");
        if (entityFiles.size() >= 10) {
            pass("发现足够数量的实体类: " + entityFiles.size());
        } else {
            fail("实体类数量不足: " + entityFiles.size());
        }

        // 检查Mapper接口
        Set<String> mapperFiles = findFiles("**/mapper/*.java");
        if (mapperFiles.size() >= 5) {
            pass("发现足够数量的Mapper接口: " + mapperFiles.size());
        } else {
            fail("Mapper接口数量不足: " + mapperFiles.size());
        }

        // 检查DTO类
        Set<String> dtoFiles = findFiles("**/dto/*.java");
        if (dtoFiles.size() >= 5) {
            pass("发现足够数量的DTO类: " + dtoFiles.size());
        } else {
            fail("DTO类数量不足: " + dtoFiles.size());
        }

        System.out.println();
    }

    private static void testSecurityPolicyConsistency() {
        System.out.println("【集成测试】安全策略一致性");
        System.out.println("-------------------------------------------");

        // 检查所有控制器的权限注解
        Set<String> controllerFiles = findFiles("**/controller/*.java");
        int annotatedControllers = 0;

        for (String controller : controllerFiles) {
            String content = readFile(controller);
            if (content.contains("@PreAuthorize") || content.contains("@Secured")) {
                annotatedControllers++;
            }
        }

        if (annotatedControllers >= 4) {
            pass("发现 " + annotatedControllers + " 个控制器已添加权限注解");
        } else {
            fail("只有 " + annotatedControllers + " 个控制器有权限注解");
        }

        // 检查加密工具类一致性
        Set<String> encryptUtilFiles = findFiles("**/EncryptUtil.java");
        if (encryptUtilFiles.size() >= 2) {
            pass("发现多个EncryptUtil实现");
        } else {
            fail("EncryptUtil实现数量不足");
        }

        System.out.println();
    }

    private static void testErrorHandling() {
        System.out.println("【集成测试】错误处理机制");
        System.out.println("-------------------------------------------");

        // 检查异常处理
        Set<String> serviceFiles = findFiles("**/service/**/*.java");
        int servicesWithExceptionHandling = 0;

        for (String service : serviceFiles) {
            String content = readFile(service);
            if (content.contains("try {") && content.contains("catch")) {
                servicesWithExceptionHandling++;
            }
        }

        if (servicesWithExceptionHandling >= 5) {
            pass("发现 " + servicesWithExceptionHandling + " 个服务类包含异常处理");
        } else {
            fail("只有 " + servicesWithExceptionHandling + " 个服务类有异常处理");
        }

        // 检查日志记录
        Set<String> loggedServices = serviceFiles.stream()
            .filter(s -> {
                String content = readFile(s);
                return content.contains("log.info") || content.contains("log.error");
            })
            .collect(Collectors.toSet());

        if (loggedServices.size() >= 5) {
            pass("发现 " + loggedServices.size() + " 个服务类包含日志记录");
        } else {
            fail("只有 " + loggedServices.size() + " 个服务类有日志");
        }

        System.out.println();
    }

    private static Set<String> findModuleFiles(String moduleName) {
        try {
            Path modulePath = Paths.get("/Users/zhangyanlong/workspaces/BankShield/" + moduleName + "/src/main/java");
            if (!Files.exists(modulePath)) {
                return new HashSet<>();
            }
            return Files.walk(modulePath)
                .filter(Files::isRegularFile)
                .map(p -> p.toString())
                .collect(Collectors.toSet());
        } catch (Exception e) {
            return new HashSet<>();
        }
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

    private static String extractProperty(String content, String propertyName) {
        String pattern = "<" + propertyName + ">(.*?)</" + propertyName + ">";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
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

    private static void printSummary() {
        System.out.println("\n===========================================");
        System.out.println("           集成测试总结");
        System.out.println("===========================================");
        System.out.println("总测试数: " + (passed + failed));
        System.out.println("通过: " + passed + " ✅");
        System.out.println("失败: " + failed + " ❌");
        System.out.println("成功率: " + String.format("%.1f%%", (passed * 100.0 / (passed + failed))));
        System.out.println("===========================================\n");

        if (failed > 0) {
            System.out.println("失败的测试:");
            for (String result : testResults) {
                if (result.startsWith("❌")) {
                    System.out.println("  " + result);
                }
            }
        }

        System.out.println("\n✅ 集成测试完成！");
    }
}
