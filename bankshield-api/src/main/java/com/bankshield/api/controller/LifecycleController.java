package com.bankshield.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.*;
import com.bankshield.api.service.LifecycleManagementService;
import com.bankshield.api.service.LifecyclePolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据生命周期管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/lifecycle")
public class LifecycleController {
    
    @Autowired
    private LifecyclePolicyService policyService;
    
    @Autowired
    private LifecycleManagementService managementService;
    
    /**
     * 分页查询策略列表
     */
    @GetMapping("/policies")
    public Map<String, Object> getPolicies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String policyName,
            @RequestParam(required = false) String policyStatus) {
        
        try {
            Page<LifecyclePolicy> page = policyService.getPage(pageNum, pageSize, policyName, policyStatus);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", page.getRecords());
            result.put("total", page.getTotal());
            result.put("pageNum", page.getCurrent());
            result.put("pageSize", page.getSize());
            
            return result;
        } catch (Exception e) {
            log.error("查询策略列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 获取所有活跃策略
     */
    @GetMapping("/policies/active")
    public Map<String, Object> getActivePolicies() {
        try {
            List<LifecyclePolicy> policies = policyService.getActivePolicies();
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", policies);
            
            return result;
        } catch (Exception e) {
            log.error("查询活跃策略失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 根据ID获取策略详情
     */
    @GetMapping("/policies/{id}")
    public Map<String, Object> getPolicyById(@PathVariable Long id) {
        try {
            LifecyclePolicy policy = policyService.getById(id);
            
            Map<String, Object> result = new HashMap<>();
            if (policy != null) {
                result.put("code", 200);
                result.put("message", "查询成功");
                result.put("data", policy);
            } else {
                result.put("code", 404);
                result.put("message", "策略不存在");
            }
            
            return result;
        } catch (Exception e) {
            log.error("查询策略详情失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 创建策略
     */
    @PostMapping("/policies")
    public Map<String, Object> createPolicy(@RequestBody LifecyclePolicy policy) {
        try {
            boolean success = policyService.createPolicy(policy);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "创建成功" : "创建失败");
            result.put("data", policy);
            
            return result;
        } catch (Exception e) {
            log.error("创建策略失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "创建失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 更新策略
     */
    @PutMapping("/policies/{id}")
    public Map<String, Object> updatePolicy(@PathVariable Long id, @RequestBody LifecyclePolicy policy) {
        try {
            policy.setId(id);
            boolean success = policyService.updatePolicy(policy);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "更新成功" : "更新失败");
            
            return result;
        } catch (Exception e) {
            log.error("更新策略失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "更新失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 删除策略
     */
    @DeleteMapping("/policies/{id}")
    public Map<String, Object> deletePolicy(@PathVariable Long id) {
        try {
            boolean success = policyService.deletePolicy(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "删除成功" : "删除失败");
            
            return result;
        } catch (Exception e) {
            log.error("删除策略失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "删除失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 启用/禁用策略
     */
    @PutMapping("/policies/{id}/status")
    public Map<String, Object> togglePolicyStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            boolean success = policyService.togglePolicyStatus(id, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "状态切换成功" : "状态切换失败");
            
            return result;
        } catch (Exception e) {
            log.error("切换策略状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "操作失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 调整策略优先级
     */
    @PutMapping("/policies/{id}/priority")
    public Map<String, Object> adjustPriority(@PathVariable Long id, @RequestParam Integer priority) {
        try {
            boolean success = policyService.adjustPriority(id, priority);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "优先级调整成功" : "优先级调整失败");
            
            return result;
        } catch (Exception e) {
            log.error("调整策略优先级失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "操作失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 归档单个数据资产
     */
    @PostMapping("/archive/{assetId}")
    public Map<String, Object> archiveAsset(
            @PathVariable Long assetId,
            @RequestParam Long policyId,
            @RequestParam(defaultValue = "admin") String operator) {
        
        try {
            boolean success = managementService.archiveAsset(assetId, policyId, operator);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "归档成功" : "归档失败");
            
            return result;
        } catch (Exception e) {
            log.error("归档数据资产失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "归档失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 批量归档数据资产
     */
    @PostMapping("/archive/batch")
    public Map<String, Object> batchArchiveAssets(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> assetIds = (List<Long>) params.get("assetIds");
            Long policyId = Long.valueOf(params.get("policyId").toString());
            String operator = params.getOrDefault("operator", "admin").toString();
            
            Map<String, Object> result = managementService.batchArchiveAssets(assetIds, policyId, operator);
            result.put("code", 200);
            result.put("message", "批量归档完成");
            
            return result;
        } catch (Exception e) {
            log.error("批量归档失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "批量归档失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 自动归档（根据策略）
     */
    @PostMapping("/archive/auto/{policyId}")
    public Map<String, Object> autoArchive(@PathVariable Long policyId) {
        try {
            Map<String, Object> result = managementService.autoArchive(policyId);
            result.put("code", 200);
            result.put("message", "自动归档完成");
            
            return result;
        } catch (Exception e) {
            log.error("自动归档失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "自动归档失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 销毁单个归档数据
     */
    @PostMapping("/destroy/{archiveId}")
    public Map<String, Object> destroyArchivedData(
            @PathVariable Long archiveId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "admin") String operator) {
        
        try {
            boolean success = managementService.destroyArchivedData(archiveId, reason, operator);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "销毁成功" : "销毁失败");
            
            return result;
        } catch (Exception e) {
            log.error("销毁归档数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "销毁失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 批量销毁归档数据
     */
    @PostMapping("/destroy/batch")
    public Map<String, Object> batchDestroyArchivedData(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> archiveIds = (List<Long>) params.get("archiveIds");
            String reason = params.get("reason").toString();
            String operator = params.getOrDefault("operator", "admin").toString();
            
            Map<String, Object> result = managementService.batchDestroyArchivedData(archiveIds, reason, operator);
            result.put("code", 200);
            result.put("message", "批量销毁完成");
            
            return result;
        } catch (Exception e) {
            log.error("批量销毁失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "批量销毁失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 自动销毁（根据策略）
     */
    @PostMapping("/destroy/auto/{policyId}")
    public Map<String, Object> autoDestroy(@PathVariable Long policyId) {
        try {
            Map<String, Object> result = managementService.autoDestroy(policyId);
            result.put("code", 200);
            result.put("message", "自动销毁完成");
            
            return result;
        } catch (Exception e) {
            log.error("自动销毁失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "自动销毁失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 获取待归档的数据资产列表
     */
    @GetMapping("/pending-archive/{policyId}")
    public Map<String, Object> getPendingArchiveAssets(@PathVariable Long policyId) {
        try {
            List<Map<String, Object>> assets = managementService.getPendingArchiveAssets(policyId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", assets);
            
            return result;
        } catch (Exception e) {
            log.error("查询待归档资产失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 获取待销毁的归档数据列表
     */
    @GetMapping("/pending-destruction")
    public Map<String, Object> getPendingDestructionData() {
        try {
            List<ArchivedData> data = managementService.getPendingDestructionData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", data);
            
            return result;
        } catch (Exception e) {
            log.error("查询待销毁数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 获取执行记录
     */
    @GetMapping("/executions")
    public Map<String, Object> getExecutionRecords(
            @RequestParam(required = false) Long policyId,
            @RequestParam(defaultValue = "100") Integer limit) {
        
        try {
            List<LifecycleExecution> records = managementService.getExecutionRecords(policyId, limit);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", records);
            
            return result;
        } catch (Exception e) {
            log.error("查询执行记录失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 获取归档数据详情
     */
    @GetMapping("/archived/{id}")
    public Map<String, Object> getArchivedDataById(@PathVariable Long id) {
        try {
            ArchivedData data = managementService.getArchivedDataById(id);
            
            Map<String, Object> result = new HashMap<>();
            if (data != null) {
                result.put("code", 200);
                result.put("message", "查询成功");
                result.put("data", data);
            } else {
                result.put("code", 404);
                result.put("message", "归档数据不存在");
            }
            
            return result;
        } catch (Exception e) {
            log.error("查询归档数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 获取销毁记录
     */
    @GetMapping("/destruction-records")
    public Map<String, Object> getDestructionRecords(@RequestParam(required = false) Long assetId) {
        try {
            List<DestructionRecord> records = managementService.getDestructionRecords(assetId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", records);
            
            return result;
        } catch (Exception e) {
            log.error("查询销毁记录失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 审批销毁申请
     */
    @PostMapping("/destruction-records/{recordId}/approve")
    public Map<String, Object> approveDestruction(
            @PathVariable Long recordId,
            @RequestParam String approver,
            @RequestParam(required = false) String comment,
            @RequestParam boolean approved) {
        
        try {
            boolean success = managementService.approveDestruction(recordId, approver, comment, approved);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "审批成功" : "审批失败");
            
            return result;
        } catch (Exception e) {
            log.error("审批销毁申请失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "审批失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 获取生命周期统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        try {
            Map<String, Object> stats = managementService.getStatistics();
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", stats);
            
            return result;
        } catch (Exception e) {
            log.error("查询统计信息失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "查询失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 验证归档数据完整性
     */
    @GetMapping("/archived/{id}/verify")
    public Map<String, Object> verifyArchivedData(@PathVariable Long id) {
        try {
            boolean valid = managementService.verifyArchivedData(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", valid ? "验证通过" : "验证失败");
            result.put("data", valid);
            
            return result;
        } catch (Exception e) {
            log.error("验证归档数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "验证失败: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * 恢复归档数据
     */
    @PostMapping("/archived/{id}/restore")
    public Map<String, Object> restoreArchivedData(
            @PathVariable Long id,
            @RequestParam(defaultValue = "admin") String operator) {
        
        try {
            boolean success = managementService.restoreArchivedData(id, operator);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "恢复成功" : "恢复失败");
            
            return result;
        } catch (Exception e) {
            log.error("恢复归档数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "恢复失败: " + e.getMessage());
            return error;
        }
    }
}
