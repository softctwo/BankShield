<template>
  <div class="threat-management-container">
    <el-card>
      <template #header>
        <span>威胁管理</span>
      </template>
      
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-statistic title="总威胁数" :value="statistics.total" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="高危威胁" :value="statistics.high" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="中危威胁" :value="statistics.medium" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="低危威胁" :value="statistics.low" />
        </el-col>
      </el-row>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="threatName" label="威胁名称" />
        <el-table-column prop="threatType" label="威胁类型" />
        <el-table-column prop="level" label="威胁等级">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="威胁来源" />
        <el-table-column prop="detectTime" label="检测时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="warning" @click="handleHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const statistics = ref({
  total: 156,
  high: 12,
  medium: 45,
  low: 99
})

const tableData = ref([])

const getLevelType = (level: string) => {
  const types: any = { high: 'danger', medium: 'warning', low: 'info' }
  return types[level] || 'info'
}

const getLevelText = (level: string) => {
  const texts: any = { high: '高危', medium: '中危', low: '低危' }
  return texts[level] || '未知'
}

const handleView = (row: any) => {
  ElMessage.info('查看威胁详情: ' + row.threatName)
}

const handleHandle = (row: any) => {
  ElMessage.info('处理威胁: ' + row.threatName)
}

onMounted(() => {
  tableData.value = [
    { id: 1, threatName: 'SQL注入攻击', threatType: '注入攻击', level: 'high', source: '192.168.1.100', detectTime: '2025-01-04 10:15:00' },
    { id: 2, threatName: '暴力破解尝试', threatType: '密码攻击', level: 'medium', source: '192.168.1.101', detectTime: '2025-01-04 10:20:00' },
    { id: 3, threatName: '异常访问', threatType: '访问异常', level: 'low', source: '192.168.1.102', detectTime: '2025-01-04 10:25:00' }
  ]
})
</script>

<style scoped>
.threat-management-container {
  padding: 20px;
}
</style>
