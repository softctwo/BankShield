package com.bankshield.demo;

import com.bankshield.demo.entity.User;
import com.bankshield.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

/**
 * BankShield演示应用主类
 *
 * 功能验证：
 * 1. 国密算法 (SM3, SM2, SM4)
 * 2. JWT认证机制
 * 3. 密码安全存储
 * 4. REST API接口
 * 5. Spring Boot Actuator健康检查
 * 6. MySQL数据库集成
 * 7. Redis缓存集成
 */
@SpringBootApplication
public class BankShieldDemoApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(BankShieldDemoApplication.class, args);
        System.out.println("🚀 BankShield演示应用启动成功！");
        System.out.println("📊 访问地址:");
        System.out.println("   - 健康检查: http://localhost:8080/actuator/health");
        System.out.println("   - API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("   - 加密API: http://localhost:8080/api/crypto");
        System.out.println("   - 用户管理: http://localhost:8080/api/user");
        System.out.println("   - 系统状态: http://localhost:8080/api/user/stats");
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🔧 初始化演示数据...");

        // 创建演示用户
        try {
            createDemoUsers();
            System.out.println("✅ 演示数据初始化完成！");
        } catch (Exception e) {
            System.out.println("⚠️ 演示数据初始化失败: " + e.getMessage());
        }

        System.out.println("\n🎯 可用测试接口:");
        System.out.println("   POST /api/user/create          - 创建用户");
        System.out.println("   GET  /api/user/findByUsername  - 根据用户名查找");
        System.out.println("   POST /api/user/login           - 用户登录验证");
        System.out.println("   GET  /api/user/stats           - 系统统计信息");
        System.out.println("   GET  /api/user/testDatabase    - 数据库连接测试");
        System.out.println("\n🧪 国密算法测试接口:");
        System.out.println("   POST /api/crypto/sm3/hash      - SM3哈希测试");
        System.out.println("   POST /api/crypto/sm4/encrypt   - SM4加密测试");
        System.out.println("   POST /api/crypto/sm2/encrypt   - SM2加密测试");
        System.out.println("   POST /api/crypto/batch/test    - 批量加密测试");
    }

    private void createDemoUsers() {
        // 创建管理员用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("Admin123!");
        adminUser.setName("管理员");
        adminUser.setEmail("admin@bankshield.com");
        adminUser.setPhone("13800138000");
        adminUser.setRealName("系统管理员");
        adminUser.setStatus(1);

        try {
            userService.createUser(adminUser);
            System.out.println("   ✅ 创建管理员用户: admin");
        } catch (Exception e) {
            System.out.println("   ℹ️ 管理员用户已存在: admin");
        }

        // 创建普通用户
        User normalUser = new User();
        normalUser.setUsername("user001");
        normalUser.setPassword("User123!");
        normalUser.setName("张三");
        normalUser.setEmail("user001@bankshield.com");
        normalUser.setPhone("13800138001");
        normalUser.setRealName("张三");
        normalUser.setStatus(1);

        try {
            userService.createUser(normalUser);
            System.out.println("   ✅ 创建普通用户: user001");
        } catch (Exception e) {
            System.out.println("   ℹ️ 普通用户已存在: user001");
        }

        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("Test123!");
        testUser.setName("测试用户");
        testUser.setEmail("test@bankshield.com");
        testUser.setPhone("13800138002");
        testUser.setRealName("测试用户");
        testUser.setStatus(1);

        try {
            userService.createUser(testUser);
            System.out.println("   ✅ 创建测试用户: testuser");
        } catch (Exception e) {
            System.out.println("   ℹ️ 测试用户已存在: testuser");
        }
    }
}