# BankShield 国密算法深度集成指南

## 概述

本指南详细说明了如何在BankShield系统中实现国密算法（SM2/SM3/SM4）的深度集成，全面替换国际算法（RSA/AES/SHA/MD5），确保系统符合等保三级测评要求。

## 国密算法简介

### SM2 - 非对称加密算法
- **用途**: 数字签名、密钥交换、身份认证
- **密钥长度**: 256位
- **特点**: 基于椭圆曲线密码学，安全性高，性能优异

### SM3 - 哈希算法
- **用途**: 数据完整性校验、密码存储、消息认证
- **输出长度**: 256位
- **特点**: 与SHA-256类似，但具有更好的安全性

### SM4 - 对称加密算法
- **用途**: 数据加密、配置文件加密、传输加密
- **密钥长度**: 128位
- **特点**: 与AES-128类似，支持多种加密模式

## 集成步骤

### 步骤1: 依赖配置

在`pom.xml`中添加国密算法支持：

```xml
<!-- 国密算法库 -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.77</version>
</dependency>

<!-- 国密SSL支持 -->
<dependency>
    <groupId>com.alipay.jc</groupId>
    <artifactId>tlcp-ssl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 步骤2: 配置国密算法

在`application.yml`中配置国密算法：

```yaml
bankshield:
  crypto:
    national:
      # 是否启用国密算法
      enabled: true
      
      # 是否强制使用国密算法（禁用国际算法）
      force-national-crypto: true
      
      # 密码编码器类型（SM3或BCrypt）
      password-encoder-type: SM3
      
      # SM4默认加密模式（ECB/CBC/CTR）
      sm4-default-mode: CBC
      
      # SSL配置
      ssl:
        # SSL协议版本（TLCP为国密SSL协议）
        protocol: TLCP
        
        # 国密SSL密钥库路径
        key-store-path: classpath:sm2-server.p12
        
        # 国密SSL密钥库密码
        key-store-password: ${KEY_STORE_PASSWORD}
        
        # 国密SSL加密套件
        ciphers:
          - ECC-SM2-WITH-SM4-SM3
          - ECDHE-SM2-WITH-SM4-SM3
```

### 步骤3: 生成国密证书

使用提供的脚本生成国密证书：

```bash
# 生成SM2证书
./scripts/generate-national-crypto-certs.sh
```

生成的证书文件：
- `certs/sm2-server.p12` - 服务器密钥库
- `certs/sm2-server.cer` - 服务器证书
- `certs/sm2-ca.p12` - CA密钥库
- `certs/sm2-ca.cer` - CA证书

### 步骤4: 替换密码存储

**替换前（BCrypt）:**
```java
@Autowired
private BCryptPasswordEncoder passwordEncoder;

public String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
}

public boolean matchesPassword(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
}
```

**替换后（SM3）:**
```java
@Autowired
private NationalCryptoManager cryptoManager;

public String encodePassword(String rawPassword) {
    return cryptoManager.encodePassword(rawPassword);
}

public boolean matchesPassword(String rawPassword, String encodedPassword) {
    return cryptoManager.matchesPassword(rawPassword, encodedPassword);
}
```

### 步骤5: 替换数据加密

**替换前（AES）:**
```java
public String encryptData(String data, String key) {
    return EncryptUtil.aesEncrypt(key, data);
}

public String decryptData(String encryptedData, String key) {
    return EncryptUtil.aesDecrypt(key, encryptedData);
}
```

**替换后（SM4）:**
```java
@Autowired
private NationalCryptoManager cryptoManager;

public String encryptData(String data, String key, String mode, String iv) {
    return cryptoManager.encryptData(data, key, mode, iv);
}

public String decryptData(String encryptedData, String key, String mode, String iv) {
    return cryptoManager.decryptData(encryptedData, key, mode, iv);
}
```

### 步骤6: 替换数字签名

**替换前（RSA）:**
```java
public String signData(String data, PrivateKey privateKey) {
    return EncryptUtil.rsaSign(data, privateKey);
}

public boolean verifySignature(String data, String signature, PublicKey publicKey) {
    return EncryptUtil.rsaVerify(data, signature, publicKey);
}
```

**替换后（SM2）:**
```java
@Autowired
private NationalCryptoManager cryptoManager;

public String signData(String data) {
    return cryptoManager.signData(data);
}

public boolean verifySignature(String data, String signature) {
    return cryptoManager.verifySignature(data, signature);
}
```

### 步骤7: 替换数据完整性校验

**替换前（SHA-256）:**
```java
public String calculateHash(String data) {
    return EncryptUtil.sha256Hash(data);
}
```

**替换后（SM3）:**
```java
@Autowired
private NationalCryptoManager cryptoManager;

public String calculateHash(String data) {
    return cryptoManager.hashData(data);
}
```

## 工具类使用

### SM2工具类

```java
// 生成密钥对
KeyPair keyPair = SM2Util.generateKeyPair();

// 公钥加密
String encrypted = SM2Util.encrypt(publicKey, plainText);

// 私钥解密
String decrypted = SM2Util.decrypt(privateKey, encryptedText);

// 私钥签名
String signature = SM2Util.sign(privateKey, data);

// 公钥验签
boolean verified = SM2Util.verify(publicKey, data, signature);
```

### SM3工具类

```java
// 计算哈希
String hash = SM3Util.hash(data);

// 生成密码哈希
String passwordHash = SM3Util.generatePasswordHash(password);

// 验证密码哈希
boolean valid = SM3Util.verifyPasswordHash(password, storedHash);

// HMAC计算
String hmac = SM3Util.hmac(key, data);
```

### SM4工具类

```java
// 生成密钥
String key = SM4Util.generateKey();

// 生成IV
String iv = SM4Util.generateIV();

// ECB模式加密
String encrypted = SM4Util.encryptECB(key, plainText);

// CBC模式加密
String encrypted = SM4Util.encryptCBC(key, iv, plainText);

// CTR模式加密
String encrypted = SM4Util.encryptCTR(key, iv, plainText);
```

## 国密SSL配置

### 服务器配置

```yaml
server:
  ssl:
    enabled: true
    protocol: TLCP
    key-store: classpath:sm2-server.p12
    key-store-password: ${KEY_STORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: sm2-server
    ciphers:
      - ECC-SM2-WITH-SM4-SM3
      - ECDHE-SM2-WITH-SM4-SM3
```

### 客户端配置

```java
@Configuration
public class NationalSslConfig {
    
    @Bean
    public RestTemplate nationalCryptoRestTemplate() {
        // 配置国密SSL的RestTemplate
        // 需要加载SM2证书和私钥
        return new RestTemplate();
    }
}
```

## 测试验证

### 单元测试

运行所有国密算法单元测试：

```bash
# 运行SM2测试
mvn test -Dtest=SM2UtilTest

# 运行SM3测试
mvn test -Dtest=SM3UtilTest

# 运行SM4测试
mvn test -Dtest=SM4UtilTest

# 运行密码编码器测试
mvn test -Dtest=SM3PasswordEncoderTest
```

### 性能测试

运行性能测试验证指标：

```bash
# 运行性能测试
mvn test -Dtest=NationalCryptoPerformanceTest
```

### 合规性验证

生成合规报告：

```java
@Autowired
private NationalCryptoComplianceReport complianceReport;

public void generateReports() {
    // 生成完整合规报告
    complianceReport.generateCompleteComplianceReport("/path/to/reports");
    
    // 生成单独的算法使用清单
    String usageReport = complianceReport.generateAlgorithmUsageReport();
    
    // 生成国际算法清理报告
    String cleanupReport = complianceReport.generateInternationalAlgorithmCleanupReport();
    
    // 生成性能测试报告
    String performanceReport = complianceReport.generatePerformanceReport();
}
```

## 性能指标

| 算法 | 操作 | 性能指标 | 实测结果 |
|------|------|----------|----------|
| SM2 | 签名 | ≥1000次/秒 | ✓ 达标 |
| SM2 | 验签 | ≥1000次/秒 | ✓ 达标 |
| SM3 | 哈希 | ≥200MB/s | ✓ 达标 |
| SM3 | 密码编码 | <1ms | ✓ 达标 |
| SM4 | 加密 | ≥100MB/s | ✓ 达标 |
| SM4 | 解密 | ≥100MB/s | ✓ 达标 |

## 安全性要求

### 密钥管理
- 使用安全的随机数生成器
- 密钥长度符合国密标准
- 定期轮换密钥
- 安全的密钥存储

### 算法参数
- SM2: 256位密钥长度
- SM3: 256位哈希输出
- SM4: 128位密钥长度
- 随机盐: 16字节

### 合规性检查
- 禁用所有国际算法
- 使用国密算法标准实现
- 通过等保三级测评要求
- 定期安全评估

## 部署注意事项

### 环境要求
- Java 8 或更高版本
- BouncyCastle 1.77
- Spring Boot 2.7+
- 支持国密算法的安全提供程序

### 配置检查
- 验证国密算法启用状态
- 检查SSL协议配置
- 确认证书有效性
- 测试所有加密操作

### 监控告警
- 监控加密操作性能
- 跟踪错误日志
- 设置性能告警阈值
- 定期检查证书有效期

## 故障排除

### 常见问题

1. **国密算法不可用**
   - 检查BouncyCastle依赖
   - 验证安全提供程序注册
   - 确认算法名称正确

2. **SSL连接失败**
   - 检查证书配置
   - 验证密钥库密码
   - 确认SSL协议版本

3. **性能不达标**
   - 检查硬件配置
   - 优化算法参数
   - 启用硬件加速

### 调试建议

1. 启用国密算法调试日志
2. 使用性能分析工具
3. 监控系统资源使用
4. 定期进行压力测试

## 后续优化

### 短期优化
- 优化密钥管理流程
- 改进错误处理机制
- 增强日志记录
- 完善监控告警

### 长期规划
- 支持更多国密算法
- 集成硬件安全模块
- 实现算法自适应选择
- 支持量子安全算法

## 支持联系

如遇到问题，请联系：
- 技术支持: support@bankshield.com
- 安全咨询: security@bankshield.com
- 文档反馈: docs@bankshield.com