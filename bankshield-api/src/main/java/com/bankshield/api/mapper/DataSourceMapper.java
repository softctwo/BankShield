package com.bankshield.api.mapper;

import com.bankshield.api.entity.DataSource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据源配置Mapper接口
 * 
 * @author BankShield
 */
@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {

    /**
     * 按扫描状态查询数据源
     * 
     * @param scanStatus 扫描状态
     * @return 数据源列表
     */
    List<DataSource> selectByScanStatus(@Param("scanStatus") Integer scanStatus);

    /**
     * 更新数据源扫描状态
     * 
     * @param id 数据源ID
     * @param scanStatus 扫描状态
     * @param scanError 扫描错误信息
     * @return 更新结果
     */
    int updateScanStatus(@Param("id") Long id, 
                        @Param("scanStatus") Integer scanStatus,
                        @Param("scanError") String scanError);
}