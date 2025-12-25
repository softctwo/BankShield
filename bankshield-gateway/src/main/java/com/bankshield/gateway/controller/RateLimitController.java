package com.bankshield.gateway.controller;

import com.bankshield.common.result.Result;
import com.bankshield.gateway.entity.RateLimitRule;
import com.bankshield.gateway.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 限流控制器 - 仅允许管理员访问
 *
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/gateway/rate-limit")
public class RateLimitController {
    
    @Autowired
    private RateLimitService rateLimitService;
    
    /**
     * 获取限流规则列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rules")
    public Result<Page<RateLimitRule>> getRules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) String limitDimension,
            @RequestParam(required = false) Boolean enabled) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
            
            // 如果有查询条件，使用自定义查询
            if (ruleName != null && !ruleName.trim().isEmpty()) {
                Optional<RateLimitRule> rule = rateLimitService.getRuleByName(ruleName);
                if (rule.isPresent()) {
                    List<RateLimitRule> rules = Arrays.asList(rule.get());
                    return Result.success(new PageImpl<>(rules, pageable, rules.size()));
                } else {
                    return Result.success(Page.empty(pageable));
                }
            }
            
            // 获取所有规则并进行过滤
            List<RateLimitRule> allRules = rateLimitService.getAllRules();
            
            if (limitDimension != null && !limitDimension.trim().isEmpty()) {
                allRules = allRules.stream()
                    .filter(rule -> limitDimension.equals(rule.getLimitDimension()))
                    .collect(Collectors.toList());
            }

            if (enabled != null) {
                allRules = allRules.stream()
                    .filter(rule -> enabled.equals(rule.getEnabled()))
                    .collect(Collectors.toList());
            }
            
            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allRules.size());
            List<RateLimitRule> pageContent = allRules.subList(start, end);
            
            Page<RateLimitRule> result = new PageImpl<>(pageContent, pageable, allRules.size());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取限流规则失败", e);
            return Result.error("获取限流规则失败");
        }
    }
    
    /**
     * 根据ID获取限流规则
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rule/{id}")
    public Result<RateLimitRule> getRuleById(@PathVariable Long id) {
        try {
            Optional<RateLimitRule> rule = rateLimitService.getRuleById(id);
            if (rule.isPresent()) {
                return Result.success(rule.get());
            } else {
                return Result.error("限流规则不存在");
            }
        } catch (Exception e) {
            log.error("获取限流规则失败", e);
            return Result.error("获取限流规则失败");
        }
    }
    
    /**
     * 创建限流规则
     */
    
    $@PreAuthorize("hasRole('ADMIN'"))
    @PostMapping("/rule")
    public Result<String> createRule(@RequestBody RateLimitRule rule) {
        try {
            // 验证必填字段
            if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
                return Result.error("规则名称不能为空");
            }
            if (rule.getLimitDimension() == null || rule.getLimitDimension().trim().isEmpty()) {
                return Result.error("限流维度不能为空");
            }
            if (rule.getLimitThreshold() == null || rule.getLimitThreshold() <= 0) {
                return Result.error("限流阈值必须大于0");
            }
            if (rule.getLimitWindow() == null || rule.getLimitWindow() <= 0) {
                return Result.error("限流窗口必须大于0");
            }
            
            // 设置默认值
            if (rule.getEnabled() == null) {
                rule.setEnabled(true);
            }
            
            rateLimitService.createRule(rule);
            log.info("创建限流规则成功：ruleName={}", rule.getRuleName());
            return Result.success("创建成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建限流规则失败", e);
            return Result.error("创建限流规则失败");
        }
    }
    
    /**
     * 更新限流规则
     */
    
    $@PreAuthorize("hasRole('ADMIN'"))
    @PutMapping("/rule/{id}")
    public Result<String> updateRule(@PathVariable Long id, @RequestBody RateLimitRule rule) {
        try {
            rule.setId(id);
            rateLimitService.updateRule(rule);
            log.info("更新限流规则成功：ruleName={}", rule.getRuleName());
            return Result.success("更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新限流规则失败", e);
            return Result.error("更新限流规则失败");
        }
    }
    
    /**
     * 删除限流规则
     */
    
    $@PreAuthorize("hasRole('ADMIN'"))
    @DeleteMapping("/rule/{id}")
    public Result<String> deleteRule(@PathVariable Long id) {
        try {
            Optional<RateLimitRule> existing = rateLimitService.getRuleById(id);
            if (!existing.isPresent()) {
                return Result.error("限流规则不存在");
            }
            
            RateLimitRule rule = existing.get();
            rateLimitService.deleteRule(id);
            log.info("删除限流规则成功：ruleName={}", rule.getRuleName());
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除限流规则失败", e);
            return Result.error("删除限流规则失败");
        }
    }
    
    /**
     * 启用/禁用限流规则
     */
    
    $@PreAuthorize("hasRole('ADMIN'"))
    @PutMapping("/rule/{id}/status")
    public Result<String> updateRuleStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        try {
            Optional<RateLimitRule> existing = rateLimitService.getRuleById(id);
            if (!existing.isPresent()) {
                return Result.error("限流规则不存在");
            }
            
            RateLimitRule rule = existing.get();
            rule.setEnabled(enabled);
            rateLimitService.updateRule(rule);
            
            log.info("更新限流规则状态成功：ruleName={}, enabled={}", rule.getRuleName(), enabled);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新限流规则状态失败", e);
            return Result.error("更新限流规则状态失败");
        }
    }
    
    /**
     * 获取启用的限流规则
     */
    
    $@PreAuthorize("hasRole('ADMIN'"))
    @GetMapping("/rules/enabled")
    public Result<List<RateLimitRule>> getEnabledRules() {
        try {
            List<RateLimitRule> rules = rateLimitService.getEnabledRules();
            return Result.success(rules);
        } catch (Exception e) {
            log.error("获取启用的限流规则失败", e);
            return Result.error("获取启用的限流规则失败");
        }
    }
    
    /**
     * 获取限流统计信息
     */
    
    $@PreAuthorize("hasRole('ADMIN'"))
    @GetMapping("/statistics")
    public Result<Object> getStatistics() {
        try {
            List<RateLimitRule> allRules = rateLimitService.getAllRules();
            long totalRules = allRules.size();
            long enabledRules = allRules.stream().filter(RateLimitRule::getEnabled).count();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRules", totalRules);
            statistics.put("enabledRules", enabledRules);
            statistics.put("disabledRules", totalRules - enabledRules);
            
            // 按维度统计
            Map<String, Long> dimensionStats = allRules.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    RateLimitRule::getLimitDimension,
                    java.util.stream.Collectors.counting()
                ));
            statistics.put("dimensionStats", dimensionStats);
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取限流统计信息失败", e);
            return Result.error("获取限流统计信息失败");
        }
    }
    
    // 内部类，用于构建分页结果
    private static class PageImpl<T> implements Page<T> {
        private final List<T> content;
        private final Pageable pageable;
        private final long total;
        
        public PageImpl(List<T> content, Pageable pageable, long total) {
            this.content = content;
            this.pageable = pageable;
            this.total = total;
        }
        
        @Override
        public int getTotalPages() {
            return (int) Math.ceil((double) total / pageable.getPageSize());
        }
        
        @Override
        public long getTotalElements() {
            return total;
        }
        
        @Override
        public int getNumber() {
            return pageable.getPageNumber();
        }
        
        @Override
        public int getSize() {
            return pageable.getPageSize();
        }
        
        @Override
        public int getNumberOfElements() {
            return content.size();
        }
        
        @Override
        public java.util.List<T> getContent() {
            return content;
        }
        
        @Override
        public boolean hasContent() {
            return !content.isEmpty();
        }
        
        @Override
        public Sort getSort() {
            return pageable.getSort();
        }
        
        @Override
        public boolean isFirst() {
            return !hasPrevious();
        }
        
        @Override
        public boolean isLast() {
            return !hasNext();
        }
        
        @Override
        public boolean hasNext() {
            return getNumber() + 1 < getTotalPages();
        }
        
        @Override
        public boolean hasPrevious() {
            return getNumber() > 0;
        }
        
        @Override
        public Pageable nextPageable() {
            return hasNext() ? pageable.next() : Pageable.unpaged();
        }
        
        @Override
        public Pageable previousPageable() {
            return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged();
        }
        
        @Override
        public <U> Page<U> map(java.util.function.Function<? super T, ? extends U> converter) {
            return new PageImpl<>(content.stream().map(converter).toList(), pageable, total);
        }
    }
}