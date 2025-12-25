package com.bankshield.api.service;

import com.bankshield.api.entity.DataSource;
import java.util.List;

/**
 * 数据源服务接口
 *
 * @author BankShield
 */
public interface IDataSourceService {

    /**
     * 查询所有数据源
     *
     * @return 数据源列表
     */
    List<DataSource> listAll();

    /**
     * 根据ID查询数据源
     *
     * @param id 数据源ID
     * @return 数据源
     */
    DataSource getById(Long id);

    /**
     * 保存数据源
     *
     * @param dataSource 数据源
     * @return 保存结果
     */
    boolean save(DataSource dataSource);

    /**
     * 更新数据源
     *
     * @param dataSource 数据源
     * @return 更新结果
     */
    boolean updateById(DataSource dataSource);

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     * @return 删除结果
     */
    boolean removeById(Long id);
}
