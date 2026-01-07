<template>
  <div class="data-map-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><MapLocation /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalMaps || 0 }}</div>
              <div class="stat-label">地图总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon active">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.activeMaps || 0 }}</div>
              <div class="stat-label">活跃地图</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon nodes">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalNodes || 0 }}</div>
              <div class="stat-label">节点总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon edges">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalEdges || 0 }}</div>
              <div class="stat-label">关系总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作区域 -->
    <el-card class="action-card">
      <el-form inline>
        <el-form-item label="地图类型">
          <el-select v-model="filterType" placeholder="全部" clearable style="width: 140px" @change="filterMaps">
            <el-option label="全局地图" value="GLOBAL" />
            <el-option label="业务域地图" value="BUSINESS_DOMAIN" />
            <el-option label="数据源地图" value="DATA_SOURCE" />
            <el-option label="自定义地图" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerateGlobal">
            <el-icon><MapLocation /></el-icon>
            生成全局地图
          </el-button>
          <el-button type="success" @click="showBusinessDomainDialog">
            <el-icon><OfficeBuilding /></el-icon>
            业务域地图
          </el-button>
          <el-button type="warning" @click="showDataSourceDialog">
            <el-icon><Coin /></el-icon>
            数据源地图
          </el-button>
          <el-button @click="showCustomDialog">
            <el-icon><Plus /></el-icon>
            自定义地图
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 地图列表 -->
    <el-row :gutter="16" class="map-list">
      <el-col :span="8" v-for="map in filteredMaps" :key="map.id">
        <el-card shadow="hover" class="map-card" :class="{ 'is-default': map.isDefault }">
          <template #header>
            <div class="map-header">
              <span class="map-name">{{ map.mapName }}</span>
              <div class="map-tags">
                <el-tag size="small" :type="getMapTypeTag(map.mapType)">{{ getMapTypeLabel(map.mapType) }}</el-tag>
                <el-tag v-if="map.isDefault" size="small" type="success">默认</el-tag>
              </div>
            </div>
          </template>
          <div class="map-body">
            <div class="map-preview" @click="handleViewMap(map)">
              <div ref="previewCharts" class="preview-chart" :data-map-id="map.id"></div>
              <div class="preview-overlay">
                <el-icon><ZoomIn /></el-icon>
                <span>点击查看</span>
              </div>
            </div>
            <div class="map-stats">
              <div class="stat-item">
                <el-icon><Grid /></el-icon>
                <span>{{ map.nodeCount || 0 }} 节点</span>
              </div>
              <div class="stat-item">
                <el-icon><Connection /></el-icon>
                <span>{{ map.relationshipCount || 0 }} 关系</span>
              </div>
            </div>
            <div class="map-info">
              <p><el-icon><Calendar /></el-icon> {{ formatTime(map.createTime) }}</p>
            </div>
          </div>
          <div class="map-actions">
            <el-button type="primary" size="small" @click="handleViewMap(map)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" @click="handleSetDefault(map)" v-if="!map.isDefault">
              <el-icon><Star /></el-icon>
              设为默认
            </el-button>
            <el-button type="info" size="small" @click="handleEditMap(map)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteMap(map)" :disabled="map.isDefault">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="filteredMaps.length === 0" description="暂无数据地图，请先生成地图" />

    <!-- 地图可视化对话框 -->
    <el-dialog v-model="viewDialogVisible" :title="currentMap?.mapName || '数据地图'" width="90%" fullscreen>
      <div class="visualization-container">
        <div class="viz-toolbar">
          <el-radio-group v-model="layoutType" size="small" @change="changeLayout">
            <el-radio-button label="force">力导向</el-radio-button>
            <el-radio-button label="circular">环形</el-radio-button>
            <el-radio-button label="hierarchical">层次</el-radio-button>
          </el-radio-group>
          <el-button-group>
            <el-button size="small" @click="zoomIn"><el-icon><ZoomIn /></el-icon></el-button>
            <el-button size="small" @click="zoomOut"><el-icon><ZoomOut /></el-icon></el-button>
            <el-button size="small" @click="resetZoom"><el-icon><Refresh /></el-icon></el-button>
          </el-button-group>
          <el-button size="small" @click="exportImage">
            <el-icon><Download /></el-icon>
            导出图片
          </el-button>
        </div>
        <div ref="mainChart" class="main-chart"></div>
        <div class="viz-legend">
          <div class="legend-item" v-for="cat in categories" :key="cat.name">
            <span class="legend-color" :style="{ background: cat.color }"></span>
            <span>{{ cat.label }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 业务域地图对话框 -->
    <el-dialog v-model="businessDomainDialogVisible" title="生成业务域地图" width="500px">
      <el-form :model="businessDomainForm" label-width="100px">
        <el-form-item label="业务域" required>
          <el-select v-model="businessDomainForm.domain" placeholder="请选择业务域" style="width: 100%">
            <el-option label="客户管理" value="customer" />
            <el-option label="账户管理" value="account" />
            <el-option label="交易管理" value="transaction" />
            <el-option label="产品管理" value="product" />
            <el-option label="风控管理" value="risk" />
            <el-option label="报表分析" value="report" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="businessDomainDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateBusinessDomain" :loading="generating">生成</el-button>
      </template>
    </el-dialog>

    <!-- 数据源地图对话框 -->
    <el-dialog v-model="dataSourceDialogVisible" title="生成数据源地图" width="500px">
      <el-form :model="dataSourceForm" label-width="100px">
        <el-form-item label="数据源" required>
          <el-select v-model="dataSourceForm.dataSourceId" placeholder="请选择数据源" style="width: 100%">
            <el-option v-for="ds in dataSources" :key="ds.id" :label="ds.name" :value="ds.id">
              <span>{{ ds.name }}</span>
              <span style="color: #999; margin-left: 10px">{{ ds.type }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataSourceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateDataSource" :loading="generating">生成</el-button>
      </template>
    </el-dialog>

    <!-- 自定义地图对话框 -->
    <el-dialog v-model="customDialogVisible" title="创建自定义地图" width="600px">
      <el-form :model="customForm" label-width="100px">
        <el-form-item label="地图名称" required>
          <el-input v-model="customForm.mapName" placeholder="请输入地图名称" />
        </el-form-item>
        <el-form-item label="包含表">
          <el-select v-model="customForm.includedTables" multiple placeholder="选择要包含的表" style="width: 100%">
            <el-option v-for="table in availableTables" :key="table" :label="table" :value="table" />
          </el-select>
        </el-form-item>
        <el-form-item label="布局类型">
          <el-select v-model="customForm.layoutType" placeholder="选择布局类型" style="width: 100%">
            <el-option label="力导向布局" value="force" />
            <el-option label="层次布局" value="hierarchical" />
            <el-option label="环形布局" value="circular" />
            <el-option label="网格布局" value="grid" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="customDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateCustom" :loading="generating">生成</el-button>
      </template>
    </el-dialog>

    <!-- 编辑地图对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑地图" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="地图名称" required>
          <el-input v-model="editForm.mapName" placeholder="请输入地图名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateMap" :loading="updating">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  MapLocation, CircleCheck, Grid, Connection, OfficeBuilding, Coin, Plus,
  ZoomIn, ZoomOut, Refresh, View, Star, Edit, Delete, Calendar, Download
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 统计数据
const statistics = ref({
  totalMaps: 0,
  activeMaps: 0,
  totalNodes: 0,
  totalEdges: 0
})

// 地图列表
const dataMaps = ref<any[]>([])
const filteredMaps = ref<any[]>([])
const filterType = ref('')
const loading = ref(false)
const generating = ref(false)
const updating = ref(false)

// 数据源列表
const dataSources = ref([
  { id: 1, name: '生产数据库', type: 'MySQL' },
  { id: 2, name: '分析数据库', type: 'PostgreSQL' },
  { id: 3, name: '数据仓库', type: 'Hive' }
])

// 可用表列表
const availableTables = ref([
  'user_info', 'account', 'transaction', 'product', 'order',
  'customer', 'payment', 'audit_log', 'config', 'report'
])

// 可视化
const viewDialogVisible = ref(false)
const currentMap = ref<any>(null)
const mainChart = ref<HTMLElement | null>(null)
const layoutType = ref('force')
let chartInstance: echarts.ECharts | null = null

// 对话框
const businessDomainDialogVisible = ref(false)
const businessDomainForm = reactive({ domain: '' })

const dataSourceDialogVisible = ref(false)
const dataSourceForm = reactive({ dataSourceId: null as number | null })

const customDialogVisible = ref(false)
const customForm = reactive({
  mapName: '',
  includedTables: [] as string[],
  layoutType: 'force'
})

const editDialogVisible = ref(false)
const editForm = reactive({ id: 0, mapName: '' })

// 图例类别
const categories = [
  { name: 'customer', label: '客户', color: '#5470c6' },
  { name: 'account', label: '账户', color: '#91cc75' },
  { name: 'transaction', label: '交易', color: '#fac858' },
  { name: 'product', label: '产品', color: '#ee6666' },
  { name: 'log', label: '日志', color: '#73c0de' },
  { name: 'other', label: '其他', color: '#9a60b4' }
]

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 模拟数据
    statistics.value = {
      totalMaps: dataMaps.value.length,
      activeMaps: dataMaps.value.filter(m => m.status === 'ACTIVE').length,
      totalNodes: dataMaps.value.reduce((sum, m) => sum + (m.nodeCount || 0), 0),
      totalEdges: dataMaps.value.reduce((sum, m) => sum + (m.relationshipCount || 0), 0)
    }
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

// 获取地图列表
const fetchDataMaps = async () => {
  loading.value = true
  try {
    // 模拟数据
    dataMaps.value = [
      {
        id: 1, mapName: '全局数据地图', mapType: 'GLOBAL',
        nodeCount: 156, relationshipCount: 234, isDefault: true,
        status: 'ACTIVE', createTime: '2025-01-03 09:00:00'
      },
      {
        id: 2, mapName: '客户域数据地图', mapType: 'BUSINESS_DOMAIN',
        nodeCount: 45, relationshipCount: 67, isDefault: false,
        status: 'ACTIVE', createTime: '2025-01-04 10:00:00'
      },
      {
        id: 3, mapName: '生产库数据地图', mapType: 'DATA_SOURCE',
        nodeCount: 89, relationshipCount: 123, isDefault: false,
        status: 'ACTIVE', createTime: '2025-01-05 11:00:00'
      }
    ]
    filteredMaps.value = dataMaps.value
    fetchStatistics()
  } catch (error) {
    console.error('获取地图列表失败', error)
  } finally {
    loading.value = false
  }
}

// 过滤地图
const filterMaps = () => {
  if (filterType.value) {
    filteredMaps.value = dataMaps.value.filter(m => m.mapType === filterType.value)
  } else {
    filteredMaps.value = dataMaps.value
  }
}

// 生成全局地图
const handleGenerateGlobal = async () => {
  try {
    generating.value = true
    ElMessage.success('全局数据地图生成成功')
    fetchDataMaps()
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    generating.value = false
  }
}

// 显示业务域对话框
const showBusinessDomainDialog = () => {
  businessDomainForm.domain = ''
  businessDomainDialogVisible.value = true
}

// 生成业务域地图
const handleGenerateBusinessDomain = async () => {
  if (!businessDomainForm.domain) {
    ElMessage.warning('请选择业务域')
    return
  }
  try {
    generating.value = true
    ElMessage.success('业务域数据地图生成成功')
    businessDomainDialogVisible.value = false
    fetchDataMaps()
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    generating.value = false
  }
}

// 显示数据源对话框
const showDataSourceDialog = () => {
  dataSourceForm.dataSourceId = null
  dataSourceDialogVisible.value = true
}

// 生成数据源地图
const handleGenerateDataSource = async () => {
  if (!dataSourceForm.dataSourceId) {
    ElMessage.warning('请选择数据源')
    return
  }
  try {
    generating.value = true
    ElMessage.success('数据源地图生成成功')
    dataSourceDialogVisible.value = false
    fetchDataMaps()
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    generating.value = false
  }
}

// 显示自定义对话框
const showCustomDialog = () => {
  customForm.mapName = `自定义地图_${Date.now()}`
  customForm.includedTables = []
  customForm.layoutType = 'force'
  customDialogVisible.value = true
}

// 生成自定义地图
const handleGenerateCustom = async () => {
  if (!customForm.mapName) {
    ElMessage.warning('请输入地图名称')
    return
  }
  try {
    generating.value = true
    ElMessage.success('自定义数据地图生成成功')
    customDialogVisible.value = false
    fetchDataMaps()
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    generating.value = false
  }
}

// 查看地图
const handleViewMap = (map: any) => {
  currentMap.value = map
  viewDialogVisible.value = true
  nextTick(() => {
    initMainChart()
  })
}

// 初始化主图表
const initMainChart = () => {
  if (!mainChart.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(mainChart.value)
  
  // 生成模拟数据
  const nodes = generateNodes(currentMap.value?.nodeCount || 50)
  const links = generateLinks(nodes, currentMap.value?.relationshipCount || 80)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          return `<b>${params.name}</b><br/>类型: ${params.data.category}<br/>连接数: ${params.data.degree || 0}`
        }
        return `${params.data.source} → ${params.data.target}`
      }
    },
    legend: {
      data: categories.map(c => c.label),
      orient: 'vertical',
      right: 10,
      top: 20
    },
    series: [{
      type: 'graph',
      layout: layoutType.value === 'hierarchical' ? 'none' : layoutType.value,
      roam: true,
      draggable: true,
      symbolSize: 30,
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [4, 8],
      categories: categories.map(c => ({ name: c.label, itemStyle: { color: c.color } })),
      data: nodes,
      links: links,
      lineStyle: {
        color: 'source',
        curveness: 0.3,
        opacity: 0.6
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3 }
      },
      force: {
        repulsion: 200,
        edgeLength: [50, 150],
        gravity: 0.1
      }
    }]
  }
  
  chartInstance.setOption(option)
}

// 生成节点
const generateNodes = (count: number) => {
  const nodes = []
  for (let i = 0; i < count; i++) {
    const catIndex = i % categories.length
    nodes.push({
      id: `node_${i}`,
      name: `${categories[catIndex].name}_table_${i}`,
      category: catIndex,
      symbolSize: 20 + Math.random() * 20,
      degree: Math.floor(Math.random() * 10),
      x: layoutType.value === 'hierarchical' ? (i % 10) * 100 + 50 : undefined,
      y: layoutType.value === 'hierarchical' ? Math.floor(i / 10) * 80 + 50 : undefined
    })
  }
  return nodes
}

// 生成连接
const generateLinks = (nodes: any[], count: number) => {
  const links = []
  for (let i = 0; i < count; i++) {
    const sourceIdx = Math.floor(Math.random() * nodes.length)
    let targetIdx = Math.floor(Math.random() * nodes.length)
    while (targetIdx === sourceIdx) {
      targetIdx = Math.floor(Math.random() * nodes.length)
    }
    links.push({
      source: nodes[sourceIdx].id,
      target: nodes[targetIdx].id
    })
  }
  return links
}

// 切换布局
const changeLayout = () => {
  initMainChart()
}

// 缩放操作
const zoomIn = () => {
  if (chartInstance) {
    const option = chartInstance.getOption() as any
    const zoom = (option.series[0].zoom || 1) * 1.2
    chartInstance.setOption({ series: [{ zoom }] })
  }
}

const zoomOut = () => {
  if (chartInstance) {
    const option = chartInstance.getOption() as any
    const zoom = (option.series[0].zoom || 1) / 1.2
    chartInstance.setOption({ series: [{ zoom }] })
  }
}

const resetZoom = () => {
  initMainChart()
}

// 导出图片
const exportImage = () => {
  if (chartInstance) {
    const url = chartInstance.getDataURL({ type: 'png', pixelRatio: 2 })
    const link = document.createElement('a')
    link.href = url
    link.download = `${currentMap.value?.mapName || 'data-map'}.png`
    link.click()
    ElMessage.success('图片导出成功')
  }
}

// 设为默认
const handleSetDefault = async (map: any) => {
  try {
    dataMaps.value.forEach(m => m.isDefault = false)
    map.isDefault = true
    ElMessage.success('已设为默认地图')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 编辑地图
const handleEditMap = (map: any) => {
  editForm.id = map.id
  editForm.mapName = map.mapName
  editDialogVisible.value = true
}

// 更新地图
const handleUpdateMap = async () => {
  try {
    updating.value = true
    const map = dataMaps.value.find(m => m.id === editForm.id)
    if (map) {
      map.mapName = editForm.mapName
    }
    ElMessage.success('更新成功')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

// 删除地图
const handleDeleteMap = async (map: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据地图吗？', '提示', {
      type: 'warning'
    })
    const index = dataMaps.value.findIndex(m => m.id === map.id)
    if (index > -1) {
      dataMaps.value.splice(index, 1)
      filterMaps()
      fetchStatistics()
    }
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 辅助方法
const getMapTypeTag = (type: string) => {
  const map: Record<string, string> = {
    GLOBAL: 'danger',
    BUSINESS_DOMAIN: 'warning',
    DATA_SOURCE: 'success',
    CUSTOM: 'info'
  }
  return map[type] || 'info'
}

const getMapTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    GLOBAL: '全局',
    BUSINESS_DOMAIN: '业务域',
    DATA_SOURCE: '数据源',
    CUSTOM: '自定义'
  }
  return map[type] || type
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.substring(0, 16)
}

onMounted(() => {
  fetchDataMaps()
})
</script>

<style scoped lang="less">
.data-map-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;

    .el-icon {
      font-size: 24px;
      color: #fff;
    }

    &.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.active { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
    &.nodes { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
    &.edges { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  }

  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
    .stat-label {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.action-card {
  margin-bottom: 16px;
}

.map-list {
  .map-card {
    margin-bottom: 16px;
    transition: all 0.3s;

    &.is-default {
      border-color: #67c23a;
    }

    &:hover {
      transform: translateY(-4px);
    }

    .map-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .map-name {
        font-weight: 600;
        font-size: 14px;
      }

      .map-tags {
        display: flex;
        gap: 4px;
      }
    }

    .map-body {
      .map-preview {
        height: 160px;
        background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
        border-radius: 8px;
        margin-bottom: 12px;
        position: relative;
        cursor: pointer;
        overflow: hidden;

        .preview-chart {
          width: 100%;
          height: 100%;
        }

        .preview-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #fff;
          opacity: 0;
          transition: opacity 0.3s;

          .el-icon {
            font-size: 32px;
            margin-bottom: 8px;
          }
        }

        &:hover .preview-overlay {
          opacity: 1;
        }
      }

      .map-stats {
        display: flex;
        gap: 16px;
        margin-bottom: 8px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #606266;
        }
      }

      .map-info {
        font-size: 12px;
        color: #909399;

        p {
          display: flex;
          align-items: center;
          gap: 4px;
          margin: 0;
        }
      }
    }

    .map-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #ebeef5;
    }
  }
}

.visualization-container {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;

  .viz-toolbar {
    display: flex;
    gap: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 12px;
  }

  .main-chart {
    flex: 1;
    min-height: 500px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
  }

  .viz-legend {
    display: flex;
    gap: 16px;
    padding: 12px;
    justify-content: center;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;

      .legend-color {
        width: 12px;
        height: 12px;
        border-radius: 2px;
      }
    }
  }
}
</style>
