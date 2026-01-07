package com.bankshield.api.service.impl;

import com.bankshield.api.entity.DataSource;
import com.bankshield.api.mapper.DataSourceMapper;
import com.bankshield.api.service.IDataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据源服务实现类
 *
 * @author BankShield
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements IDataSourceService {

    private final DataSourceMapper dataSourceMapper;

    @Override
    public List<DataSource> listAll() {
        try {
            return dataSourceMapper.selectList(null);
        } catch (Exception e) {
            log.error("查询所有数据源失败", e);
            return List.of();
        }
    }

    @Override
    public DataSource getById(Long id) {
        try {
            return dataSourceMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据ID查询数据源失败: {}", id, e);
            return null;
        }
    }

    @Override
    public boolean save(DataSource dataSource) {
        try {
            return dataSourceMapper.insert(dataSource) > 0;
        } catch (Exception e) {
            log.error("保存数据源失败", e);
            return false;
        }
    }

    @Override
    public boolean updateById(DataSource dataSource) {
        try {
            return dataSourceMapper.updateById(dataSource) > 0;
        } catch (Exception e) {
            log.error("更新数据源失败", e);
            return false;
        }
    }

    @Override
    public boolean removeById(Long id) {
        try {
            return dataSourceMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除数据源失败: {}", id, e);
            return false;
        }
    }
}
