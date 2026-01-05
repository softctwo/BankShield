<template>
  <div class="lineage-track-container">
    <el-card>
      <template #header>
        <span>血缘追踪</span>
      </template>
      
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="数据资产">
          <el-input v-model="searchForm.assetName" placeholder="请输入资产名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="assetName" label="数据资产" />
        <el-table-column prop="assetType" label="资产类型" />
        <el-table-column prop="sourceSystem" label="来源系统" />
        <el-table-column prop="targetSystem" label="目标系统" />
        <el-table-column prop="updateTime" label="更新时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewLineage(row)">查看血缘</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const searchForm = ref({
  assetName: ''
})

const tableData = ref([])

const handleSearch = () => {
  ElMessage.info('查询功能')
}

const handleReset = () => {
  searchForm.value.assetName = ''
}

const handleViewLineage = (row: any) => {
  ElMessage.info('查看血缘: ' + row.assetName)
}

onMounted(() => {
  tableData.value = [
    { id: 1, assetName: '用户表', assetType: '数据表', sourceSystem: 'MySQL', targetSystem: 'DataWarehouse', updateTime: '2025-01-04 09:00:00' },
    { id: 2, assetName: '订单表', assetType: '数据表', sourceSystem: 'Oracle', targetSystem: 'DataLake', updateTime: '2025-01-04 09:30:00' }
  ]
})
</script>

<style scoped>
.lineage-track-container {
  padding: 20px;
}
</style>
