package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.SecurityScanResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 安全扫描结果Mapper
 * @author BankShield
 */
@Mapper
public interface SecurityScanResultMapper extends BaseMapper<SecurityScanResult> {

    /**
     * 根据任务ID查询扫描结果
     */
    @Select("SELECT * FROM security_scan_result WHERE task_id = #{taskId} ORDER BY risk_level, create_time DESC")
    List<SecurityScanResult> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据风险级别查询扫描结果
     */
    @Select("SELECT * FROM security_scan_result WHERE risk_level = #{riskLevel} ORDER BY create_time DESC")
    List<SecurityScanResult> selectByRiskLevel(@Param("riskLevel") String riskLevel);

    /**
     * 统计任务的风险分布
     */
    @Select("SELECT risk_level, COUNT(*) as count FROM security_scan_result WHERE task_id = #{taskId} GROUP BY risk_level")
    List<RiskLevelCount> countByRiskLevel(@Param("taskId") Long taskId);

    /**
     * 统计不同修复状态的结果数量
     */
    @Select("SELECT fix_status, COUNT(*) as count FROM security_scan_result WHERE task_id = #{taskId} GROUP BY fix_status")
    List<FixStatusCount> countByFixStatus(@Param("taskId") Long taskId);

    /**
     * 查询未修复的高危和严重风险
     */
    @Select("SELECT * FROM security_scan_result WHERE fix_status = 'UNFIXED' AND risk_level IN ('CRITICAL', 'HIGH') ORDER BY create_time DESC")
    List<SecurityScanResult> selectUnfixedHighRisk();

    /**
     * 查询指定时间范围内发现的风险
     */
    @Select("SELECT * FROM security_scan_result WHERE discovered_time >= #{startTime} AND discovered_time <= #{endTime} ORDER BY discovered_time DESC")
    List<SecurityScanResult> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 更新修复状态
     */
    int updateFixStatus(@Param("id") Long id, @Param("fixStatus") String fixStatus, 
                       @Param("fixBy") String fixBy, @Param("fixTime") LocalDateTime fixTime);

    /**
     * 更新验证结果
     */
    int updateVerifyResult(@Param("id") Long id, @Param("verifyResult") String verifyResult,
                          @Param("verifyBy") String verifyBy, @Param("verifyTime") LocalDateTime verifyTime);

    /**
     * 批量插入扫描结果
     */
    int batchInsert(@Param("results") List<SecurityScanResult> results);

    /**
     * 风险级别统计DTO
     */
    class RiskLevelCount {
        private String riskLevel;
        private Long count;

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    /**
     * 修复状态统计DTO
     */
    class FixStatusCount {
        private String fixStatus;
        private Long count;

        public String getFixStatus() {
            return fixStatus;
        }

        public void setFixStatus(String fixStatus) {
            this.fixStatus = fixStatus;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }
}