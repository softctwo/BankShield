<template>
  <div class="impact-analysis-container">
    <el-card>
      <template #header>
        <span>影响分析</span>
      </template>
      
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="选择资产">
          <el-select v-model="selectedAsset" placeholder="请选择数据资产">
            <el-option label="用户表" value="user_table" />
            <el-option label="订单表" value="order_table" />
            <el-option label="产品表" value="product_table" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAnalyze">分析影响</el-button>
        </el-form-item>
      </el-form>
      
      <el-row :gutter="20" v-if="showResult">
        <el-col :span="8">
          <el-card>
            <el-statistic title="上游影响" :value="impactStats.upstream" suffix="个" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <el-statistic title="下游影响" :value="impactStats.downstream" suffix="个" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <el-statistic title="关联系统" :value="impactStats.systems" suffix="个" />
          </el-card>
        </el-col>
      </el-row>
      
      <el-table :data="tableData" style="width: 100%; margin-top: 20px" v-if="showResult">
        <el-table-column prop="assetName" label="受影响资产" />
        <el-table-column prop="impactType" label="影响类型" />
        <el-table-column prop="impactLevel" label="影响程度">
          <template #default="{ row }">
            <el-tag :type="getImpactType(row.impactLevel)">
              {{ row.impactLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="影响说明" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const selectedAsset = ref('')
const showResult = ref(false)

const impactStats = ref({
  upstream: 5,
  downstream: 12,
  systems: 8
})

const tableData = ref([])

const getImpactType = (level: string) => {
  const types: any = { '高': 'danger', '中': 'warning', '低': 'info' }
  return types[level] || 'info'
}

const handleAnalyze = () => {
  if (!selectedAsset.value) {
    ElMessage.warning('请先选择数据资产')
    return
  }
  
  showResult.value = true
  tableData.value = [
    { id: 1, assetName: '用户画像表', impactType: '数据依赖', impactLevel: '高', description: '直接依赖用户表数据' },
    { id: 2, assetName: '报表系统', impactType: '业务影响', impactLevel: '中', description: '报表生成依赖该数据' },
    { id: 3, assetName: '数据仓库', impactType: '存储影响', impactLevel: '低', description: '历史数据存储' }
  ]
  
  ElMessage.success('影响分析完成')
}
</script>

<style scoped>
.impact-analysis-container {
  padding: 20px;
}
</style>
