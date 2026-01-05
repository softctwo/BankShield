<template>
  <div class="lineage-enhanced-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Connection /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalLineages || 0 }}</div>
              <div class="stat-label">血缘关系总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><Finished /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.discoveryTasks || 0 }}</div>
              <div class="stat-label">发现任务数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.impactAnalyses || 0 }}</div>
              <div class="stat-label">影响分析数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C"><DataAnalysis /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.dataMaps || 0 }}</div>
              <div class="stat-label">数据地图数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 功能区域 -->
    <el-tabs v-model="activeTab" class="lineage-tabs">
      <!-- 血缘发现 -->
      <el-tab-pane label="血缘发现" name="discovery">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>血缘发现任务</span>
              <el-button type="primary" @click="showCreateDiscoveryDialog">
                <el-icon><Plus /></el-icon>
                创建发现任务
              </el-button>
            </div>
          </template>

          <el-table :data="discoveryTasks" stripe>
            <el-table-column prop="taskName" label="任务名称" width="200" />
            <el-table-column prop="discoveryStrategy" label="发现策略" width="150">
              <template #default="{ row }">
                <el-tag>{{ row.discoveryStrategy }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="taskStatus" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.taskStatus)">
                  {{ row.taskStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度" width="150">
              <template #default="{ row }">
                <el-progress :percentage="row.progress || 0" />
              </template>
            </el-table-column>
            <el-table-column prop="discoveredCount" label="发现数量" width="120" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="viewDiscoveryDetail(row)">详情</el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  v-if="row.taskStatus === 'RUNNING'"
                  @click="cancelDiscoveryTask(row.id)">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="discoveryPage"
            v-model:page-size="discoveryPageSize"
            :total="discoveryTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadDiscoveryTasks"
          />
        </el-card>
      </el-tab-pane>

      <!-- 影响分析 -->
      <el-tab-pane label="影响分析" name="impact">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>影响分析任务</span>
              <el-button type="primary" @click="showCreateImpactDialog">
                <el-icon><Plus /></el-icon>
                创建影响分析
              </el-button>
            </div>
          </template>

          <el-table :data="impactAnalyses" stripe>
            <el-table-column prop="analysisName" label="分析名称" width="200" />
            <el-table-column prop="analysisType" label="分析类型" width="150">
              <template #default="{ row }">
                <el-tag>{{ row.analysisType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="impactObjectName" label="影响对象" width="200" />
            <el-table-column prop="analysisStatus" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.analysisStatus)">
                  {{ row.analysisStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="upstreamCount" label="上游影响" width="100" />
            <el-table-column prop="downstreamCount" label="下游影响" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="viewImpactDetail(row)">查看结果</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="impactPage"
            v-model:page-size="impactPageSize"
            :total="impactTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadImpactAnalyses"
          />
        </el-card>
      </el-tab-pane>

      <!-- 数据地图 -->
      <el-tab-pane label="数据地图" name="datamap">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据地图</span>
              <el-space>
                <el-button type="primary" @click="generateGlobalMap">
                  <el-icon><MapLocation /></el-icon>
                  生成全局地图
                </el-button>
                <el-button type="success" @click="showCreateMapDialog">
                  <el-icon><Plus /></el-icon>
                  自定义地图
                </el-button>
              </el-space>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :span="8" v-for="map in dataMaps" :key="map.id">
              <el-card shadow="hover" class="data-map-card">
                <template #header>
                  <div class="map-header">
                    <span>{{ map.mapName }}</span>
                    <el-tag v-if="map.isDefault" type="success" size="small">默认</el-tag>
                  </div>
                </template>
                <div class="map-info">
                  <p><strong>类型：</strong>{{ map.mapType }}</p>
                  <p><strong>节点数：</strong>{{ map.nodeCount }}</p>
                  <p><strong>关系数：</strong>{{ map.edgeCount }}</p>
                  <p><strong>创建时间：</strong>{{ map.createTime }}</p>
                </div>
                <div class="map-actions">
                  <el-button size="small" type="primary" @click="viewDataMap(map)">
                    查看地图
                  </el-button>
                  <el-button size="small" @click="setDefaultMap(map.id)" v-if="!map.isDefault">
                    设为默认
                  </el-button>
                  <el-button size="small" type="danger" @click="deleteDataMap(map.id)">
                    删除
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>

      <!-- 可视化 -->
      <el-tab-pane label="血缘可视化" name="visualization">
        <el-card>
          <el-form :inline="true" :model="vizForm">
            <el-form-item label="表名">
              <el-input v-model="vizForm.tableName" placeholder="请输入表名" />
            </el-form-item>
            <el-form-item label="字段名">
              <el-input v-model="vizForm.columnName" placeholder="可选" />
            </el-form-item>
            <el-form-item label="深度">
              <el-input-number v-model="vizForm.depth" :min="1" :max="5" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="generateLineageGraph">
                <el-icon><View /></el-icon>
                生成血缘图
              </el-button>
            </el-form-item>
          </el-form>

          <div id="lineage-graph" style="width: 100%; height: 600px; border: 1px solid #ddd; margin-top: 20px;"></div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建发现任务对话框 -->
    <el-dialog v-model="createDiscoveryVisible" title="创建血缘发现任务" width="600px">
      <el-form :model="discoveryForm" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="discoveryForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="数据源">
          <el-select v-model="discoveryForm.dataSourceId" placeholder="请选择数据源">
            <el-option label="MySQL主库" :value="1" />
            <el-option label="PostgreSQL" :value="2" />
            <el-option label="Oracle" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="发现策略">
          <el-select v-model="discoveryForm.discoveryStrategy" placeholder="请选择发现策略">
            <el-option label="SQL解析" value="SQL_PARSE" />
            <el-option label="元数据爬取" value="METADATA_CRAWL" />
            <el-option label="日志分析" value="LOG_ANALYSIS" />
            <el-option label="机器学习推断" value="ML_INFERENCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDiscoveryVisible = false">取消</el-button>
        <el-button type="primary" @click="createDiscoveryTask">确定</el-button>
      </template>
    </el-dialog>

    <!-- 创建影响分析对话框 -->
    <el-dialog v-model="createImpactVisible" title="创建影响分析" width="600px">
      <el-form :model="impactForm" label-width="120px">
        <el-form-item label="分析名称">
          <el-input v-model="impactForm.analysisName" placeholder="请输入分析名称" />
        </el-form-item>
        <el-form-item label="分析类型">
          <el-select v-model="impactForm.analysisType" placeholder="请选择分析类型">
            <el-option label="表级影响" value="TABLE_LEVEL" />
            <el-option label="字段级影响" value="COLUMN_LEVEL" />
            <el-option label="全链路影响" value="FULL_CHAIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响对象类型">
          <el-select v-model="impactForm.impactObjectType" placeholder="请选择对象类型">
            <el-option label="表" value="TABLE" />
            <el-option label="字段" value="COLUMN" />
            <el-option label="视图" value="VIEW" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响对象名称">
          <el-input v-model="impactForm.impactObjectName" placeholder="请输入对象名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createImpactVisible = false">取消</el-button>
        <el-button type="primary" @click="createImpactAnalysis">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Connection, Finished, Warning, DataAnalysis, Plus, MapLocation, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const activeTab = ref('discovery')

const statistics = reactive({
  totalLineages: 0,
  discoveryTasks: 0,
  impactAnalyses: 0,
  dataMaps: 0
})

const discoveryTasks = ref([])
const discoveryPage = ref(1)
const discoveryPageSize = ref(10)
const discoveryTotal = ref(0)

const impactAnalyses = ref([])
const impactPage = ref(1)
const impactPageSize = ref(10)
const impactTotal = ref(0)

const dataMaps = ref([])

const createDiscoveryVisible = ref(false)
const discoveryForm = reactive({
  taskName: '',
  dataSourceId: null,
  discoveryStrategy: ''
})

const createImpactVisible = ref(false)
const impactForm = reactive({
  analysisName: '',
  analysisType: '',
  impactObjectType: '',
  impactObjectName: ''
})

const vizForm = reactive({
  tableName: '',
  columnName: '',
  depth: 2
})

let lineageChart: any = null

onMounted(() => {
  loadStatistics()
  loadDiscoveryTasks()
  loadImpactAnalyses()
  loadDataMaps()
})

const loadStatistics = async () => {
  try {
    statistics.totalLineages = 156
    statistics.discoveryTasks = 23
    statistics.impactAnalyses = 45
    statistics.dataMaps = 8
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadDiscoveryTasks = async () => {
  try {
    discoveryTasks.value = [
      {
        id: 1,
        taskName: '全量血缘发现',
        discoveryStrategy: 'SQL_PARSE',
        taskStatus: 'COMPLETED',
        progress: 100,
        discoveredCount: 156,
        createTime: '2025-01-04 10:00:00'
      },
      {
        id: 2,
        taskName: '元数据爬取任务',
        discoveryStrategy: 'METADATA_CRAWL',
        taskStatus: 'RUNNING',
        progress: 65,
        discoveredCount: 89,
        createTime: '2025-01-04 14:30:00'
      }
    ]
    discoveryTotal.value = 2
  } catch (error) {
    console.error('加载发现任务失败:', error)
  }
}

const loadImpactAnalyses = async () => {
  try {
    impactAnalyses.value = [
      {
        id: 1,
        analysisName: '用户表影响分析',
        analysisType: 'TABLE_LEVEL',
        impactObjectName: 'user_info',
        analysisStatus: 'COMPLETED',
        upstreamCount: 5,
        downstreamCount: 12,
        createTime: '2025-01-04 11:00:00'
      }
    ]
    impactTotal.value = 1
  } catch (error) {
    console.error('加载影响分析失败:', error)
  }
}

const loadDataMaps = async () => {
  try {
    dataMaps.value = [
      {
        id: 1,
        mapName: '全局数据地图',
        mapType: 'GLOBAL',
        nodeCount: 156,
        edgeCount: 234,
        isDefault: true,
        createTime: '2025-01-03 09:00:00'
      },
      {
        id: 2,
        mapName: '用户域数据地图',
        mapType: 'BUSINESS_DOMAIN',
        nodeCount: 45,
        edgeCount: 67,
        isDefault: false,
        createTime: '2025-01-04 10:00:00'
      }
    ]
  } catch (error) {
    console.error('加载数据地图失败:', error)
  }
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'COMPLETED': 'success',
    'RUNNING': 'warning',
    'FAILED': 'danger',
    'PENDING': 'info'
  }
  return typeMap[status] || 'info'
}

const showCreateDiscoveryDialog = () => {
  createDiscoveryVisible.value = true
}

const createDiscoveryTask = async () => {
  try {
    ElMessage.success('血缘发现任务创建成功')
    createDiscoveryVisible.value = false
    loadDiscoveryTasks()
  } catch (error) {
    ElMessage.error('创建任务失败')
  }
}

const viewDiscoveryDetail = (row: any) => {
  ElMessage.info(`查看任务详情: ${row.taskName}`)
}

const cancelDiscoveryTask = async (taskId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该任务吗？', '提示', {
      type: 'warning'
    })
    ElMessage.success('任务已取消')
    loadDiscoveryTasks()
  } catch {
    // 用户取消操作
  }
}

const showCreateImpactDialog = () => {
  createImpactVisible.value = true
}

const createImpactAnalysis = async () => {
  try {
    ElMessage.success('影响分析任务创建成功')
    createImpactVisible.value = false
    loadImpactAnalyses()
  } catch (error) {
    ElMessage.error('创建分析失败')
  }
}

const viewImpactDetail = (row: any) => {
  ElMessage.info(`查看分析结果: ${row.analysisName}`)
}

const generateGlobalMap = async () => {
  try {
    ElMessage.success('全局数据地图生成成功')
    loadDataMaps()
  } catch (error) {
    ElMessage.error('生成地图失败')
  }
}

const showCreateMapDialog = () => {
  ElMessage.info('自定义数据地图功能开发中')
}

const viewDataMap = (map: any) => {
  ElMessage.info(`查看数据地图: ${map.mapName}`)
}

const setDefaultMap = async (mapId: number) => {
  try {
    ElMessage.success('已设置为默认地图')
    loadDataMaps()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const deleteDataMap = async (mapId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据地图吗？', '提示', {
      type: 'warning'
    })
    ElMessage.success('数据地图已删除')
    loadDataMaps()
  } catch {
    // 用户取消操作
  }
}

const generateLineageGraph = () => {
  if (!vizForm.tableName) {
    ElMessage.warning('请输入表名')
    return
  }

  const chartDom = document.getElementById('lineage-graph')
  if (!chartDom) return

  if (!lineageChart) {
    lineageChart = echarts.init(chartDom)
  }

  const option = {
    title: {
      text: `${vizForm.tableName} 血缘关系图`,
      left: 'center'
    },
    tooltip: {},
    series: [{
      type: 'graph',
      layout: 'force',
      data: [
        { name: vizForm.tableName, symbolSize: 50, itemStyle: { color: '#409EFF' } },
        { name: '上游表1', symbolSize: 30 },
        { name: '上游表2', symbolSize: 30 },
        { name: '下游表1', symbolSize: 30 },
        { name: '下游表2', symbolSize: 30 }
      ],
      links: [
        { source: '上游表1', target: vizForm.tableName },
        { source: '上游表2', target: vizForm.tableName },
        { source: vizForm.tableName, target: '下游表1' },
        { source: vizForm.tableName, target: '下游表2' }
      ],
      roam: true,
      label: {
        show: true,
        position: 'right'
      },
      force: {
        repulsion: 100
      }
    }]
  }

  lineageChart.setOption(option)
  ElMessage.success('血缘关系图生成成功')
}
</script>

<style scoped lang="less">
.lineage-enhanced-container {
  padding: 20px;

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 15px;

      .stat-icon {
        font-size: 40px;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }

  .lineage-tabs {
    background: white;
    padding: 20px;
    border-radius: 4px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .data-map-card {
    margin-bottom: 20px;

    .map-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .map-info {
      margin-bottom: 15px;

      p {
        margin: 8px 0;
        font-size: 14px;
        color: #606266;
      }
    }

    .map-actions {
      display: flex;
      gap: 10px;
    }
  }
}
</style>
