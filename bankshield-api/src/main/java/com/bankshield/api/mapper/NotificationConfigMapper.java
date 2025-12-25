package com.bankshield.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bankshield.api.entity.NotificationConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知配置Mapper接口
 */
@Repository
public interface NotificationConfigMapper extends BaseMapper<NotificationConfig> {

    /**
     * 分页查询通知配置
     */
    IPage<NotificationConfig> selectPage(Page<NotificationConfig> page,
                                        @Param("notifyType") String notifyType,
                                        @Param("enabled") Integer enabled);

    /**
     * 查询所有启用的通知配置
     */
    List<NotificationConfig> selectEnabledConfigs();

    /**
     * 根据通知类型查询启用的配置
     */
    List<NotificationConfig> selectEnabledByType(@Param("notifyType") String notifyType);
}