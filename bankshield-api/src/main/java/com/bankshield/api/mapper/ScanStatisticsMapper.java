package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.api.entity.ScanStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScanStatisticsMapper extends BaseMapper<ScanStatistics> {

    @Select("SELECT * FROM scan_statistics WHERE stat_date >= #{startDate} AND stat_date <= #{endDate} ORDER BY stat_date ASC")
    List<ScanStatistics> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM scan_statistics ORDER BY stat_date DESC LIMIT #{days}")
    List<ScanStatistics> selectRecentDays(@Param("days") int days);
}
