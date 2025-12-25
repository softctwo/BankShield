package com.bankshield.gateway.controller;

import com.bankshield.common.result.Result;
import com.bankshield.gateway.entity.BlacklistIp;
import com.bankshield.gateway.service.BlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 黑名单控制器 - 仅允许管理员访问
 *
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/gateway/blacklist")
public class BlacklistController {
    
    @Autowired
    private BlacklistService blacklistService;
    
    /**
     * 获取IP黑名单列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ips")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<Page<BlacklistIp>> getBlacklistIps(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String blockStatus) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("blockTime").descending());
            
            Page<BlacklistIp> result;
            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                // 根据IP地址查询
                List<BlacklistIp> ipList = blacklistService.getBlacklistInfo(ipAddress)
                    .map(List::of)
                    .orElse(List.of());
                
                // 手动分页
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), ipList.size());
                List<BlacklistIp> pageContent = ipList.subList(start, end);
                
                result = new PageImpl<>(pageContent, pageable, ipList.size());
            } else if (blockStatus != null && !blockStatus.trim().isEmpty()) {
                result = blacklistService.getBlacklistsByStatus(blockStatus, pageable);
            } else {
                result = blacklistService.getBlacklists(pageable);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取IP黑名单列表失败", e);
            return Result.error("获取IP黑名单列表失败");
        }
    }
    
    /**
     * 根据ID获取黑名单记录
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ip/{id}")
    public Result<BlacklistIp> getBlacklistIpById(@PathVariable Long id) {
        try {
            java.util.Optional<BlacklistIp> blacklistIp = blacklistService.getBlacklists(Pageable.unpaged())
                .stream()
                .filter(ip -> ip.getId().equals(id))
                .findFirst();
                
            if (blacklistIp.isPresent()) {
                return Result.success(blacklistIp.get());
            } else {
                return Result.error("黑名单记录不存在");
            }
        } catch (Exception e) {
            log.error("获取黑名单记录失败", e);
            return Result.error("获取黑名单记录失败");
        }
    }
    
    /**
     * 添加IP到黑名单
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ip")
    public Result<String> addToBlacklist(@RequestBody Map<String, String> params) {
        try {
            String ipAddress = params.get("ipAddress");
            String blockReason = params.get("blockReason");
            String blockDurationStr = params.get("blockDuration");
            String operator = params.get("operator");
            
            // 验证必填字段
            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                return Result.error("IP地址不能为空");
            }
            if (blockReason == null || blockReason.trim().isEmpty()) {
                return Result.error("封禁原因不能为空");
            }
            if (operator == null || operator.trim().isEmpty()) {
                return Result.error("操作人员不能为空");
            }
            
            // 解析封禁时长
            Long blockDuration = null;
            if (blockDurationStr != null && !blockDurationStr.trim().isEmpty()) {
                try {
                    blockDuration = Long.parseLong(blockDurationStr);
                    if (blockDuration <= 0) {
                        return Result.error("封禁时长必须大于0");
                    }
                } catch (NumberFormatException e) {
                    return Result.error("封禁时长格式错误");
                }
            }
            
            // 添加IP到黑名单
            BlacklistIp blacklistIp = blacklistService.addToBlacklist(ipAddress, blockReason, blockDuration, operator);
            log.info("添加IP到黑名单成功：ipAddress={}, reason={}, operator={}", ipAddress, blockReason, operator);
            return Result.success("添加黑名单成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加IP到黑名单失败", e);
            return Result.error("添加IP到黑名单失败");
        }
    }
    
    /**
     * 批量添加IP到黑名单
     */
    @PostMapping("/ips/batch")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<String> addToBlacklistBatch(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<String> ipAddresses = (List<String>) params.get("ipAddresses");
            String blockReason = (String) params.get("blockReason");
            String blockDurationStr = (String) params.get("blockDuration");
            String operator = (String) params.get("operator");
            
            // 验证必填字段
            if (ipAddresses == null || ipAddresses.isEmpty()) {
                return Result.error("IP地址列表不能为空");
            }
            if (blockReason == null || blockReason.trim().isEmpty()) {
                return Result.error("封禁原因不能为空");
            }
            if (operator == null || operator.trim().isEmpty()) {
                return Result.error("操作人员不能为空");
            }
            
            // 解析封禁时长
            Long blockDuration = null;
            if (blockDurationStr != null && !blockDurationStr.trim().isEmpty()) {
                try {
                    blockDuration = Long.parseLong(blockDurationStr);
                    if (blockDuration <= 0) {
                        return Result.error("封禁时长必须大于0");
                    }
                } catch (NumberFormatException e) {
                    return Result.error("封禁时长格式错误");
                }
            }
            
            // 批量添加IP到黑名单
            blacklistService.addToBlacklistBatch(ipAddresses, blockReason, blockDuration, operator);
            log.info("批量添加IP到黑名单成功：ipCount={}, reason={}, operator={}", ipAddresses.size(), blockReason, operator);
            return Result.success("批量添加黑名单成功");
        } catch (Exception e) {
            log.error("批量添加IP到黑名单失败", e);
            return Result.error("批量添加IP到黑名单失败");
        }
    }
    
    /**
     * 从黑名单中移除IP
     */
    @PutMapping("/ip/{id}/unblock")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<String> removeFromBlacklist(@PathVariable Long id, @RequestParam String operator) {
        try {
            if (operator == null || operator.trim().isEmpty()) {
                return Result.error("操作人员不能为空");
            }
            
            blacklistService.removeFromBlacklist(id, operator);
            log.info("从黑名单中移除IP成功：id={}, operator={}", id, operator);
            return Result.success("解封成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("从黑名单中移除IP失败", e);
            return Result.error("从黑名单中移除IP失败");
        }
    }
    
    /**
     * 批量从黑名单中移除IP
     */
    @PutMapping("/ips/batch/unblock")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<String> removeFromBlacklistBatch(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) params.get("ids");
            String operator = (String) params.get("operator");
            
            // 验证必填字段
            if (ids == null || ids.isEmpty()) {
                return Result.error("ID列表不能为空");
            }
            if (operator == null || operator.trim().isEmpty()) {
                return Result.error("操作人员不能为空");
            }
            
            // 批量从黑名单中移除IP
            blacklistService.removeFromBlacklistBatch(ids, operator);
            log.info("批量从黑名单中移除IP成功：ipCount={}, operator={}", ids.size(), operator);
            return Result.success("批量解封成功");
        } catch (Exception e) {
            log.error("批量从黑名单中移除IP失败", e);
            return Result.error("批量从黑名单中移除IP失败");
        }
    }
    
    /**
     * 检查IP是否在黑名单中
     */
    @GetMapping("/ip/check")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<Map<String, Object>> checkIpInBlacklist(@RequestParam String ipAddress) {
        try {
            boolean isBlacklisted = blacklistService.isBlacklisted(ipAddress);
            
            Map<String, Object> result = new HashMap<>();
            result.put("ipAddress", ipAddress);
            result.put("isBlacklisted", isBlacklisted);
            
            if (isBlacklisted) {
                java.util.Optional<BlacklistIp> blacklistInfo = blacklistService.getBlacklistInfo(ipAddress);
                if (blacklistInfo.isPresent()) {
                    BlacklistIp blacklistIp = blacklistInfo.get();
                    result.put("blockReason", blacklistIp.getBlockReason());
                    result.put("blockTime", blacklistIp.getBlockTime());
                    result.put("unblockTime", blacklistIp.getUnblockTime());
                    result.put("blockStatus", blacklistIp.getBlockStatus());
                }
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查IP黑名单状态失败", e);
            return Result.error("检查IP黑名单状态失败");
        }
    }
    
    /**
     * 获取黑名单统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<BlacklistService.BlacklistStatistics> getStatistics() {
        try {
            BlacklistService.BlacklistStatistics statistics = blacklistService.getStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取黑名单统计信息失败", e);
            return Result.error("获取黑名单统计信息失败");
        }
    }
    
    /**
     * 获取活跃的黑名单
     */
    @GetMapping("/ips/active")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<List<BlacklistIp>> getActiveBlacklists() {
        try {
            List<BlacklistIp> activeBlacklists = blacklistService.getActiveBlacklists();
            return Result.success(activeBlacklists);
        } catch (Exception e) {
            log.error("获取活跃黑名单失败", e);
            return Result.error("获取活跃黑名单失败");
        }
    }
    
    /**
     * 处理过期的黑名单（定时任务调用）
     */
    @PutMapping("/ips/process-expired")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public Result<String> processExpiredBlacklists() {
        try {
            blacklistService.processExpiredBlacklists();
            log.info("处理过期黑名单成功");
            return Result.success("处理过期黑名单成功");
        } catch (Exception e) {
            log.error("处理过期黑名单失败", e);
            return Result.error("处理过期黑名单失败");
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