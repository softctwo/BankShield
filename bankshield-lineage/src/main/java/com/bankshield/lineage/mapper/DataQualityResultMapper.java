package com.bankshield.lineage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bankshield.lineage.entity.DataQualityResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据质量检查结果 Mapper 接口
 *
 * @author BankShield
 * @since 2024-01-24
 */
@Mapper
public interface DataQualityResultMapper extends BaseMapper<DataQualityResult> {
}
