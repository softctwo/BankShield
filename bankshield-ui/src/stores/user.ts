import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  // 注意：token 存储在 localStorage 存在 XSS 攻击风险
  // 建议后续改为 HttpOnly Cookie，由后端设置：
  // - Access Token（短期，如 15 分钟）
  // - Refresh Token（长期，如 7 天，HttpOnly）
  const token = ref<string>(localStorage.getItem('token') || '')
  const tokenExpiry = ref<number>(0) // token 过期时间戳
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLogin = computed(() => !!token.value && Date.now() < tokenExpiry.value)
  const hasRole = computed(() => (role: string) => {
    return userInfo.value?.roles?.includes(role) || false
  })
  const hasPermission = computed(() => (permission: string) => {
    return userInfo.value?.permissions?.includes(permission) || false
  })

  // 方法
  const setToken = (newToken: string, expiresIn: number = 3600) => {
    token.value = newToken
    // 设置过期时间，默认 1 小时
    tokenExpiry.value = Date.now() + (expiresIn * 1000)
    localStorage.setItem('token', newToken)
    localStorage.setItem('tokenExpiry', tokenExpiry.value.toString())
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  const clearUserInfo = () => {
    token.value = ''
    tokenExpiry.value = 0
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('tokenExpiry')
  }

  const logout = () => {
    clearUserInfo()
  }

  // 登录方法
  const login = async (loginData: { username: string; token: string; roles: string[] }) => {
    setToken(loginData.token)
    setUserInfo({
      username: loginData.username,
      roles: loginData.roles,
      permissions: [], // 可以根据需要添加权限
      avatar: '',
      email: '',
      phone: ''
    })
  }

  // 初始化时检查 token 是否过期
  const initTokenExpiry = () => {
    const storedExpiry = localStorage.getItem('tokenExpiry')
    if (storedExpiry) {
      tokenExpiry.value = parseInt(storedExpiry, 10)
      if (Date.now() >= tokenExpiry.value) {
        // token 已过期，清除
        clearUserInfo()
      }
    }
  }

  // 页面加载时初始化
  initTokenExpiry()

  return {
    // 状态
    token,
    tokenExpiry,
    userInfo,
    // 计算属性
    isLogin,
    hasRole,
    hasPermission,
    // 方法
    setToken,
    setUserInfo,
    clearUserInfo,
    logout,
    login
  }
})