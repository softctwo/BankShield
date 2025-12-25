import request from '@/utils/request'
import type { LoginRequest, LoginResponse } from '@/types/user'

// 登录
export const login = (data: LoginRequest) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  }) as Promise<LoginResponse>
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/auth/userinfo',
    method: 'get'
  })
}

// 登出
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}