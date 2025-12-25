package com.bankshield.api.watermark;

import com.bankshield.api.entity.WatermarkTemplate;
import com.bankshield.api.entity.WatermarkTask;
import com.bankshield.api.entity.WatermarkExtractLog;
import com.bankshield.api.enums.WatermarkType;
import com.bankshield.api.service.WatermarkTemplateService;
import com.bankshield.api.service.WatermarkTaskService;
import com.bankshield.api.service.WatermarkExtractService;
import com.bankshield.api.service.WatermarkEmbeddingEngine;
import com.bankshield.api.service.WatermarkExtractEngine;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 水印服务测试类
 * 
 * @author BankShield
 */
@Slf4j
@SpringBootTest
@Transactional
public class WatermarkServiceTest {

    @Autowired
    private WatermarkTemplateService templateService;

    @Autowired
    private WatermarkTaskService taskService;

    @Autowired
    private WatermarkExtractService extractService;

    @Autowired
    private WatermarkEmbeddingEngine embeddingEngine;

    @Autowired
    private WatermarkExtractEngine extractEngine;

    /**
     * 测试创建文本水印模板
     */
    @Test
    public void testCreateTextWatermarkTemplate() {
        WatermarkTemplate template = WatermarkTemplate.builder()
                .templateName("测试文本水印模板")
                .watermarkType(WatermarkType.TEXT.getCode())
                .watermarkContent("BankShield - 测试文档 - {{TIMESTAMP}}")
                .watermarkPosition("BOTTOM_RIGHT")
                .transparency(30)
                .fontSize(12)
                .fontColor("#CCCCCC")
                .fontFamily("Arial")
                .enabled(1)
                .createdBy("test_user")
                .description("测试文本水印模板描述")
                .build();

        WatermarkTemplate createdTemplate = templateService.createTemplate(template);
        
        assertNotNull(createdTemplate.getId());
        assertEquals("测试文本水印模板", createdTemplate.getTemplateName());
        assertEquals(WatermarkType.TEXT.getCode(), createdTemplate.getWatermarkType());
        log.info("创建文本水印模板成功，ID: {}", createdTemplate.getId());
    }

    /**
     * 测试创建图像水印模板
     */
    @Test
    public void testCreateImageWatermarkTemplate() {
        WatermarkTemplate template = WatermarkTemplate.builder()
                .templateName("测试图像水印模板")
                .watermarkType(WatermarkType.IMAGE.getCode())
                .watermarkContent("/assets/watermarks/test_logo.png")
                .watermarkPosition("TOP_LEFT")
                .transparency(20)
                .enabled(1)
                .createdBy("test_user")
                .description("测试图像水印模板描述")
                .build();

        WatermarkTemplate createdTemplate = templateService.createTemplate(template);
        
        assertNotNull(createdTemplate.getId());
        assertEquals("测试图像水印模板", createdTemplate.getTemplateName());
        assertEquals(WatermarkType.IMAGE.getCode(), createdTemplate.getWatermarkType());
        log.info("创建图像水印模板成功，ID: {}", createdTemplate.getId());
    }

    /**
     * 测试创建数据库水印模板
     */
    @Test
    public void testCreateDatabaseWatermarkTemplate() {
        WatermarkTemplate template = WatermarkTemplate.builder()
                .templateName("测试数据库水印模板")
                .watermarkType(WatermarkType.DATABASE.getCode())
                .watermarkContent("{{USER}}_{{TIMESTAMP}}")
                .enabled(1)
                .createdBy("test_user")
                .description("测试数据库水印模板描述")
                .build();

        WatermarkTemplate createdTemplate = templateService.createTemplate(template);
        
        assertNotNull(createdTemplate.getId());
        assertEquals("测试数据库水印模板", createdTemplate.getTemplateName());
        assertEquals(WatermarkType.DATABASE.getCode(), createdTemplate.getWatermarkType());
        log.info("创建数据库水印模板成功，ID: {}", createdTemplate.getId());
    }

    /**
     * 测试模板验证
     */
    @Test
    public void testTemplateValidation() {
        WatermarkTemplate validTemplate = WatermarkTemplate.builder()
                .templateName("有效模板")
                .watermarkType(WatermarkType.TEXT.getCode())
                .watermarkContent("测试内容")
                .watermarkPosition("BOTTOM_RIGHT")
                .transparency(50)
                .fontSize(12)
                .fontColor("#000000")
                .fontFamily("Arial")
                .build();

        assertTrue(templateService.validateTemplate(validTemplate));

        WatermarkTemplate invalidTemplate = WatermarkTemplate.builder()
                .templateName("")
                .watermarkType(WatermarkType.TEXT.getCode())
                .watermarkContent("测试内容")
                .build();

        assertFalse(templateService.validateTemplate(invalidTemplate));
        log.info("模板验证测试完成");
    }

    /**
     * 测试分页查询模板
     */
    @Test
    public void testGetTemplatePage() {
        // 先创建一些测试数据
        for (int i = 0; i < 5; i++) {
            WatermarkTemplate template = WatermarkTemplate.builder()
                    .templateName("测试模板" + i)
                    .watermarkType(WatermarkType.TEXT.getCode())
                    .watermarkContent("测试内容" + i)
                    .enabled(1)
                    .createdBy("test_user")
                    .build();
            templateService.createTemplate(template);
        }

        Page<WatermarkTemplate> page = new Page<>(1, 10);
        IPage<WatermarkTemplate> result = templateService.getTemplatePage(page, "测试模板", "TEXT", 1);
        
        assertNotNull(result);
        assertTrue(result.getRecords().size() > 0);
        log.info("分页查询模板成功，总记录数: {}", result.getTotal());
    }

    /**
     * 测试创建水印任务
     */
    @Test
    public void testCreateWatermarkTask() {
        // 先创建模板
        WatermarkTemplate template = WatermarkTemplate.builder()
                .templateName("任务测试模板")
                .watermarkType(WatermarkType.TEXT.getCode())
                .watermarkContent("任务测试内容")
                .enabled(1)
                .createdBy("test_user")
                .build();
        template = templateService.createTemplate(template);

        WatermarkTask task = WatermarkTask.builder()
                .taskName("测试水印任务")
                .taskType("FILE")
                .templateId(template.getId())
                .createdBy("test_user")
                .build();

        WatermarkTask createdTask = taskService.createTask(task);
        
        assertNotNull(createdTask.getId());
        assertEquals("测试水印任务", createdTask.getTaskName());
        assertEquals("PENDING", createdTask.getStatus());
        log.info("创建水印任务成功，ID: {}", createdTask.getId());
    }

    /**
     * 测试获取任务进度
     */
    @Test
    public void testGetTaskProgress() {
        // 创建任务
        WatermarkTemplate template = WatermarkTemplate.builder()
                .templateName("进度测试模板")
                .watermarkType(WatermarkType.TEXT.getCode())
                .watermarkContent("进度测试内容")
                .enabled(1)
                .createdBy("test_user")
                .build();
        template = templateService.createTemplate(template);

        WatermarkTask task = WatermarkTask.builder()
                .taskName("进度测试任务")
                .taskType("FILE")
                .templateId(template.getId())
                .createdBy("test_user")
                .build();
        task = taskService.createTask(task);

        int progress = taskService.getTaskProgress(task.getId());
        assertEquals(0, progress); // 新创建的任务进度为0
        log.info("任务进度: {}%", progress);
    }

    /**
     * 测试提取水印
     */
    @Test
    public void testExtractWatermark() throws Exception {
        // 创建一个简单的文本文件内容
        String testContent = "BankShield - 测试文档 - 2024-01-15 10:30:25";
        MockMultipartFile file = new MockMultipartFile(
                "test.txt",
                "test.txt",
                "text/plain",
                testContent.getBytes()
        );

        WatermarkExtractLog result = extractService.extractFromFile(file, "test_user");
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.getExtractResult());
        assertTrue(result.getWatermarkContent().contains("BankShield"));
        log.info("提取水印成功，内容: {}", result.getWatermarkContent());
    }

    /**
     * 测试验证水印完整性
     */
    @Test
    public void testVerifyWatermarkIntegrity() {
        String validWatermark = "BankShield - 内部文档 - 2024-01-15 10:30:25";
        String invalidWatermark = "这是普通文本内容";

        assertTrue(extractEngine.verifyWatermarkIntegrity(validWatermark));
        assertFalse(extractEngine.verifyWatermarkIntegrity(invalidWatermark));
        assertFalse(extractEngine.verifyWatermarkIntegrity(null));
        assertFalse(extractEngine.verifyWatermarkIntegrity(""));
        log.info("水印完整性验证测试完成");
    }

    /**
     * 测试解析水印内容
     */
    @Test
    public void testParseWatermarkContent() {
        String watermarkContent = "BankShield - 机密文档 - 2024-01-15 16:45:30 - admin";
        
        Map<String, Object> parsedContent = extractEngine.parseWatermarkContent(watermarkContent);
        
        assertNotNull(parsedContent);
        assertEquals(watermarkContent, parsedContent.get("rawContent"));
        assertEquals(true, parsedContent.get("hasBankShield"));
        log.info("解析水印内容成功: {}", parsedContent);
    }

    /**
     * 测试文件类型支持
     */
    @Test
    public void testFileTypeSupport() {
        List<String> supportedTypes = embeddingEngine.getSupportedFileTypes();
        
        assertTrue(supportedTypes.contains("pdf"));
        assertTrue(supportedTypes.contains("doc"));
        assertTrue(supportedTypes.contains("docx"));
        assertTrue(supportedTypes.contains("xls"));
        assertTrue(supportedTypes.contains("xlsx"));
        assertTrue(supportedTypes.contains("jpg"));
        assertTrue(supportedTypes.contains("png"));
        
        assertTrue(embeddingEngine.isFileTypeSupported("test.pdf"));
        assertTrue(embeddingEngine.isFileTypeSupported("document.docx"));
        assertFalse(embeddingEngine.isFileTypeSupported("unknown.xyz"));
        log.info("文件类型支持测试完成");
    }

    /**
     * 测试批量提取
     */
    @Test
    public void testBatchExtract() throws Exception {
        // 创建多个测试文件
        MockMultipartFile file1 = new MockMultipartFile(
                "test1.txt",
                "test1.txt",
                "text/plain",
                "BankShield - 测试文档1 - 2024-01-15 10:30:25".getBytes()
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
                "test2.txt",
                "test2.txt",
                "text/plain",
                "BankShield - 测试文档2 - 2024-01-15 14:20:10".getBytes()
        );

        MockMultipartFile[] files = new MockMultipartFile[]{file1, file2};
        List<WatermarkExtractLog> results = extractService.batchExtractFromFiles(files, "test_user");
        
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("SUCCESS", results.get(0).getExtractResult());
        assertEquals("SUCCESS", results.get(1).getExtractResult());
        log.info("批量提取成功，提取了 {} 个文件", results.size());
    }

    /**
     * 测试统计信息
     */
    @Test
    public void testExtractStatistics() {
        Map<String, Object> statistics = extractService.getExtractStatistics(7);
        
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalExtracts"));
        assertTrue(statistics.containsKey("successCount"));
        assertTrue(statistics.containsKey("failCount"));
        assertTrue(statistics.containsKey("successRate"));
        assertTrue(statistics.containsKey("dailyStats"));
        log.info("提取统计信息: {}", statistics);
    }
}