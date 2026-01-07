/**
 * API请求辅助工具
 * 提供统一的错误处理和降级方案
 */

/**
 * 安全的API调用包装器
 * 当API调用失败时，返回fallbackData而不是抛出错误
 */
export async function safeApiCall<T>(
  apiCall: () => Promise<any>,
  fallbackData: T,
  options?: {
    showError?: boolean
    errorMessage?: string
  }
): Promise<T> {
  try {
    const res = await apiCall()
    if (res && res.code === 200) {
      return res.data || fallbackData
    }
    return fallbackData
  } catch (error) {
    console.error('API调用失败:', error)
    if (options?.showError && options?.errorMessage) {
      // 可以在这里添加错误提示逻辑
    }
    return fallbackData
  }
}

/**
 * 批量安全API调用
 */
export async function batchSafeApiCall<T extends Record<string, any>>(
  apiCalls: Record<keyof T, () => Promise<any>>,
  fallbackData: T
): Promise<T> {
  const results = {} as T
  const promises = Object.entries(apiCalls).map(async ([key, apiCall]) => {
    const data = await safeApiCall(apiCall as () => Promise<any>, fallbackData[key as keyof T])
    return { key, data }
  })
  
  const settled = await Promise.allSettled(promises)
  settled.forEach((result) => {
    if (result.status === 'fulfilled') {
      results[result.value.key as keyof T] = result.value.data
    }
  })
  
  return results
}
