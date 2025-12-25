export interface RoleInfo {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number // 0: 禁用, 1: 启用
  createTime?: string
  updateTime?: string
}

export interface RoleListParams {
  page?: number
  size?: number
  roleName?: string
  roleCode?: string
  status?: number
}

export interface RoleListResponse {
  list: RoleInfo[]
  total: number
  page: number
  size: number
}

export interface RoleCreateRequest {
  roleName: string
  roleCode: string
  description?: string
  status: number
}

export interface RoleUpdateRequest {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
}