package com.bankshield.api.service;

import com.bankshield.api.entity.SecurityBaseline;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 安全基线服务接口
 * @author BankShield
 */
public interface SecurityBaselineService extends IService<SecurityBaseline> {

    /**
     * 获取所有基线
     */
    List<SecurityBaseline> getAllBaselines();

    /**
     * 获取启用的基线检查项
     */
    List<SecurityBaseline> getEnabledBaselines();

    /**
     * 根据检查类型获取基线
     */
    List<SecurityBaseline> getBaselinesByType(String checkType);

    /**
     * 获取内置的基线检查项
     */
    List<SecurityBaseline> getBuiltinBaselines();

    /**
     * 获取自定义的基线检查项
     */
    List<SecurityBaseline> getCustomBaselines();

    /**
     * 批量更新启用状态
     */
    boolean batchUpdateEnabled(List<Long> ids, Boolean enabled);

    /**
     * 初始化安全基线
     */
    void initBaselines();

    /**
     * 同步更新内置安全基线
     */
    void syncBuiltinBaselines();

    /**
     * 获取基线统计信息
     */
    List<BaselineStatistics> getBaselineStatistics();

    /**
     * 检查基线名称是否存在
     */
    boolean checkNameExists(String checkName);

    /**
     * 分页查询基线
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<SecurityBaseline> getBaselinesPage(
            int page, int size, String checkName, String complianceStandard, String checkType);

    /**
     * 基线统计DTO
     */
    class BaselineStatistics {
        private String complianceStandard;
        private Long totalCount;
        private Long enabledCount;
        private Long passCount;
        private Long failCount;

        public BaselineStatistics() {}

        public BaselineStatistics(String complianceStandard, Long totalCount, Long enabledCount, 
                                Long passCount, Long failCount) {
            this.complianceStandard = complianceStandard;
            this.totalCount = totalCount;
            this.enabledCount = enabledCount;
            this.passCount = passCount;
            this.failCount = failCount;
        }

        public String getComplianceStandard() {
            return complianceStandard;
        }

        public void setComplianceStandard(String complianceStandard) {
            this.complianceStandard = complianceStandard;
        }

        public Long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Long totalCount) {
            this.totalCount = totalCount;
        }

        public Long getEnabledCount() {
            return enabledCount;
        }

        public void setEnabledCount(Long enabledCount) {
            this.enabledCount = enabledCount;
        }

        public Long getPassCount() {
            return passCount;
        }

        public void setPassCount(Long passCount) {
            this.passCount = passCount;
        }

        public Long getFailCount() {
            return failCount;
        }

        public void setFailCount(Long failCount) {
            this.failCount = failCount;
        }
    }
}