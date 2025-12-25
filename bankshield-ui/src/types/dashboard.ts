/**
 * Dashboard 相关类型定义
 */

// 仪表板指标数据
export interface MetricsData {
  totalAssets?: number
  protectedAssets?: number
  securityScore?: number
  activeAlerts?: number
  todayScans?: number
  criticalVulnerabilities?: number
}

// 图表数据
export interface ChartData {
  trend?: ChartSeries[]
  distribution?: PieChartData[]
  comparison?: BarChartData[]
}

// 折线图系列数据
export interface ChartSeries {
  name: string
  data: number[]
  smooth?: boolean
}

// 饼图数据
export interface PieChartData {
  name: string
  value: number
  color?: string
}

// 柱状图数据
export interface BarChartData {
  name: string
  data: number[]
  color?: string
}

// 仪表板配置
export interface DashboardConfig {
  layout: 'default' | 'compact' | 'full'
  refreshInterval: number
  visibleWidgets: string[]
  theme: 'light' | 'dark'
}

// 仪表板小部件
export interface DashboardWidget {
  id: string
  type: 'metrics' | 'chart' | 'table' | 'alert'
  title: string
  position: { x: number; y: number; w: number; h: number }
  config?: Record<string, any>
}

// 仪表板筛选条件
export interface DashboardFilter {
  timeRange: 'today' | 'week' | 'month' | 'quarter' | 'year' | 'custom'
  startTime?: string
  endTime?: string
  assetType?: string
  severity?: string
}
