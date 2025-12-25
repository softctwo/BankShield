/**
 * 前端性能监控工具
 * 监控页面加载性能、API响应时间、资源加载等
 */

interface PerformanceMetrics {
  pageLoadTime: number
  domContentLoadedTime: number
  firstPaintTime: number
  firstContentfulPaintTime: number
  resourceLoadTimes: ResourceTiming[]
  apiResponseTimes: ApiTiming[]
}

interface ResourceTiming {
  name: string
  duration: number
  size: number
  type: string
}

interface ApiTiming {
  url: string
  method: string
  duration: number
  status: number
  timestamp: number
}

class PerformanceMonitor {
  private metrics: PerformanceMetrics = {
    pageLoadTime: 0,
    domContentLoadedTime: 0,
    firstPaintTime: 0,
    firstContentfulPaintTime: 0,
    resourceLoadTimes: [],
    apiResponseTimes: []
  }

  private apiTimings: Map<string, number> = new Map()
  private maxApiTimings = 100
  private maxResourceTimings = 50

  constructor() {
    this.init()
  }

  private init() {
    // 监听页面加载完成
    window.addEventListener('load', () => {
      this.collectPageLoadMetrics()
    })

    // 监听路由变化（Vue Router）
    this.observeRouteChanges()

    // 监听资源加载
    this.observeResourceLoading()

    // 监听API调用
    this.interceptApiCalls()
  }

  /**
   * 收集页面加载性能指标
   */
  private collectPageLoadMetrics() {
    if (!window.performance || !window.performance.timing) return

    const timing = window.performance.timing
    const navigation = window.performance.navigation

    // 页面加载时间
    this.metrics.pageLoadTime = timing.loadEventEnd - timing.navigationStart

    // DOM内容加载时间
    this.metrics.domContentLoadedTime = timing.domContentLoadedEventEnd - timing.navigationStart

    // 使用PerformanceObserver获取FP和FCP
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.name === 'first-paint') {
            this.metrics.firstPaintTime = entry.startTime
          } else if (entry.name === 'first-contentful-paint') {
            this.metrics.firstContentfulPaintTime = entry.startTime
          }
        }
      })

      observer.observe({ entryTypes: ['paint'] })
    }

    // 输出性能指标
    this.logPerformanceMetrics()
  }

  /**
   * 监听路由变化
   */
  private observeRouteChanges() {
    // 监听Vue Router的路由变化
    if (window.__VUE_ROUTER__) {
      window.__VUE_ROUTER__.beforeEach((to: any, from: any, next: any) => {
        // 记录路由切换开始时间
        window.performance.mark(`route-start-${to.path}`)
        next()
      })

      window.__VUE_ROUTER__.afterEach((to: any, from: any) => {
        // 记录路由切换结束时间
        window.performance.mark(`route-end-${to.path}`)
        window.performance.measure(
          `route-${to.path}`,
          `route-start-${to.path}`,
          `route-end-${to.path}`
        )

        const measure = window.performance.getEntriesByName(`route-${to.path}`)[0]
        if (measure) {
          console.log(`路由切换性能: ${to.path} - ${measure.duration.toFixed(2)}ms`)
        }
      })
    }
  }

  /**
   * 监听资源加载性能
   */
  private observeResourceLoading() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.entryType === 'resource') {
            const resourceEntry = entry as PerformanceResourceTiming
            
            // 只关注重要的资源
            if (this.isImportantResource(resourceEntry.name)) {
              const timing: ResourceTiming = {
                name: resourceEntry.name,
                duration: resourceEntry.duration,
                size: resourceEntry.transferSize || 0,
                type: resourceEntry.initiatorType
              }

              this.metrics.resourceLoadTimes.push(timing)

              // 限制记录数量
              if (this.metrics.resourceLoadTimes.length > this.maxResourceTimings) {
                this.metrics.resourceLoadTimes.shift()
              }

              // 输出慢资源警告
              if (resourceEntry.duration > 1000) {
                console.warn(`慢资源加载: ${resourceEntry.name} - ${resourceEntry.duration.toFixed(2)}ms`)
              }
            }
          }
        }
      })

      observer.observe({ entryTypes: ['resource'] })
    }
  }

  /**
   * 拦截API调用，监控响应时间
   */
  private interceptApiCalls() {
    const originalFetch = window.fetch
    const self = this

    window.fetch = async function (input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
      const url = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url
      const method = init?.method || 'GET'
      const startTime = performance.now()
      const timestamp = Date.now()

      try {
        const response = await originalFetch.call(this, input, init)
        const duration = performance.now() - startTime

        const timing: ApiTiming = {
          url: url.split('?')[0], // 去除查询参数
          method,
          duration,
          status: response.status,
          timestamp
        }

        self.metrics.apiResponseTimes.push(timing)

        // 限制记录数量
        if (self.metrics.apiResponseTimes.length > self.maxApiTimings) {
          self.metrics.apiResponseTimes.shift()
        }

        // 输出慢API警告
        if (duration > 2000) {
          console.warn(`慢API响应: ${method} ${url} - ${duration.toFixed(2)}ms`)
        }

        return response
      } catch (error) {
        const duration = performance.now() - startTime
        console.error(`API请求失败: ${method} ${url} - ${duration.toFixed(2)}ms`, error)
        throw error
      }
    }
  }

  /**
   * 判断是否为重要资源
   */
  private isImportantResource(url: string): boolean {
    const importantPatterns = [
      /\.js$/,
      /\.css$/,
      /\.vue$/,
      /api\//,
      /assets\//,
      /components\//
    ]

    return importantPatterns.some(pattern => pattern.test(url))
  }

  /**
   * 输出性能指标
   */
  private logPerformanceMetrics() {
    console.group('页面性能指标')
    console.log(`页面加载时间: ${this.metrics.pageLoadTime}ms`)
    console.log(`DOM内容加载时间: ${this.metrics.domContentLoadedTime}ms`)
    console.log(`首次绘制时间: ${this.metrics.firstPaintTime.toFixed(2)}ms`)
    console.log(`首次内容绘制时间: ${this.metrics.firstContentfulPaintTime.toFixed(2)}ms`)
    
    if (this.metrics.resourceLoadTimes.length > 0) {
      console.log(`资源加载数量: ${this.metrics.resourceLoadTimes.length}`)
      const slowResources = this.metrics.resourceLoadTimes.filter(r => r.duration > 1000)
      if (slowResources.length > 0) {
        console.warn(`慢资源数量: ${slowResources.length}`)
      }
    }
    console.groupEnd()
  }

  /**
   * 获取性能指标
   */
  public getMetrics(): PerformanceMetrics {
    return { ...this.metrics }
  }

  /**
   * 获取慢API列表
   */
  public getSlowApis(threshold: number = 1000): ApiTiming[] {
    return this.metrics.apiResponseTimes.filter(api => api.duration > threshold)
  }

  /**
   * 获取慢资源列表
   */
  public getSlowResources(threshold: number = 1000): ResourceTiming[] {
    return this.metrics.resourceLoadTimes.filter(resource => resource.duration > threshold)
  }

  /**
   * 清理性能数据
   */
  public clearMetrics() {
    this.metrics = {
      pageLoadTime: 0,
      domContentLoadedTime: 0,
      firstPaintTime: 0,
      firstContentfulPaintTime: 0,
      resourceLoadTimes: [],
      apiResponseTimes: []
    }
  }

  /**
   * 导出性能报告
   */
  public exportReport(): string {
    const report = {
      timestamp: new Date().toISOString(),
      metrics: this.getMetrics(),
      slowApis: this.getSlowApis(),
      slowResources: this.getSlowResources(),
      userAgent: navigator.userAgent,
      url: window.location.href
    }

    return JSON.stringify(report, null, 2)
  }
}

// 创建全局性能监控实例
export const performanceMonitor = new PerformanceMonitor()

// 将性能监控挂载到window对象，方便调试
if (typeof window !== 'undefined') {
  ;(window as any).performanceMonitor = performanceMonitor
}

export default performanceMonitor