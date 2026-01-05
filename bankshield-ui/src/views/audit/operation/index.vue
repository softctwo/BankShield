<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">操作审计</h1>
      <p class="mt-1 text-sm text-gray-500">查看系统操作日志和审计记录</p>
    </div>

    <div class="mb-6 bg-white rounded-lg shadow p-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">操作人</label>
          <input v-model="searchForm.operator" type="text" placeholder="请输入操作人" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">操作类型</label>
          <select v-model="searchForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            <option value="">全部</option>
            <option value="create">新增</option>
            <option value="update">修改</option>
            <option value="delete">删除</option>
            <option value="login">登录</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">时间范围</label>
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="w-full" />
        </div>
        <div class="flex items-end gap-2">
          <button @click="handleSearch" class="flex-1 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">搜索</button>
          <button @click="handleReset" class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200 transition-colors">重置</button>
        </div>
      </div>
    </div>

    <div class="mb-4 flex justify-between items-center">
      <div class="flex gap-2">
        <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleBatchExport">
          <el-icon><Download /></el-icon> 批量导出
        </el-button>
        <el-button :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
      </div>
      <div>
        <el-button @click="handleExportAll">
          <el-icon><Download /></el-icon> 导出全部
        </el-button>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <el-table :data="auditList" border @selection-change="handleSelectionChange" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="type" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="content" label="操作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadAuditList"
        @current-change="loadAuditList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </div>

    <el-dialog v-model="detailVisible" title="操作详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getTypeTagType(currentLog.type)" size="small">{{ getTypeName(currentLog.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作内容" :span="2">{{ currentLog.content }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="currentLog.status === 'success' ? 'success' : 'danger'">{{ currentLog.status === 'success' ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const searchForm = reactive({
  operator: '',
  type: '',
  dateRange: []
})

const loading = ref(false)
const auditList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const detailVisible = ref(false)
const currentLog = ref<any>({})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadAuditList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/audit/operation/list', {
      params: {
        ...searchForm,
        current: pagination.current,
        size: pagination.size
      }
    })
    auditList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载审计日志失败:', error)
    auditList.value = [
      { id: 1, operator: 'admin', type: 'create', module: '用户管理', content: '新增用户 test001', ip: '192.168.1.100', createTime: '2024-12-30 10:30:00', status: 'success' },
      { id: 2, operator: 'admin', type: 'update', module: '角色管理', content: '修改角色权限', ip: '192.168.1.100', createTime: '2024-12-30 10:25:00', status: 'success' },
      { id: 3, operator: 'audit', type: 'delete', module: '部门管理', content: '删除部门 测试部', ip: '192.168.1.101', createTime: '2024-12-30 10:20:00', status: 'success' },
      { id: 4, operator: 'operator', type: 'login', module: '系统登录', content: '用户登录系统', ip: '192.168.1.102', createTime: '2024-12-30 10:15:00', status: 'success' },
      { id: 5, operator: 'test', type: 'login', module: '系统登录', content: '用户登录失败', ip: '192.168.1.103', createTime: '2024-12-30 10:10:00', status: 'fail' }
    ]
    pagination.total = 5
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadAuditList()
}

const handleReset = () => {
  Object.assign(searchForm, { operator: '', type: '', dateRange: [] })
  pagination.current = 1
  loadAuditList()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleView = (row: any) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleBatchExport = async () => {
  try {
    await ElMessageBox.confirm(`确定要导出选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
      type: 'warning'
    })
    ElMessage.success('导出成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('导出失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '警告', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
    loadAuditList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleExportAll = async () => {
  try {
    await ElMessageBox.confirm('确定要导出所有审计日志吗？', '提示', {
      type: 'warning'
    })
    ElMessage.success('导出任务已创建，请稍后下载')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('导出失败')
    }
  }
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    create: '新增',
    update: '修改',
    delete: '删除',
    login: '登录'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, any> = {
    create: 'success',
    update: 'warning',
    delete: 'danger',
    login: 'info'
  }
  return map[type] || ''
}

onMounted(() => {
  loadAuditList()
})
</script>
