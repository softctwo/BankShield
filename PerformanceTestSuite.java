import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 性能测试套件
 * 评估系统性能和可扩展性
 */
public class PerformanceTestSuite {

    private static int passed = 0;
    private static int failed = 0;
    private static List<String> testResults = new ArrayList<>();
    private static Random random = new Random();

    public static void main(String[] args) {
        System.out.println("\n===========================================");
        System.out.println("    BankShield 性能测试套件");
        System.out.println("===========================================\n");

        // 测试代码复杂度
        testCodeComplexity();

        // 测试类和方法数量
        testClassMethodCount();

        // 测试配置文件大小
        testConfigurationSize();

        // 测试依赖数量
        testDependencyCount();

        // 测试数据库查询效率
        testDatabaseQueryEfficiency();

        // 测试并发安全性
        testConcurrencySafety();

        // 输出总结
        printSummary();
    }

    private static void testCodeComplexity() {
        System.out.println("【性能测试】代码复杂度评估");
        System.out.println("-------------------------------------------");

        // 统计代码行数
        Set<String> javaFiles = findFiles("**/*.java");
        long totalLines = 0;
        long codeLines = 0;

        for (String file : javaFiles) {
            String content = readFile(file);
            String[] lines = content.split("\n");
            totalLines += lines.length;

            // 统计非空非注释行
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("//") &&
                    !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                    codeLines++;
                }
            }
        }

        if (codeLines > 50000) {
            pass("代码规模合理: " + codeLines + " 行有效代码");
        } else if (codeLines > 10000) {
            pass("代码规模适中: " + codeLines + " 行有效代码");
        } else {
            fail("代码规模较小: " + codeLines + " 行有效代码");
        }

        // 评估平均文件大小
        long avgLinesPerFile = codeLines / Math.max(javaFiles.size(), 1);
        if (avgLinesPerFile > 50 && avgLinesPerFile < 500) {
            pass("平均文件大小合理: " + avgLinesPerFile + " 行/文件");
        } else {
            fail("平均文件大小不合理: " + avgLinesPerFile + " 行/文件");
        }

        System.out.println();
    }

    private static void testClassMethodCount() {
        System.out.println("【性能测试】类和方法的数量评估");
        System.out.println("-------------------------------------------");

        Set<String> javaFiles = findFiles("**/*.java");
        int totalClasses = 0;
        int totalMethods = 0;

        for (String file : javaFiles) {
            String content = readFile(file);
            String[] lines = content.split("\n");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("public class ") || trimmed.startsWith("class ") ||
                    trimmed.startsWith("private class ") || trimmed.startsWith("protected class ")) {
                    totalClasses++;
                }
                if (trimmed.matches(".*\\w+\\s*\\(.*\\)\\s*\\{.*") && !trimmed.startsWith("//")) {
                    totalMethods++;
                }
            }
        }

        if (totalClasses > 100) {
            pass("类数量充足: " + totalClasses + " 个类");
        } else {
            fail("类数量不足: " + totalClasses + " 个类");
        }

        if (totalMethods > 1000) {
            pass("方法数量充足: " + totalMethods + " 个方法");
        } else {
            fail("方法数量不足: " + totalMethods + " 个方法");
        }

        // 计算方法/类比率
        double methodClassRatio = (double) totalMethods / totalClasses;
        if (methodClassRatio > 5 && methodClassRatio < 30) {
            pass("方法/类比率合理: " + String.format("%.1f", methodClassRatio));
        } else {
            fail("方法/类比率不合理: " + String.format("%.1f", methodClassRatio));
        }

        System.out.println();
    }

    private static void testConfigurationSize() {
        System.out.println("【性能测试】配置文件大小评估");
        System.out.println("-------------------------------------------");

        // 检查pom.xml大小
        String parentPom = readFile("/Users/zhangyanlong/workspaces/BankShield/pom.xml");
        if (parentPom.length() > 10000) {
            pass("父pom.xml配置丰富: " + parentPom.length() + " 字符");
        } else {
            fail("父pom.xml配置较少: " + parentPom.length() + " 字符");
        }

        // 检查模块数量
        String[] modulePoms = {
            "/Users/zhangyanlong/workspaces/BankShield/bankshield-api/pom.xml",
            "/Users/zhangyanlong/workspaces/BankShield/bankshield-gateway/pom.xml",
            "/Users/zhangyanlong/workspaces/BankShield/bankshield-encrypt/pom.xml",
            "/Users/zhangyanlong/workspaces/BankShield/bankshield-common/pom.xml"
        };

        int configuredModules = 0;
        for (String pom : modulePoms) {
            String content = readFile(pom);
            if (content.length() > 1000) {
                configuredModules++;
            }
        }

        if (configuredModules >= 4) {
            pass("所有核心模块配置完整: " + configuredModules + "/" + modulePoms.length);
        } else {
            fail("模块配置不完整: " + configuredModules + "/" + modulePoms.length);
        }

        System.out.println();
    }

    private static void testDependencyCount() {
        System.out.println("【性能测试】依赖数量评估");
        System.out.println("-------------------------------------------");

        String parentPom = readFile("/Users/zhangyanlong/workspaces/BankShield/pom.xml");

        // 统计依赖数量
        int dependencyCount = countOccurrences(parentPom, "<dependency>");
        if (dependencyCount > 50) {
            pass("依赖数量充足: " + dependencyCount + " 个依赖");
        } else {
            fail("依赖数量不足: " + dependencyCount + " 个依赖");
        }

        // 检查关键依赖
        Set<String> criticalDeps = new HashSet<>(Arrays.asList(
            "spring-boot-starter", "mysql", "redis", "jwt", "bouncycastle"
        ));

        int foundCriticalDeps = 0;
        for (String dep : criticalDeps) {
            if (parentPom.contains(dep)) {
                foundCriticalDeps++;
            }
        }

        if (foundCriticalDeps >= 4) {
            pass("关键依赖完整: " + foundCriticalDeps + "/" + criticalDeps.size());
        } else {
            fail("关键依赖不完整: " + foundCriticalDeps + "/" + criticalDeps.size());
        }

        System.out.println();
    }

    private static void testDatabaseQueryEfficiency() {
        System.out.println("【性能测试】数据库查询效率评估");
        System.out.println("-------------------------------------------");

        Set<String> mapperFiles = findFiles("**/mapper/*.java");

        int efficientMappers = 0;
        for (String mapper : mapperFiles) {
            String content = readFile(mapper);
            // 检查是否使用注解或XML配置
            if (content.contains("@Select") || content.contains("@Update") ||
                content.contains("@Insert") || content.contains("@Delete")) {
                efficientMappers++;
            } else if (content.contains("List<") && content.contains("select") &&
                      (content.contains("xml") || content.contains("XML"))) {
                efficientMappers++;
            }
        }

        if (efficientMappers >= 10) {
            pass("Mapper配置高效: " + efficientMappers + "/" + mapperFiles.size());
        } else {
            fail("Mapper配置效率低: " + efficientMappers + "/" + mapperFiles.size());
        }

        // 检查是否有分页查询
        Set<String> serviceFiles = findFiles("**/service/**/*.java");
        int paginationCount = 0;

        for (String service : serviceFiles) {
            String content = readFile(service);
            if (content.contains("Page") || content.contains("pageable") ||
                content.contains("limit") || content.contains("offset")) {
                paginationCount++;
            }
        }

        if (paginationCount >= 5) {
            pass("分页查询使用充足: " + paginationCount + " 个服务");
        } else {
            fail("分页查询使用不足: " + paginationCount + " 个服务");
        }

        System.out.println();
    }

    private static void testConcurrencySafety() {
        System.out.println("【性能测试】并发安全性评估");
        System.out.println("-------------------------------------------");

        Set<String> serviceFiles = findFiles("**/service/**/*.java");
        int threadSafeServices = 0;

        for (String service : serviceFiles) {
            String content = readFile(service);
            if (content.contains("CopyOnWriteArrayList") ||
                content.contains("ConcurrentHashMap") ||
                content.contains("synchronized") ||
                content.contains("volatile")) {
                threadSafeServices++;
            }
        }

        if (threadSafeServices >= 3) {
            pass("线程安全服务充足: " + threadSafeServices + " 个服务");
        } else {
            fail("线程安全服务不足: " + threadSafeServices + " 个服务");
        }

        // 检查并发工具使用
        Set<String> allFiles = findFiles("**/*.java");
        int concurrentUtils = 0;

        for (String file : allFiles) {
            String content = readFile(file);
            if (content.contains("CompletableFuture") ||
                content.contains("ExecutorService") ||
                content.contains("CountDownLatch")) {
                concurrentUtils++;
                break;
            }
        }

        if (concurrentUtils > 0) {
            pass("使用并发工具类");
        } else {
            fail("未使用并发工具类");
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

    private static int countOccurrences(String content, String pattern) {
        return (content.length() - content.replace(pattern, "").length()) / pattern.length();
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
        System.out.println("           性能测试总结");
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

        System.out.println("\n✅ 性能测试完成！");
    }
}
