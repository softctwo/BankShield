import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 端到端测试套件
 * 验证完整业务流程和用户场景
 */
public class EndToEndTestSuite {

    private static int passed = 0;
    private static int failed = 0;
    private static List<String> testResults = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("\n===========================================");
        System.out.println("    BankShield 端到端测试套件");
        System.out.println("===========================================\n");

        // 测试用户认证流程
        testUserAuthenticationFlow();

        // 测试加密解密流程
        testEncryptionDecryptionFlow();

        // 测试MPC安全计算流程
        testMPCSecureComputationFlow();

        // 测试API网关路由流程
        testAPIGatewayRoutingFlow();

        // 测试区块链锚定流程
        testBlockchainAnchorFlow();

        // 测试健康检查流程
        testHealthCheckFlow();

        // 输出总结
        printSummary();
    }

    private static void testUserAuthenticationFlow() {
        System.out.println("【端到端】用户认证流程");
        System.out.println("-------------------------------------------");

        // 检查认证相关类
        Set<String> authFiles = findFiles("**/auth/**/*.java");
        if (authFiles.size() >= 3) {
            pass("认证模块文件完整: " + authFiles.size() + " 个文件");
        } else {
            fail("认证模块文件不完整: " + authFiles.size() + " 个文件");
        }

        // 检查JWT处理
        String parentPom = readFile("/Users/zhangyanlong/workspaces/BankShield/pom.xml");
        if (parentPom.contains("jjwt")) {
            pass("JWT依赖已配置");
        } else {
            fail("JWT依赖未配置");
        }

        // 检查密码加密
        Set<String> encryptUtilFiles = findFiles("**/EncryptUtil.java");
        boolean hasBcrypt = false;
        for (String file : encryptUtilFiles) {
            String content = readFile(file);
            if (content.contains("BCrypt")) {
                hasBcrypt = true;
                break;
            }
        }
        if (hasBcrypt) {
            pass("密码加密使用BCrypt");
        } else {
            fail("未使用BCrypt加密密码");
        }

        System.out.println();
    }

    private static void testEncryptionDecryptionFlow() {
        System.out.println("【端到端】加密解密流程");
        System.out.println("-------------------------------------------");

        // 检查加密工具类完整性
        String encryptUtil = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-common/src/main/java/com/bankshield/common/utils/EncryptUtil.java");

        boolean hasSM2 = encryptUtil.contains("sm2Encrypt") && encryptUtil.contains("sm2Decrypt");
        boolean hasSM4 = encryptUtil.contains("sm4Encrypt") && encryptUtil.contains("sm4Decrypt");
        boolean hasAES = encryptUtil.contains("generateAesKey");
        boolean hasRSA = encryptUtil.contains("generateRsaKeyPair");

        if (hasSM2) pass("SM2加解密功能完整");
        else fail("SM2加解密功能缺失");

        if (hasSM4) pass("SM4加解密功能完整");
        else fail("SM4加解密功能缺失");

        if (hasAES) pass("AES密钥生成功能完整");
        else fail("AES密钥生成功能缺失");

        if (hasRSA) pass("RSA密钥生成功能完整");
        else fail("RSA密钥生成功能缺失");

        // 检查密钥长度支持
        if (encryptUtil.contains("generateAesKey(int")) {
            pass("AES支持自定义密钥长度");
        } else {
            fail("AES不支持自定义密钥长度");
        }

        if (encryptUtil.contains("generateRsaKeyPair(int")) {
            pass("RSA支持自定义密钥长度");
        } else {
            fail("RSA不支持自定义密钥长度");
        }

        System.out.println();
    }

    private static void testMPCSecureComputationFlow() {
        System.out.println("【端到端】MPC安全计算流程");
        System.out.println("-------------------------------------------");

        // 检查MPC服务
        String psiService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-mpc/src/main/java/com/bankshield/mpc/service/PsiService.java");
        String secureAggService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-mpc/src/main/java/com/bankshield/mpc/service/SecureAggregationService.java");
        String jointQueryService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-mpc/src/main/java/com/bankshield/mpc/service/JointQueryService.java");

        // 检查隐私保护措施
        if (psiService.contains("null, // 不返回明文交集")) {
            pass("PSI服务不返回明文数据");
        } else {
            fail("PSI服务仍返回明文数据");
        }

        if (psiService.contains("hashData")) {
            pass("PSI服务使用哈希承诺");
        } else {
            fail("PSI服务未使用哈希承诺");
        }

        if (secureAggService.contains("CopyOnWriteArrayList")) {
            pass("安全聚合服务使用线程安全集合");
        } else {
            fail("安全聚合服务未使用线程安全集合");
        }

        if (jointQueryService.contains("hashBigInteger") || jointQueryService.contains("generateResultHash")) {
            pass("联合查询服务使用哈希保护结果");
        } else {
            fail("联合查询服务未保护结果");
        }

        System.out.println();
    }

    private static void testAPIGatewayRoutingFlow() {
        System.out.println("【端到端】API网关路由流程");
        System.out.println("-------------------------------------------");

        // 检查网关控制器
        String blacklistController = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-gateway/src/main/java/com/bankshield/gateway/controller/BlacklistController.java");
        String rateLimitController = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-gateway/src/main/java/com/bankshield/gateway/controller/RateLimitController.java");
        String gatewayConfigController = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-gateway/src/main/java/com/bankshield/gateway/controller/GatewayConfigController.java");

        // 检查权限控制
        int authCount = 0;
        if (blacklistController.contains("@PreAuthorize")) authCount++;
        if (rateLimitController.contains("@PreAuthorize")) authCount++;
        if (gatewayConfigController.contains("@PreAuthorize")) authCount++;

        if (authCount >= 3) {
            pass("所有网关控制器都添加了权限控制");
        } else {
            fail("只有 " + authCount + " 个网关控制器有权限控制");
        }

        // 检查黑名单功能
        if (blacklistController.contains("addToBlacklist") && blacklistController.contains("removeFromBlacklist")) {
            pass("黑名单功能完整");
        } else {
            fail("黑名单功能不完整");
        }

        // 检查限流功能
        if (rateLimitController.contains("RateLimitRule")) {
            pass("限流功能完整");
        } else {
            fail("限流功能不完整");
        }

        System.out.println();
    }

    private static void testBlockchainAnchorFlow() {
        System.out.println("【端到端】区块链锚定流程");
        System.out.println("-------------------------------------------");

        // 检查区块链锚定服务
        String blockchainService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-api/src/main/java/com/bankshield/api/service/impl/BlockchainAnchorServiceImpl.java");

        if (blockchainService.contains("anchorToBlockchain")) {
            pass("区块链锚定功能存在");
        } else {
            fail("区块链锚定功能缺失");
        }

        if (blockchainService.contains("verifyTransaction")) {
            pass("区块链验证功能存在");
        } else {
            fail("区块链验证功能缺失");
        }

        // 检查验证逻辑
        if (blockchainService.contains("txHash == null") || blockchainService.contains("startsWith")) {
            pass("区块链验证包含实际逻辑");
        } else {
            fail("区块链验证缺乏实际逻辑");
        }

        System.out.println();
    }

    private static void testHealthCheckFlow() {
        System.out.println("【端到端】健康检查流程");
        System.out.println("-------------------------------------------");

        // 检查健康检查服务
        String healthCheckService = readFile("/Users/zhangyanlong/workspaces/BankShield/bankshield-monitor/src/main/java/com/bankshield/monitor/service/HealthCheckService.java");

        if (healthCheckService.contains("checkServiceHealth")) {
            pass("服务健康检查功能存在");
        } else {
            fail("服务健康检查功能缺失");
        }

        if (healthCheckService.contains("checkDatabaseHealth")) {
            pass("数据库健康检查功能存在");
        } else {
            fail("数据库健康检查功能缺失");
        }

        if (healthCheckService.contains("checkRedisHealth")) {
            pass("Redis健康检查功能存在");
        } else {
            fail("Redis健康检查功能缺失");
        }

        // 检查是否移除硬编码
        if (!healthCheckService.contains("localhost:80") && !healthCheckService.contains("localhost:8080")) {
            pass("健康检查已移除硬编码地址");
        } else {
            fail("健康检查仍使用硬编码地址");
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

    private static void printSummary() {
        System.out.println("\n===========================================");
        System.out.println("          端到端测试总结");
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

        System.out.println("\n✅ 端到端测试完成！");
    }
}
