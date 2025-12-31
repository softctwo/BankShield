# BankShield 实施指南

## 快速开始

本文档提供了BankShield系统各项功能的详细实施指南。

---

## 1. 测试覆盖率提升

### 1.1 安装测试依赖

```bash
# 前端测试框架
npm install -D vitest @vitest/ui @vue/test-utils happy-dom
npm install -D @testing-library/vue @testing-library/user-event

# E2E测试
npm install -D playwright @playwright/test

# 覆盖率报告
npm install -D @vitest/coverage-v8
```

### 1.2 配置Vitest

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'happy-dom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.spec.ts',
        '**/*.test.ts'
      ]
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
```

### 1.3 编写单元测试示例

```typescript
// src/utils/__tests__/format.spec.ts
import { describe, it, expect } from 'vitest'
import { formatDate, formatNumber, formatFileSize } from '../format'

describe('Format Utils', () => {
  describe('formatDate', () => {
    it('should format date correctly', () => {
      const date = new Date('2024-12-30T12:00:00')
      expect(formatDate(date)).toBe('2024-12-30 12:00:00')
    })
  })

  describe('formatNumber', () => {
    it('should format number with commas', () => {
      expect(formatNumber(1234567)).toBe('1,234,567')
    })
  })

  describe('formatFileSize', () => {
    it('should format file size', () => {
      expect(formatFileSize(1024)).toBe('1 KB')
      expect(formatFileSize(1048576)).toBe('1 MB')
    })
  })
})
```

### 1.4 组件测试示例

```typescript
// src/components/__tests__/Button.spec.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from '../Button.vue'

describe('Button Component', () => {
  it('renders properly', () => {
    const wrapper = mount(Button, {
      props: { label: 'Click me' }
    })
    expect(wrapper.text()).toContain('Click me')
  })

  it('emits click event', async () => {
    const wrapper = mount(Button)
    await wrapper.trigger('click')
    expect(wrapper.emitted()).toHaveProperty('click')
  })
})
```

---

## 2. 监控体系完善

### 2.1 Sentry集成

```typescript
// src/plugins/sentry.ts
import * as Sentry from '@sentry/vue'
import type { App } from 'vue'
import { Router } from 'vue-router'

export function setupSentry(app: App, router: Router) {
  Sentry.init({
    app,
    dsn: import.meta.env.VITE_SENTRY_DSN,
    integrations: [
      new Sentry.BrowserTracing({
        routingInstrumentation: Sentry.vueRouterInstrumentation(router)
      }),
      new Sentry.Replay()
    ],
    tracesSampleRate: 1.0,
    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,
    environment: import.meta.env.MODE
  })
}
```

### 2.2 性能监控

```typescript
// src/utils/performance.ts
export class PerformanceMonitor {
  private static instance: PerformanceMonitor

  static getInstance() {
    if (!this.instance) {
      this.instance = new PerformanceMonitor()
    }
    return this.instance
  }

  // 监控页面加载性能
  measurePageLoad() {
    if (typeof window === 'undefined') return

    window.addEventListener('load', () => {
      const perfData = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
      
      const metrics = {
        dns: perfData.domainLookupEnd - perfData.domainLookupStart,
        tcp: perfData.connectEnd - perfData.connectStart,
        request: perfData.responseStart - perfData.requestStart,
        response: perfData.responseEnd - perfData.responseStart,
        dom: perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart,
        load: perfData.loadEventEnd - perfData.loadEventStart,
        total: perfData.loadEventEnd - perfData.fetchStart
      }

      console.log('Performance Metrics:', metrics)
      this.reportMetrics(metrics)
    })
  }

  // 监控API请求性能
  measureApiCall(url: string, duration: number) {
    if (duration > 1000) {
      console.warn(`Slow API call: ${url} took ${duration}ms`)
      this.reportSlowApi(url, duration)
    }
  }

  private reportMetrics(metrics: any) {
    // 发送到监控服务
    fetch('/api/monitor/performance', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(metrics)
    })
  }

  private reportSlowApi(url: string, duration: number) {
    fetch('/api/monitor/slow-api', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ url, duration, timestamp: Date.now() })
    })
  }
}
```

---

## 3. 性能优化

### 3.1 路由懒加载

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index.vue')
    },
    {
      path: '/system/user',
      name: 'UserManagement',
      component: () => import('@/views/system/user/index.vue')
    },
    {
      path: '/ai/analysis',
      name: 'AIAnalysis',
      component: () => import('@/views/ai/analysis/index.vue')
    }
  ]
})

export default router
```

### 3.2 组件懒加载

```typescript
// src/components/LazyComponents.ts
import { defineAsyncComponent } from 'vue'

export const LazyChart = defineAsyncComponent(() =>
  import('./Chart.vue')
)

export const LazyTable = defineAsyncComponent(() =>
  import('./Table.vue')
)

export const LazyModal = defineAsyncComponent(() =>
  import('./Modal.vue')
)
```

### 3.3 图片懒加载

```typescript
// src/directives/lazy.ts
import type { Directive } from 'vue'

export const lazyLoad: Directive = {
  mounted(el: HTMLImageElement, binding) {
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        el.src = binding.value
        observer.unobserve(el)
      }
    })
    observer.observe(el)
  }
}
```

---

## 4. AI API集成

### 4.1 OpenAI集成

```typescript
// src/services/ai/openai.ts
import OpenAI from 'openai'

class OpenAIService {
  private client: OpenAI

  constructor() {
    this.client = new OpenAI({
      apiKey: import.meta.env.VITE_OPENAI_API_KEY,
      baseURL: import.meta.env.VITE_OPENAI_BASE_URL,
      dangerouslyAllowBrowser: true
    })
  }

  async analyzeAnomaly(data: any[]) {
    try {
      const response = await this.client.chat.completions.create({
        model: 'gpt-4',
        messages: [
          {
            role: 'system',
            content: '你是一个银行数据安全分析专家，负责检测异常行为和潜在威胁。'
          },
          {
            role: 'user',
            content: `请分析以下数据并识别异常行为：\n${JSON.stringify(data, null, 2)}`
          }
        ],
        temperature: 0.7,
        max_tokens: 2000
      })

      return {
        success: true,
        analysis: response.choices[0].message.content,
        usage: response.usage
      }
    } catch (error) {
      console.error('AI分析失败:', error)
      return {
        success: false,
        error: error.message
      }
    }
  }

  async predictThreat(historicalData: any[]) {
    try {
      const response = await this.client.chat.completions.create({
        model: 'gpt-4',
        messages: [
          {
            role: 'system',
            content: '你是一个网络安全威胁预测专家，基于历史数据预测未来威胁。'
          },
          {
            role: 'user',
            content: `基于以下历史数据，预测未来24小时的威胁趋势：\n${JSON.stringify(historicalData, null, 2)}`
          }
        ],
        temperature: 0.7,
        max_tokens: 1500
      })

      return {
        success: true,
        prediction: response.choices[0].message.content,
        usage: response.usage
      }
    } catch (error) {
      console.error('威胁预测失败:', error)
      return {
        success: false,
        error: error.message
      }
    }
  }
}

export const openAIService = new OpenAIService()
```

### 4.2 AI服务统一接口

```typescript
// src/services/ai/index.ts
import { openAIService } from './openai'

export interface AIAnalysisRequest {
  data: any[]
  type: 'anomaly' | 'threat' | 'behavior'
  threshold?: number
}

export interface AIAnalysisResponse {
  success: boolean
  result?: any
  error?: string
  usage?: any
}

class AIService {
  async analyze(request: AIAnalysisRequest): Promise<AIAnalysisResponse> {
    switch (request.type) {
      case 'anomaly':
        return await openAIService.analyzeAnomaly(request.data)
      case 'threat':
        return await openAIService.predictThreat(request.data)
      default:
        return {
          success: false,
          error: '不支持的分析类型'
        }
    }
  }
}

export const aiService = new AIService()
```

---

## 5. 数据导出功能

### 5.1 Excel导出

```typescript
// src/utils/export/excel.ts
import * as XLSX from 'xlsx'

export interface ExportOptions {
  filename: string
  sheetName?: string
  data: any[]
  headers?: string[]
}

export function exportToExcel(options: ExportOptions) {
  const { filename, sheetName = 'Sheet1', data, headers } = options

  // 创建工作表
  const ws = XLSX.utils.json_to_sheet(data, {
    header: headers
  })

  // 创建工作簿
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, sheetName)

  // 导出文件
  XLSX.writeFile(wb, `${filename}.xlsx`)
}

// 使用示例
export function exportUserList(users: any[]) {
  exportToExcel({
    filename: `用户列表_${new Date().getTime()}`,
    sheetName: '用户数据',
    data: users,
    headers: ['用户名', '姓名', '邮箱', '角色', '状态']
  })
}
```

### 5.2 PDF导出

```typescript
// src/utils/export/pdf.ts
import jsPDF from 'jspdf'
import 'jspdf-autotable'
import { UserOptions } from 'jspdf-autotable'

interface jsPDFWithAutoTable extends jsPDF {
  autoTable: (options: UserOptions) => jsPDF
}

export interface PDFExportOptions {
  filename: string
  title: string
  headers: string[]
  data: any[][]
}

export function exportToPDF(options: PDFExportOptions) {
  const { filename, title, headers, data } = options

  const doc = new jsPDF() as jsPDFWithAutoTable

  // 添加标题
  doc.setFontSize(16)
  doc.text(title, 14, 15)

  // 添加表格
  doc.autoTable({
    head: [headers],
    body: data,
    startY: 25,
    styles: {
      fontSize: 10,
      cellPadding: 3
    },
    headStyles: {
      fillColor: [37, 99, 235],
      textColor: 255
    }
  })

  // 保存文件
  doc.save(`${filename}.pdf`)
}

// 使用示例
export function exportUserListToPDF(users: any[]) {
  const data = users.map(user => [
    user.username,
    user.realName,
    user.email,
    user.role,
    user.status
  ])

  exportToPDF({
    filename: `用户列表_${new Date().getTime()}`,
    title: 'BankShield 用户列表',
    headers: ['用户名', '姓名', '邮箱', '角色', '状态'],
    data
  })
}
```

---

## 6. 批量操作通用组件

```typescript
// src/composables/useBatchOperation.ts
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useBatchOperation<T extends { id: number; selected?: boolean }>(
  dataList: Ref<T[]>
) {
  const selectAll = ref(false)

  const selectedIds = computed(() => {
    return dataList.value.filter(item => item.selected).map(item => item.id)
  })

  const selectedCount = computed(() => selectedIds.value.length)

  const hasSelected = computed(() => selectedCount.value > 0)

  const handleSelectAll = () => {
    dataList.value.forEach(item => {
      item.selected = selectAll.value
    })
  }

  const handleSelect = (item: T) => {
    selectAll.value = dataList.value.every(item => item.selected)
  }

  const handleBatchDelete = async () => {
    if (!hasSelected.value) {
      ElMessage.warning('请先选择要删除的项')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedCount.value} 项吗？`,
        '批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      // 执行删除操作
      dataList.value = dataList.value.filter(item => !item.selected)
      selectAll.value = false
      ElMessage.success(`成功删除 ${selectedCount.value} 项`)
    } catch {
      // 用户取消
    }
  }

  const handleBatchExport = () => {
    if (!hasSelected.value) {
      ElMessage.warning('请先选择要导出的项')
      return
    }

    const selectedData = dataList.value.filter(item => item.selected)
    ElMessage.success(`正在导出 ${selectedCount.value} 项数据`)
    return selectedData
  }

  return {
    selectAll,
    selectedIds,
    selectedCount,
    hasSelected,
    handleSelectAll,
    handleSelect,
    handleBatchDelete,
    handleBatchExport
  }
}
```

---

## 7. 分页通用组件

```vue
<!-- src/components/Pagination.vue -->
<template>
  <div class="flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200">
    <div class="flex items-center">
      <span class="text-sm text-gray-700">
        显示第 {{ startIndex }} 到 {{ endIndex }} 条，共 {{ total }} 条
      </span>
    </div>
    <div class="flex items-center space-x-2">
      <button
        @click="handlePrevious"
        :disabled="currentPage === 1"
        class="px-3 py-1 text-sm border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        上一页
      </button>
      <div class="flex space-x-1">
        <button
          v-for="page in displayPages"
          :key="page"
          @click="handlePageChange(page)"
          :class="[
            'px-3 py-1 text-sm border rounded',
            page === currentPage
              ? 'bg-blue-600 text-white border-blue-600'
              : 'hover:bg-gray-50'
          ]"
        >
          {{ page }}
        </button>
      </div>
      <button
        @click="handleNext"
        :disabled="currentPage === totalPages"
        class="px-3 py-1 text-sm border rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        下一页
      </button>
      <select
        v-model="pageSize"
        @change="handlePageSizeChange"
        class="px-2 py-1 text-sm border rounded"
      >
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
        <option :value="100">100条/页</option>
      </select>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  currentPage: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  (e: 'update:currentPage', value: number): void
  (e: 'update:pageSize', value: number): void
  (e: 'change', page: number): void
}>()

const totalPages = computed(() => Math.ceil(props.total / props.pageSize))
const startIndex = computed(() => (props.currentPage - 1) * props.pageSize + 1)
const endIndex = computed(() => Math.min(props.currentPage * props.pageSize, props.total))

const displayPages = computed(() => {
  const pages: number[] = []
  const maxDisplay = 5
  let start = Math.max(1, props.currentPage - Math.floor(maxDisplay / 2))
  let end = Math.min(totalPages.value, start + maxDisplay - 1)

  if (end - start < maxDisplay - 1) {
    start = Math.max(1, end - maxDisplay + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const handlePrevious = () => {
  if (props.currentPage > 1) {
    handlePageChange(props.currentPage - 1)
  }
}

const handleNext = () => {
  if (props.currentPage < totalPages.value) {
    handlePageChange(props.currentPage + 1)
  }
}

const handlePageChange = (page: number) => {
  emit('update:currentPage', page)
  emit('change', page)
}

const handlePageSizeChange = (e: Event) => {
  const newSize = parseInt((e.target as HTMLSelectElement).value)
  emit('update:pageSize', newSize)
  emit('update:currentPage', 1)
  emit('change', 1)
}
</script>
```

---

## 8. 使用示例

### 8.1 在页面中使用批量操作

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useBatchOperation } from '@/composables/useBatchOperation'

const dataList = ref([
  { id: 1, name: 'Item 1', selected: false },
  { id: 2, name: 'Item 2', selected: false }
])

const {
  selectAll,
  selectedCount,
  hasSelected,
  handleSelectAll,
  handleSelect,
  handleBatchDelete,
  handleBatchExport
} = useBatchOperation(dataList)
</script>
```

### 8.2 在页面中使用分页

```vue
<script setup lang="ts">
import { ref } from 'vue'
import Pagination from '@/components/Pagination.vue'

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

const handlePageChange = (page: number) => {
  console.log('Page changed to:', page)
  // 加载数据
}
</script>

<template>
  <Pagination
    v-model:current-page="currentPage"
    v-model:page-size="pageSize"
    :total="total"
    @change="handlePageChange"
  />
</template>
```

---

## 总结

本实施指南涵盖了BankShield系统的核心功能实现，包括：

1. ✅ 测试框架搭建和测试用例编写
2. ✅ 监控体系集成（Sentry、性能监控）
3. ✅ 性能优化（懒加载、代码分割）
4. ✅ AI API集成（OpenAI）
5. ✅ 数据导出功能（Excel、PDF）
6. ✅ 批量操作通用组件
7. ✅ 分页通用组件

按照本指南实施，可以快速完成系统的质量提升和功能完善。
