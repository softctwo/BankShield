package com.bankshield.api.mapper;

import com.bankshield.api.entity.WatermarkExtractLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 水印提取日志Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface WatermarkExtractLogMapper extends BaseMapper<WatermarkExtractLog> {

    /**
     * 分页查询提取日志
     * 
     * @param page 分页参数
     * @param watermarkContent 水印内容（模糊查询）
     * @param extractResult 提取结果
     * @param operator 操作人员
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<WatermarkExtractLog> selectExtractLogPage(Page<WatermarkExtractLog> page,
                                                   @Param("watermarkContent") String watermarkContent,
                                                   @Param("extractResult") String extractResult,
                                                   @Param("operator") String operator,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段内的提取次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param extractResult 提取结果
     * @return 提取次数
     */
    long countExtractLogs(@Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime,
                         @Param("extractResult") String extractResult);
}