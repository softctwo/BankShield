// 菜单类型枚举
export enum MenuType {
  DIRECTORY = 0, // 目录
  MENU = 1,      // 菜单
  BUTTON = 2     // 按钮
}

// 菜单状态枚举
export enum MenuStatus {
  DISABLED = 0, // 禁用
  ENABLED = 1   // 启用
}

// 菜单信息接口
export interface MenuInfo {
  id: number
  parentId: number | null
  menuName: string
  menuCode: string
  path: string | null
  component: string | null
  icon: string | null
  sortOrder: number
  menuType: MenuType
  permission: string | null
  status: MenuStatus
  createTime: string
  updateTime: string
  children?: MenuInfo[]
}

// 菜单列表响应
export interface MenuListResponse {
  code: number
  data: MenuInfo[]
  message: string
}

// 菜单树响应
export interface MenuTreeResponse {
  code: number
  data: MenuInfo[]
  message: string
}

// 创建菜单请求
export interface MenuCreateRequest {
  parentId: number | null
  menuName: string
  menuCode: string
  path: string | null
  component: string | null
  icon: string | null
  sortOrder: number
  menuType: MenuType
  permission: string | null
  status: MenuStatus
}

// 更新菜单请求
export interface MenuUpdateRequest {
  parentId: number | null
  menuName: string
  menuCode: string
  path: string | null
  component: string | null
  icon: string | null
  sortOrder: number
  menuType: MenuType
  permission: string | null
  status: MenuStatus
}

// 菜单树节点（用于前端展示）
export interface MenuTreeNode {
  id: number
  label: string
  children?: MenuTreeNode[]
  disabled?: boolean
}

// 图标选择器选项
export interface IconOption {
  value: string
  label: string
  icon: string
}