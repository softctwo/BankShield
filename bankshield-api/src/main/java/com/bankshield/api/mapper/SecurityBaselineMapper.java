package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.SecurityBaseline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 安全基线配置Mapper
 * @author BankShield
 */
@Mapper
public interface SecurityBaselineMapper extends BaseMapper<SecurityBaseline> {

    /**
     * 查询启用的基线检查项
     */
    @Select("SELECT * FROM security_baseline WHERE enabled = true ORDER BY check_type, check_name")
    List<SecurityBaseline> selectEnabledBaselines();

    /**
     * 根据检查类型查询基线
     */
    @Select("SELECT * FROM security_baseline WHERE check_type = #{checkType} ORDER BY check_name")
    List<SecurityBaseline> selectByCheckType(@Param("checkType") String checkType);

    /**
     * 查询内置的基线检查项
     */
    @Select("SELECT * FROM security_baseline WHERE builtin = true ORDER BY check_type, check_name")
    List<SecurityBaseline> selectBuiltinBaselines();

    /**
     * 查询自定义的基线检查项
     */
    @Select("SELECT * FROM security_baseline WHERE builtin = false ORDER BY check_type, check_name")
    List<SecurityBaseline> selectCustomBaselines();

    /**
     * 统计不同检查类型的基线数量
     */
    @Select("SELECT check_type, COUNT(*) as count FROM security_baseline GROUP BY check_type")
    List<CheckTypeCount> countByCheckType();

    /**
     * 统计启用和禁用的基线数量
     */
    @Select("SELECT enabled, COUNT(*) as count FROM security_baseline GROUP BY enabled")
    List<EnabledCount> countByEnabled();

    /**
     * 根据名称查询基线（检查重复）
     */
    @Select("SELECT COUNT(*) FROM security_baseline WHERE check_name = #{checkName}")
    int countByCheckName(@Param("checkName") String checkName);

    /**
     * 批量更新启用状态
     */
    int batchUpdateEnabled(@Param("ids") List<Long> ids, @Param("enabled") Boolean enabled);

    /**
     * 检查类型统计DTO
     */
    class CheckTypeCount {
        private String checkType;
        private Long count;

        public String getCheckType() {
            return checkType;
        }

        public void setCheckType(String checkType) {
            this.checkType = checkType;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    /**
     * 启用状态统计DTO
     */
    class EnabledCount {
        private Boolean enabled;
        private Long count;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }
}