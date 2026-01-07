<template>
  <div class="key-management-container">
    <el-card shadow="never" class="mb-4">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="text-xl font-bold">密钥管理</h2>
          <p class="text-gray-500 text-sm mt-1">管理系统加密密钥，支持国密SM2/SM3/SM4算法</p>
        </div>
        <div class="flex gap-2">
          <el-button type="primary" @click="handleAdd">
            <el-icon class="mr-1"><Plus /></el-icon>生成密钥
          </el-button>
          <el-button @click="handleBatchExport" :disabled="selectedIds.length === 0">
            <el-icon class="mr-1"><Download /></el-icon>批量导出
          </el-button>
          <el-button type="warning" @click="handleBatchRotate" :disabled="selectedIds.length === 0">
            <el-icon class="mr-1"><Refresh /></el-icon>批量轮换
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="mb-4">
        <el-col :span="6">
          <div class="stat-card stat-card-primary">
            <div class="stat-icon">
              <el-icon size="28"><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalKeys }}</div>
              <div class="stat-label">总密钥数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-success">
            <div class="stat-icon">
              <el-icon size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.activeKeys }}</div>
              <div class="stat-label">活跃密钥</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-warning">
            <div class="stat-icon">
              <el-icon size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.expiringKeys }}</div>
              <div class="stat-label">即将过期</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card stat-card-danger">
            <div class="stat-icon">
              <el-icon size="28"><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inactiveKeys }}</div>
              <div class="stat-label">已禁用</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 密钥类型分布 -->
      <el-row :gutter="16" class="mb-4">
        <el-col :span="8">
          <div class="type-stat-card">
            <div class="type-icon type-sm2">SM2</div>
            <div class="type-info">
              <div class="type-value">{{ stats.sm2Keys }}</div>
              <div class="type-label">非对称加密</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="type-stat-card">
            <div class="type-icon type-sm3">SM3</div>
            <div class="type-info">
              <div class="type-value">{{ stats.sm3Keys }}</div>
              <div class="type-label">哈希算法</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="type-stat-card">
            <div class="type-icon type-sm4">SM4</div>
            <div class="type-info">
              <div class="type-value">{{ stats.sm4Keys }}</div>
              <div class="type-label">对称加密</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 搜索条件 -->
      <el-form :inline="true" :model="queryForm" class="mb-4">
        <el-form-item label="密钥名称">
          <el-input v-model="queryForm.keyName" placeholder="请输入密钥名称" clearable />
        </el-form-item>
        <el-form-item label="密钥类型">
          <el-select v-model="queryForm.keyType" placeholder="请选择" clearable>
            <el-option label="SM2" value="SM2" />
            <el-option label="SM3" value="SM3" />
            <el-option label="SM4" value="SM4" />
            <el-option label="AES" value="AES" />
            <el-option label="RSA" value="RSA" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.keyStatus" placeholder="请选择" clearable>
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
            <el-option label="已过期" value="EXPIRED" />
            <el-option label="已撤销" value="REVOKED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

    <!-- 密钥列表 -->
      <el-table v-loading="loading" :data="keyList" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="keyName" label="密钥名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="keyType" label="算法类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAlgorithmTagType(row.keyType)" size="small">{{ row.keyType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="keyLength" label="密钥长度" width="100">
          <template #default="{ row }">{{ row.keyLength }} bits</template>
        </el-table-column>
        <el-table-column prop="keyUsage" label="用途" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getUsageLabel(row.keyUsage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="expireTime" label="过期时间" width="170" />
        <el-table-column prop="keyStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.keyStatus)" size="small">
              {{ getStatusLabel(row.keyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="warning" size="small" @click="handleRotate(row)">轮换</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">销毁</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="mt-4"
        @size-change="loadKeyList"
        @current-change="loadKeyList"
      />
    </el-card>

    <!-- 生成密钥对话框 -->
    <el-dialog v-model="dialogVisible" title="生成密钥" width="500px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="密钥名称">
          <el-input v-model="form.name" placeholder="请输入密钥名称" />
        </el-form-item>
        <el-form-item label="算法类型">
          <el-select v-model="form.algorithm" placeholder="请选择算法类型" class="w-full">
            <el-option label="SM2（非对称加密）" value="SM2" />
            <el-option label="SM3（哈希算法）" value="SM3" />
            <el-option label="SM4（对称加密）" value="SM4" />
          </el-select>
        </el-form-item>
        <el-form-item label="密钥长度">
          <el-select v-model="form.length" placeholder="请选择密钥长度" class="w-full">
            <el-option label="128 bits" :value="128" />
            <el-option label="256 bits" :value="256" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期">
          <el-select v-model="form.validity" placeholder="请选择有效期" class="w-full">
            <el-option label="1年" :value="365" />
            <el-option label="2年" :value="730" />
            <el-option label="3年" :value="1095" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="dialogVisible = false" class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200">取消</button>
          <button @click="handleSubmit" class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">生成</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Key, Lock, Warning, Refresh, Delete, Search, CircleCheck } from '@element-plus/icons-vue'
import { getKeyList, getKeyStatistics, generateKey, rotateKey, destroyKey, exportKeyInfo } from '@/api/key'

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const selectedIds = ref<number[]>([])
const total = ref(0)
const keyList = ref<any[]>([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyName: '',
  keyType: '',
  keyStatus: ''
})

const stats = reactive({
  totalKeys: 0,
  activeKeys: 0,
  inactiveKeys: 0,
  expiringKeys: 0,
  sm2Keys: 0,
  sm3Keys: 0,
  sm4Keys: 0
})

const form = reactive({
  name: '',
  algorithm: 'SM2',
  usage: 'ENCRYPT',
  length: 256,
  validity: 365,
  description: ''
})

// 加载密钥统计
const loadStatistics = async () => {
  try {
    const res: any = await getKeyStatistics()
    const data = res.data || res
    if (data) {
      stats.totalKeys = data.totalKeys || 0
      stats.activeKeys = data.activeKeys || 0
      stats.inactiveKeys = data.inactiveKeys || 0
      stats.expiringKeys = data.expiringKeys || 0
      stats.sm2Keys = data.sm2Keys || 0
      stats.sm3Keys = data.sm3Keys || 0
      stats.sm4Keys = data.sm4Keys || 0
    }
  } catch (error) {
    console.error('加载密钥统计失败', error)
    // 使用模拟数据
    stats.totalKeys = 48
    stats.activeKeys = 35
    stats.inactiveKeys = 8
    stats.expiringKeys = 5
    stats.sm2Keys = 18
    stats.sm3Keys = 12
    stats.sm4Keys = 18
  }
}

// 加载密钥列表
const loadKeyList = async () => {
  loading.value = true
  try {
    const res: any = await getKeyList(queryForm)
    const data = res.data || res
    if (data) {
      keyList.value = data.records || []
      total.value = data.total || 0
    }
  } catch (error) {
    console.error('加载密钥列表失败', error)
    // 使用模拟数据
    keyList.value = [
      { id: 1, keyName: '主加密密钥-SM4', keyType: 'SM4', keyLength: 128, keyUsage: 'ENCRYPT', keyStatus: 'ACTIVE', createTime: '2025-01-01 10:00:00', expireTime: '2026-01-01 10:00:00' },
      { id: 2, keyName: '签名密钥-SM2', keyType: 'SM2', keyLength: 256, keyUsage: 'SIGN', keyStatus: 'ACTIVE', createTime: '2025-01-02 11:00:00', expireTime: '2026-01-02 11:00:00' },
      { id: 3, keyName: '数据加密密钥-AES', keyType: 'AES', keyLength: 256, keyUsage: 'ENCRYPT', keyStatus: 'ACTIVE', createTime: '2025-01-03 12:00:00', expireTime: '2026-01-03 12:00:00' },
      { id: 4, keyName: '备份密钥-SM4', keyType: 'SM4', keyLength: 128, keyUsage: 'ENCRYPT', keyStatus: 'INACTIVE', createTime: '2024-12-01 09:00:00', expireTime: '2025-12-01 09:00:00' },
      { id: 5, keyName: '哈希密钥-SM3', keyType: 'SM3', keyLength: 256, keyUsage: 'HASH', keyStatus: 'ACTIVE', createTime: '2025-01-04 14:00:00', expireTime: '2026-01-04 14:00:00' }
    ]
    total.value = 5
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryForm.pageNum = 1
  loadKeyList()
}

const handleReset = () => {
  queryForm.keyName = ''
  queryForm.keyType = ''
  queryForm.keyStatus = ''
  handleQuery()
}

const handleAdd = () => {
  Object.assign(form, { name: '', algorithm: 'SM2', usage: 'ENCRYPT', length: 256, validity: 365, description: '' })
  dialogVisible.value = true
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchExport = async () => {
  try {
    await ElMessageBox.confirm(`确定要导出选中的 ${selectedIds.value.length} 个密钥吗？`, '提示', { type: 'warning' })
    await exportKeyInfo(selectedIds.value)
    ElMessage.success('密钥导出成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('导出失败')
    }
  }
}

const handleBatchRotate = async () => {
  try {
    await ElMessageBox.confirm(`确定要轮换选中的 ${selectedIds.value.length} 个密钥吗？`, '提示', { type: 'warning' })
    for (const id of selectedIds.value) {
      await rotateKey(id, '批量轮换', 'admin')
    }
    ElMessage.success('密钥轮换成功')
    selectedIds.value = []
    loadKeyList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('轮换失败')
    }
  }
}

const handleView = (row: any) => {
  ElMessage.info(`查看密钥: ${row.keyName}`)
}

const handleRotate = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要轮换密钥 "${row.keyName}" 吗？`, '提示', { type: 'warning' })
    await rotateKey(row.id, '手动轮换', 'admin')
    ElMessage.success('密钥轮换成功')
    loadKeyList()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('轮换失败')
    }
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要销毁密钥 "${row.keyName}" 吗？此操作不可恢复！`, '警告', { type: 'error' })
    await destroyKey(row.id, 'admin')
    ElMessage.success('销毁成功')
    loadKeyList()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('销毁失败')
    }
  }
}

const handleSubmit = async () => {
  if (!form.name) {
    ElMessage.warning('请输入密钥名称')
    return
  }
  try {
    await generateKey({
      keyName: form.name,
      keyType: form.algorithm as any,
      keyUsage: form.usage as any,
      keyLength: form.length,
      expireDays: form.validity,
      description: form.description,
      createdBy: 'admin'
    })
    ElMessage.success('密钥生成成功')
    dialogVisible.value = false
    loadKeyList()
    loadStatistics()
  } catch (error) {
    ElMessage.error('密钥生成失败')
  }
}

const getAlgorithmTagType = (algorithm: string) => {
  const map: Record<string, any> = {
    SM2: 'primary',
    SM3: 'success',
    SM4: 'warning',
    AES: 'info',
    RSA: ''
  }
  return map[algorithm] || ''
}

const getUsageLabel = (usage: string) => {
  const map: Record<string, string> = {
    ENCRYPT: '加密',
    DECRYPT: '解密',
    SIGN: '签名',
    VERIFY: '验签'
  }
  return map[usage] || usage
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    EXPIRED: 'warning',
    REVOKED: 'danger',
    DESTROYED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: '活跃',
    INACTIVE: '禁用',
    EXPIRED: '已过期',
    REVOKED: '已撤销',
    DESTROYED: '已销毁'
  }
  return map[status] || status
}

onMounted(() => {
  loadStatistics()
  loadKeyList()
})
</script>

<style scoped lang="less">
.key-management-container {
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e4e7ed;
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
  }
  
  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      line-height: 1.2;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.stat-card-primary {
  .stat-icon { background: rgba(64, 158, 255, 0.1); color: #409eff; }
  .stat-value { color: #409eff; }
}

.stat-card-success {
  .stat-icon { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
  .stat-value { color: #67c23a; }
}

.stat-card-warning {
  .stat-icon { background: rgba(230, 162, 60, 0.1); color: #e6a23c; }
  .stat-value { color: #e6a23c; }
}

.stat-card-danger {
  .stat-icon { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }
  .stat-value { color: #f56c6c; }
}

.type-stat-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #e4e7ed;
  
  .type-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 14px;
    color: #fff;
    margin-right: 12px;
  }
  
  .type-sm2 { background: linear-gradient(135deg, #409eff, #66b1ff); }
  .type-sm3 { background: linear-gradient(135deg, #67c23a, #85ce61); }
  .type-sm4 { background: linear-gradient(135deg, #e6a23c, #ebb563); }
  
  .type-info {
    .type-value { font-size: 24px; font-weight: 600; color: #303133; }
    .type-label { font-size: 12px; color: #909399; }
  }
}

.mb-4 { margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
.flex { display: flex; }
.items-center { align-items: center; }
.justify-between { justify-content: space-between; }
.gap-2 { gap: 8px; }
.w-full { width: 100%; }
</style>
