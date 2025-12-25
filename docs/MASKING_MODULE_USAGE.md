# 数据脱敏规则管理模块使用说明

## 模块概述

数据脱敏规则管理模块为BankShield系统提供了完整的敏感数据脱敏功能，支持多种脱敏算法和场景，确保敏感数据在展示、导出、查询等过程中的安全性。

## 功能特性

### 支持的敏感数据类型
- 手机号 (PHONE)
- 身份证号 (ID_CARD)
- 银行卡号 (BANK_CARD)
- 姓名 (NAME)
- 邮箱 (EMAIL)
- 地址 (ADDRESS)

### 支持的脱敏算法
- **部分掩码 (PARTIAL_MASK)**：保留前后部分字符，中间用掩码字符替换
- **完整掩码 (FULL_MASK)**：全部字符用掩码字符替换
- **哈希算法 (HASH)**：使用SM3或SHA256进行不可逆哈希
- **对称加密 (SYMMETRIC_ENCRYPT)**：使用国密SM4等对称加密算法
- **格式保留加密 (FORMAT_PRESERVING)**：保持数据格式不变，内容加密

### 支持的适用场景
- **页面展示 (DISPLAY)**：前端页面数据展示
- **数据导出 (EXPORT)**：数据导出到文件
- **查询结果 (QUERY)**：数据库查询结果
- **数据传输 (TRANSFER)**：系统间数据传输

## 后端使用示例

### 1. 创建脱敏规则

```java
@DataMaskingRule rule = DataMaskingRule.builder()
    .ruleName("手机号脱敏规则")
    .sensitiveDataType(SensitiveDataType.PHONE.getCode())
    .maskingAlgorithm(MaskingAlgorithm.PARTIAL_MASK.getCode())
    .algorithmParams("{\"keepPrefix\": 3, \"keepSuffix\": 4, \"maskChar\": \"*\"}")
    .applicableScenarios("DISPLAY,EXPORT,QUERY")
    .description("手机号保留前3位后4位，中间用*掩码")
    .build();

Result<String> result = maskingRuleService.createRule(rule, currentUser);
```

### 2. 使用脱敏引擎

```java
// 根据规则脱敏
String originalData = "13812345678";
String maskedData = maskingEngine.maskData(originalData, rule);
// 结果：138****5678

// 根据类型自动脱敏
String maskedData = maskingEngine.maskByType(originalData, "PHONE", "DISPLAY");
// 结果：138****5678

// 批量脱敏
List<String> dataList = Arrays.asList("13812345678", "13987654321");
List<String> maskedList = maskingEngine.maskDataList(dataList, rule);
// 结果：[138****5678, 139****4321]

// Map字段脱敏
Map<String, Object> dataMap = new HashMap<>();
dataMap.put("name", "张三");
dataMap.put("phone", "13812345678");
Map<String, Object> maskedMap = maskingEngine.maskMapField(dataMap, "phone", "PHONE", "DISPLAY");
```

### 3. 使用注解自动脱敏

#### 字段级脱敏（MyBatis拦截器）
```java
public class User {
    private String name;
    
    @MaskData(sensitiveType = "PHONE", scenario = "DISPLAY")
    private String phone;
    
    @MaskData(sensitiveType = "ID_CARD", scenario = "DISPLAY")
    private String idCard;
    
    @MaskData(sensitiveType = "EMAIL", scenario = "DISPLAY")
    private String email;
    
    // getters and setters
}
```

#### 方法级脱敏（AOP切面）
```java
@Service
public class UserService {
    
    @MaskData(sensitiveType = "PHONE", scenario = "DISPLAY")
    public String getUserPhone(Long userId) {
        // 查询用户手机号
        return "13812345678";
    }
    
    @MaskData(sensitiveType = "NAME", scenario = "EXPORT")
    public List<String> getUserNames() {
        // 查询用户姓名列表
        return Arrays.asList("张三", "李四", "王五");
    }
}
```

## 前端使用示例

### 1. API调用

```typescript
import { 
  getMaskingRulePage, 
  createMaskingRule, 
  testMaskingRule 
} from '@/api/masking'

// 分页查询脱敏规则
const params = {
  pageNum: 1,
  pageSize: 10,
  sensitiveDataType: 'PHONE',
  enabled: true
}
const res = await getMaskingRulePage(params)

// 创建脱敏规则
const rule: DataMaskingRule = {
  ruleName: '手机号脱敏规则',
  sensitiveDataType: 'PHONE',
  maskingAlgorithm: 'PARTIAL_MASK',
  algorithmParams: '{"keepPrefix": 3, "keepSuffix": 4, "maskChar": "*"}',
  applicableScenarios: 'DISPLAY,EXPORT,QUERY',
  description: '手机号保留前3位后4位'
}
const res = await createMaskingRule(rule)

// 测试脱敏规则
const testData: MaskingTestRequest = {
  testData: '13812345678',
  sensitiveDataType: 'PHONE',
  maskingAlgorithm: 'PARTIAL_MASK',
  algorithmParams: '{"keepPrefix": 3, "keepSuffix": 4, "maskChar": "*"}'
}
const res = await testMaskingRule(testData)
console.log(res.data.maskedData) // 138****5678
```

### 2. 页面组件使用

```vue
<template>
  <div>
    <!-- 脱敏规则管理页面 -->
    <MaskingRuleList />
    
    <!-- 新增/编辑规则弹窗 -->
    <MaskingRuleDialog 
      v-model="dialogVisible" 
      :rule="currentRule"
      @success="handleSuccess" />
    
    <!-- 测试规则弹窗 -->
    <MaskingTestDialog 
      v-model="testDialogVisible" 
      :rule="currentRule" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import MaskingRuleDialog from '@/views/classification/masking-rule/components/MaskingRuleDialog.vue'
import MaskingTestDialog from '@/views/classification/masking-rule/components/MaskingTestDialog.vue'

const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const currentRule = ref(null)

const handleSuccess = () => {
  // 处理成功回调
}
</script>
```

## 算法参数配置

### 部分掩码算法参数
```json
{
  "keepPrefix": 3,      // 保留前缀长度
  "keepSuffix": 4,      // 保留后缀长度
  "maskChar": "*",      // 掩码字符
  "maskLength": 0       // 掩码长度（可选，0表示自动计算）
}
```

### 哈希算法参数
```json
{
  "hashAlgorithm": "SM3"  // 哈希算法：SM3 或 SHA256
}
```

### 格式保留加密参数
```json
{
  "formatPreserveLength": 4  // 保留的字符总长度
}
```

## 数据库表结构

```sql
CREATE TABLE masking_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
  sensitive_data_type VARCHAR(50) NOT NULL COMMENT '敏感数据类型',
  masking_algorithm VARCHAR(50) NOT NULL COMMENT '脱敏算法',
  algorithm_params TEXT COMMENT '算法参数(JSON)',
  applicable_scenarios VARCHAR(200) COMMENT '适用场景',
  enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) COMMENT '创建人',
  description TEXT,
  INDEX idx_sensitive_type (sensitive_data_type),
  INDEX idx_enabled (enabled),
  UNIQUE KEY uk_rule_name (rule_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据脱敏规则表';
```

## 性能优化建议

1. **缓存机制**：脱敏规则会被缓存，避免频繁查询数据库
2. **批量处理**：支持批量脱敏，减少方法调用开销
3. **异步处理**：对于大量数据的脱敏，可以考虑异步处理
4. **规则预加载**：系统启动时预加载常用脱敏规则

## 安全注意事项

1. **规则管理权限**：脱敏规则的管理需要严格控制权限
2. **算法选择**：根据数据敏感度选择合适的脱敏算法
3. **不可逆算法**：对于高敏感数据，建议使用哈希等不可逆算法
4. **审计日志**：记录脱敏规则的创建、修改、删除操作
5. **测试验证**：规则生效前必须进行充分的测试验证

## 扩展开发

### 添加新的脱敏算法

1. 在`MaskingAlgorithm`枚举中添加新算法
2. 在`DataMaskingEngine`中实现算法逻辑
3. 在前端界面中添加对应的参数配置表单
4. 更新算法参数解析逻辑

### 添加新的敏感数据类型

1. 在`SensitiveDataType`枚举中添加新类型
2. 在前端界面中添加对应的类型选项
3. 更新相关的映射和显示逻辑

### 自定义脱敏拦截器

可以通过实现MyBatis的`Interceptor`接口来自定义脱敏逻辑：

```java
@Component
@Intercepts({
    @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class CustomMaskingInterceptor implements Interceptor {
    // 自定义脱敏逻辑
}
```

## 故障排查

### 常见问题

1. **脱敏不生效**：检查规则是否启用，参数是否正确
2. **性能问题**：检查是否频繁查询数据库，考虑增加缓存
3. **数据格式错误**：检查算法参数是否符合数据格式要求
4. **权限问题**：检查用户是否有相应的操作权限

### 调试方法

1. 开启DEBUG日志查看脱敏过程
2. 使用测试功能验证规则参数
3. 检查数据库中的规则配置
4. 验证MyBatis拦截器是否生效

## 联系支持

如有问题，请联系开发团队或提交Issue到项目仓库。