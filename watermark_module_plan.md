# 数据水印模块开发计划

## 功能概述
在数据中嵌入不可见标记，用于追踪数据泄露源头。

## 后端实现

### 实体类
1. WatermarkTemplate - 水印模板
2. WatermarkTask - 水印任务
3. WatermarkExtractLog - 水印提取日志

### 核心功能
1. **文本水印**：支持PDF、Word、Excel、图片
2. **图像水印**：透明度混合算法
3. **数据库水印**：伪列、数据微小扰动

### API接口
- POST /api/watermark/template - 创建水印模板
- GET /api/watermark/task/{id} - 查询水印任务状态
- POST /api/watermark/extract - 提取水印

## 前端页面
1. 水印模板管理（创建、编辑、预览）
2. 水印任务管理（创建任务、查看进度、下载）
3. 水印提取溯源（上传文件、提取水印）

## 技术要点
- Apache PDFBox处理PDF水印
- POI处理Office文档
- Thumbnails处理图片水印
- 数据库水印：基于伪列和数据扰动
