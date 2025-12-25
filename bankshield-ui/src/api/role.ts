import request from '@/utils/request'
import type { 
  RoleInfo, 
  RoleListParams, 
  RoleListResponse, 
  RoleCreateRequest, 
  RoleUpdateRequest 
} from '@/types/role'

// 获取角色列表（分页）
export const getRolePage = (params: RoleListParams) => {
  return request({
    url: '/api/role/page',
    method: 'get',
    params
  }) as Promise<RoleListResponse>
}

// 创建角色
export const createRole = (data: RoleCreateRequest) => {
  return request({
    url: '/api/role',
    method: 'post',
    data
  })
}

// 更新角色
export const updateRole = (data: RoleUpdateRequest) => {
  return request({
    url: '/api/role',
    method: 'put',
    data
  })
}

// 删除角色
export const deleteRole = (id: number) => {
  return request({
    url: `/api/role/${id}`,
    method: 'delete'
  })
}

// 获取所有启用角色（用于下拉选择）
export const getEnabledRoles = () => {
  return request({
    url: '/api/role/enabled',
    method: 'get'
  }) as Promise<RoleInfo[]>
}