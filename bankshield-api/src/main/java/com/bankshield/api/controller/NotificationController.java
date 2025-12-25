package com.bankshield.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.common.result.Result;
import com.bankshield.api.entity.NotificationConfig;
import com.bankshield.api.mapper.NotificationConfigMapper;
import com.bankshield.api.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知配置接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@Api(tags = "通知配置管理")
public class NotificationController {

    @Autowired
    private NotificationConfigMapper notificationConfigMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * 检查用户是否已认证
     */
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查认证状态，未认证返回错误结果
     */
    private <T> Result<T> checkAuth() {
        if (!isAuthenticated()) {
            log.warn("未授权访问通知配置接口");
            return Result.error(401, "未授权访问，请先登录");
        }
        return null;
    }

    @GetMapping("/config/page")
    @ApiOperation("分页查询通知配置")
    public Result<IPage<NotificationConfig>> getNotificationConfigPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("通知类型") @RequestParam(required = false) String notifyType,
            @ApiParam("启用状态") @RequestParam(required = false) Integer enabled) {

        // 检查认证
        Result<IPage<NotificationConfig>> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            Page<NotificationConfig> pageParam = new Page<>(page, size);
            IPage<NotificationConfig> result = notificationConfigMapper.selectPage(pageParam, notifyType, enabled);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询通知配置失败", e);
            return Result.error("分页查询通知配置失败");
        }
    }

    @GetMapping("/config/{id}")
    @ApiOperation("获取通知配置详情")
    public Result<NotificationConfig> getNotificationConfig(@PathVariable Long id) {
        // 检查认证
        Result<NotificationConfig> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            NotificationConfig config = notificationConfigMapper.selectById(id);
            if (config == null) {
                return Result.error("通知配置不存在");
            }
            // 隐藏敏感配置参数
            config.setConfigParams(null);
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取通知配置详情失败", e);
            return Result.error("获取通知配置详情失败");
        }
    }

    @PostMapping("/config")
    @ApiOperation("创建通知配置")
    public Result<String> createNotificationConfig(@RequestBody NotificationConfig config) {
        // 检查认证
        Result<String> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            // 参数验证
            if (config.getNotifyType() == null || config.getNotifyType().trim().isEmpty()) {
                return Result.error("通知类型不能为空");
            }
            if (config.getRecipients() == null || config.getRecipients().trim().isEmpty()) {
                return Result.error("接收人不能为空");
            }

            // 设置默认值
            if (config.getEnabled() == null) {
                config.setEnabled(1);
            }
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());

            int result = notificationConfigMapper.insert(config);
            if (result > 0) {
                log.info("创建通知配置成功: {} - {}", config.getNotifyType(), config.getRecipients());
                return Result.success("创建通知配置成功");
            } else {
                return Result.error("创建通知配置失败");
            }
        } catch (Exception e) {
            log.error("创建通知配置失败", e);
            return Result.error("创建通知配置失败");
        }
    }

    @PutMapping("/config/{id}")
    @ApiOperation("更新通知配置")
    public Result<String> updateNotificationConfig(@PathVariable Long id, @RequestBody NotificationConfig config) {
        // 检查认证
        Result<String> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            // 检查配置是否存在
            NotificationConfig existingConfig = notificationConfigMapper.selectById(id);
            if (existingConfig == null) {
                return Result.error("通知配置不存在");
            }

            // 参数验证
            if (config.getNotifyType() == null || config.getNotifyType().trim().isEmpty()) {
                return Result.error("通知类型不能为空");
            }
            if (config.getRecipients() == null || config.getRecipients().trim().isEmpty()) {
                return Result.error("接收人不能为空");
            }

            config.setId(id);
            config.setUpdateTime(LocalDateTime.now());

            int result = notificationConfigMapper.updateById(config);
            if (result > 0) {
                log.info("更新通知配置成功: {} - {}", config.getNotifyType(), config.getRecipients());
                return Result.success("更新通知配置成功");
            } else {
                return Result.error("更新通知配置失败");
            }
        } catch (Exception e) {
            log.error("更新通知配置失败", e);
            return Result.error("更新通知配置失败");
        }
    }

    @DeleteMapping("/config/{id}")
    @ApiOperation("删除通知配置")
    public Result<String> deleteNotificationConfig(@PathVariable Long id) {
        // 检查认证
        Result<String> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            NotificationConfig existingConfig = notificationConfigMapper.selectById(id);
            if (existingConfig == null) {
                return Result.error("通知配置不存在");
            }

            int result = notificationConfigMapper.deleteById(id);
            if (result > 0) {
                log.info("删除通知配置成功: {} - {}", existingConfig.getNotifyType(), existingConfig.getRecipients());
                return Result.success("删除通知配置成功");
            } else {
                return Result.error("删除通知配置失败");
            }
        } catch (Exception e) {
            log.error("删除通知配置失败", e);
            return Result.error("删除通知配置失败");
        }
    }

    @PutMapping("/config/{id}/enable")
    @ApiOperation("启用/禁用通知配置")
    public Result<String> toggleNotificationConfig(@PathVariable Long id, @RequestParam Integer enabled) {
        // 检查认证
        Result<String> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            NotificationConfig existingConfig = notificationConfigMapper.selectById(id);
            if (existingConfig == null) {
                return Result.error("通知配置不存在");
            }

            NotificationConfig updateConfig = new NotificationConfig();
            updateConfig.setId(id);
            updateConfig.setEnabled(enabled);
            updateConfig.setUpdateTime(LocalDateTime.now());

            int result = notificationConfigMapper.updateById(updateConfig);
            if (result > 0) {
                String status = enabled == 1 ? "启用" : "禁用";
                log.info("{}通知配置成功: {} - {}", status, existingConfig.getNotifyType(), existingConfig.getRecipients());
                return Result.success(status + "通知配置成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("启用/禁用通知配置失败", e);
            return Result.error("操作失败");
        }
    }

    @PostMapping("/test")
    @ApiOperation("测试通知配置")
    public Result<String> testNotificationConfig(@RequestBody NotificationConfig config) {
        // 检查认证
        Result<String> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            boolean success = notificationService.testNotificationConfig(config);
            if (success) {
                log.info("通知配置测试成功: {} - {}", config.getNotifyType(), config.getRecipients());
                return Result.success("通知配置测试成功");
            } else {
                return Result.error("通知配置测试失败");
            }
        } catch (Exception e) {
            log.error("测试通知配置失败", e);
            return Result.error("通知配置测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/config/{id}/test")
    @ApiOperation("测试指定通知配置")
    public Result<String> testNotificationConfigById(@PathVariable Long id) {
        // 检查认证
        Result<String> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            NotificationConfig config = notificationConfigMapper.selectById(id);
            if (config == null) {
                return Result.error("通知配置不存在");
            }

            boolean success = notificationService.testNotificationConfig(config);
            if (success) {
                log.info("通知配置测试成功: {} - {}", config.getNotifyType(), config.getRecipients());
                return Result.success("通知配置测试成功");
            } else {
                return Result.error("通知配置测试失败");
            }
        } catch (Exception e) {
            log.error("测试通知配置失败", e);
            return Result.error("通知配置测试失败: " + e.getMessage());
        }
    }

    @GetMapping("/config/enabled")
    @ApiOperation("获取所有启用的通知配置")
    public Result<List<NotificationConfig>> getEnabledNotificationConfigs() {
        // 检查认证
        Result<List<NotificationConfig>> authResult = checkAuth();
        if (authResult != null) {
            return authResult;
        }

        try {
            List<NotificationConfig> enabledConfigs = notificationConfigMapper.selectEnabledConfigs();
            // 隐藏敏感配置参数
            for (NotificationConfig config : enabledConfigs) {
                config.setConfigParams(null);
            }
            return Result.success(enabledConfigs);
        } catch (Exception e) {
            log.error("获取启用的通知配置失败", e);
            return Result.error("获取启用的通知配置失败");
        }
    }

    @GetMapping("/config/types")
    @ApiOperation("获取支持的通知类型")
    public Result<List<Map<String, String>>> getNotificationTypes() {
        try {
            List<Map<String, String>> types = new java.util.ArrayList<>();
            
            Map<String, String> email = new java.util.HashMap<>();
            email.put("code", "EMAIL");
            email.put("name", "邮件");
            types.add(email);
            
            Map<String, String> sms = new java.util.HashMap<>();
            sms.put("code", "SMS");
            sms.put("name", "短信");
            types.add(sms);
            
            Map<String, String> webhook = new java.util.HashMap<>();
            webhook.put("code", "WEBHOOK");
            webhook.put("name", "Webhook");
            types.add(webhook);
            
            Map<String, String> dingtalk = new java.util.HashMap<>();
            dingtalk.put("code", "DINGTALK");
            dingtalk.put("name", "钉钉");
            types.add(dingtalk);
            
            Map<String, String> wechat = new java.util.HashMap<>();
            wechat.put("code", "WECHAT");
            wechat.put("name", "企业微信");
            types.add(wechat);
            
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取通知类型失败", e);
            return Result.error("获取通知类型失败");
        }
    }
}