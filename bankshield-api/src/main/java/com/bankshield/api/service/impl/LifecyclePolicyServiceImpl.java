package com.bankshield.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.LifecyclePolicy;
import com.bankshield.api.mapper.LifecyclePolicyMapper;
import com.bankshield.api.service.LifecyclePolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 生命周期策略服务实现
 */
@Slf4j
@Service
public class LifecyclePolicyServiceImpl implements LifecyclePolicyService {
    
    @Autowired
    private LifecyclePolicyMapper policyMapper;
    
    @Override
    public Page<LifecyclePolicy> getPage(Integer pageNum, Integer pageSize, String policyName, String policyStatus) {
        Page<LifecyclePolicy> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<LifecyclePolicy> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(policyName)) {
            wrapper.like(LifecyclePolicy::getPolicyName, policyName);
        }
        if (StringUtils.hasText(policyStatus)) {
            wrapper.eq(LifecyclePolicy::getPolicyStatus, policyStatus);
        }
        wrapper.orderByDesc(LifecyclePolicy::getPriority)
               .orderByAsc(LifecyclePolicy::getId);
        
        return policyMapper.selectPage(page, wrapper);
    }
    
    @Override
    public List<LifecyclePolicy> getActivePolicies() {
        return policyMapper.selectActivePolicies();
    }
    
    @Override
    public LifecyclePolicy getById(Long id) {
        return policyMapper.selectById(id);
    }
    
    @Override
    public LifecyclePolicy getPolicyByDataTypeAndLevel(String dataType, String sensitivityLevel) {
        return policyMapper.selectByDataTypeAndLevel(dataType, sensitivityLevel);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPolicy(LifecyclePolicy policy) {
        try {
            if (policy.getPolicyStatus() == null) {
                policy.setPolicyStatus("ACTIVE");
            }
            if (policy.getPriority() == null) {
                policy.setPriority(0);
            }
            if (policy.getArchiveEnabled() == null) {
                policy.setArchiveEnabled(1);
            }
            if (policy.getDestroyEnabled() == null) {
                policy.setDestroyEnabled(1);
            }
            if (policy.getApprovalRequired() == null) {
                policy.setApprovalRequired(0);
            }
            if (policy.getNotificationEnabled() == null) {
                policy.setNotificationEnabled(1);
            }
            if (policy.getNotificationDays() == null) {
                policy.setNotificationDays(7);
            }
            
            int result = policyMapper.insert(policy);
            log.info("创建生命周期策略成功: {}", policy.getPolicyName());
            return result > 0;
        } catch (Exception e) {
            log.error("创建生命周期策略失败", e);
            throw new RuntimeException("创建策略失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePolicy(LifecyclePolicy policy) {
        try {
            int result = policyMapper.updateById(policy);
            log.info("更新生命周期策略成功: {}", policy.getId());
            return result > 0;
        } catch (Exception e) {
            log.error("更新生命周期策略失败", e);
            throw new RuntimeException("更新策略失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePolicy(Long id) {
        try {
            int result = policyMapper.deleteById(id);
            log.info("删除生命周期策略成功: {}", id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除生命周期策略失败", e);
            throw new RuntimeException("删除策略失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean togglePolicyStatus(Long id, String status) {
        try {
            LifecyclePolicy policy = policyMapper.selectById(id);
            if (policy == null) {
                throw new RuntimeException("策略不存在");
            }
            
            policy.setPolicyStatus(status);
            int result = policyMapper.updateById(policy);
            log.info("切换策略状态成功: {} -> {}", id, status);
            return result > 0;
        } catch (Exception e) {
            log.error("切换策略状态失败", e);
            throw new RuntimeException("切换状态失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustPriority(Long id, Integer priority) {
        try {
            LifecyclePolicy policy = policyMapper.selectById(id);
            if (policy == null) {
                throw new RuntimeException("策略不存在");
            }
            
            policy.setPriority(priority);
            int result = policyMapper.updateById(policy);
            log.info("调整策略优先级成功: {} -> {}", id, priority);
            return result > 0;
        } catch (Exception e) {
            log.error("调整策略优先级失败", e);
            throw new RuntimeException("调整优先级失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<LifecyclePolicy> getArchivePolicies() {
        return policyMapper.selectArchivePolicies();
    }
    
    @Override
    public List<LifecyclePolicy> getDestroyPolicies() {
        return policyMapper.selectDestroyPolicies();
    }
}
