import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLogin = computed(() => !!token.value)
  const hasRole = computed(() => (role: string) => {
    return userInfo.value?.roles?.includes(role) || false
  })
  const hasPermission = computed(() => (permission: string) => {
    return userInfo.value?.permissions?.includes(permission) || false
  })

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
  }

  return {
    // 状态
    token,
    userInfo,
    // 计算属性
    isLogin,
    hasRole,
    hasPermission,
    // 方法
    setToken,
    setUserInfo,
    clearUserInfo,
    logout
  }
})