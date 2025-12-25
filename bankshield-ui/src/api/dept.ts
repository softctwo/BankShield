import request from '@/utils/request'
import type { Dept, DeptListParams, DeptListResponse, DeptTreeNode } from '@/types/dept'

// 获取部门列表（分页）
export const getDeptList = (params: DeptListParams) => {
  return request({
    url: '/api/dept/page',
    method: 'get',
    params
  }).then((response: any) => response.result)
}

// 获取部门树形结构
export const getDeptTree = () => {
  return request({
    url: '/api/dept/tree',
    method: 'get'
  }).then((response: any) => response.result)
}

// 根据ID获取部门信息
export const getDeptById = (id: number) => {
  return request({
    url: `/api/dept/${id}`,
    method: 'get'
  }).then((response: any) => response.result)
}

// 创建部门
export const createDept = (data: Partial<Dept>) => {
  return request({
    url: '/api/dept',
    method: 'post',
    data
  }).then((response: any) => response.result)
}

// 更新部门
export const updateDept = (data: Partial<Dept>) => {
  return request({
    url: '/api/dept',
    method: 'put',
    data
  }).then((response: any) => response.result)
}

// 删除部门
export const deleteDept = (id: number) => {
  return request({
    url: `/api/dept/${id}`,
    method: 'delete'
  }).then((response: any) => response.result)
}