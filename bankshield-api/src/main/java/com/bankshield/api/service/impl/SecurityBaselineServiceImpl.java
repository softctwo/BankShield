package com.bankshield.api.service.impl;

import com.bankshield.api.entity.SecurityBaseline;
import com.bankshield.api.enums.ComplianceStandard;
import com.bankshield.api.mapper.SecurityBaselineMapper;
import com.bankshield.api.service.SecurityBaselineService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 安全基线服务实现类
 * @author BankShield
 */
@Slf4j
@Service
public class SecurityBaselineServiceImpl extends ServiceImpl<SecurityBaselineMapper, SecurityBaseline> 
        implements SecurityBaselineService {

    @Autowired
    private SecurityBaselineMapper baselineMapper;

    @Override
    public List<SecurityBaseline> getAllBaselines() {
        return baselineMapper.selectList(null);
    }

    @Override
    public List<SecurityBaseline> getEnabledBaselines() {
        return baselineMapper.selectEnabledBaselines();
    }

    @Override
    public List<SecurityBaseline> getBaselinesByType(String checkType) {
        return baselineMapper.selectByCheckType(checkType);
    }

    @Override
    public List<SecurityBaseline> getBuiltinBaselines() {
        return baselineMapper.selectBuiltinBaselines();
    }

    @Override
    public List<SecurityBaseline> getCustomBaselines() {
        return baselineMapper.selectCustomBaselines();
    }

    @Override
    @Transactional
    public boolean batchUpdateEnabled(List<Long> ids, Boolean enabled) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        
        try {
            int updateCount = baselineMapper.batchUpdateEnabled(ids, enabled);
            log.info("批量更新基线启用状态成功，数量: {}, 状态: {}", ids.size(), enabled);
            return updateCount > 0;
        } catch (Exception e) {
            log.error("批量更新基线启用状态失败", e);
            throw new RuntimeException("批量更新基线启用状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void initBaselines() {
        log.info("初始化安全基线配置");
        
        try {
            // 检查是否已存在内置基线
            List<SecurityBaseline> existingBaselines = baselineMapper.selectBuiltinBaselines();
            if (!existingBaselines.isEmpty()) {
                log.info("安全基线已初始化，跳过");
                return;
            }
            
            // 初始化等保三级基线
            initGBLevel3Baselines();
            
            // 初始化PCI-DSS基线
            initPCIDSSBaselines();
            
            // 初始化OWASP TOP10基线
            initOWASPTop10Baselines();
            
            log.info("安全基线初始化成功");
            
        } catch (Exception e) {
            log.error("初始化安全基线失败", e);
            throw new RuntimeException("初始化安全基线失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void syncBuiltinBaselines() {
        log.info("同步内置安全基线");
        
        try {
            // 删除现有的内置基线
            QueryWrapper<SecurityBaseline> wrapper = new QueryWrapper<>();
            wrapper.eq("builtin", true);
            baselineMapper.delete(wrapper);
            
            // 重新初始化内置基线
            initGBLevel3Baselines();
            initPCIDSSBaselines();
            initOWASPTop10Baselines();
            
            log.info("内置安全基线同步成功");
            
        } catch (Exception e) {
            log.error("同步内置安全基线失败", e);
            throw new RuntimeException("同步内置安全基线失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BaselineStatistics> getBaselineStatistics() {
        List<BaselineStatistics> statistics = new ArrayList<>();
        
        // 统计不同合规标准的数据
        for (ComplianceStandard standard : ComplianceStandard.values()) {
            QueryWrapper<SecurityBaseline> wrapper = new QueryWrapper<>();
            wrapper.eq("compliance_standard", standard.name());
            
            long totalCount = baselineMapper.selectCount(wrapper);
            
            wrapper.eq("enabled", true);
            long enabledCount = baselineMapper.selectCount(wrapper);
            
            wrapper.eq("pass_status", "PASS");
            long passCount = baselineMapper.selectCount(wrapper);
            
            wrapper = new QueryWrapper<>();
            wrapper.eq("compliance_standard", standard.name());
            wrapper.eq("pass_status", "FAIL");
            long failCount = baselineMapper.selectCount(wrapper);
            
            statistics.add(new BaselineStatistics(
                standard.getName(), totalCount, enabledCount, passCount, failCount));
        }
        
        return statistics;
    }

    @Override
    public boolean checkNameExists(String checkName) {
        return baselineMapper.countByCheckName(checkName) > 0;
    }

    @Override
    public Page<SecurityBaseline> getBaselinesPage(int page, int size, String checkName, 
                                                   String complianceStandard, String checkType) {
        Page<SecurityBaseline> pageParam = new Page<>(page, size);
        QueryWrapper<SecurityBaseline> wrapper = new QueryWrapper<>();
        
        if (checkName != null && !checkName.trim().isEmpty()) {
            wrapper.like("check_item_name", checkName);
        }
        
        if (complianceStandard != null && !complianceStandard.trim().isEmpty()) {
            wrapper.eq("compliance_standard", complianceStandard);
        }
        
        if (checkType != null && !checkType.trim().isEmpty()) {
            wrapper.eq("check_type", checkType);
        }
        
        wrapper.orderByAsc("compliance_standard", "check_type", "check_item_name");
        
        return baselineMapper.selectPage(pageParam, wrapper);
    }

    // 私有方法：初始化基线数据

    private void initGBLevel3Baselines() {
        log.info("初始化等保三级基线");
        
        String[][] gbBaselines = {
            {"用户密码复杂度策略", "PASSWORD", "HIGH", "检查密码是否符合复杂度要求（8位以上，包含大小写字母、数字、特殊字符）", "配置密码策略：minimum-length=8, require-uppercase=true, require-lowercase=true, require-digit=true, require-special=true"},
            {"会话超时自动退出", "SESSION", "MEDIUM", "检查会话超时是否设置为30分钟", "设置session-timeout=30分钟，配置最大会话数限制"},
            {"敏感数据加密传输", "ENCRYPTION", "HIGH", "检查是否使用HTTPS加密传输", "强制使用HTTPS，禁用HTTP访问，配置HSTS"},
            {"数据库连接密码加密存储", "ENCRYPTION", "HIGH", "检查数据库密码是否加密存储", "使用Jasypt加密配置文件中的数据库密码"},
            {"文件上传大小限制", "ACCESS_CONTROL", "MEDIUM", "检查文件上传是否限制大小和类型", "配置max-file-size=10MB, allowed-file-types=jpg,png,pdf,doc"},
            {"SQL注入防护", "INPUT_VALIDATION", "CRITICAL", "检查是否对所有输入进行SQL注入过滤", "使用MyBatis参数化查询，禁止拼接SQL"},
            {"XSS攻击防护", "INPUT_VALIDATION", "HIGH", "检查是否对所有输出进行XSS过滤", "使用HTML转义库，过滤script标签"},
            {"CSRF令牌验证", "AUTH", "HIGH", "检查是否启用CSRF防护", "启用Spring Security CSRF保护，所有POST请求验证令牌"},
            {"审计日志完整性", "AUDIT", "MEDIUM", "检查审计日志是否完整记录", "确保所有增删改操作记录到audit_operation表"},
            {"密钥管理合规性", "KEY_MANAGEMENT", "HIGH", "检查密钥是否符合管理要求", "使用国密算法，密钥定期轮换（90天），密钥材料加密存储"}
        };
        
        for (String[] baseline : gbBaselines) {
            SecurityBaseline entity = new SecurityBaseline();
            entity.setCheckItemName(baseline[0]);
            entity.setComplianceStandard(ComplianceStandard.GB_LEVEL3.name());
            entity.setCheckType(baseline[1]);
            entity.setRiskLevel(baseline[2]);
            entity.setDescription(baseline[3]);
            entity.setRemedyAdvice(baseline[4]);
            entity.setEnabled(true);
            entity.setBuiltin(true);
            entity.setCreatedBy("system");
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            
            baselineMapper.insert(entity);
        }
    }

    private void initPCIDSSBaselines() {
        log.info("初始化PCI-DSS基线");
        
        String[][] pciBaselines = {
            {"支付卡数据加密存储", "ENCRYPTION", "CRITICAL", "检查支付卡数据是否加密存储", "使用强加密算法（AES-256）加密存储支付卡数据"},
            {"支付卡数据传输加密", "ENCRYPTION", "CRITICAL", "检查支付卡数据传输是否加密", "使用TLS 1.3加密传输支付卡数据"},
            {"访问控制策略", "ACCESS_CONTROL", "HIGH", "检查是否实施访问控制策略", "实施最小权限原则，定期审查访问权限"},
            {"网络安全防护", "NETWORK", "HIGH", "检查网络安全防护措施", "配置防火墙、入侵检测系统等网络安全设备"},
            {"恶意软件防护", "MALWARE", "MEDIUM", "检查恶意软件防护措施", "安装杀毒软件，定期更新病毒库"}
        };
        
        for (String[] baseline : pciBaselines) {
            SecurityBaseline entity = new SecurityBaseline();
            entity.setCheckItemName(baseline[0]);
            entity.setComplianceStandard(ComplianceStandard.PCI_DSS.name());
            entity.setCheckType(baseline[1]);
            entity.setRiskLevel(baseline[2]);
            entity.setDescription(baseline[3]);
            entity.setRemedyAdvice(baseline[4]);
            entity.setEnabled(true);
            entity.setBuiltin(true);
            entity.setCreatedBy("system");
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            
            baselineMapper.insert(entity);
        }
    }

    private void initOWASPTop10Baselines() {
        log.info("初始化OWASP TOP10基线");
        
        String[][] owaspBaselines = {
            {"注入攻击防护", "INPUT_VALIDATION", "CRITICAL", "检查注入攻击防护措施", "使用参数化查询，输入验证和过滤"},
            {"失效的身份认证", "AUTH", "CRITICAL", "检查身份认证机制", "实施多因素认证，密码复杂度策略"},
            {"敏感数据暴露", "ENCRYPTION", "HIGH", "检查敏感数据保护措施", "加密存储和传输敏感数据"},
            {"XML外部实体攻击", "INPUT_VALIDATION", "HIGH", "检查XML外部实体攻击防护", "禁用XML外部实体，使用安全的XML解析器"},
            {"失效的访问控制", "ACCESS_CONTROL", "HIGH", "检查访问控制机制", "实施基于角色的访问控制（RBAC）"},
            {"安全配置错误", "CONFIG", "MEDIUM", "检查安全配置是否正确", "使用安全配置模板，定期安全审计"},
            {"跨站脚本攻击", "INPUT_VALIDATION", "HIGH", "检查XSS攻击防护措施", "输出编码，内容安全策略（CSP）"},
            {"不安全的反序列化", "INPUT_VALIDATION", "HIGH", "检查反序列化安全措施", "验证反序列化对象，使用安全的反序列化库"},
            {"使用含有已知漏洞的组件", "COMPONENT", "MEDIUM", "检查组件安全性", "定期更新组件，使用组件漏洞扫描工具"},
            {"日志记录和监控不足", "AUDIT", "MEDIUM", "检查日志记录和监控", "实施完整的日志记录和监控告警"}
        };
        
        for (String[] baseline : owaspBaselines) {
            SecurityBaseline entity = new SecurityBaseline();
            entity.setCheckItemName(baseline[0]);
            entity.setComplianceStandard(ComplianceStandard.OWASP_TOP10.name());
            entity.setCheckType(baseline[1]);
            entity.setRiskLevel(baseline[2]);
            entity.setDescription(baseline[3]);
            entity.setRemedyAdvice(baseline[4]);
            entity.setEnabled(true);
            entity.setBuiltin(true);
            entity.setCreatedBy("system");
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            
            baselineMapper.insert(entity);
        }
    }
}