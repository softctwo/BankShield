package com.bankshield.api.service.impl;

import com.bankshield.api.entity.Dept;
import com.bankshield.api.mapper.DeptMapper;
import com.bankshield.api.service.DeptService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 * 
 * @author BankShield
 */
@Slf4j
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public Result<Dept> getDeptById(Long id) {
        try {
            Dept dept = this.getById(id);
            if (dept == null) {
                return Result.error("部门不存在");
            }
            return Result.OK(dept);
        } catch (Exception e) {
            log.error("查询部门失败: {}", e.getMessage());
            return Result.error("查询部门失败");
        }
    }

    @Override
    public Result<Page<Dept>> getDeptPage(int page, int size, String deptName, String deptCode) {
        try {
            Page<Dept> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加查询条件
            if (StringUtils.hasText(deptName)) {
                queryWrapper.like(Dept::getDeptName, deptName);
            }
            if (StringUtils.hasText(deptCode)) {
                queryWrapper.like(Dept::getDeptCode, deptCode);
            }
            
            // 按排序字段升序排序，如果没有指定则按创建时间降序
            queryWrapper.orderByAsc(Dept::getSortOrder);
            queryWrapper.orderByDesc(Dept::getCreateTime);
            
            Page<Dept> deptPage = this.page(pageParam, queryWrapper);
            return Result.OK(deptPage);
        } catch (Exception e) {
            log.error("分页查询部门失败: {}", e.getMessage());
            return Result.error("分页查询部门失败");
        }
    }

    @Override
    public Result<List<Dept>> getDeptTree() {
        try {
            LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dept::getStatus, 1); // 只查询启用的部门
            queryWrapper.orderByAsc(Dept::getSortOrder);
            queryWrapper.orderByDesc(Dept::getCreateTime);
            
            List<Dept> allDepts = this.list(queryWrapper);
            
            // 构建树形结构
            List<Dept> deptTree = buildDeptTree(allDepts);
            return Result.OK(deptTree);
        } catch (Exception e) {
            log.error("查询部门树失败: {}", e.getMessage());
            return Result.error("查询部门树失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addDept(Dept dept) {
        try {
            // 参数校验
            if (dept == null) {
                return Result.error("部门信息不能为空");
            }
            if (!StringUtils.hasText(dept.getDeptName())) {
                return Result.error("部门名称不能为空");
            }
            if (!StringUtils.hasText(dept.getDeptCode())) {
                return Result.error("部门编码不能为空");
            }

            // 检查部门编码是否已存在
            LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dept::getDeptCode, dept.getDeptCode());
            if (this.count(queryWrapper) > 0) {
                return Result.error("部门编码已存在");
            }

            // 设置默认值
            if (dept.getParentId() == null) {
                dept.setParentId(0L); // 默认根部门
            }
            if (dept.getSortOrder() == null) {
                dept.setSortOrder(0); // 默认排序
            }
            if (dept.getStatus() == null) {
                dept.setStatus(1); // 默认启用
            }
            dept.setCreateTime(LocalDateTime.now());
            dept.setUpdateTime(LocalDateTime.now());

            // 保存部门
            boolean result = this.save(dept);
            if (result) {
                return Result.OK("添加部门成功");
            } else {
                return Result.error("添加部门失败");
            }
        } catch (Exception e) {
            log.error("添加部门失败: {}", e.getMessage());
            return Result.error("添加部门失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateDept(Dept dept) {
        try {
            // 参数校验
            if (dept == null || dept.getId() == null) {
                return Result.error("部门ID不能为空");
            }

            // 检查部门是否存在
            Dept existingDept = this.getById(dept.getId());
            if (existingDept == null) {
                return Result.error("部门不存在");
            }

            // 如果修改了部门编码，检查新编码是否已存在
            if (StringUtils.hasText(dept.getDeptCode()) && !dept.getDeptCode().equals(existingDept.getDeptCode())) {
                LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Dept::getDeptCode, dept.getDeptCode());
                if (this.count(queryWrapper) > 0) {
                    return Result.error("部门编码已存在");
                }
            }

            // 设置更新时间
            dept.setUpdateTime(LocalDateTime.now());

            // 更新部门
            boolean result = this.updateById(dept);
            if (result) {
                return Result.OK("更新部门成功");
            } else {
                return Result.error("更新部门失败");
            }
        } catch (Exception e) {
            log.error("更新部门失败: {}", e.getMessage());
            return Result.error("更新部门失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteDept(Long id) {
        try {
            if (id == null) {
                return Result.error("部门ID不能为空");
            }

            // 检查部门是否存在
            Dept dept = this.getById(id);
            if (dept == null) {
                return Result.error("部门不存在");
            }

            // 检查是否有子部门
            LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dept::getParentId, id);
            if (this.count(queryWrapper) > 0) {
                return Result.error("该部门存在子部门，不能删除");
            }

            // TODO: 检查部门下是否有用户，如果有用户则不允许删除
            // 这里可以添加关联查询逻辑

            // 删除部门
            boolean result = this.removeById(id);
            if (result) {
                return Result.OK("删除部门成功");
            } else {
                return Result.error("删除部门失败");
            }
        } catch (Exception e) {
            log.error("删除部门失败: {}", e.getMessage());
            return Result.error("删除部门失败");
        }
    }

    /**
     * 构建部门树形结构
     * 
     * @param allDepts 所有部门列表
     * @return 树形结构的部门列表
     */
    private List<Dept> buildDeptTree(List<Dept> allDepts) {
        if (allDepts == null || allDepts.isEmpty()) {
            return new ArrayList<>();
        }

        // 按parentId分组
        Map<Long, List<Dept>> deptMap = allDepts.stream()
                .collect(Collectors.groupingBy(Dept::getParentId));

        // 递归构建树形结构
        return buildTreeRecursive(deptMap, 0L);
    }

    /**
     * 递归构建树形结构
     * 
     * @param deptMap 部门分组Map
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    private List<Dept> buildTreeRecursive(Map<Long, List<Dept>> deptMap, Long parentId) {
        List<Dept> children = deptMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        // 对每个子部门递归构建其子部门
        for (Dept dept : children) {
            List<Dept> subDepts = buildTreeRecursive(deptMap, dept.getId());
            if (!subDepts.isEmpty()) {
                // 这里可以添加子部门列表字段，如果实体类中有的话
                // dept.setChildren(subDepts);
            }
        }

        return children;
    }
}