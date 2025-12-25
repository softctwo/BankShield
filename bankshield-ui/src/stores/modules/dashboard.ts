import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { dashboardApi } from '@/api/dashboard'
import type { MetricsData, ChartData } from '@/types/dashboard'

/**
 * 仪表板状态管理模块
 * 优化：分离仪表板相关状态，避免全局状态臃肿
 */
export const useDashboardStore = defineStore('dashboard', () => {
  // 状态
  const metrics = ref<MetricsData>({})
  const charts = ref<ChartData>({})
  const loading = ref<boolean>(false)
  const lastUpdateTime = ref<Date | null>(null)

  // 计算属性
  const isLoading = computed(() => loading.value)
  const hasData = computed(() => Object.keys(metrics.value).length > 0)
  const isDataStale = computed(() => {
    if (!lastUpdateTime.value) return true
    const now = new Date()
    const diff = now.getTime() - lastUpdateTime.value.getTime()
    return diff > 5 * 60 * 1000 // 5分钟过期
  })

  // 方法
  const setMetrics = (data: MetricsData) => {
    metrics.value = data
  }

  const setCharts = (data: ChartData) => {
    charts.value = data
  }

  const clearData = () => {
    metrics.value = {}
    charts.value = {}
    lastUpdateTime.value = null
  }

  // 异步方法
  const fetchMetrics = async () => {
    try {
      loading.value = true
      const response = await dashboardApi.getMetrics()
      setMetrics(response.data)
      lastUpdateTime.value = new Date()
      return { success: true }
    } catch (error) {
      return { success: false, error }
    } finally {
      loading.value = false
    }
  }

  const fetchCharts = async (chartTypes: string[]) => {
    try {
      loading.value = true
      const response = await dashboardApi.getCharts(chartTypes)
      setCharts(response.data)
      return { success: true }
    } catch (error) {
      return { success: false, error }
    } finally {
      loading.value = false
    }
  }

  const refreshData = async () => {
    if (isDataStale.value) {
      await Promise.all([fetchMetrics(), fetchCharts(['line', 'bar', 'pie'])])
    }
  }

  // 定时刷新数据（每5分钟）
  const startAutoRefresh = () => {
    const interval = setInterval(async () => {
      if (!isLoading.value) {
        await refreshData()
      }
    }, 5 * 60 * 1000)

    // 清理函数
    return () => clearInterval(interval)
  }

  return {
    // 状态
    metrics,
    charts,
    loading,
    lastUpdateTime,
    // 计算属性
    isLoading,
    hasData,
    isDataStale,
    // 方法
    setMetrics,
    setCharts,
    clearData,
    fetchMetrics,
    fetchCharts,
    refreshData,
    startAutoRefresh
  }
})