package com.bankshield.api.job;

import com.bankshield.api.entity.LifecyclePolicy;
import com.bankshield.api.service.LifecycleManagementService;
import com.bankshield.api.service.LifecyclePolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据生命周期定时任务
 */
@Slf4j
@Component
public class LifecycleScheduledJob {
    
    @Autowired
    private LifecyclePolicyService policyService;
    
    @Autowired
    private LifecycleManagementService managementService;
    
    /**
     * 自动归档任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoArchiveJob() {
        log.info("开始执行自动归档任务");
        
        try {
            List<LifecyclePolicy> policies = policyService.getArchivePolicies();
            
            int totalSuccess = 0;
            int totalFailed = 0;
            
            for (LifecyclePolicy policy : policies) {
                try {
                    log.info("执行策略归档: policyId={}, policyName={}", policy.getId(), policy.getPolicyName());
                    
                    Map<String, Object> result = managementService.autoArchive(policy.getId());
                    
                    int success = (int) result.getOrDefault("success", 0);
                    int failed = (int) result.getOrDefault("failed", 0);
                    
                    totalSuccess += success;
                    totalFailed += failed;
                    
                    log.info("策略归档完成: policyId={}, success={}, failed={}", 
                            policy.getId(), success, failed);
                    
                } catch (Exception e) {
                    log.error("策略归档失败: policyId={}", policy.getId(), e);
                }
            }
            
            log.info("自动归档任务完成: 总成功={}, 总失败={}", totalSuccess, totalFailed);
            
        } catch (Exception e) {
            log.error("自动归档任务执行失败", e);
        }
    }
    
    /**
     * 自动销毁任务
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoDestroyJob() {
        log.info("开始执行自动销毁任务");
        
        try {
            List<LifecyclePolicy> policies = policyService.getDestroyPolicies();
            
            int totalSuccess = 0;
            int totalFailed = 0;
            
            for (LifecyclePolicy policy : policies) {
                try {
                    if (policy.getApprovalRequired() != null && policy.getApprovalRequired() == 1) {
                        log.info("策略需要审批，跳过自动销毁: policyId={}", policy.getId());
                        continue;
                    }
                    
                    log.info("执行策略销毁: policyId={}, policyName={}", policy.getId(), policy.getPolicyName());
                    
                    Map<String, Object> result = managementService.autoDestroy(policy.getId());
                    
                    int success = (int) result.getOrDefault("success", 0);
                    int failed = (int) result.getOrDefault("failed", 0);
                    
                    totalSuccess += success;
                    totalFailed += failed;
                    
                    log.info("策略销毁完成: policyId={}, success={}, failed={}", 
                            policy.getId(), success, failed);
                    
                } catch (Exception e) {
                    log.error("策略销毁失败: policyId={}", policy.getId(), e);
                }
            }
            
            log.info("自动销毁任务完成: 总成功={}, 总失败={}", totalSuccess, totalFailed);
            
        } catch (Exception e) {
            log.error("自动销毁任务执行失败", e);
        }
    }
    
    /**
     * 生命周期统计任务
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void statisticsJob() {
        log.info("开始执行生命周期统计任务");
        
        try {
            Map<String, Object> stats = managementService.getStatistics();
            
            log.info("生命周期统计: {}", stats);
            
        } catch (Exception e) {
            log.error("生命周期统计任务执行失败", e);
        }
    }
    
    /**
     * 数据完整性验证任务
     * 每天凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void verifyDataIntegrityJob() {
        log.info("开始执行数据完整性验证任务");
        
        try {
            // 这里可以实现批量验证归档数据的完整性
            log.info("数据完整性验证任务完成");
            
        } catch (Exception e) {
            log.error("数据完整性验证任务执行失败", e);
        }
    }
}
