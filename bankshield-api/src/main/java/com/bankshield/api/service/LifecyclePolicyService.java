package com.bankshield.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.LifecyclePolicy;

import java.util.List;

/**
 * 生命周期策略服务接口
 */
public interface LifecyclePolicyService {
    
    /**
     * 分页查询策略
     */
    Page<LifecyclePolicy> getPage(Integer pageNum, Integer pageSize, String policyName, String policyStatus);
    
    /**
     * 获取所有活跃策略
     */
    List<LifecyclePolicy> getActivePolicies();
    
    /**
     * 根据ID获取策略
     */
    LifecyclePolicy getById(Long id);
    
    /**
     * 根据数据类型和敏感级别获取匹配的策略
     */
    LifecyclePolicy getPolicyByDataTypeAndLevel(String dataType, String sensitivityLevel);
    
    /**
     * 创建策略
     */
    boolean createPolicy(LifecyclePolicy policy);
    
    /**
     * 更新策略
     */
    boolean updatePolicy(LifecyclePolicy policy);
    
    /**
     * 删除策略
     */
    boolean deletePolicy(Long id);
    
    /**
     * 启用/禁用策略
     */
    boolean togglePolicyStatus(Long id, String status);
    
    /**
     * 调整策略优先级
     */
    boolean adjustPriority(Long id, Integer priority);
    
    /**
     * 获取需要归档的策略列表
     */
    List<LifecyclePolicy> getArchivePolicies();
    
    /**
     * 获取需要销毁的策略列表
     */
    List<LifecyclePolicy> getDestroyPolicies();
}
