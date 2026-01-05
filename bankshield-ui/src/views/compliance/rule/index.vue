<template>
  <div class="compliance-rule-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>合规规则管理</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="ruleType" label="规则类型" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

const handleAdd = () => {
  ElMessage.info('新增规则功能')
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑规则: ' + row.ruleName)
}

const handleDelete = (row: any) => {
  ElMessage.info('删除规则: ' + row.ruleName)
}

onMounted(() => {
  // 模拟数据
  tableData.value = [
    { id: 1, ruleName: '数据加密规则', ruleType: '加密', status: 1 },
    { id: 2, ruleName: '访问控制规则', ruleType: '权限', status: 1 }
  ]
})
</script>

<style scoped>
.compliance-rule-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
