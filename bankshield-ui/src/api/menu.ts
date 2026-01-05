import request from '@/utils/request'

/**
 * 获取用户菜单树
 */
export function getUserMenuTree(userId: number) {
  return request({
    url: `/api/system/menu/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取用户权限
 */
export function getUserPerms(userId: number) {
  return request({
    url: `/api/system/menu/perms/${userId}`,
    method: 'get'
  })
}

/**
 * 获取路由信息
 */
export function getRouters() {
  return request({
    url: '/api/system/menu/getRouters',
    method: 'get'
  })
}

/**
 * 查询所有菜单
 */
export function listMenu() {
  return request({
    url: '/api/system/menu/list',
    method: 'get'
  })
}

/**
 * 获取菜单树
 */
export function getMenuTree() {
  return request({
    url: '/api/system/menu/tree',
    method: 'get'
  })
}

/**
 * 根据ID查询菜单
 */
export function getMenu(menuId: number) {
  return request({
    url: `/api/system/menu/${menuId}`,
    method: 'get'
  })
}

/**
 * 新增菜单
 */
export function addMenu(data: any) {
  return request({
    url: '/api/system/menu',
    method: 'post',
    data
  })
}

/**
 * 修改菜单
 */
export function updateMenu(data: any) {
  return request({
    url: '/api/system/menu',
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 */
export function delMenu(menuId: number) {
  return request({
    url: `/api/system/menu/${menuId}`,
    method: 'delete'
  })
} from '@/utils/request'

/**
 * 获取用户菜单树
 */
export function getUserMenuTree(userId: number) {
  return request({
    url: `/api/system/menu/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取用户权限
 */
export function getUserPerms(userId: number) {
  return request({
    url: `/api/system/menu/perms/${userId}`,
    method: 'get'
  })
}

/**
 * 获取路由信息
 */
export function getRouters() {
  return request({
    url: '/api/system/menu/getRouters',
    method: 'get'
  })
}

/**
 * 查询所有菜单
 */
export function listMenu() {
  return request({
    url: '/api/system/menu/list',
    method: 'get'
  })
}

/**
 * 获取菜单树
 */
export function getMenuTree() {
  return request({
    url: '/api/system/menu/tree',
    method: 'get'
  })
}

/**
 * 根据ID查询菜单
 */
export function getMenu(menuId: number) {
  return request({
    url: `/api/system/menu/${menuId}`,
    method: 'get'
  })
}

/**
 * 新增菜单
 */
export function addMenu(data: any) {
  return request({
    url: '/api/system/menu',
    method: 'post',
    data
  })
}

/**
 * 修改菜单
 */
export function updateMenu(data: any) {
  return request({
    url: '/api/system/menu',
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 */
export function delMenu(menuId: number) {
  return request({
    url: `/api/system/menu/${menuId}`,
    method: 'delete'
  })
} from '@/utils/request'
import type { MenuInfo, MenuCreateRequest, MenuUpdateRequest } from '@/types/menu'

// 获取菜单列表
export const getMenuList = () => {
  return request({
    url: '/menu/list',
    method: 'get'
  }) as Promise<MenuInfo[]>
}

// 获取菜单树结构
export const getMenuTree = () => {
  return request({
    url: '/menu/tree',
    method: 'get'
  }) as Promise<MenuInfo[]>
}

// 根据ID获取菜单信息
export const getMenuById = (id: number) => {
  return request({
    url: `/menu/${id}`,
    method: 'get'
  }) as Promise<MenuInfo>
}

// 创建菜单
export const createMenu = (data: MenuCreateRequest) => {
  return request({
    url: '/menu',
    method: 'post',
    data
  }) as Promise<number>
}

// 更新菜单
export const updateMenu = (id: number, data: MenuUpdateRequest) => {
  return request({
    url: `/menu/${id}`,
    method: 'put',
    data
  }) as Promise<string>
}

// 删除菜单
export const deleteMenu = (id: number) => {
  return request({
    url: `/menu/${id}`,
    method: 'delete'
  }) as Promise<string>
}

// 获取父级菜单列表（用于选择上级菜单）
export const getParentMenuList = () => {
  return request({
    url: '/menu/parent-list',
    method: 'get'
  }) as Promise<MenuInfo[]>
}