<template>
  <div class="compliance-report-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>合规报告</span>
          <el-button type="primary" @click="handleGenerate">生成报告</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="reportName" label="报告名称" />
        <el-table-column prop="reportType" label="报告类型" />
        <el-table-column prop="generateTime" label="生成时间" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'warning'">
              {{ row.status === 'completed' ? '已完成' : '生成中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" @click="handleDownload(row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const tableData = ref([])

const handleGenerate = () => {
  ElMessage.info('生成报告功能')
}

const handleView = (row: any) => {
  ElMessage.info('查看报告: ' + row.reportName)
}

const handleDownload = (row: any) => {
  ElMessage.info('下载报告: ' + row.reportName)
}

onMounted(() => {
  tableData.value = [
    { id: 1, reportName: '2025年1月合规报告', reportType: '月度报告', generateTime: '2025-01-04 09:00:00', status: 'completed' },
    { id: 2, reportName: '数据加密专项报告', reportType: '专项报告', generateTime: '2025-01-04 10:30:00', status: 'completed' }
  ]
})
</script>

<style scoped>
.compliance-report-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
