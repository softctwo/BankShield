package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.SecurityScanTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全扫描任务Mapper
 * @author BankShield
 */
@Mapper
public interface SecurityScanTaskMapper extends BaseMapper<SecurityScanTask> {

    /**
     * 根据状态查询扫描任务
     */
    @Select("SELECT * FROM security_scan_task WHERE status = #{status} ORDER BY create_time DESC")
    List<SecurityScanTask> selectByStatus(@Param("status") String status);

    /**
     * 查询待执行的扫描任务
     */
    @Select("SELECT * FROM security_scan_task WHERE status = 'PENDING' ORDER BY create_time ASC")
    List<SecurityScanTask> selectPendingTasks();

    /**
     * 统计指定时间范围内的扫描任务
     */
    @Select("SELECT COUNT(*) FROM security_scan_task WHERE create_time >= #{startTime} AND create_time <= #{endTime}")
    int countTasksByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计不同状态的扫描任务数量
     */
    @Select("SELECT status, COUNT(*) as count FROM security_scan_task GROUP BY status")
    List<TaskStatusCount> countTasksByStatus();

    /**
     * 查询最近的扫描任务
     */
    @Select("SELECT * FROM security_scan_task ORDER BY create_time DESC LIMIT #{limit}")
    List<SecurityScanTask> selectRecentTasks(@Param("limit") int limit);

    /**
     * 更新扫描任务状态
     */
    int updateTaskStatus(@Param("id") Long id, @Param("status") String status, 
                        @Param("progress") Integer progress, @Param("errorMessage") String errorMessage);

    /**
     * 更新扫描任务完成信息
     */
    int updateTaskComplete(@Param("id") Long id, @Param("status") String status, 
                          @Param("endTime") LocalDateTime endTime, @Param("riskCount") Integer riskCount,
                          @Param("reportPath") String reportPath);

    /**
     * 任务状态统计DTO
     */
    class TaskStatusCount {
        private String status;
        private Long count;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }
}