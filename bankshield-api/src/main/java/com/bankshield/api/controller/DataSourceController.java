package com.bankshield.api.controller;

import com.bankshield.api.entity.DataSource;
import com.bankshield.api.mapper.DataSourceMapper;
import com.bankshield.api.service.AutoClassificationService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源管理控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/data-source")
@PreAuthorize("hasRole('ADMIN')")
public class DataSourceController {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private AutoClassificationService autoClassificationService;

    /**
     * 添加数据源
     */
    @PostMapping
    public Result<String> addDataSource(@RequestBody DataSource dataSource) {
        log.info("添加数据源，名称: {}", dataSource.getSourceName());
        try {
            // 参数校验
            if (dataSource.getSourceName() == null || dataSource.getSourceName().trim().isEmpty()) {
                return Result.error("数据源名称不能为空");
            }
            if (dataSource.getSourceType() == null || dataSource.getSourceType().trim().isEmpty()) {
                return Result.error("数据源类型不能为空");
            }
            
            dataSourceMapper.insert(dataSource);
            return Result.OK("添加数据源成功");
        } catch (Exception e) {
            log.error("添加数据源失败: {}", e.getMessage());
            return Result.error("添加数据源失败");
        }
    }

    /**
     * 更新数据源
     */
    @PutMapping
    public Result<String> updateDataSource(@RequestBody DataSource dataSource) {
        log.info("更新数据源，ID: {}", dataSource.getId());
        try {
            if (dataSource.getId() == null) {
                return Result.error("数据源ID不能为空");
            }
            
            dataSourceMapper.updateById(dataSource);
            return Result.OK("更新数据源成功");
        } catch (Exception e) {
            log.error("更新数据源失败: {}", e.getMessage());
            return Result.error("更新数据源失败");
        }
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDataSource(@PathVariable Long id) {
        log.info("删除数据源，ID: {}", id);
        try {
            dataSourceMapper.deleteById(id);
            return Result.OK("删除数据源成功");
        } catch (Exception e) {
            log.error("删除数据源失败: {}", e.getMessage());
            return Result.error("删除数据源失败");
        }
    }

    /**
     * 查询数据源详情
     */
    @GetMapping("/{id}")
    public Result<DataSource> getDataSourceById(@PathVariable Long id) {
        log.info("查询数据源详情，ID: {}", id);
        try {
            DataSource dataSource = dataSourceMapper.selectById(id);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            return Result.OK(dataSource);
        } catch (Exception e) {
            log.error("查询数据源失败: {}", e.getMessage());
            return Result.error("查询数据源失败");
        }
    }

    /**
     * 分页查询数据源列表
     */
    @GetMapping("/page")
    public Result<Page<DataSource>> getDataSourcePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sourceName,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Integer scanStatus) {
        log.info("分页查询数据源列表，页码: {}, 每页大小: {}", page, size);
        try {
            Page<DataSource> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
            
            if (sourceName != null && !sourceName.trim().isEmpty()) {
                queryWrapper.like(DataSource::getSourceName, sourceName);
            }
            if (sourceType != null && !sourceType.trim().isEmpty()) {
                queryWrapper.eq(DataSource::getSourceType, sourceType);
            }
            if (scanStatus != null) {
                queryWrapper.eq(DataSource::getScanStatus, scanStatus);
            }
            
            queryWrapper.orderByDesc(DataSource::getCreateTime);
            
            Page<DataSource> dataSourcePage = dataSourceMapper.selectPage(pageParam, queryWrapper);
            return Result.OK(dataSourcePage);
        } catch (Exception e) {
            log.error("分页查询数据源失败: {}", e.getMessage());
            return Result.error("分页查询数据源失败");
        }
    }

    /**
     * 获取所有数据源
     */
    @GetMapping("/all")
    public Result<List<DataSource>> getAllDataSources() {
        log.info("获取所有数据源");
        try {
            LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(DataSource::getCreateTime);
            
            List<DataSource> dataSources = dataSourceMapper.selectList(queryWrapper);
            return Result.OK(dataSources);
        } catch (Exception e) {
            log.error("获取数据源列表失败: {}", e.getMessage());
            return Result.error("获取数据源列表失败");
        }
    }

    /**
     * 按扫描状态查询数据源
     */
    @GetMapping("/by-scan-status")
    public Result<List<DataSource>> getDataSourcesByScanStatus(@RequestParam Integer scanStatus) {
        log.info("按扫描状态查询数据源，状态: {}", scanStatus);
        try {
            List<DataSource> dataSources = dataSourceMapper.selectByScanStatus(scanStatus);
            return Result.OK(dataSources);
        } catch (Exception e) {
            log.error("按扫描状态查询数据源失败: {}", e.getMessage());
            return Result.error("按扫描状态查询数据源失败");
        }
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/{id}/test-connection")
    public Result<String> testConnection(@PathVariable Long id) {
        log.info("测试数据源连接，ID: {}", id);
        try {
            DataSource dataSource = dataSourceMapper.selectById(id);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            
            return autoClassificationService.testConnection(dataSource);
        } catch (Exception e) {
            log.error("测试数据源连接失败: {}", e.getMessage());
            return Result.error("测试数据源连接失败");
        }
    }

    /**
     * 启动数据源扫描
     */
    @PostMapping("/{id}/scan")
    public Result<Long> startScan(@PathVariable Long id) {
        log.info("启动数据源扫描，ID: {}", id);
        return autoClassificationService.scanDataSource(id);
    }

    /**
     * 重新扫描数据源
     */
    @PostMapping("/{id}/rescan")
    public Result<Long> rescanDataSource(@PathVariable Long id) {
        log.info("重新扫描数据源，ID: {}", id);
        return autoClassificationService.rescanDataSource(id);
    }

    /**
     * 更新数据源扫描状态
     */
    @PutMapping("/{id}/scan-status")
    public Result<String> updateScanStatus(@PathVariable Long id,
                                          @RequestParam Integer scanStatus,
                                          @RequestParam(required = false) String scanError) {
        log.info("更新数据源扫描状态，ID: {}, 状态: {}", id, scanStatus);
        try {
            int result = dataSourceMapper.updateScanStatus(id, scanStatus, scanError);
            if (result > 0) {
                return Result.OK("更新扫描状态成功");
            } else {
                return Result.error("更新扫描状态失败");
            }
        } catch (Exception e) {
            log.error("更新扫描状态失败: {}", e.getMessage());
            return Result.error("更新扫描状态失败");
        }
    }
}