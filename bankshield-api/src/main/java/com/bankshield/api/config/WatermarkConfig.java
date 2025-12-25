package com.bankshield.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 水印模块配置类
 * 
 * @author BankShield
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "watermark")
public class WatermarkConfig {
    
    /**
     * 输出配置
     */
    private OutputConfig output = new OutputConfig();
    
    /**
     * 上传配置
     */
    private UploadConfig upload = new UploadConfig();
    
    /**
     * 图片配置
     */
    private ImageConfig image = new ImageConfig();
    
    /**
     * 提取配置
     */
    private ExtractConfig extract = new ExtractConfig();
    
    /**
     * 嵌入配置
     */
    private EmbedConfig embed = new EmbedConfig();
    
    /**
     * 任务配置
     */
    private TaskConfig task = new TaskConfig();
    
    /**
     * 数据库配置
     */
    private DatabaseConfig database = new DatabaseConfig();
    
    /**
     * 输出配置
     */
    @Data
    public static class OutputConfig {
        private String path = "/app/watermark/output";
    }
    
    /**
     * 上传配置
     */
    @Data
    public static class UploadConfig {
        private String path = "/app/watermark/upload";
    }
    
    /**
     * 图片配置
     */
    @Data
    public static class ImageConfig {
        private String path = "/app/watermark/images";
    }
    
    /**
     * 提取配置
     */
    @Data
    public static class ExtractConfig {
        private double sensitivity = 0.8;
        private int maxFileSize = 50;
        private String supportedTypes = "pdf,doc,docx,xls,xlsx,jpg,jpeg,png,gif,bmp";
    }
    
    /**
     * 嵌入配置
     */
    @Data
    public static class EmbedConfig {
        private int defaultTransparency = 30;
        private int defaultFontSize = 12;
        private String defaultFontColor = "#CCCCCC";
        private String defaultFontFamily = "Arial";
    }
    
    /**
     * 任务配置
     */
    @Data
    public static class TaskConfig {
        private int maxConcurrentTasks = 10;
        private int taskTimeout = 60;
        private int progressInterval = 5;
    }
    
    /**
     * 数据库配置
     */
    @Data
    public static class DatabaseConfig {
        private String columnPrefix = "wm_";
        private String pseudoColumn = "watermark_data";
        private int batchSize = 1000;
    }
}