# BankShield 数据水印模块

## 概述

BankShield数据水印模块提供了完整的数据水印解决方案，支持在文档、图片和数据库中嵌入不可见的水印信息，实现数据来源追溯和完整性验证。

## 功能特性

### 1. 水印模板管理
- **多种水印类型**：支持文本水印、图像水印、数据库水印
- **灵活配置**：可自定义水印位置、透明度、字体样式等参数
- **模板复用**：创建一次模板，多次使用
- **实时预览**：支持水印效果实时预览

### 2. 水印任务管理
- **批量处理**：支持批量文件水印处理
- **异步执行**：任务异步执行，不影响系统性能
- **进度跟踪**：实时跟踪任务执行进度
- **失败重试**：支持失败任务重试机制

### 3. 水印提取溯源
- **智能提取**：自动识别文件类型并提取水印
- **批量提取**：支持多个文件同时提取
- **完整性验证**：验证水印内容的完整性
- **提取日志**：完整的提取操作日志记录

### 4. 支持的文件格式
- **文档格式**：PDF、Word（.doc/.docx）、Excel（.xls/.xlsx）
- **图片格式**：JPG、JPEG、PNG、GIF、BMP
- **数据库**：支持主流关系型数据库

## 技术架构

### 后端架构
```
bankshield-api
├── entity/                 # 实体类
│   ├── WatermarkTemplate   # 水印模板实体
│   ├── WatermarkTask       # 水印任务实体
│   └── WatermarkExtractLog # 提取日志实体
├── mapper/                 # 数据访问层
│   ├── WatermarkTemplateMapper
│   ├── WatermarkTaskMapper
│   └── WatermarkExtractLogMapper
├── service/                # 业务逻辑层
│   ├── WatermarkTemplateService
│   ├── WatermarkTaskService
│   ├── WatermarkExtractService
│   ├── WatermarkEmbeddingEngine  # 水印嵌入引擎
│   └── WatermarkExtractEngine    # 水印提取引擎
├── controller/             # 控制层
│   └── WatermarkController
├── job/                    # 定时任务
│   └── WatermarkEmbeddingJob
└── config/                 # 配置类
    └── WatermarkConfig
```

### 前端架构
```
bankshield-ui
├── api/
│   └── watermark.ts        # 水印相关API
├── types/
│   └── watermark.d.ts      # TypeScript类型定义
└── views/security/
    ├── watermark-template/     # 水印模板管理
    ├── watermark-task/         # 水印任务管理
    └── watermark-extract/      # 水印提取溯源
```

## 快速开始

### 1. 环境准备
- Java 8+
- Spring Boot 2.7+
- MySQL 5.7+
- Node.js 16+

### 2. 数据库初始化
执行SQL脚本：`/sql/watermark_init.sql`

### 3. 配置文件
在`application.yml`中添加水印配置：
```yaml
watermark:
  output:
    path: /app/watermark/output
  upload:
    path: /app/watermark/upload
  image:
    path: /app/watermark/images
  extract:
    sensitivity: 0.8
    max-file-size: 50
  embed:
    default-transparency: 30
    default-font-size: 12
```

### 4. 启动服务
```bash
# 启动后端服务
cd bankshield-api
mvn spring-boot:run

# 启动前端服务
cd bankshield-ui
npm run dev
```

## API接口文档

### 水印模板管理

#### 创建水印模板
```http
POST /api/watermark/template
Content-Type: application/json

{
  "templateName": "银行内部文档水印",
  "watermarkType": "TEXT",
  "watermarkContent": "BankShield - 内部文档 - {{TIMESTAMP}}",
  "watermarkPosition": "BOTTOM_RIGHT",
  "transparency": 30,
  "fontSize": 12,
  "fontColor": "#CCCCCC",
  "fontFamily": "Arial",
  "description": "银行内部文档默认水印"
}
```

#### 获取模板列表
```http
GET /api/watermark/template/page?page=1&size=10&templateName=银行&watermarkType=TEXT&enabled=1
```

#### 更新模板
```http
PUT /api/watermark/template
Content-Type: application/json

{
  "id": 1,
  "templateName": "更新后的模板名称",
  "watermarkContent": "更新后的水印内容"
}
```

#### 删除模板
```http
DELETE /api/watermark/template/{id}
```

#### 启用/禁用模板
```http
PUT /api/watermark/template/{id}/enable?enabled=1
```

### 水印任务管理

#### 创建水印任务
```http
POST /api/watermark/task
Content-Type: application/json

{
  "taskName": "2024年Q1财务报表水印处理",
  "taskType": "FILE",
  "templateId": 3,
  "createdBy": "admin"
}
```

#### 获取任务列表
```http
GET /api/watermark/task/page?page=1&size=10&taskName=财务&taskType=FILE&status=SUCCESS
```

#### 获取任务详情
```http
GET /api/watermark/task/{id}
```

#### 获取任务进度
```http
GET /api/watermark/task/{id}/progress
```

#### 取消任务
```http
PUT /api/watermark/task/{id}/cancel
```

#### 重试任务
```http
PUT /api/watermark/task/{id}/retry
```

#### 下载任务结果
```http
GET /api/watermark/task/{id}/download
```

### 水印提取溯源

#### 提取文件水印
```http
POST /api/watermark/extract
Content-Type: multipart/form-data

file: <二进制文件数据>
operator: admin
```

#### 批量提取水印
```http
POST /api/watermark/extract/batch
Content-Type: multipart/form-data

files: <多个文件>
operator: admin
```

#### 验证文件是否包含水印
```http
POST /api/watermark/extract/verify
Content-Type: multipart/form-data

file: <二进制文件数据>
```

#### 获取提取日志
```http
GET /api/watermark/extract/log/page?page=1&size=10&extractResult=SUCCESS&operator=admin
```

#### 获取提取统计
```http
GET /api/watermark/extract/statistics?days=7
```

## 使用示例

### 1. 创建文本水印模板
```java
WatermarkTemplate template = WatermarkTemplate.builder()
    .templateName("银行内部文档水印")
    .watermarkType("TEXT")
    .watermarkContent("BankShield - 内部文档 - {{TIMESTAMP}}")
    .watermarkPosition("BOTTOM_RIGHT")
    .transparency(30)
    .fontSize(12)
    .fontColor("#CCCCCC")
    .fontFamily("Arial")
    .enabled(1)
    .createdBy("admin")
    .description("银行内部文档默认水印")
    .build();

WatermarkTemplate createdTemplate = templateService.createTemplate(template);
```

### 2. 创建水印任务
```java
WatermarkTask task = WatermarkTask.builder()
    .taskName("2024年Q1财务报表水印处理")
    .taskType("FILE")
    .templateId(template.getId())
    .createdBy("admin")
    .build();

WatermarkTask createdTask = taskService.createTask(task);
```

### 3. 异步执行任务
```java
CompletableFuture<Void> future = taskService.executeTaskAsync(createdTask.getId());
// 等待任务完成
future.get();
```

### 4. 提取水印
```java
MultipartFile file = // 从上传获取的文件
WatermarkExtractLog result = extractService.extractFromFile(file, "admin");

if ("SUCCESS".equals(result.getExtractResult())) {
    String watermarkContent = result.getWatermarkContent();
    // 处理提取的水印内容
}
```

## 水印内容格式

### 文本水印
支持使用模板变量：
- `{{TIMESTAMP}}` - 当前时间戳
- `{{USER}}` - 当前用户
- `{{DATE}}` - 当前日期

示例：
```
BankShield - 内部文档 - {{TIMESTAMP}} - {{USER}}
```

### 图像水印
支持PNG、JPG等格式，建议使用透明背景的PNG图片以获得更好的视觉效果。

### 数据库水印
使用伪列方式，不修改原始数据：
```sql
-- 伪列示例
ALTER TABLE customer_info ADD COLUMN wm_watermark_data VARCHAR(255);
```

## 性能优化

### 1. 异步处理
所有水印嵌入任务都采用异步方式执行，避免阻塞主线程。

### 2. 批量处理
支持批量文件处理，减少系统开销。

### 3. 缓存机制
任务进度和结果会被缓存，提高响应速度。

### 4. 数据库优化
- 合理的索引设计
- 批量操作优化
- 连接池配置

## 安全考虑

### 1. 文件上传安全
- 文件类型验证
- 文件大小限制
- 病毒扫描（建议集成）

### 2. 水印内容安全
- 敏感信息脱敏
- 内容长度限制
- 格式验证

### 3. 权限控制
- 基于角色的访问控制
- 操作权限验证
- 审计日志记录

## 监控与运维

### 1. 日志监控
- 任务执行日志
- 错误日志记录
- 性能指标监控

### 2. 任务监控
- 任务队列监控
- 执行成功率监控
- 异常告警

### 3. 资源监控
- 磁盘空间监控
- 内存使用监控
- CPU使用率监控

## 故障排查

### 常见问题

#### 1. 水印嵌入失败
- 检查文件格式是否支持
- 验证模板配置是否正确
- 查看任务执行日志

#### 2. 水印提取失败
- 检查文件是否包含水印
- 验证提取算法参数
- 检查文件完整性

#### 3. 任务执行超时
- 检查文件大小
- 优化处理算法
- 调整超时配置

## 扩展开发

### 1. 添加新的文件格式支持
1. 在`WatermarkEmbeddingEngine`中添加新的处理方法
2. 在`WatermarkExtractEngine`中添加对应的提取方法
3. 更新支持的文件类型列表

### 2. 自定义水印算法
1. 继承`WatermarkEmbeddingEngine`或`WatermarkExtractEngine`
2. 实现自定义的嵌入/提取逻辑
3. 注册到Spring容器中

### 3. 添加新的模板变量
1. 在`prepareWatermarkContent`方法中添加变量处理逻辑
2. 更新前端模板编辑界面
3. 添加相应的验证规则

## 版本更新记录

### v1.0.0 (2024-01-15)
- 初始版本发布
- 支持文本、图像、数据库水印
- 完整的CRUD操作
- 异步任务处理
- 批量处理支持

## 技术支持

如遇到问题，请通过以下方式获取支持：
- 查看系统日志
- 联系开发团队
- 提交Issue到项目仓库

## 许可证

本项目采用商业许可证，详情请查看LICENSE文件。