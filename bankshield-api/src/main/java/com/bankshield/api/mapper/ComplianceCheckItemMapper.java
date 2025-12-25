package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ComplianceCheckItem;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 合规检查项Mapper接口
 */
public interface ComplianceCheckItemMapper extends BaseMapper<ComplianceCheckItem> {
    
    /**
     * 分页查询合规检查项
     */
    IPage<ComplianceCheckItem> selectCheckItemPage(Page<ComplianceCheckItem> page,
                                                   @Param("checkItemName") String checkItemName,
                                                   @Param("complianceStandard") String complianceStandard,
                                                   @Param("passStatus") String passStatus);
    
    /**
     * 查询需要检查的项目
     */
    List<ComplianceCheckItem> selectItemsNeedCheck(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 统计合规状态
     */
    List<ComplianceCheckItem> selectComplianceStatistics(@Param("complianceStandard") String complianceStandard);
    
    /**
     * 查询最近的不合规项
     */
    List<ComplianceCheckItem> selectRecentFailedItems(@Param("limit") int limit);
}