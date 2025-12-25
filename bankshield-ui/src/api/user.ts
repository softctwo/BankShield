import request from '@/utils/request'
import type { UserInfo, UserListParams, UserListResponse } from '@/types/user'

// 登录
export const login = (username: string, password: string) => {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data: { username, password }
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get<UserInfo>('/api/user/info')
}

// 更新用户信息
export const updateUserInfo = (data: Partial<UserInfo>) => {
  return request.put('/api/user/info', data)
}

// 获取用户列表
export const getUserList = (params: UserListParams) => {
  return request({
    url: '/api/user/list',
    method: 'get',
    params
  }) as Promise<UserListResponse>
}

// 创建用户
export const createUser = (data: Partial<UserInfo>) => {
  return request({
    url: '/api/user',
    method: 'post',
    data
  })
}

// 更新用户
export const updateUser = (id: number, data: Partial<UserInfo>) => {
  return request({
    url: `/api/user/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUser = (id: number) => {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

// 导出 userApi 对象
export const userApi = {
  login,
  getUserInfo,
  updateUserInfo,
  getUserList,
  createUser,
  updateUser,
  deleteUser
}

export default userApi