<template>
  <div class="anomaly-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>异常行为详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      <el-empty description="功能开发中" />
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AnomalyDetail'
}
</script>

<style scoped lang="less">
.anomaly-detail-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
