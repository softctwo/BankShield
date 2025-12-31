package com.bankshield.api.controller;

import com.bankshield.api.entity.Dept;
import com.bankshield.api.service.DeptService;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 * 
 * @author BankShield
 */
@Slf4j
@RestController
@RequestMapping("/api/dept")
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 根据ID获取部门信息
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Result<Dept> getDeptById(@PathVariable Long id) {
        log.info("查询部门信息，ID: {}", id);
        return deptService.getDeptById(id);
    }

    /**
     * 分页查询部门列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Result<Page<Dept>> getDeptPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) String deptCode) {
        log.info("分页查询部门列表，页码: {}, 每页大小: {}, 部门名称: {}, 部门编码: {}", page, size, deptName, deptCode);
        return deptService.getDeptPage(page, size, deptName, deptCode);
    }

    /**
     * 获取部门树形结构
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tree")
    public Result<List<Dept>> getDeptTree() {
        log.info("获取部门树形结构");
        return deptService.getDeptTree();
    }

    /**
     * 添加部门
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<String> addDept(@RequestBody Dept dept) {
        log.info("添加部门，部门名称: {}", dept.getDeptName());
        return deptService.addDept(dept);
    }

    /**
     * 更新部门信息
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<String> updateDept(@RequestBody Dept dept) {
        log.info("更新部门信息，ID: {}", dept.getId());
        return deptService.updateDept(dept);
    }

    /**
     * 删除部门
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<String> deleteDept(@PathVariable Long id) {
        log.info("删除部门，ID: {}", id);
        return deptService.deleteDept(id);
    }
}