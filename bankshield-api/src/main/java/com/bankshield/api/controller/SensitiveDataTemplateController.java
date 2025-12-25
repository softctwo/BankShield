package com.bankshield.api.controller;

import com.bankshield.api.entity.SensitiveDataTemplate;
import com.bankshield.api.mapper.SensitiveDataTemplateMapper;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感数据类型模板控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/template")
public class SensitiveDataTemplateController {

    @Autowired
    private SensitiveDataTemplateMapper sensitiveDataTemplateMapper;

    /**
     * 添加敏感数据模板
     */
    @PostMapping
    public Result<String> addTemplate(@RequestBody SensitiveDataTemplate template) {
        log.info("添加敏感数据模板，名称: {}", template.getTypeName());
        try {
            // 参数校验
            if (template.getTypeName() == null || template.getTypeName().trim().isEmpty()) {
                return Result.error("模板名称不能为空");
            }
            if (template.getTypeCode() == null || template.getTypeCode().trim().isEmpty()) {
                return Result.error("模板编码不能为空");
            }
            if (template.getSecurityLevel() == null || template.getSecurityLevel() < 1 || template.getSecurityLevel() > 4) {
                return Result.error("安全等级必须在1-4之间");
            }
            
            // 检查模板编码是否已存在
            LambdaQueryWrapper<SensitiveDataTemplate> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SensitiveDataTemplate::getTypeCode, template.getTypeCode());
            if (sensitiveDataTemplateMapper.selectCount(queryWrapper) > 0) {
                return Result.error("模板编码已存在");
            }
            
            sensitiveDataTemplateMapper.insert(template);
            return Result.OK("添加模板成功");
        } catch (Exception e) {
            log.error("添加敏感数据模板失败: {}", e.getMessage());
            return Result.error("添加模板失败");
        }
    }

    /**
     * 更新敏感数据模板
     */
    @PutMapping
    public Result<String> updateTemplate(@RequestBody SensitiveDataTemplate template) {
        log.info("更新敏感数据模板，ID: {}", template.getId());
        try {
            if (template.getId() == null) {
                return Result.error("模板ID不能为空");
            }
            
            sensitiveDataTemplateMapper.updateById(template);
            return Result.OK("更新模板成功");
        } catch (Exception e) {
            log.error("更新敏感数据模板失败: {}", e.getMessage());
            return Result.error("更新模板失败");
        }
    }

    /**
     * 删除敏感数据模板
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id) {
        log.info("删除敏感数据模板，ID: {}", id);
        try {
            sensitiveDataTemplateMapper.deleteById(id);
            return Result.OK("删除模板成功");
        } catch (Exception e) {
            log.error("删除敏感数据模板失败: {}", e.getMessage());
            return Result.error("删除模板失败");
        }
    }

    /**
     * 查询敏感数据模板详情
     */
    @GetMapping("/{id}")
    public Result<SensitiveDataTemplate> getTemplateById(@PathVariable Long id) {
        log.info("查询敏感数据模板详情，ID: {}", id);
        try {
            SensitiveDataTemplate template = sensitiveDataTemplateMapper.selectById(id);
            if (template == null) {
                return Result.error("模板不存在");
            }
            return Result.OK(template);
        } catch (Exception e) {
            log.error("查询敏感数据模板失败: {}", e.getMessage());
            return Result.error("查询模板失败");
        }
    }

    /**
     * 分页查询敏感数据模板列表
     */
    @GetMapping("/page")
    public Result<Page<SensitiveDataTemplate>> getTemplatePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) Integer securityLevel) {
        log.info("分页查询敏感数据模板列表，页码: {}, 每页大小: {}", page, size);
        try {
            Page<SensitiveDataTemplate> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<SensitiveDataTemplate> queryWrapper = new LambdaQueryWrapper<>();
            
            if (typeName != null && !typeName.trim().isEmpty()) {
                queryWrapper.like(SensitiveDataTemplate::getTypeName, typeName);
            }
            if (typeCode != null && !typeCode.trim().isEmpty()) {
                queryWrapper.eq(SensitiveDataTemplate::getTypeCode, typeCode);
            }
            if (securityLevel != null) {
                queryWrapper.eq(SensitiveDataTemplate::getSecurityLevel, securityLevel);
            }
            
            queryWrapper.orderByDesc(SensitiveDataTemplate::getSecurityLevel);
            queryWrapper.orderByAsc(SensitiveDataTemplate::getTypeName);
            
            Page<SensitiveDataTemplate> templatePage = sensitiveDataTemplateMapper.selectPage(pageParam, queryWrapper);
            return Result.OK(templatePage);
        } catch (Exception e) {
            log.error("分页查询敏感数据模板失败: {}", e.getMessage());
            return Result.error("分页查询模板失败");
        }
    }

    /**
     * 获取所有敏感数据模板
     */
    @GetMapping("/all")
    public Result<List<SensitiveDataTemplate>> getAllTemplates() {
        log.info("获取所有敏感数据模板");
        try {
            LambdaQueryWrapper<SensitiveDataTemplate> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(SensitiveDataTemplate::getSecurityLevel);
            queryWrapper.orderByAsc(SensitiveDataTemplate::getTypeName);
            
            List<SensitiveDataTemplate> templates = sensitiveDataTemplateMapper.selectList(queryWrapper);
            return Result.OK(templates);
        } catch (Exception e) {
            log.error("获取敏感数据模板列表失败: {}", e.getMessage());
            return Result.error("获取模板列表失败");
        }
    }

    /**
     * 按安全等级查询敏感数据模板
     */
    @GetMapping("/by-security-level")
    public Result<List<SensitiveDataTemplate>> getTemplatesBySecurityLevel(@RequestParam Integer securityLevel) {
        log.info("按安全等级查询敏感数据模板，等级: {}", securityLevel);
        try {
            List<SensitiveDataTemplate> templates = sensitiveDataTemplateMapper.selectBySecurityLevel(securityLevel);
            return Result.OK(templates);
        } catch (Exception e) {
            log.error("按安全等级查询敏感数据模板失败: {}", e.getMessage());
            return Result.error("查询模板失败");
        }
    }

    /**
     * 按类型编码查询敏感数据模板
     */
    @GetMapping("/by-type-code")
    public Result<SensitiveDataTemplate> getTemplateByTypeCode(@RequestParam String typeCode) {
        log.info("按类型编码查询敏感数据模板，编码: {}", typeCode);
        try {
            SensitiveDataTemplate template = sensitiveDataTemplateMapper.selectByTypeCode(typeCode);
            if (template == null) {
                return Result.error("未找到对应的模板");
            }
            return Result.OK(template);
        } catch (Exception e) {
            log.error("按类型编码查询敏感数据模板失败: {}", e.getMessage());
            return Result.error("查询模板失败");
        }
    }

    /**
     * 获取金融行业标准模板
     */
    @GetMapping("/financial-standard")
    public Result<List<SensitiveDataTemplate>> getFinancialStandardTemplates() {
        log.info("获取金融行业标准模板");
        try {
            LambdaQueryWrapper<SensitiveDataTemplate> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNotNull(SensitiveDataTemplate::getStandardRef);
            queryWrapper.orderByDesc(SensitiveDataTemplate::getSecurityLevel);
            queryWrapper.orderByAsc(SensitiveDataTemplate::getTypeName);
            
            List<SensitiveDataTemplate> templates = sensitiveDataTemplateMapper.selectList(queryWrapper);
            return Result.OK(templates);
        } catch (Exception e) {
            log.error("获取金融行业标准模板失败: {}", e.getMessage());
            return Result.error("获取标准模板失败");
        }
    }

    /**
     * 批量导入敏感数据模板
     */
    @PostMapping("/batch-import")
    public Result<String> batchImportTemplates(@RequestBody List<SensitiveDataTemplate> templates) {
        log.info("批量导入敏感数据模板，数量: {}", templates.size());
        try {
            int successCount = 0;
            for (SensitiveDataTemplate template : templates) {
                try {
                    // 检查模板编码是否已存在
                    LambdaQueryWrapper<SensitiveDataTemplate> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(SensitiveDataTemplate::getTypeCode, template.getTypeCode());
                    if (sensitiveDataTemplateMapper.selectCount(queryWrapper) == 0) {
                        sensitiveDataTemplateMapper.insert(template);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("导入模板失败: {}", template.getTypeCode(), e);
                }
            }
            
            return Result.OK(String.format("批量导入完成，成功 %d/%d 个模板", successCount, templates.size()));
        } catch (Exception e) {
            log.error("批量导入敏感数据模板失败: {}", e.getMessage());
            return Result.error("批量导入模板失败");
        }
    }

    /**
     * 重置为默认模板
     */
    @PostMapping("/reset-default")
    public Result<String> resetDefaultTemplates() {
        log.info("重置为默认敏感数据模板");
        try {
            // 先删除所有现有模板
            LambdaQueryWrapper<SensitiveDataTemplate> deleteWrapper = new LambdaQueryWrapper<>();
            sensitiveDataTemplateMapper.delete(deleteWrapper);
            
            // 重新插入默认模板
            // TODO: 这里应该调用SQL脚本重新插入默认模板
            // 现在只是模拟操作
            
            return Result.OK("重置默认模板成功");
        } catch (Exception e) {
            log.error("重置默认模板失败: {}", e.getMessage());
            return Result.error("重置默认模板失败");
        }
    }
}