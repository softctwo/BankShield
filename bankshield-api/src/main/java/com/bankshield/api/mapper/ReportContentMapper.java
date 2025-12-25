package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.ReportContent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报表内容Mapper接口
 */
public interface ReportContentMapper extends BaseMapper<ReportContent> {
    
    /**
     * 分页查询报表内容
     */
    IPage<ReportContent> selectContentPage(Page<ReportContent> page,
                                           @Param("taskId") Long taskId,
                                           @Param("chapterName") String chapterName);
    
    /**
     * 根据任务ID查询所有内容
     */
    List<ReportContent> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据任务ID删除所有内容
     */
    int deleteByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 批量插入报表内容
     */
    int batchInsert(@Param("contents") List<ReportContent> contents);
}