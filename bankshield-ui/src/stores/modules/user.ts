import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/user'
import { userApi } from '@/api/user'

/**
 * 用户状态管理模块
 * 优化：按业务模块拆分状态管理
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref<boolean>(false)

  // 计算属性
  const isLogin = computed(() => !!token.value)
  const hasRole = computed(() => (role: string) => {
    return userInfo.value?.roles?.includes(role) || false
  })
  const hasPermission = computed(() => (permission: string) => {
    return userInfo.value?.permissions?.includes(permission) || false
  })
  const userRoles = computed(() => userInfo.value?.roles || [])
  const userPermissions = computed(() => userInfo.value?.permissions || [])

  // 方法
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  const clearUserInfo = () => {
    userInfo.value = null
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    sessionStorage.clear()
  }

  // 异步方法
  const login = async (username: string, password: string) => {
    try {
      loading.value = true
      const response = await userApi.login(username, password)
      const { token: newToken, userInfo: info } = response.data
      
      setToken(newToken)
      setUserInfo(info)
      
      return { success: true }
    } catch (error) {
      return { success: false, error }
    } finally {
      loading.value = false
    }
  }

  const getUserInfo = async () => {
    if (!token.value) return null
    
    try {
      loading.value = true
      const response = await userApi.getUserInfo()
      setUserInfo(response.data)
      return response.data
    } catch (error) {
      logout()
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateUserInfo = async (data: Partial<UserInfo>) => {
    try {
      loading.value = true
      const response = await userApi.updateUserInfo(data)
      setUserInfo(response.data)
      return { success: true }
    } catch (error) {
      return { success: false, error }
    } finally {
      loading.value = false
    }
  }

  return {
    // 状态
    token,
    userInfo,
    loading,
    // 计算属性
    isLogin,
    hasRole,
    hasPermission,
    userRoles,
    userPermissions,
    // 方法
    setToken,
    setUserInfo,
    clearUserInfo,
    logout,
    login,
    getUserInfo,
    updateUserInfo
  }
})