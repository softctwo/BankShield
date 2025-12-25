package com.bankshield.api.service;

import com.bankshield.api.entity.Dept;
import com.bankshield.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 部门服务接口
 * 
 * @author BankShield
 */
public interface DeptService {

    /**
     * 根据ID获取部门信息
     * 
     * @param id 部门ID
     * @return 部门信息和结果状态
     */
    Result<Dept> getDeptById(Long id);

    /**
     * 分页查询部门列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param deptName 部门名称（可选，用于模糊查询）
     * @param deptCode 部门编码（可选，用于模糊查询）
     * @return 分页部门数据和结果状态
     */
    Result<Page<Dept>> getDeptPage(int page, int size, String deptName, String deptCode);

    /**
     * 获取所有启用的部门列表（树形结构）
     * 
     * @return 部门树列表和结果状态
     */
    Result<List<Dept>> getDeptTree();

    /**
     * 添加部门
     * 
     * @param dept 部门信息
     * @return 操作结果
     */
    Result<String> addDept(Dept dept);

    /**
     * 更新部门信息
     * 
     * @param dept 部门信息
     * @return 操作结果
     */
    Result<String> updateDept(Dept dept);

    /**
     * 删除部门
     * 
     * @param id 部门ID
     * @return 操作结果
     */
    Result<String> deleteDept(Long id);
}