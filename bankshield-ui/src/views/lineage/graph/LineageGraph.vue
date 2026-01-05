<template>
  <div class="lineage-graph-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">数据血缘关系图</span>
          <div class="actions">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询血缘</el-button>
            <el-button :icon="Refresh" @click="handleRefresh">刷新</el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="数据资产ID">
            <el-input v-model="searchForm.assetId" placeholder="请输入资产ID" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="血缘深度">
            <el-select v-model="searchForm.depth" placeholder="选择深度" style="width: 120px">
              <el-option label="1层" :value="1" />
              <el-option label="2层" :value="2" />
              <el-option label="3层" :value="3" />
              <el-option label="4层" :value="4" />
              <el-option label="5层" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="方向">
            <el-select v-model="searchForm.direction" placeholder="选择方向" style="width: 120px">
              <el-option label="上游" value="upstream" />
              <el-option label="下游" value="downstream" />
              <el-option label="全部" value="both" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div v-loading="loading" class="graph-container">
        <div v-if="!graphData" class="empty-state">
          <el-empty description="请输入资产ID查询血缘关系图" />
        </div>
        <div v-else id="lineage-graph" class="graph-canvas"></div>
      </div>

      <div v-if="graphData" class="graph-stats">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-statistic title="节点总数" :value="stats.nodeCount">
              <template #prefix>
                <el-icon><Connection /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="边数量" :value="stats.edgeCount">
              <template #prefix>
                <el-icon><Share /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="最大深度" :value="stats.maxDepth">
              <template #prefix>
                <el-icon><DataLine /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="复杂度" :value="stats.complexity">
              <template #prefix>
                <el-icon><TrendCharts /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Connection, Share, DataLine, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

interface SearchForm {
  assetId: string
  depth: number
  direction: string
}

interface GraphStats {
  nodeCount: number
  edgeCount: number
  maxDepth: number
  complexity: number
}

const loading = ref(false)
const searchForm = reactive<SearchForm>({
  assetId: '',
  depth: 3,
  direction: 'both'
})

const graphData = ref<any>(null)
const stats = reactive<GraphStats>({
  nodeCount: 0,
  edgeCount: 0,
  maxDepth: 0,
  complexity: 0
})

let chartInstance: echarts.ECharts | null = null

const handleSearch = async () => {
  if (!searchForm.assetId) {
    ElMessage.warning('请输入数据资产ID')
    return
  }

  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    graphData.value = {
      nodes: [
        { id: '1', name: '源表A', category: 'table', symbolSize: 50 },
        { id: '2', name: '源表B', category: 'table', symbolSize: 50 },
        { id: '3', name: '中间表C', category: 'table', symbolSize: 60 },
        { id: '4', name: '目标表D', category: 'table', symbolSize: 70 },
        { id: '5', name: 'ETL任务1', category: 'task', symbolSize: 40 },
        { id: '6', name: 'ETL任务2', category: 'task', symbolSize: 40 }
      ],
      edges: [
        { source: '1', target: '5' },
        { source: '2', target: '5' },
        { source: '5', target: '3' },
        { source: '3', target: '6' },
        { source: '6', target: '4' }
      ]
    }

    stats.nodeCount = graphData.value.nodes.length
    stats.edgeCount = graphData.value.edges.length
    stats.maxDepth = searchForm.depth
    stats.complexity = Math.round(stats.edgeCount / stats.nodeCount * 100) / 100

    renderGraph()
    ElMessage.success('血缘关系图加载成功')
  } catch (error) {
    ElMessage.error('加载血缘关系图失败')
  } finally {
    loading.value = false
  }
}

const renderGraph = () => {
  const chartDom = document.getElementById('lineage-graph')
  if (!chartDom) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartDom)

  const option: EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          return `${params.data.name}<br/>类型: ${params.data.category}`
        }
        return `${params.data.source} → ${params.data.target}`
      }
    },
    legend: {
      data: ['表', '任务'],
      top: 10
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: graphData.value.nodes.map((node: any) => ({
          ...node,
          itemStyle: {
            color: node.category === 'table' ? '#409EFF' : '#67C23A'
          }
        })),
        links: graphData.value.edges,
        categories: [
          { name: '表' },
          { name: '任务' }
        ],
        roam: true,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        labelLayout: {
          hideOverlap: true
        },
        scaleLimit: {
          min: 0.4,
          max: 2
        },
        lineStyle: {
          color: 'source',
          curveness: 0.3
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 10
          }
        },
        force: {
          repulsion: 1000,
          edgeLength: 150
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const handleRefresh = () => {
  if (searchForm.assetId) {
    handleSearch()
  }
}

const handleExport = () => {
  if (!chartInstance) {
    ElMessage.warning('请先查询血缘关系图')
    return
  }
  
  const url = chartInstance.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff'
  })
  
  const link = document.createElement('a')
  link.href = url
  link.download = `lineage-graph-${searchForm.assetId}.png`
  link.click()
  
  ElMessage.success('导出成功')
}

onMounted(() => {
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped lang="less">
.lineage-graph-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: 600;
    }

    .actions {
      display: flex;
      gap: 8px;
    }
  }

  .search-bar {
    margin-bottom: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
  }

  .graph-container {
    min-height: 600px;
    position: relative;

    .empty-state {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 600px;
    }

    .graph-canvas {
      width: 100%;
      height: 600px;
    }
  }

  .graph-stats {
    margin-top: 20px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 4px;
  }
}
</style>
